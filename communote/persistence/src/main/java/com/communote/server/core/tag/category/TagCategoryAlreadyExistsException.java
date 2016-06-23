package com.communote.server.core.tag.category;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagCategoryAlreadyExistsException extends
        com.communote.server.core.tag.category.TagCategoryException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4471142611084743993L;

    /**
     * Constructs a new instance of TagCategoryAlreadyExistsException
     *
     */
    public TagCategoryAlreadyExistsException(String message) {
        super(message);
    }

}
