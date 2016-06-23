package com.communote.server.api.core.image;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Image
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class Image implements Serializable {

    private static final long serialVersionUID = 907391372137072221L;

    private String mimeType;

    private Date lastModificationDate;

    private long size;

    private boolean isExternal;
    private final boolean defaultImage;
    private final String providerId;

    /**
     * @param mimeType
     *            The mimetype.
     * @param lastModificationDate
     *            The last modification date.
     * @param size
     *            Size of the image in bytes.
     * @param providerId
     *            The identifier of the provider that loaded the image
     * @param defaultImage
     *            whether the image is a default image or a custom image
     */
    public Image(String mimeType, Date lastModificationDate, long size, String providerId,
            boolean defaultImage) {
        this(mimeType, lastModificationDate, size, providerId, defaultImage, false);
    }

    /**
     * @param mimeType
     *            The mimetype.
     * @param lastModificationDate
     *            The last modification date.
     * @param size
     *            Size of the image in bytes.
     * @param providerId
     *            The identifier of the provider that loaded the image
     * @param defaultImage
     *            whether the image is a default image or a custom image
     * @param isExternal
     *            Set to true, if this image is an external image.
     */
    public Image(String mimeType, Date lastModificationDate, long size, String providerId,
            boolean defaultImage, boolean isExternal) {
        this.mimeType = mimeType;
        this.lastModificationDate = lastModificationDate;
        this.size = size;
        this.providerId = providerId;
        this.defaultImage = defaultImage;
        this.setExternal(isExternal);
    }

    /**
     * @return Returns the image data as byte array.
     * @throws IOException
     *             Thrown, when there was an array creating the bytes.
     */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = openStream();
        IOUtils.copy(inputStream, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        IOUtils.closeQuietly(outputStream);
        IOUtils.closeQuietly(inputStream);
        return byteArray;
    }

    /**
     * @return the lastModificationDate
     */
    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return the identifier of the provider that loaded the image
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @return whether the image is a default image or a custom image
     */
    public boolean isDefaultImage() {
        return defaultImage;
    }

    /**
     * @return the isExternal
     */
    public boolean isExternal() {
        return isExternal;
    }

    /**
     * @return {@link InputStream} to the image.
     * @throws IOException
     *             Thrown, when there was an error opening the stream.
     */
    public abstract InputStream openStream() throws IOException;

    /**
     * @param isExternal
     *            the isExternal to set
     */
    public void setExternal(boolean isExternal) {
        this.isExternal = isExternal;
    }

    /**
     * @param lastModificationDate
     *            the lastModificationDate to set
     */
    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * @param mimeType
     *            the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @param size
     *            the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }
}