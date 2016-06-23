package com.communote.server.core.security.iprange;

import java.util.ArrayList;
import java.util.List;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.security.iprange.IpRangeFilterManagement;
import com.communote.server.model.security.IpRange;


/**
 * Cache Provider for ip ranges
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class IpFilterCacheElementProvider implements
        CacheElementProvider<IpFilterCacheKey, IpRange[]> {

    private final static String CONTENT_TYPE = "ipFilter";
    private final static int TIME_TO_LIVE = 3600;

    /**
     * {@inheritDoc}
     */
    public String getContentType() {
        return CONTENT_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    public int getTimeToLive() {
        return TIME_TO_LIVE;
    }

    /**
     * {@inheritDoc}
     */
    public IpRange[] load(IpFilterCacheKey key) {
        IpRangeFilterManagement irfm = ServiceLocator.instance().getService(
                IpRangeFilterManagement.class);
        List<IpRange> ranges = new ArrayList<IpRange>();
        switch (key.getRangeType()) {
        case IpFilterCacheKey.RANGE_TYPE_EXCLUDE:
            ranges = irfm.findIpRanges(key.getChannelType(), false);
            break;
        case IpFilterCacheKey.RANGE_TYPE_INCLUDE:
            ranges = irfm.findIpRanges(key.getChannelType(), true);
            break;
        default:
            throw new IllegalArgumentException("Range type not supported");
        }
        return ranges.toArray(new IpRange[] { });
    }
}
