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

    private boolean useHttps;

    /**
     * @param mediaId
     *            the media ID as string
     * @param mediaTypeId
     *            the media type
     */
    public RichMediaDescription(String mediaId, String mediaTypeId) {
        this(mediaId, mediaTypeId, false);
    }

    /**
     * @param mediaId
     *            the media ID as string
     * @param mediaTypeId
     *            the media type
     * @param useHttps
     *            Renders the link with https, if set to true.
     *
     */
    public RichMediaDescription(String mediaId, String mediaTypeId, boolean useHttps) {
        this.mediaId = mediaId;
        this.mediaTypeId = mediaTypeId;
        this.useHttps = useHttps;
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
     * @return the useHttps
     */
    public boolean isUseHttps() {
        return useHttps;
    }

    /**
     * @param mediaId
     *            the mediaId to set
     */
    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    /**
     * @param useHttps
     *            the useHttps to set
     */
    public void setUseHttps(boolean useHttps) {
        this.useHttps = useHttps;
    }

}