package com.communote.server.core.tag.category;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagCategoryNotFoundException extends
        com.communote.server.core.tag.category.TagCategoryException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 8831983083586468003L;

    /**
     * Constructs a new instance of TagCategoryNotFoundException
     *
     */
    public TagCategoryNotFoundException(String message) {
        super(message);
    }

}
