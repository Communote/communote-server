package com.communote.server.web.api.to.listitem;

import com.communote.server.api.core.common.IdentifiableEntityData;

/**
 * Base class for list items that describe notes.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommonPostListItem extends IdentifiableEntityData implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 2571943259990379421L;

    private Long postId;

    private String text;

    /**
     * Creates a new instance
     */
    public CommonPostListItem() {
        super();
        this.postId = null;
        this.text = null;
    }

    /**
     * Creates a new instance by cloning the provided instance
     * 
     * @param otherBean
     *            the instance to copy from, cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public CommonPostListItem(CommonPostListItem otherBean) {
        this(otherBean.getPostId(), otherBean.getText());
    }

    /**
     * Creates a new instance
     * 
     * @param postId
     *            the ID of the note
     * @param text
     *            the content of the note
     */
    public CommonPostListItem(Long postId, String text) {
        super();
        this.postId = postId;
        this.text = text;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     * 
     * @param otherBean
     *            the instance to copy from
     */
    public void copy(CommonPostListItem otherBean) {
        if (otherBean != null) {
            this.setPostId(otherBean.getPostId());
            this.setText(otherBean.getText());
        }
    }

    /**
     * @return the ID of the note
     */
    public Long getPostId() {
        return this.postId;
    }

    /**
     * @return the content of the note
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets the ID of the note
     * 
     * @param postId
     *            the ID to set
     */
    public void setPostId(Long postId) {
        this.postId = postId;
    }

    /**
     * sets the content of the note
     * 
     * @param text
     *            the content to set
     */
    public void setText(String text) {
        this.text = text;
    }

}