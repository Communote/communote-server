package com.communote.server.core.image.caching;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.image.ImageSize;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.image.Image;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Helper to cache images on disk.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageDiskCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageDiskCache.class);

    private static final String SIZE_MARKER = "_Size";

    private String cacheRootDirectory;

    /**
     * Default constructor
     */
    public ImageDiskCache() {
        // default
    }

    /**
     * Constructor.
     *
     * @param cacheRootDirectory
     *            The caches root dir.
     */
    // TODO This is ugly, but needed for simple tests.
    public ImageDiskCache(String cacheRootDirectory) {
        this.cacheRootDirectory = cacheRootDirectory;
    }

    /**
     * Delete files in the given base directory that match the filter.
     *
     * @param baseDir
     *            the directory to search for files to delete
     * @param fileFilter
     *            filter a file needs to match to get deleted
     * @param typeName
     *            name of the image type. Only for logging.
     * @param providerId
     *            the ID of the provider that loaded the image. Only for logging.
     */
    private void deleteFiles(File baseDir, IOFileFilter fileFilter, String typeName,
            String providerId) {
        Iterator<File> fileIter = FileUtils.iterateFiles(baseDir, fileFilter, null);
        while (fileIter.hasNext()) {
            File imageFile = fileIter.next();
            try {
                FileUtils.forceDelete(imageFile);
            } catch (IOException e) {
                LOGGER.warn("Deleting cached image " + imageFile.getName() + " of type " + typeName
                        + " and provider " + providerId + " failed:" + e.getMessage());
            }
        }
    }

    /**
     * Get the directory for storing certain image files
     *
     * @param typeName
     *            Name of the image type.
     * @param isScaled
     *            whether scaled images should be stored
     * @param clientId
     *            ID of the client, can be null.
     * @return the directory for storing the images
     */
    private File getCacheDir(String typeName, boolean isScaled, String clientId) {
        StringBuilder builder = new StringBuilder(getCacheRootDir());
        builder.append(File.separator + "images" + File.separator);
        builder.append(typeName);
        builder.append(File.separator);
        if (isScaled) {
            builder.append("scaled");
        } else {
            builder.append("unscaled");
        }
        builder.append(File.separator);
        builder.append(StringUtils.trimToEmpty(clientId));
        return new File(builder.toString());
    }

    /**
     * @return Returns the cache root directory.
     */
    // TODO This is ugly, but needed for simple tests.
    private String getCacheRootDir() {
        if (cacheRootDirectory != null) {
            return cacheRootDirectory;
        }
        return CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties()
                .getCacheRootDirectory().getAbsolutePath();
    }

    /**
     * Get the stored default image.
     *
     * @param the
     *            name of the image type
     * @param imageProviderId
     *            the ID of the provider which loaded the image
     * @param imageSize
     *            the size of the default image
     * @return the file pointing to the stored default image or null if the file does not exist
     */
    public File getDefaultImage(String typeName, String imageProviderId, ImageSize imageSize) {
        File imageFile = getDefaultImageFile(typeName, imageProviderId, imageSize);
        if (imageFile.exists()) {
            return imageFile;
        }
        return null;
    }

    /**
     * Get path to the directory for storing the default images. The returned directory might not
     * exist.
     *
     * @param typeName
     *            the name of the image type
     * @return the image directory
     */
    private File getDefaultImageCacheDir(String typeName) {
        return new File(getCacheDir(typeName, true, ClientHelper.getCurrentClientId()), "default");
    }

    /**
     * The file pointing to the default image.
     *
     * @param typeName
     *            the name of the image type
     * @param imageProviderId
     *            the ID of the provider which loads the unscaled default image
     * @param imageSize
     *            the size of the default image
     * @return the file of the default image which might not exist
     */
    private File getDefaultImageFile(String typeName, String imageProviderId, ImageSize imageSize) {
        File imageCacheDir = getDefaultImageCacheDir(typeName);
        imageCacheDir.mkdirs();
        return new File(imageCacheDir, imageProviderId + SIZE_MARKER + imageSize.getHeight() + "x"
                + imageSize.getWidth());
    }

    /**
     * Get the file pointing to the image
     *
     * @param typeName
     *            Name of the image type.
     * @param imageProviderId
     *            the ID of the provider which loads the unscaled default image
     * @param imageIdentifier
     *            the identifier of the image
     * @param imageSize
     *            the size of the image. If null the unscaled is returned
     * @return the image file which might not exist
     */
    private File getImageFile(String typeName, String imageProviderId, String imageIdentifier,
            ImageSize size) {
        boolean scaled = size != null;
        File imageCacheDir = getCacheDir(typeName, scaled, ClientHelper.getCurrentClientId());
        // We assume, that the cache directory is writable, else the whole application wouldn't
        // start.
        imageCacheDir.mkdirs();
        String fileName = imageProviderId + "_" + imageIdentifier;
        if (scaled) {
            fileName += SIZE_MARKER + size.getHeight() + "x" + size.getWidth();
        }
        return new File(imageCacheDir, fileName);
    }

    /**
     * Get the stored scaled image
     *
     * @param typeName
     *            the name of the image type
     * @param imageProviderId
     *            the ID of the provider which loaded the unscaled image
     * @param imageIdentifier
     *            The identifier of the unscaled image
     * @param imageSize
     *            the size of the scaled image
     * @return the file pointing to the scaled image or null if it does not exist
     */
    public File getScaledImage(String typeName, String imageProviderId, String imageIdentifier,
            ImageSize imageSize) {
        File imageFile = getImageFile(typeName, imageProviderId, imageIdentifier, imageSize);
        if (imageFile.exists()) {
            return imageFile;
        }
        return null;
    }

    /**
     * Get the stored unscaled image
     *
     * @param typeName
     *            the name of the image type
     * @param imageProviderId
     *            the ID of the provider which loaded the unscaled image
     * @param imageIdentifier
     *            The identifier of the unscaled image
     * @return the file pointing to the image or null if it does not exist
     */
    public File getUnscaledImage(String typeName, String imageProviderId, String imageIdentifier) {
        File imageFile = getImageFile(typeName, imageProviderId, imageIdentifier, null);
        if (imageFile.exists()) {
            return imageFile;
        }
        return null;
    }

    /**
     * Remove the stored default images for the given type
     *
     * @param typeName
     *            the name of the image type
     * @param imageProviderId
     *            the ID of the image provider which loaded the default image
     */
    public void removeDefaultImages(String typeName, String imageProviderId) {
        File cacheDir = getDefaultImageCacheDir(typeName);
        if (cacheDir.exists()) {
            deleteFiles(cacheDir, new PrefixFileFilter(imageProviderId + SIZE_MARKER), typeName,
                    imageProviderId);
        }
    }

    /**
     * Remove the scaled and unscaled images of the given type that were loaded by the given
     * provider from disk.
     *
     * @param typeName
     *            the name of the image type.
     * @param providerId
     *            the ID of the provider that loaded the image
     * @param imageIdentifier
     *            identifier of the image to remove. Can be null to remove all images of the
     *            provider
     */
    public void removeImages(String typeName, String providerId, String imageIdentifier) {
        LOGGER.debug("Removing cached images of type {} and provider {}: {}", typeName, providerId,
                imageIdentifier);
        if (providerId == null) {
            throw new IllegalArgumentException("The providerId must not be null");
        }
        String clientId = ClientHelper.getCurrentClientId();
        File baseDir = getCacheDir(typeName, false, clientId);
        if (baseDir.exists()) {
            IOFileFilter fileFilter;
            String filePrefix = providerId + "_";
            if (imageIdentifier != null) {
                fileFilter = new NameFileFilter(filePrefix + imageIdentifier);
            } else {
                fileFilter = new PrefixFileFilter(filePrefix);
            }
            deleteFiles(baseDir, fileFilter, typeName, providerId);
        }
        baseDir = getCacheDir(typeName, true, clientId);
        if (baseDir.exists()) {
            String filePrefix = providerId + "_";
            if (imageIdentifier != null) {
                filePrefix += imageIdentifier + SIZE_MARKER;
            }
            deleteFiles(baseDir, new PrefixFileFilter(filePrefix), typeName, providerId);
        }
    }

    /**
     * Remove the scaled images (including the default images) of the given type from disk
     *
     * @param typeName
     *            the name of the image type
     */
    public void removeScaledImages(String typeName) {
        LOGGER.debug("Removing cached scaled images of type {}", typeName);
        String clientId = ClientHelper.getCurrentClientId();
        File baseDir = getCacheDir(typeName, true, clientId);
        try {
            FileUtils.deleteDirectory(baseDir);
        } catch (IOException e) {
            LOGGER.warn("Deleting cached scaled image files failed " + e.getMessage());
        }
    }

    /**
     * Store the provided default image on disk
     *
     * @param typeName
     *            the name of the image type
     * @param imageProviderId
     *            the ID of the provider which loaded the image
     * @param image
     *            the default image to store
     * @param imageSize
     *            the size of the default image
     * @return the file pointing to the stored image
     * @throws IOException
     *             in case storing the image failed
     */
    public File storeDefaultImage(String typeName, String imageProviderId, Image image,
            ImageSize imageSize) throws IOException {
        File path = getDefaultImageFile(typeName, imageProviderId, imageSize);
        if (path.exists()) {
            try {
                FileUtils.forceDelete(path);
            } catch (IOException e) {
                LOGGER.error("Deleting cached default image " + path.getAbsolutePath()
                        + " failed. Default image cannot be updated.", e);
                throw e;
            }
        }
        LOGGER.debug("Caching default image of type {} at {}", typeName, path.getAbsolutePath());
        storeImage(image, path);
        return path;
    }

    /**
     * Store the image on disk
     *
     * @param image
     *            the image to store
     * @param target
     *            the file where the image should be stored
     * @throws IOException
     *             in case storing failed
     */
    private void storeImage(Image image, File target) throws IOException {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = new FileOutputStream(target);
            inputStream = image.openStream();
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);
        }

    }

    /**
     * Store the provided scaled image on disk if it does not yet exist
     *
     * @param typeName
     *            the name of the image type
     * @param imageProviderId
     *            the ID of the provider which loaded the unscaled image
     * @param imageIdentifier
     *            The identifier of the unscaled image
     * @param image
     *            the scaled image to store
     * @param imageSize
     *            the size of the scaled image
     * @return the file pointing to the stored image
     * @throws IOException
     *             in case storing failed
     */
    public File storeScaledImage(String typeName, String imageProviderId, String imageIdentifier,
            Image image, ImageSize imageSize) throws IOException {
        File path = getImageFile(typeName, imageProviderId, imageIdentifier, imageSize);
        if (!path.exists()) {
            LOGGER.debug("Caching scaled image of type {} on disk at {}", typeName, path);
            storeImage(image, path);
        }
        return path;
    }

    /**
     * Store the provided image on disk. If the unscaled image exists it will be removed before
     * storing. Any scaled version will also be removed.
     *
     * @param typeName
     *            the name of the image type
     * @param imageProviderId
     *            the ID of the provider which loaded the image
     * @param imageIdentifier
     *            The identifier of the image
     * @param image
     *            the unscaled image to store
     * @return the file pointing to the stored image
     * @throws IOException
     *             in case storing failed
     */
    public File storeUnscaledImage(String typeName, String imageProviderId, String imageIdentifier,
            Image image) throws IOException {
        removeImages(typeName, imageProviderId, imageIdentifier);
        File path = getImageFile(typeName, imageProviderId, imageIdentifier, null);
        LOGGER.debug("Caching unscaled image of type {} on disk at {}", typeName,
                path.getAbsolutePath());
        storeImage(image, path);
        return path;
    }

}
