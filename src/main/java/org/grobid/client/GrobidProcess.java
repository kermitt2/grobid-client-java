package org.grobid.client;

import org.grobid.client.exceptions.UnreachableGrobidServiceException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrice
 */
public class GrobidProcess {

    private static final Logger logger = LoggerFactory.getLogger(GrobidProcess.class);

    private MainArgs gbdArgs;

    public GrobidProcess(MainArgs gbdArgs) {
        this.gbdArgs = gbdArgs;
    }

    /**
    * Extracts the TEI using the available PDF.
    */
    public void process() {
        try {
            GrobidService grobidService = new GrobidService(this.gbdArgs);
            //if (grobidService.isGrobidOk()) 
            {
                ExecutorService executor = Executors.newFixedThreadPool(gbdArgs.getNbConcurrency());
                File dirInputPath = new File(gbdArgs.getInput());

                final File[] refFiles = dirInputPath.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".pdf");
                    }
                }); 

                if (refFiles == null) {
                    throw new IllegalStateException("Folder " + dirInputPath.getAbsolutePath()
                            + " does not seem to contain any PDF file");
                }

                System.out.println(refFiles.length + " PDF files");
                int n = 0;
                for (; n < refFiles.length; n++) {
                    final File pdfFile = refFiles[n];
                    Runnable worker = new GrobidWorker(pdfFile, gbdArgs);
                    executor.execute(worker);
                }
                
                try {
                    System.out.println("wait for thread completion");
                    executor.shutdown();
                    //executor.awaitTermination(48, TimeUnit.HOURS);
                    while (!executor.isTerminated()) {
                    }
                } finally {
                    if (!executor.isTerminated()) {
                        System.err.println("cancel all non-finished workers");
                    }
                    executor.shutdownNow();
                }
            }
            logger.info("Finished all threads");
        } catch (UnreachableGrobidServiceException ugse) {
            logger.error(ugse.getMessage());
        }
    }
}
