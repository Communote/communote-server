package com.communote.server.core.tag.category;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CategoryPrefixAlreadyExistsException extends
com.communote.server.core.tag.category.TagCategoryAlreadyExistsException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -266887413103835416L;

    /**
     * Constructs a new instance of CategoryPrefixAlreadyExistsException
     *
     */
    public CategoryPrefixAlreadyExistsException(String message) {
        super(message);
    }

}
