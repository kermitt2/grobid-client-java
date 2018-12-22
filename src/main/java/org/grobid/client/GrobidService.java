package org.grobid.client;

import org.grobid.client.exceptions.GrobidTimeoutException;
import org.grobid.client.exceptions.UnreachableGrobidServiceException;
import java.io.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpRetryException;
import java.net.ConnectException;

import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Call of Grobid process via its REST web services.
 *
 * @author Patrice
 */
public class GrobidService {

    private static final Logger logger = LoggerFactory.getLogger(GrobidService.class);

    private int start = -1;
    private int end = -1;
    private MainArgs gbdArgs;

    //int TIMEOUT_VALUE = 30000;

    public GrobidService(MainArgs gbdArgs) {
        this.gbdArgs = gbdArgs;
    }

    public GrobidService(MainArgs gbdArgs, int start, int end) {
        this.start = start;
        this.end = end;
        this.gbdArgs = gbdArgs;
    }

    /**
     * Call the Grobid full text extraction service on server.
     *
     * @param pdfBinary InputStream of the PDF file to be processed
     * @return the resulting TEI document as a String or null if the service
     * failed
     */
    public String runGrobid(File pdfFile) {
        String tei = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://" + gbdArgs.getGrobidHost()
                    + (gbdArgs.getGrobidPort().isEmpty() ? "" : ":" + gbdArgs.getGrobidPort()) + "/api/" + gbdArgs.getProcessMethodName());
            conn = (HttpURLConnection) url.openConnection();
            //conn.setConnectTimeout(TIMEOUT_VALUE);
            //conn.setReadTimeout(TIMEOUT_VALUE);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            FileBody fileBody = new FileBody(pdfFile);
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
            multipartEntity.addPart("input", fileBody);

            if (start != -1) {
                StringBody contentString = new StringBody("" + start);
                multipartEntity.addPart("start", contentString);
            }
            if (end != -1) {
                StringBody contentString = new StringBody("" + end);
                multipartEntity.addPart("end", contentString);
            }
            //multipartEntity.addPart("consolidateHeader", new StringBody("1"));
            //multipartEntity.addPart("consolidateCitations", new StringBody("1"));
            conn.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
            OutputStream out = conn.getOutputStream();
            try {
                multipartEntity.writeTo(out);
            } finally {
                out.close();
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
                // error 503 corresponds to the case all the treads in the GROBID thread pool are 
                // used, we will need to wait a bit and re-send the query 
                throw new HttpRetryException("Failed : HTTP error code : " 
                    + conn.getResponseCode(), conn.getResponseCode());
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                tei = "";
            } else if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode() + " " + IOUtils.toString(conn.getErrorStream(), "UTF-8"));
            } else {
                InputStream in = conn.getInputStream();
                tei = IOUtils.toString(in, "UTF-8");
                IOUtils.closeQuietly(in);
            }
        } catch (ConnectException e) {
            logger.error(e.getMessage(), e.getCause());
            try {
                Thread.sleep(20000);
                runGrobid(pdfFile);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (HttpRetryException e) {
            logger.error(e.getMessage(), e.getCause());
            try {
                Thread.sleep(gbdArgs.getSleepTime());
                runGrobid(pdfFile);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (SocketTimeoutException e) {
            throw new GrobidTimeoutException("Grobid processing timed out.");
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e.getCause());
        } catch (IOException e) {
            logger.error(e.getMessage(), e.getCause());
        } finally {
            if (conn != null)
                conn.disconnect();
        }
        return tei;
    }

    /**
     * Checks if Grobid service is responding and local tmp directory is
     * available.
     *
     * @return boolean
     */
    public boolean isGrobidOk() throws UnreachableGrobidServiceException {
        logger.info("Cheking Grobid service...");

        int responseCode = 0;
        try {
            URL url = new URL("http://" + gbdArgs.getGrobidHost()
                    + (gbdArgs.getGrobidPort().isEmpty() ? "" : ":" + gbdArgs.getGrobidPort()) + "/api/isalive");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            responseCode = conn.getResponseCode();
            conn.disconnect();
        } catch (IOException e) {
            throw new UnreachableGrobidServiceException("Grobid service is not alive.", e);
        }
        if (responseCode != 200) {
            throw new UnreachableGrobidServiceException(responseCode);
        }
        logger.info("Grobid service is ok and can be used.");
        return true;
    }
}
