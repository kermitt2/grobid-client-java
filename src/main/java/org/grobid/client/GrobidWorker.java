package org.grobid.client;

import org.grobid.client.exceptions.GrobidTimeoutException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Patrice
 */
public class GrobidWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GrobidWorker.class);
    protected File pdfFile;
    protected String serviceName;
    protected int start = -1;
    protected int end = -1;
    protected MainArgs gbdArgs;

    public GrobidWorker(File pdfFile, MainArgs gbdArgs) {
        this.pdfFile = pdfFile;
        this.gbdArgs = gbdArgs;
    }

    public GrobidWorker(File pdfFile, MainArgs gbdArgs, int start, int end) {
        this.start = start;
        this.end = end;
        this.gbdArgs = gbdArgs;
        this.pdfFile = pdfFile;
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " Start. Processing = " + pdfFile.getPath());
        processCommand();
        long endTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " End. :" + (endTime - startTime) / 1000000 + " ms");
    }

    protected void processCommand() {
        try {
            GrobidService grobidService = new GrobidService(this.gbdArgs, this.start, this.end);
            String tei = grobidService.runGrobid(pdfFile);
            File outputFile = new File(this.gbdArgs.getOutput() + File.separator + pdfFile.getName().replace(".pdf", ".tei.xml"));
            try {
                FileUtils.writeStringToFile(outputFile, tei, "UTF-8");
            } catch(Exception e) {
                logger.error("\t\t error wiring result under path " + outputFile.getPath());
            }
            logger.info("\t\t " + pdfFile.getPath() + " processed.");
        } catch (GrobidTimeoutException e) {
            logger.warn("Processing of " + pdfFile.getPath() + " timed out");
        } catch (RuntimeException e) {
            logger.error("\t\t error occurred while processing " + pdfFile.getPath());
            logger.error(e.getMessage(), e.getCause());
        } 
    }

}
