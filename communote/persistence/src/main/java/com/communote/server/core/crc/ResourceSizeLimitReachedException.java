package com.communote.server.core.crc;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResourceSizeLimitReachedException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -5102252716715183963L;

    /**
     * Constructs a new instance of ResourceSizeLimitReachedException
     *
     */
    public ResourceSizeLimitReachedException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of ResourceSizeLimitReachedException
     *
     */
    public ResourceSizeLimitReachedException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
