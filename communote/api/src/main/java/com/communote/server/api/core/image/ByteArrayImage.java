package com.communote.server.api.core.image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ByteArrayImage extends Image {

    private static final long serialVersionUID = 6321581977345236274L;

    private final byte[] payload;

    /**
     * @param payload
     *            The payload.
     * @param mimeType
     *            The mimetype.
     * @param lastModificationDate
     *            The last modification data.
     * @param providerId
     *            The identifier of the provider that loaded the image
     * @param defaultImage
     *            whether the image is a default image or a custom image
     */
    public ByteArrayImage(byte[] payload, String mimeType, Date lastModificationDate,
            String providerId, boolean defaultImage) {
        this(payload, mimeType, lastModificationDate, providerId, defaultImage, false);
    }

    /**
     * @param payload
     *            The payload.
     * @param mimeType
     *            The mimetype.
     * @param lastModificationDate
     *            The last modification data.
     * @param providerId
     *            The identifier of the provider that loaded the image
     * @param defaultImage
     *            whether the image is a default image or a custom image
     * @param isExternal
     *            True, if this is an external image.
     */
    public ByteArrayImage(byte[] payload, String mimeType, Date lastModificationDate,
            String providerId, boolean defaultImage, boolean isExternal) {
        super(mimeType, lastModificationDate, payload.length, providerId, defaultImage, isExternal);
        this.payload = payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getBytes() throws IOException {
        return payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream openStream() {
        return new ByteArrayInputStream(payload);
    }
}
