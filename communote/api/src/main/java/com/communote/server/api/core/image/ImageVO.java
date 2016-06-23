package com.communote.server.api.core.image;

import java.util.Date;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageVO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3469071092893248193L;

    private Date lastModificationDate;

    private byte[] image;

    public ImageVO() {
        this.lastModificationDate = null;
        this.image = null;
    }

    public ImageVO(Date lastModificationDate, byte[] image) {
        this.lastModificationDate = lastModificationDate;
        this.image = image;
    }

    /**
     * Copies constructor from other ImageVO
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public ImageVO(ImageVO otherBean) {
        this(otherBean.getLastModificationDate(), otherBean.getImage());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(ImageVO otherBean) {
        if (otherBean != null) {
            this.setLastModificationDate(otherBean.getLastModificationDate());
            this.setImage(otherBean.getImage());
        }
    }

    /**
     * <p>
     * The content of the image
     * </p>
     */
    public byte[] getImage() {
        return this.image;
    }

    /**
     * <p>
     * The last modification of this image
     * </p>
     */
    public Date getLastModificationDate() {
        return this.lastModificationDate;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

}