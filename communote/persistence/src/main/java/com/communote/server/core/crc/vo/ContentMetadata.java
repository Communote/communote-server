package com.communote.server.core.crc.vo;

/**
 * The Metadata to store with the content.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ContentMetadata implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2372686379138086418L;

    private String mimeType;

    private java.util.Date date;

    private String filename;

    private long contentSize;

    private String url;

    private String version;

    private Object[] versions;

    private com.communote.server.core.crc.vo.ContentId contentId;

    public ContentMetadata() {
        this.mimeType = null;
        this.date = null;
        this.filename = null;
        this.contentSize = 0;
        this.url = null;
        this.versions = null;
        this.contentId = null;
    }

    /**
     * Copies constructor from other ContentMetadata
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public ContentMetadata(ContentMetadata otherBean) {
        this(otherBean.getMimeType(), otherBean.getDate(), otherBean.getFilename(), otherBean
                .getContentSize(), otherBean.getUrl(), otherBean.getVersion(), otherBean
                .getVersions(), otherBean.getContentId());
    }

    public ContentMetadata(String mimeType, java.util.Date date, String filename, long contentSize,
            String url, Object[] versions, com.communote.server.core.crc.vo.ContentId contentId) {
        this.mimeType = mimeType;
        this.date = date;
        this.filename = filename;
        this.contentSize = contentSize;
        this.url = url;
        this.versions = versions;
        this.contentId = contentId;
    }

    public ContentMetadata(String mimeType, java.util.Date date, String filename, long contentSize,
            String url, String version, Object[] versions,
            com.communote.server.core.crc.vo.ContentId contentId) {
        this.mimeType = mimeType;
        this.date = date;
        this.filename = filename;
        this.contentSize = contentSize;
        this.url = url;
        this.version = version;
        this.versions = versions;
        this.contentId = contentId;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(ContentMetadata otherBean) {
        if (otherBean != null) {
            this.setMimeType(otherBean.getMimeType());
            this.setDate(otherBean.getDate());
            this.setFilename(otherBean.getFilename());
            this.setContentSize(otherBean.getContentSize());
            this.setUrl(otherBean.getUrl());
            this.setVersion(otherBean.getVersion());
            this.setVersions(otherBean.getVersions());
            this.setContentId(otherBean.getContentId());
        }
    }

    /**
     * Get the contentId
     *
     */
    public com.communote.server.core.crc.vo.ContentId getContentId() {
        return this.contentId;
    }

    /**
     * <p>
     * The size of the content in bytes.
     * </p>
     */
    public long getContentSize() {
        return this.contentSize;
    }

    /**
     * <p>
     * The current date and time.
     * </p>
     */
    public java.util.Date getDate() {
        return this.date;
    }

    /**
     *
     */
    public String getFilename() {
        return this.filename;
    }

    /**
     * <p>
     * The MIME-type of this content.
     * </p>
     */
    public String getMimeType() {
        return this.mimeType;
    }

    /**
     *
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * <p>
     * The version of this content. Only set if this content is versionized.
     * </p>
     */
    public String getVersion() {
        return this.version;
    }

    /**
     *
     */
    public Object[] getVersions() {
        return this.versions;
    }

    /**
     * Sets the contentId
     */
    public void setContentId(com.communote.server.core.crc.vo.ContentId contentId) {
        this.contentId = contentId;
    }

    public void setContentSize(long contentSize) {
        this.contentSize = contentSize;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setVersions(Object[] versions) {
        this.versions = versions;
    }

}