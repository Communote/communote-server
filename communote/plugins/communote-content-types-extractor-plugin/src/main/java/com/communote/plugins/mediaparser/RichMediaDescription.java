package com.communote.plugins.mediaparser;

/**
 * Identifies a specific object Rich Media.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class RichMediaDescription {

    /** the media identifier */
    private String mediaId;

    /** the media type */
    private final String mediaTypeId;

    /**
     * @param mediaId
     *            the media ID as string
     * @param mediaTypeId
     *            the media type
     */
    public RichMediaDescription(String mediaId, String mediaTypeId) {
        this.mediaId = mediaId;
        this.mediaTypeId = mediaTypeId;
    }

    /**
     * @return the mediaId
     */
    public String getMediaId() {
        return mediaId;
    }

    /**
     * @return the mediaType
     */
    public String getMediaTypeId() {
        return mediaTypeId;
    }

    /**
     * @param mediaId
     *            the mediaId to set
     */
    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }
}