package com.communote.server.core.tag.category;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagCategoryAlreadyAssignedException extends
        com.communote.server.core.tag.category.TagCategoryException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1930097747938832445L;

    /**
     * Constructs a new instance of TagCategoryAlreadyAssignedException
     *
     */
    public TagCategoryAlreadyAssignedException(String message) {
        super(message);
    }

}
