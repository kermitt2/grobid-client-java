package org.grobid.client;

import java.io.*;
import java.util.*;

/**
 * The entrance point for starting the client from command line
 *
 * @author Patrice Lopez
 */
public class Main {
    private static List<String> availableServices = Arrays.asList("processFulltextDocument", "processHeaderDocument", "processReference");

    /**
     * Arguments of the command.
     */
    private static MainArgs gbdArgs;

    /**
     * @return String to display for help.
     */
    protected static String getHelp() {
        final StringBuilder help = new StringBuilder();
        help.append("HELP Java GROBID client\n");
        help.append("-h: displays this help\n");
        help.append("-in: directory path containing the PDF files to process.\n");
        help.append("-out: directory path to write the result files\n");
        help.append("-exe: gives the command to execute. The value should be one of these (default is processFulltextDocument):\n");
        help.append("\t" + availableServices + "\n");
        return help.toString();
    }

    /**
     * Process command given the args.
     *
     * @param pArgs The arguments given to the batch.
     */
    protected static boolean processArgs(final String[] pArgs) {
        boolean result = true;

        // read the properties and put the values in the args object
        Properties props = new Properties();
        String grobidHost = null;
        String grobidPort = null;
        int sleepTime = 5000;
        try(InputStream resourceStream = new FileInputStream("grobid-client.properties")) {
            props.load(resourceStream);
            grobidHost = props.getProperty("grobidHost", "localhost");
            grobidPort = props.getProperty("grobidPort", "8070");
            String sleepTimeStr = props.getProperty("sleepTime", "5000");
            try {
                sleepTime = Integer.parseInt(sleepTimeStr);
            } catch(Exception e) {
                System.err.println("sleep time value should be an integer, default value will be used");
            }
        } catch(Exception e) {
            System.err.println("property file not found, default values will be used");
        }

        gbdArgs.setGrobidHost(grobidHost);
        gbdArgs.setGrobidPort(grobidPort);
        gbdArgs.setSleepTime(sleepTime);
        if (pArgs.length == 0) {
            System.out.println(getHelp());
            result = false;
        } else {
            String currArg;
            for (int i = 0; i < pArgs.length; i++) {
                currArg = pArgs[i];
                if (currArg.equals("-h")) {
                    System.out.println(getHelp());
                    result = false;
                    break;
                }
                if (currArg.equals("-in")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setInput(pArgs[i + 1]);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-out")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setOutput(pArgs[i + 1]);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-exe")) {
                    final String command = pArgs[i + 1];
                    if (availableServices.contains(command)) {
                        gbdArgs.setProcessMethodName(command);
                        i++;
                        continue;
                    } else {
                        System.err.println("-exe value should be one value from this list: " + availableServices);
                        result = false;
                        break;
                    }
                }
                if (currArg.equals("-n")) {
                    if (pArgs[i + 1] != null) {
                        String nb = pArgs[i + 1];
                        int nbConcurrency = 1;
                        try {
                            nbConcurrency = Integer.parseInt(nb);
                            gbdArgs.setNbConcurrency(nbConcurrency);
                        }
                        catch(Exception e) {
                            System.err.println("-n value should be an integer, default value will be used");
                        }
                    }
                    i++;
                    continue;
                }
            }
        }
        return result;
    }

    /**
     * Starts nerd from command line using the following parameters:
     *
     * @param args The arguments
     */
    public static void main(final String[] args) throws Exception {
        gbdArgs = new MainArgs();
        if (processArgs(args)) {

            File dirOutputPath = new File(gbdArgs.getOutput());
            if (!dirOutputPath.exists()) {
                System.out.println("Cannot find the destination directory " + dirOutputPath.getAbsolutePath() + ". Creating it.");
                dirOutputPath.mkdir();
            }

            File dirInputPath = new File(gbdArgs.getInput());
            if (!dirInputPath.exists()) {
                System.err.println("Cannot find the input directory: " + dirInputPath.getAbsolutePath());
            }

            long startTime = System.nanoTime();
            GrobidProcess process = new GrobidProcess(gbdArgs);
            process.process();
            long endTime = System.nanoTime();
            System.out.println("\ntotal runtime:" + (endTime - startTime) / 1000000 + " ms");
        }
    }

}
