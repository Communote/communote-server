package com.communote.server.core.image.caching;

import java.io.File;
import java.io.IOException;

import com.communote.server.api.core.image.FileImage;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.ImageProvider;
import com.communote.server.api.core.image.ImageTemporarilyNotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.common.caching.AccessRestrictedCacheElementProvider;
import com.communote.server.core.common.caching.CacheElementProviderException;

/**
 * Provider which loads the unscaled images of a certain image type using an {@link ImageProvider}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ImageCacheElementProvider implements
AccessRestrictedCacheElementProvider<ImageCacheKey, Image> {

    private final int timeToLive;

    private final ImageProvider imageProvider;

    private final String typeName;
    private final ImageDiskCache diskCache;

    private final String contentType;

    /**
     * @param typeName
     *            The type this provider is used for.
     * @param imageProvider
     *            The image provider.
     * @param diskCache
     *            storage for caching images locally as files on disk
     */
    public ImageCacheElementProvider(String typeName, ImageProvider imageProvider,
            ImageDiskCache diskCache) {
        this.typeName = typeName;
        this.imageProvider = imageProvider;
        this.timeToLive = imageProvider.getTimeToLive();
        this.diskCache = diskCache;
        // the provider is responsible for images of a certain image-type that are provided by a
        // certain imageProvider. Content type must contain all these information.
        this.contentType = "image_" + typeName + imageProvider.getIdentifier();
    }

    /**
     * @param cacheKey
     *            The key to check access against.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access the item.
     */
    @Override
    public void assertAccess(ImageCacheKey cacheKey) throws AuthorizationException {
        if (!imageProvider.isAuthorized(cacheKey.getImageIdentifier())) {
            throw new AuthorizationException(
                    "The current user is not authorized to access the image: "
                            + cacheKey.getImageIdentifier());
        }
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * @return image time to live
     */
    @Override
    public int getTimeToLive() {
        return timeToLive;
    }

    /**
     * @param key
     *            The key, to be used to load the image.
     * @return The image or null, if there is no image.
     */
    @Override
    public Image load(ImageCacheKey key) throws CacheElementProviderException {
        try {
            Image image = imageProvider.loadImage(key.getImageIdentifier());
            if (image == null) {
                return null;
            }
            // TODO handle case when default image is returned?
            // cache unscaled image.
            File imageFile = diskCache.storeUnscaledImage(typeName, imageProvider.getIdentifier(),
                    key.getImageIdentifier(), image);
            return new FileImage(imageFile, image.getMimeType(), imageProvider.getIdentifier(),
                    false, image.isExternal());
        } catch (ImageTemporarilyNotFoundException e) {
            throw new CacheElementProviderException("Image temporarily not found", e);
        } catch (ImageNotFoundException e) {
            return null;
        } catch (AuthorizationException e) {
            throw new CacheElementProviderException("Current user has no access to the image", e);
        } catch (IOException e) {
            throw new CacheElementProviderException("Caching image on disk failed: "
                    + e.getMessage(), e);
        }
    }
}
