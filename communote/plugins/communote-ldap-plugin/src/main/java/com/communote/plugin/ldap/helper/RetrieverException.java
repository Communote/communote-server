package com.communote.plugin.ldap.helper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RetrieverException extends Exception {

    private static final long serialVersionUID = 5033950750039759589L;

    /**
     * Constructor.
     * 
     * @param message
     *            A detail message for this exception.
     */
    public RetrieverException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            Root cause of this exception.
     */
    public RetrieverException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
