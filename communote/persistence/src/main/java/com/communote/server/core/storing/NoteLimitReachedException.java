package com.communote.server.core.storing;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteLimitReachedException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4611734304949781994L;

    /**
     * Constructs a new instance of NoteLimitReachedException
     *
     */
    public NoteLimitReachedException(String message) {
        super(message);
    }

}
