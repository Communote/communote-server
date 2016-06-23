package com.communote.server.plugins.api.externals;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalUserGroupAccessorException extends Exception {

    private static final long serialVersionUID = 5033950750039759589L;

    /**
     * Constructor.
     * 
     * @param message
     *            A detail message for this exception.
     */
    public ExternalUserGroupAccessorException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            A detail message for this exception.
     * @param cause
     *            Root cause of this exception.
     */
    public ExternalUserGroupAccessorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            Root cause of this exception.
     */
    public ExternalUserGroupAccessorException(Throwable cause) {
        this(cause.getMessage(), cause);
    }
}
