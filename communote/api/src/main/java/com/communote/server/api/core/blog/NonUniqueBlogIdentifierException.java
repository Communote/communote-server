package com.communote.server.api.core.blog;


/**
 * Thrown if the blog identifier (alias) is not unique and already used by another blog.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NonUniqueBlogIdentifierException extends BlogValidationException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7496958252111303901L;

    /**
     * Constructs a new instance of NonUniqueBlogIdentifierException
     *
     */
    public NonUniqueBlogIdentifierException(String message) {
        super(message);
    }

}
