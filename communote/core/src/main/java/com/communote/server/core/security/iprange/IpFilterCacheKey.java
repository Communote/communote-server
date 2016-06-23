package com.communote.server.core.security.iprange;

import com.communote.server.core.common.caching.CacheKey;
import com.communote.server.model.security.ChannelType;


/**
 * Key class for ip ranges caching
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class IpFilterCacheKey implements CacheKey {

    private static final String PARTS_DELIMITER = "#";

    /**
     * Type for excludes
     */
    public final static int RANGE_TYPE_EXCLUDE = 0;
    /**
     * Type for includes
     */
    public final static int RANGE_TYPE_INCLUDE = 1;

    private ChannelType channelType;
    private int rangeType;

    /**
     * Constructor
     * 
     * @param channelType
     *            ChannelType
     * @param rangeType
     *            RangeType
     */
    public IpFilterCacheKey(ChannelType channelType, int rangeType) {
        super();
        this.channelType = channelType;
        this.rangeType = rangeType;
    }

    /**
     * {@inheritDoc}
     */
    public String getCacheKeyString() {
        StringBuilder sb = new StringBuilder();
        sb.append(channelType.toString());
        sb.append(IpFilterCacheKey.PARTS_DELIMITER);
        sb.append(rangeType);
        return sb.toString();
    }

    /**
     * Returns channel type
     * 
     * @return ChannelType
     */
    public ChannelType getChannelType() {
        return channelType;
    }

    /**
     * Get Range Type
     * 
     * @return Range Type
     */
    public int getRangeType() {
        return rangeType;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUniquePerClient() {
        return true;
    }

    /**
     * Set Channel type
     * 
     * @param channelType
     *            ChannelType
     */
    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    /**
     * Set Range Type
     * 
     * @param rangeType
     *            Range Type
     */
    public void setRangeType(int rangeType) {
        this.rangeType = rangeType;
    }

}
