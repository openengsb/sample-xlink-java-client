package org.openengsb.xlinkSQLViewer.exceptions;

/**
 * This exception is thrown, if something goes wrong during the generation of an XLinkURL
 */
@SuppressWarnings("serial")
public class GenerateXLinkException extends Exception {
    public GenerateXLinkException() {
        super();
    }

    public GenerateXLinkException(String message) {
        super(message);
    }

    public GenerateXLinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenerateXLinkException(Throwable cause) {
        super(cause);
    }
}
