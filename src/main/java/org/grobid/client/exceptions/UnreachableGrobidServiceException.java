package org.grobid.client.exceptions;

public class UnreachableGrobidServiceException extends RuntimeException {

    public UnreachableGrobidServiceException(int responseCode) {
        super("Grobid service is not alive. HTTP error: " + responseCode);
    }

    public UnreachableGrobidServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
