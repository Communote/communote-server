package com.communote.server.api.core.blog;

/**
 * Base exception for for all validation errors of blog attributes.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogValidationException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1309859092279562641L;

    /**
     * Constructs a new instance of BlogValidationException
     *
     */
    public BlogValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of BlogValidationException
     *
     */
    public BlogValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
