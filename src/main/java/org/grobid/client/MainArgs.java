package org.grobid.client;

import java.text.ParseException;

/**
 * Class containing args of the command line {@link Main}.
 *
 * @author Patrice
 */
public class MainArgs {

    private String input;
    private String output;
    private String processMethodName = "processFulltextDocument";
    private int nbConcurrency = 10; // default

    // from property file
    private String grobidHost;
    private String grobidPort;
    private int sleepTime;

    /**
     * @return the processMethodName
     */
    public final String getProcessMethodName() {
        return this.processMethodName;
    }

    /**
     * @param pProcessMethodName the processMethodName to set
     */
    public final void setProcessMethodName(final String pProcessMethodName) {
        this.processMethodName = pProcessMethodName;
    }
    
    /**
     * @return the input path
     */
    public final String getInput() {
        return this.input;
    }

    /**
     * @param pPathInputDirectory the input path to set
     */
    public final void setInput(final String pPathInputDirectory) {
        this.input = pPathInputDirectory;
    }

    /**
     * @return the output path
     */
    public final String getOutput() {
        return this.output;
    }

    /**
     * @param pPathOutputDirectory the output path to set
     */
    public final void setOutput(final String pPathOutputDirectory) {
        this.output = pPathOutputDirectory;
    }
    
    public int getNbConcurrency() {
        return this.nbConcurrency;
    }

    public void setNbConcurrency(int nb) {
        this.nbConcurrency = nb;
    }

    public String getGrobidHost() {
        return this.grobidHost;
    }

    public void setGrobidHost(String grobidHost) {
        this.grobidHost = grobidHost;
    }

    public String getGrobidPort() {
        return this.grobidPort;
    }

    public void setGrobidPort(String grobidPort) {
        this.grobidPort = grobidPort;
    }

    public int getSleepTime() {
        return this.sleepTime;
    }

    public void setSleepTime(int nb) {
        this.sleepTime = nb;
    }
}
