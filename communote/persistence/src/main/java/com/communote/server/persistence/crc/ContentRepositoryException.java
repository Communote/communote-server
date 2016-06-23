package com.communote.server.persistence.crc;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ContentRepositoryException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7064056180581145901L;

    /**
     * Constructs a new instance of ContentRepositoryException
     *
     */
    public ContentRepositoryException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of ContentRepositoryException
     *
     */
    public ContentRepositoryException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
