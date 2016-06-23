package com.communote.server.core.tag.category;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.tag.category.TagCategoryManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagCategoryManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6816431260398481796L;

    /**
     * Constructs a new instance of <code>TagCategoryManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public TagCategoryManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>TagCategoryManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public TagCategoryManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}