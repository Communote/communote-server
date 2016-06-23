package com.communote.common.io;

/**
 * Encapsulates binary data with its meta information.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BinaryData {
    private final String contentType;
    private final byte[] data;
    private final long lastModified;

    /**
     * Constructor
     * 
     * @param contentType
     *            Content type of the byte array
     * @param data
     *            byte array
     */
    public BinaryData(String contentType, byte[] data) {
        this(contentType, data, 0L);
    }

    /**
     * Constructor
     * 
     * @param contentType
     *            Content type of the byte array
     * @param data
     *            byte array
     * @param lastModified
     *            last modified
     */
    public BinaryData(String contentType, byte[] data, long lastModified) {
        this.contentType = contentType;
        this.data = data;
        this.lastModified = lastModified;
    }

    /**
     * Return content type
     * 
     * @return Content Type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Return the byte array
     * 
     * @return Byte array
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Return last modified
     * 
     * @return Last modified
     */
    public Long getLastModified() {
        return lastModified;
    }
}
