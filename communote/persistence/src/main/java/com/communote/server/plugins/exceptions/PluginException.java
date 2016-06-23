package com.communote.server.plugins.exceptions;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class PluginException extends Exception {

    private static final long serialVersionUID = -5409136625823604254L;

    /**
     * Constructor.
     * 
     * @param message
     *            The message.
     */
    public PluginException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            The message.
     * @param cause
     *            The cause.
     */
    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            The Cause.
     */
    public PluginException(Throwable cause) {
        super(cause);
    }
}
