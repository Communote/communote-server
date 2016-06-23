package com.communote.plugins.api.rest.exception;

/**
 * Exception while building the response.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResponseBuildException
        extends Exception {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8984648421251603448L;

    /**
     * The default constructor for <code>ResponseBuildException</code>.
     */
    public ResponseBuildException() {
    }

    /**
     * Constructs a new instance of <code>ResponseBuildException</code>.
     * 
     * @param message
     *            the throwable message.
     */
    public ResponseBuildException(String message) {
        super(message);
    }

}