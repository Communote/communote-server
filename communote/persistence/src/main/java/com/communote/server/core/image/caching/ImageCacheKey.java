package com.communote.server.core.image.caching;

import com.communote.server.core.common.caching.CacheKey;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ImageCacheKey implements CacheKey {

    private final String cacheKey;
    private final String imageIdentifier;

    /**
     * @param imageIdentifier
     *            The identifier of the image
     */
    public ImageCacheKey(String imageIdentifier) {
        this.imageIdentifier = imageIdentifier;
        this.cacheKey = imageIdentifier;
    }

    @Override
    public String getCacheKeyString() {
        return cacheKey;
    }

    /**
     * @return the image identifier
     */
    public String getImageIdentifier() {
        return imageIdentifier;
    }

    @Override
    public boolean isUniquePerClient() {
        return true;
    }

}
