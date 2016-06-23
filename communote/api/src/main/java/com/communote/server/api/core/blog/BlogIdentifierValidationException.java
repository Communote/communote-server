package com.communote.server.api.core.blog;


/**
 * Thrown to indicate that the blog alias is not valid.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogIdentifierValidationException extends BlogValidationException {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    private final String alias;

    /**
     * Creates a new instance with a detail message
     * 
     * @param message
     *            the message holding the details
     * @param alias
     *            the blog alias that is not valid
     */
    public BlogIdentifierValidationException(String message, String alias) {
        super(message);
        this.alias = alias;
    }

    /**
     * @return the alias that is not valid
     */
    public String getAlias() {
        return this.alias;
    }
}
