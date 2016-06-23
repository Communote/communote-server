package com.communote.server.core.tag;

/**
 * Thrown to indicate that a tag tag with the same tagStoreTagId already exists inside a TagStore.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TagAlreadyExistsException extends Exception {
    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    private final String tagDefaultName;
    private final String tagStoreId;
    private final Long tagId;

    /**
     * Create a new exception
     * 
     * @param message
     *            descriptive detail message
     * @param tagId
     *            the ID of the existing tag
     * @param tagDefaultName
     *            the name of the tag that caused the exception
     * @param tagStoreId
     *            the ID of the TagStore where the tag already exists
     */
    public TagAlreadyExistsException(String message, Long tagId, String tagDefaultName,
            String tagStoreId) {
        super(message);
        this.tagId = tagId;
        this.tagDefaultName = tagDefaultName;
        this.tagStoreId = tagStoreId;
    }

    /**
     * 
     * @return the name of the tag that caused the exception
     */
    public String getTagDefaultName() {
        return tagDefaultName;
    }

    /**
     * @return the ID of the existing tag that caused the exception
     */
    public Long getTagId() {
        return tagId;
    }

    /**
     * 
     * @return the ID of the TagStore where the tag already exists
     */
    public String getTagStoreId() {
        return tagStoreId;
    }
}
