package com.communote.server.api.core.image;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.io.MimeTypeHelper;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.security.AuthorizationException;

/**
 * An image provider loads the unscaled images for a certain image type described by an
 * {@link ImageTypeDescriptor}.
 *
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ImageProvider {

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(ImageProvider.class);

    /** default time to live 43200 (12h) */
    private static final int DEFAULT_TIME_TO_LIVE = 43200;
    private final String pathToDefaultImage;
    private boolean defaultImagePrepared;
    private FileImage defaultImageFile;
    private String defaultImageResourcePath;
    private String defaultImageResourceMimeType;

    private final String identifier;

    /**
     * Create a new ImageProvider.
     *
     * @param identifier
     *            The identifier of the provider. The identifier has to be unique among all
     *            providers that are registered for an image type.
     * @param pathToDefaultImage
     *            If the path starts with file: it is interpreted as a file URI otherwise it is
     *            interpreted as the name of a resource containing the default image. This resource
     *            will be loaded with the class loader of this class. If null, there will be no
     *            default image.
     */
    public ImageProvider(String identifier, String pathToDefaultImage) {
        this.identifier = identifier;
        this.pathToDefaultImage = pathToDefaultImage;
    }

    /**
     * Test whether a provider can load the image. This should for instance return false if the
     * provider was disabled completely or partially for certain identifiers. Checking whether the
     * identified image exists is not necessary because {@link #loadImage(String)} will handle it.
     *
     * @param imageIdentifier
     *            the identifier of the image
     * @return whether the image can be loaded
     */
    public abstract boolean canLoad(String imageIdentifier);

    /**
     * Get the mime type of the default image
     *
     * @return the mime type
     * @throws ImageNotFoundException
     *             if there is no (valid) default image
     */
    public String getDefaultImageMimeType() throws ImageNotFoundException {
        if (defaultImageFile != null) {
            return defaultImageFile.getMimeType();
        }
        if (defaultImageResourcePath != null) {
            return defaultImageResourceMimeType;
        }
        throw new ImageNotFoundException("This provider has no default image");
    }

    /**
     * Get the timestamp of the last modification of the default image resource. This method is used
     * by {@link #getDefaultImageVersionString()} when the default image exists and is resource that
     * should be loaded with the class loader. This implementation returns the build-timestamp of
     * the application.
     *
     * @return the timestamp of the last modification
     */
    protected long getDefaultImageResourceLastModificationTimestamp() {
        return CommunoteRuntime.getInstance().getApplicationInformation().getBuildTimestamp();
    }

    /**
     * The version string of the default image.
     *
     * @return the version string
     * @throws ImageNotFoundException
     *             in case there is no default image
     *
     * @see #getVersionString(String)
     */
    public String getDefaultImageVersionString() throws ImageNotFoundException {
        prepareDefaultImage();
        if (defaultImageFile != null) {
            return String.valueOf(defaultImageFile.getLastModificationDate().getTime());
        }
        if (defaultImageResourcePath != null) {
            return String.valueOf(getDefaultImageResourceLastModificationTimestamp());
        }
        throw new ImageNotFoundException("This provider has no default image");
    }

    /**
     * Get a FileImage of the default image if it is a valid and supported image
     *
     * @param fileUri
     *            path to the default image as file URI
     * @return the image or null
     * @throws ImageNotFoundException
     *             in case the file was not found
     */
    private FileImage getFileDefaultImage(String fileUri) throws ImageNotFoundException {
        try {
            URI uri = new URI(fileUri);
            File imageFile = new File(uri);
            if (imageFile.exists()) {
                String mimeType = MimeTypeHelper.getMimeType(imageFile);
                if (mimeType != null && mimeType.startsWith("image/")) {
                    return new FileImage(imageFile, mimeType, this.getIdentifier(), true);
                } else {
                    LOGGER.warn("Default image file {} has no image mime type: {}",
                            imageFile.getAbsolutePath(), mimeType);
                }
            } else {
                LOGGER.warn("Default image file {} does not exist", imageFile.getAbsolutePath());
                throw new ImageNotFoundException();
            }
        } catch (URISyntaxException e) {
            LOGGER.warn("Default image URI {} is not valid: {}", fileUri, e.getMessage());
        }
        return null;
    }

    /**
     * Return the identifier of the provider. The identifier has to be unique among all providers
     * that are registered for an image type.
     *
     * @return the identifier of the provider
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the mime type of the the resource if it is a valid image
     *
     * @param stream
     *            the stream of the default image resource
     * @param resourceName
     *            name of the resource where the stream was loaded from
     * @return the mime type or null
     */
    private String getMimeTypeOfDefaultImageResource(InputStream stream, String resourceName) {
        String localName = null;
        int idx = resourceName.lastIndexOf('/');
        if (idx != -1 && idx < resourceName.length() - 1) {
            localName = resourceName.substring(idx);
        }
        String mimeType = MimeTypeHelper.getMimeType(stream, localName);
        if (mimeType != null && mimeType.startsWith("image/")) {
            return mimeType;
        }
        LOGGER.warn("The default image {} is not a valid image because it has {} mime type",
                resourceName, mimeType);
        return null;
    }

    /**
     * Get the mime type of the the resource if it is a valid image
     *
     * @param resourceName
     *            name of the resource to load with the class loader of the resource
     * @return the mime type or null
     */
    private String getMimeTypeOfDefaultImageResource(String resourceName) {
        InputStream stream = null;
        try {
            stream = this.getClass().getResourceAsStream(resourceName);
            if (stream == null) {
                LOGGER.warn("The default image {} cannot be found", resourceName);
            } else {
                // wrap into a stream that supports mark and reset which is required by MimeType
                // parsing
                stream = new BufferedInputStream(stream);
                return getMimeTypeOfDefaultImageResource(stream, resourceName);
            }
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
        return null;
    }

    /**
     * Get the number of seconds until a cached image should expire and thus be reloaded
     *
     * @return time to live in the cache
     */
    public int getTimeToLive() {
        return DEFAULT_TIME_TO_LIVE;
    }

    /**
     * Return a string which represents the version of the image. Whenever an image is modified the
     * version string should change. An example of a version string could be the timestamp of the
     * last modification.<br>
     *
     * This method is called by the image manager from a method with the same name. The method of
     * the image manager is intended to be called during image URL creation to get an image URL
     * reflecting the latest image version. Since an image URL is usually rendered along with other
     * content, this method should return quickly. The actual image retrieval (download) one the
     * other hand which is done by loadImage can take longer.
     *
     * @param imageIdentifier
     *            the identifier of the image
     * @return the version string of the image.
     * @throws ImageNotFoundException
     *             in case there is no image for the imageIdentifier
     * @throws AuthorizationException
     *             in case the current user is not allowed to access the image
     */
    public abstract String getVersionString(String imageIdentifier) throws AuthorizationException,
    ImageNotFoundException;

    /**
     * Return whether the provider has a default image for the case when there is no image for a
     * given identifier.
     *
     * This implementation will return true if the resource identified by the constructor parameter
     * pathToDefaultImage exists.
     *
     * @param imageIdentifier
     *            the identifier for which no image was found
     *
     * @return whether there is a default image
     */
    public boolean hasDefaultImage(String imageIdentifier) {
        prepareDefaultImage();
        return defaultImageFile != null || defaultImageResourcePath != null;
    }

    /**
     * Invoked by the {@link ImageManager#imageChanged(String, String, String)} to notify the
     * provider about a changed image. The provider can use this method to reset internal caches.
     * The default implementation does nothing.
     *
     * @param imageIdentifier
     *            the identifier of the changed image. Can be null in the case that all images have
     *            changed.
     */
    public void imageChanged(String imageIdentifier) {
    }

    /**
     * Return whether the current user is allowed to access the given image.
     *
     * @param imageIdentifier
     *            identifier of the image
     * @return True, if the user is allowed to access the image.
     */
    public abstract boolean isAuthorized(String imageIdentifier);

    /**
     * Return whether this provider loads image from an external resource (like another server) and
     * not from the local database or file system.
     *
     * @return true, if it is a provider for external images
     */
    public abstract boolean isExternalProvider();

    /**
     * Load the default image.
     *
     * @return the default image
     * @throws ImageNotFoundException
     *             in case there is no default image.
     */
    public Image loadDefaultImage() throws ImageNotFoundException {
        prepareDefaultImage();
        if (defaultImageFile != null) {
            return defaultImageFile;
        }
        if (defaultImageResourcePath != null) {
            InputStream stream = null;
            try {
                stream = this.getClass().getResourceAsStream(defaultImageResourcePath);
                if (stream == null) {
                    LOGGER.warn("The default image {} cannot be found", defaultImageResourcePath);
                } else {
                    // wrap into a stream that supports mark and reset which is required by MimeType
                    // parsing
                    stream = new BufferedInputStream(stream);
                    String mimeType = getMimeTypeOfDefaultImageResource(stream,
                            defaultImageResourcePath);
                    if (mimeType != null) {
                        // unsure how much data has been consumed by other methods and whether
                        // markpos is set to 0 so that reset will lead to reading the whole image.
                        // Thus, close and open the stream again.
                        IOUtils.closeQuietly(stream);
                        stream = this.getClass().getResourceAsStream(defaultImageResourcePath);
                        byte[] imageAsBytes = IOUtils.toByteArray(stream);
                        return new ByteArrayImage(imageAsBytes, mimeType, new Date(
                                getDefaultImageResourceLastModificationTimestamp()),
                                this.getIdentifier(), true, false);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("There was an error reading the default image resource {}",
                        defaultImageResourcePath, e);
            } finally {
                if (stream != null) {
                    // really needed?
                    IOUtils.closeQuietly(stream);
                }
            }
            // resource not valid, do not try again
            defaultImageResourcePath = null;
        }
        throw new ImageNotFoundException("This provider has no default image");
    }

    /**
     * Load the unscaled image
     *
     * @param imageIdentifier
     *            The identifier of the image
     * @return The image.
     * @throws ImageNotFoundException
     *             in case there is no image for the imageIdentifier or loading failed
     * @throws AuthorizationException
     *             in case the current user is not allowed to access the image
     */
    public abstract Image loadImage(String imageIdentifier) throws ImageNotFoundException,
    AuthorizationException;

    /**
     * Prepare the default image if not yet prepared.
     */
    private void prepareDefaultImage() {
        if (!defaultImagePrepared) {
            synchronized (this) {
                if (defaultImagePrepared) {
                    return;
                }
                if (pathToDefaultImage == null) {
                    defaultImagePrepared = true;
                    return;
                }
                try {
                    FileImage defaultImage = null;
                    if (pathToDefaultImage.startsWith("file:")) {
                        defaultImage = getFileDefaultImage(pathToDefaultImage);
                    } else {
                        // just check whether it is valid and load it when needed
                        defaultImageResourceMimeType = getMimeTypeOfDefaultImageResource(pathToDefaultImage);
                        if (defaultImageResourceMimeType != null) {
                            defaultImageResourcePath = pathToDefaultImage;
                        }
                    }
                    defaultImageFile = defaultImage;
                    defaultImagePrepared = true;
                } catch (ImageNotFoundException e) {
                    // ignore and try again next time (useful for bundles, when image is a loaded
                    // from bundle storage which is populated asynchronously)
                }
            }
        }
    }
}
