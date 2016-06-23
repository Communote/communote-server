package com.communote.common.image;

/**
 * The format types valid for
 * {@link javax.imageio.ImageIO#write(java.awt.image.RenderedImage, String, java.io.File)}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum ImageFormatType {

    /** GIF */
    gif("image/gif", true),

    /** PNG */
    png("image/png", true),

    /** JPEG */
    jpeg("image/jpeg", false);

    /**
     * @param mimeType
     *            The type to check for.
     * @return The first format matching the given mime type.
     */
    public static ImageFormatType fromMimeType(String mimeType) {
        mimeType = mimeType.toLowerCase();
        for (ImageFormatType format : ImageFormatType.values()) {
            if (format.contentType.equals(mimeType)) {
                return format;
            }
        }
        throw new IllegalArgumentException("The given mimetype is not valid: " + mimeType);
    }

    private final String contentType;

    private final boolean supportsTransparency;

    /**
     * @param contentType
     *            The content type.
     * @param supportsTransparancy
     *            whether the format supports transparency
     */
    ImageFormatType(String contentType, boolean supportsTransparancy) {
        this.contentType = contentType;
        this.supportsTransparency = supportsTransparancy;
    }

    /**
     * @return The content type in MIME type notation
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return whether the image type supports transparency
     */
    public boolean isSupportsTransparency() {
        return supportsTransparency;
    }
}
