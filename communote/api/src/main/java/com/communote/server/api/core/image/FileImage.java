package com.communote.server.api.core.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Image which streams the image from a file.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class FileImage extends Image {

    private static final long serialVersionUID = -8046410129116651203L;
    private final String pathToImage;

    /**
     * @param file
     *            The file this image is for.
     * @param mimeType
     *            The mimetype for this image.
     * @param providerId
     *            The identifier of the provider that loaded the image
     * @param defaultImage
     *            whether the image is a default image or a custom image
     */
    public FileImage(File file, String mimeType, String providerId, boolean defaultImage) {
        this(file, mimeType, providerId, defaultImage, false);
    }

    /**
     * @param file
     *            The file this image is for.
     * @param mimeType
     *            The mimetype for this image.
     * @param providerId
     *            The identifier of the provider that loaded the image
     * @param defaultImage
     *            whether the image is a default image or a custom image
     * @param isExternal
     *            Set to true, if this image is an external image.
     */
    public FileImage(File file, String mimeType, String providerId, boolean defaultImage,
            boolean isExternal) {
        super(mimeType, new Date(file.lastModified()), file.length(), providerId, defaultImage,
                isExternal);
        this.pathToImage = file.getAbsolutePath();

    }

    /**
     * {@inheritDoc}
     * 
     * @return Returns a new {@link FileInputStream} to the file containing the image. You should
     *         explicitly close the stream after processing the image to avoid locked files.
     */
    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(pathToImage);
    }
}
