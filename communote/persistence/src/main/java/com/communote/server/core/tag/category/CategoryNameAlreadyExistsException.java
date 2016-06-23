package com.communote.server.core.tag.category;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CategoryNameAlreadyExistsException extends
com.communote.server.core.tag.category.TagCategoryAlreadyExistsException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3731843104180864245L;

    /**
     * Constructs a new instance of CategoryNameAlreadyExistsException
     *
     */
    public CategoryNameAlreadyExistsException(String message) {
        super(message);
    }

}
