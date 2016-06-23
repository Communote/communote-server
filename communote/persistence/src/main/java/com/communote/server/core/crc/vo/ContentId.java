package com.communote.server.core.crc.vo;

import java.io.Serializable;

/**
 * <p>
 * This object is representing the content id of a stored ContentTO.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ContentId implements Serializable {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 7432277927847015079L;

    private String contentId;

    private String connectorId;

    public ContentId() {
        this.contentId = null;
        this.connectorId = null;
    }

    /**
     * Copies constructor from other ContentId
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public ContentId(ContentId otherBean) {
        this(otherBean.getContentId(), otherBean.getConnectorId());
    }

    public ContentId(String contentId, String connectorId) {
        this.contentId = contentId;
        this.connectorId = connectorId;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(ContentId otherBean) {
        if (otherBean != null) {
            this.setContentId(otherBean.getContentId());
            this.setConnectorId(otherBean.getConnectorId());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ContentId)) {
            return false;
        }
        ContentId other = (ContentId) obj;
        if (connectorId == null) {
            if (other.connectorId != null) {
                return false;
            }
        } else if (!connectorId.equals(other.connectorId)) {
            return false;
        }
        if (contentId == null) {
            if (other.contentId != null) {
                return false;
            }
        } else if (!contentId.equals(other.contentId)) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * The id of this repository.
     * </p>
     */
    public String getConnectorId() {
        return this.connectorId;
    }

    /**
     * <p>
     * The id of the stored content in this repository.
     * </p>
     */
    public String getContentId() {
        return this.contentId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((connectorId == null) ? 0 : connectorId.hashCode());
        result = prime * result + ((contentId == null) ? 0 : contentId.hashCode());
        return result;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public String toString() {
        return "ContentId [contentId=" + contentId + ", connectorId=" + connectorId + "]";
    }

}