package com.communote.server.core.image;

import java.awt.color.CMMException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.communote.common.image.ImageFormatType;
import com.communote.common.image.ImageScaler;
import com.communote.common.image.ImageSize;
import com.communote.server.api.core.image.ByteArrayImage;
import com.communote.server.api.core.image.FileImage;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageDescriptor;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.ImageProvider;
import com.communote.server.api.core.image.ImageProviderManagerException;
import com.communote.server.api.core.image.ImageTypeDescriptor;
import com.communote.server.api.core.image.ImageTypeNotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.image.caching.ImageCacheElementProvider;
import com.communote.server.core.image.caching.ImageCacheKey;
import com.communote.server.core.image.caching.ImageDiskCache;
import com.communote.server.model.user.ImageSizeType;

/**
 * Implementation which uses the file system for caching images.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com</a>
 */
@Service("imageManager")
public class FileSystemCachingImageManager implements ImageManager {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FileSystemCachingImageManager.class);

    @Autowired
    @Qualifier("cacheManager")
    private CacheManager cacheManager;

    private final ImageProviderManager providerManager;

    private final ImageDiskCache diskCache;

    /**
     * Constructor.
     */
    public FileSystemCachingImageManager() {
        this.diskCache = new ImageDiskCache();
        this.providerManager = new ImageProviderManager();
    }

    @Override
    public void defaultImageChanged(String typeName, String providerIdentifier) {
        List<ImageProvider> providers = providerManager.getProviders(typeName);
        if (providers != null) {
            for (ImageProvider provider : providers) {
                if (providerIdentifier == null
                        || provider.getIdentifier().equals(providerIdentifier)) {
                    diskCache.removeDefaultImages(typeName, providerIdentifier);
                }
            }
            // TODO not cluster save. See notes in imageChanged.
        }
    }

    /**
     * Get an image provider for the given type that can load the image with the identifier.
     *
     * @param type
     *            Type of the image.
     * @param imageIdentifier
     *            identifier of the image
     * @return An image provider or null if there is no matching.
     */
    private ImageProvider findImageProvider(String type, String imageIdentifier) {
        List<ImageProvider> providers = providerManager.getProviders(type);
        if (providers != null) {
            for (ImageProvider provider : providers) {
                if (provider.canLoad(imageIdentifier)) {
                    return provider;
                }
            }
        }
        return null;
    }

    /**
     * Get the default image or null if the provider has no default image.
     *
     * @param typeName
     *            the type of the image
     * @param descriptor
     *            the descriptor with image ID and details about scaling
     * @param imageProvider
     *            the provider which should be used to load the unscaled default image
     * @return the scaled default image or null
     * @throws ImageNotFoundException
     *             in case there is a default image but cannot be loaded or if loading the image
     *             during scaling failed
     * @throws IOException
     *             in case scaling failed or caching default image or a scaled image on disk failed
     */
    private Image getDefaultImage(String typeName, ImageDescriptor descriptor,
            ImageProvider imageProvider) throws ImageNotFoundException, IOException {
        Image image = null;
        if (imageProvider.hasDefaultImage(descriptor.getIdentifier())) {
            LOGGER.debug("Image provider {} of type {} did not provide "
                    + "the image with ID {}. Loading default image.",
                    imageProvider.getIdentifier(), typeName, descriptor.getIdentifier());
            File cachedImageFile = diskCache.getDefaultImage(typeName,
                    imageProvider.getIdentifier(), descriptor.getSize());
            if (cachedImageFile != null && cachedImageFile.exists()) {
                image = new FileImage(cachedImageFile, imageProvider.getDefaultImageMimeType(),
                        imageProvider.getIdentifier(), true, imageProvider.isExternalProvider());
            } else {
                // load and cache it
                try {
                    image = imageProvider.loadDefaultImage();
                } catch (ImageNotFoundException e) {
                    // because of parallel requests an external image provider can be temporarily
                    // disabled and might not be able to provide the default image anymore
                    if (!imageProvider.isExternalProvider()
                            && imageProvider.hasDefaultImage(descriptor.getIdentifier())) {
                        throw e;
                    }
                    return null;
                }
                ImageFormatType formatType = ImageFormatType.fromMimeType(image.getMimeType());
                image = resizeImage(typeName, image, descriptor, formatType);
                if (image == null) {
                    LOGGER.error("Scaling default image of type {} loaded by {} failed", typeName,
                            imageProvider.getIdentifier());
                    throw new ImageNotFoundException("Scaling default image failed");
                }
                cachedImageFile = diskCache.storeDefaultImage(typeName,
                        imageProvider.getIdentifier(), image, descriptor.getSize());
                image = new FileImage(cachedImageFile, formatType.getContentType(),
                        imageProvider.getIdentifier(), true, imageProvider.isExternalProvider());
            }
        }
        return image;
    }

    @Override
    public Image getImage(String typeName, ImageDescriptor descriptor)
            throws AuthorizationException, IOException, ImageNotFoundException {
        ImageProvider imageProvider = findImageProvider(typeName, descriptor.getIdentifier());
        if (imageProvider == null) {
            throw new ImageNotFoundException("There is no provider for the image type " + typeName);
        }
        Image image = getImage(typeName, descriptor, imageProvider);
        if (image == null) {
            // TODO should using the fallback be optional (global setting or defined by the
            // provider)?
            if (imageProvider.isExternalProvider()) {
                imageProvider = providerManager.getBuiltInProvider(typeName);
                if (imageProvider != null && imageProvider.canLoad(descriptor.getIdentifier())) {
                    LOGGER.debug(
                            "Attempting to load the image with ID {} of type {} with fallback provider {}",
                            descriptor.getIdentifier(), typeName, imageProvider.getIdentifier());
                    image = getImage(typeName, descriptor, imageProvider);
                }
            }
            // if there is no fallback or the provider did not return anything, give up
            if (image == null) {
                throw new ImageNotFoundException("Provider " + imageProvider.getIdentifier()
                        + " has no default for image " + descriptor.getIdentifier() + " of type "
                        + typeName);
            }
        }
        return image;
    }

    /**
     * Get the image of the given type
     *
     * @param typeName
     *            the type of the image
     * @param descriptor
     *            the descriptor with image ID and details about scaling
     * @param imageProvider
     *            the provider which should be used to load the unscaled image or default of that
     *            type
     * @return the image or if there is none the default image. The resulting image will be scaled
     *         as requested. If the provider has no default image for the identifier, null is
     *         returned.
     * @throws ImageNotFoundException
     *             in case there is a default image but cannot be loaded or if loading the image
     *             during scaling failed
     * @throws AuthorizationException
     *             in case the current user has no access to the image
     * @throws IOException
     *             in case scaling failed or caching default image or a scaled image on disk failed
     */
    private Image getImage(String typeName, ImageDescriptor descriptor, ImageProvider imageProvider)
            throws ImageNotFoundException, AuthorizationException, IOException {
        Image image = cacheManager.getCache().get(new ImageCacheKey(descriptor.getIdentifier()),
                new ImageCacheElementProvider(typeName, imageProvider, diskCache));
        if (image == null) {
            // provider did not find an image use the default image in case it exists
            image = getDefaultImage(typeName, descriptor, imageProvider);
        } else {
            // if the unscaled image was requested just return the one from the cache, otherwise
            // scale it
            if (descriptor.isScaled()) {
                String mimeType = image.getMimeType();
                File scaledImageFile = diskCache.getScaledImage(typeName,
                        imageProvider.getIdentifier(), descriptor.getIdentifier(),
                        descriptor.getSize());
                if (scaledImageFile == null || !scaledImageFile.exists()) {
                    ImageFormatType formatType = ImageFormatType.fromMimeType(image.getMimeType());
                    image = resizeImage(typeName, image, descriptor, formatType);
                    if (image == null) {
                        LOGGER.error("Scaling image with ID {} of type {} loaded by {} failed",
                                descriptor.getIdentifier(), typeName, imageProvider.getIdentifier());
                        throw new ImageNotFoundException("Scaling image failed");
                    }
                    scaledImageFile = diskCache.storeScaledImage(typeName,
                            imageProvider.getIdentifier(), descriptor.getIdentifier(), image,
                            descriptor.getSize());
                }
                image = new FileImage(scaledImageFile, mimeType, image.getProviderId(), false,
                        image.isExternal());
            }
        }
        return image;
    }

    @Override
    public Image getImage(String typeName, Map<String, ? extends Object> parameters,
            ImageSizeType size) throws AuthorizationException, IOException, ImageNotFoundException {
        // create a descriptor
        ImageTypeDescriptor type = providerManager.getTypeDescriptor(typeName);
        if (type == null) {
            throw new ImageNotFoundException("The image type " + typeName + " is not registered");
        }
        String imageIdentifier = type.extractImageIdentifier(parameters);
        if (imageIdentifier == null) {
            throw new ImageNotFoundException("No image identifier was given");
        }
        ImageSize imageSize = type.getSizeForImageSizeType(size);
        ImageDescriptor descriptor = new ImageDescriptor(imageSize, imageIdentifier, true,
                type.isDrawBackground(), type.getBackgroundColor(), type.isPreserveAspectRation(),
                type.getValidMimeTypes());
        descriptor.setHorizontalAlignment(type.getHorizontalAlignment());
        descriptor.setVerticalAlignment(type.getVerticalAlignment());
        return getImage(typeName, descriptor);
    }

    @Override
    public Image getImage(String typeName, String imageIdentifier, ImageSizeType size)
            throws AuthorizationException, IOException, ImageNotFoundException {
        // create a descriptor
        ImageTypeDescriptor type = providerManager.getTypeDescriptor(typeName);
        if (type == null) {
            throw new ImageNotFoundException("The image type " + typeName + " is not registered");
        }
        ImageSize imageSize = type.getSizeForImageSizeType(size);
        ImageDescriptor descriptor = new ImageDescriptor(imageSize, imageIdentifier, true,
                type.isDrawBackground(), type.getBackgroundColor(), type.isPreserveAspectRation(),
                type.getValidMimeTypes());
        descriptor.setHorizontalAlignment(type.getHorizontalAlignment());
        descriptor.setVerticalAlignment(type.getVerticalAlignment());
        return getImage(typeName, descriptor);
    }

    @Override
    public ImageSize getImageSize(String typeName, ImageSizeType sizeType) {
        ImageTypeDescriptor type = providerManager.getTypeDescriptor(typeName);
        if (type != null) {
            return type.getSizeForImageSizeType(sizeType);
        }
        return null;
    }

    @Override
    public String getImageVersionString(String typeName, String imageIdentifier)
            throws ImageNotFoundException, AuthorizationException {
        ImageTypeDescriptor type = providerManager.getTypeDescriptor(typeName);
        if (type == null) {
            throw new ImageNotFoundException("The image type " + typeName + " is not registered");
        }
        ImageProvider imageProvider = findImageProvider(typeName, imageIdentifier);
        if (imageProvider == null) {
            new ImageNotFoundException("There is no provider for the image type " + typeName);
        }
        String versionString;
        try {
            versionString = imageProvider.getVersionString(imageIdentifier);
        } catch (ImageNotFoundException e) {
            // check for default image
            if (imageProvider.hasDefaultImage(imageIdentifier)) {
                versionString = imageProvider.getDefaultImageVersionString();
            } else {
                // TODO do the fallback stuff here or is this enough when delivering the image?
                throw e;
            }
        }
        // also add version information of ImageTypeDescriptor so that version of a scaled image
        // changes if scaling behavior changes
        String typeVersionString = type.getVersionString();
        if (typeVersionString != null) {
            versionString += "_t" + typeVersionString;
        }
        return versionString;
    }

    @Override
    public void imageChanged(String typeName, String providerIdentifier, String imageIdentifier) {
        List<ImageProvider> providers = providerManager.getProviders(typeName);
        if (providers != null) {
            for (ImageProvider provider : providers) {
                if (providerIdentifier == null
                        || provider.getIdentifier().equals(providerIdentifier)) {
                    imageOfProviderChanged(typeName, provider, imageIdentifier);
                }
            }
            // TODO not cluster save. To get it working in cluster send a distributable event. The
            // event listener clears the local caches (disk) and notifies the providers. Other
            // caches are invalidated automatically. Event and listener should be hidden to avoid
            // that others send the event.
        }
    }

    private void imageOfProviderChanged(String typeName, ImageProvider imageProvider,
            String imageIdentifier) {
        ImageCacheElementProvider cacheProvider = new ImageCacheElementProvider(typeName,
                imageProvider, diskCache);
        if (imageIdentifier != null) {
            cacheManager.getCache().invalidate(new ImageCacheKey(imageIdentifier), cacheProvider);
        } else {
            cacheManager.getCache().invalidateAllOfCurrentClient(cacheProvider);
        }
        // remove from disk. Well, actually not necessary because when the unscaled image is
        // reloaded from the image provider the images stored on disk will be removed. The reason
        // for still doing it is to reduce the possibility that images cannot be removed because of
        // a concurrent read (e.g. streaming to a client)
        diskCache.removeImages(typeName, imageProvider.getIdentifier(), imageIdentifier);
        imageProvider.imageChanged(imageIdentifier);
    }

    @Override
    public void registerImageProvider(String imageTypeName, ImageProvider provider)
            throws ImageProviderManagerException, ImageTypeNotFoundException {
        this.providerManager.registerProvider(imageTypeName, provider);
    }

    @Override
    public void registerImageType(ImageTypeDescriptor imageType) {
        if (this.providerManager.registerTypeDescriptor(imageType)) {
            // since the type and thus sizes might have changed, invalidate scaled images for that
            // type
            this.diskCache.removeScaledImages(imageType.getName());
        }
    }

    /**
     * Resize the given image.
     *
     * @param typeName
     *            Name of the type.
     * @param image
     *            Image to resize.
     * @param descriptor
     *            The descriptor.
     * @param imageFormatType
     *            the mime type of the image to resize
     * @return The resized image or null if resizing failed
     * @throws IOException
     *             in case reading the image to resize failed
     */
    private Image resizeImage(String typeName, Image image, ImageDescriptor descriptor,
            ImageFormatType imageFormatType) throws IOException {
        ImageScaler scaler = new ImageScaler(descriptor.getSize(), imageFormatType);
        scaler.setDrawBackground(descriptor.isDrawBackground());
        scaler.setBackgroundColor(descriptor.getBackgroundColor());
        scaler.setSameAspectRatio(descriptor.isPreserveAspectRation());
        scaler.setHorizontalAlignment(descriptor.getHorizontalAlignment());
        scaler.setVerticalAlignment(descriptor.getVerticalAlignment());
        byte[] resizedImage = null;
        try {
            resizedImage = scaler.resizeImage(image.getBytes());
        } catch (CMMException e) {
            LOGGER.error("Was not able to read an image: " + typeName + ":"
                    + descriptor.getIdentifier() + " -> " + e.getMessage());
        }
        if (resizedImage != null) {
            return new ByteArrayImage(resizedImage, imageFormatType.getContentType(),
                    image.getLastModificationDate(), image.getProviderId(), image.isDefaultImage(),
                    image.isExternal());
        }
        return null;
    }

    @Override
    public void unregisterImageProvider(String imageTypeName, ImageProvider provider)
            throws ImageProviderManagerException {
        this.providerManager.unregisterProvider(imageTypeName, provider);
    }

    @Override
    public void unregisterImageType(ImageTypeDescriptor imageType) {
        if (this.providerManager.unregisterTypeDescriptor(imageType)) {
            // since the active overlay was removed, sizes might have changed and scaled images have
            // to be refreshed
            this.diskCache.removeScaledImages(imageType.getName());
        }
    }

}
