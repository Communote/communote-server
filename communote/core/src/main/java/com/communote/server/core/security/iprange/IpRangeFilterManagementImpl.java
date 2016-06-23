package com.communote.server.core.security.iprange;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.security.IpRange;
import com.communote.server.model.security.IpRangeChannel;
import com.communote.server.model.security.IpRangeFilter;
import com.communote.server.persistence.security.iprange.IpRangeFilterVO;

/**
 * Service for client ip range access management.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeFilterManagementImpl extends
com.communote.server.core.security.iprange.IpRangeFilterManagementBase {

    /** The Constant RANGES_DELIMITER. */
    private static final String RANGES_DELIMITER = ",";

    private final IpFilterCacheElementProvider cacheElementProvider = new IpFilterCacheElementProvider();

    /**
     * Drop cache for all channels
     */
    private synchronized void dropCache() {
        for (String channel : ChannelType.names()) {
            dropCache(new ChannelType[] { ChannelType.fromString(channel) });
        }
    }

    /**
     * Drop cache for a channel.
     *
     * @param channels
     *            the channels
     */
    private synchronized void dropCache(ChannelType[] channels) {
        Cache cache = ServiceLocator.findService(CacheManager.class).getCache();
        for (ChannelType channel : channels) {
            IpFilterCacheKey keyEx = new IpFilterCacheKey(channel,
                    IpFilterCacheKey.RANGE_TYPE_EXCLUDE);
            IpFilterCacheKey keyIn = new IpFilterCacheKey(channel,
                    IpFilterCacheKey.RANGE_TYPE_INCLUDE);
            cache.invalidate(keyEx, cacheElementProvider);
            cache.invalidate(keyIn, cacheElementProvider);
        }
    }

    /**
     * Transforms a collection of ip range filter to value objects.
     *
     * @param filters
     *            the filters
     * @return the list
     */
    private List<IpRangeFilterVO> entityCollectionToVO(Collection<IpRangeFilter> filters) {
        List<IpRangeFilterVO> result = new ArrayList<IpRangeFilterVO>();
        if (filters != null) {
            for (IpRangeFilter filter : filters) {
                result.add(entityToVO(filter));
            }
        }
        return result;
    }

    /**
     * Transform an entity to a value object.
     *
     * @param filter
     *            the filter
     * @return the iP range filter vo
     */
    private IpRangeFilterVO entityToVO(IpRangeFilter filter) {
        IpRangeFilterVO result = new IpRangeFilterVO();

        result.setIncludes(IpRangeHelper.rangesToString(filter.getIncludes(), RANGES_DELIMITER));
        result.setExcludes(IpRangeHelper.rangesToString(filter.getExcludes(), RANGES_DELIMITER));

        List<ChannelType> channels = new ArrayList<ChannelType>();
        for (IpRangeChannel c : filter.getChannels()) {
            channels.add(c.getChannel());
        }
        result.setChannels(channels.toArray(new ChannelType[channels.size()]));
        result.setId(filter.getId());
        result.setName(filter.getName());
        result.setEnabled(filter.isEnabled());
        return result;
    }

    /**
     * Gets the channels.
     *
     * @param channels
     *            the channels
     * @return the channels
     */
    private ChannelType[] getChannels(Collection<IpRangeChannel> channels) {
        ChannelType[] result = new ChannelType[channels.size()];
        int i = 0;
        for (IpRangeChannel channel : channels) {
            result[i] = channel.getChannel();
            i++;
        }
        return result;
    }

    /**
     * Gets the excludes from database or cache.
     *
     * @param channel
     *            the channel
     * @return the excludes
     */
    public synchronized IpRange[] getExcludes(ChannelType channel) {
        Cache cache = ServiceLocator.findService(CacheManager.class).getCache();
        IpFilterCacheKey key = new IpFilterCacheKey(channel, IpFilterCacheKey.RANGE_TYPE_EXCLUDE);
        return cache.get(key, cacheElementProvider);
    }

    /**
     * Gets the include ranges from cache or database.
     *
     * @param channel
     *            the channel
     * @return the includes
     */
    public synchronized IpRange[] getIncludes(ChannelType channel) {
        Cache cache = ServiceLocator.findService(CacheManager.class).getCache();
        IpFilterCacheKey key = new IpFilterCacheKey(channel, IpFilterCacheKey.RANGE_TYPE_INCLUDE);
        return cache.get(key, cacheElementProvider);
    }

    /**
     * Gets the iP range channel.
     *
     * @param channel
     *            the channel
     * @return the iP range channel
     */
    private IpRangeChannel getIpRangeChannel(ChannelType channel) {
        IpRangeChannel rangeChannel = getIpRangeChannelDao().load(channel.getValue());
        if (rangeChannel == null) {
            rangeChannel = IpRangeChannel.Factory.newInstance();
            rangeChannel.setChannel(channel);
            rangeChannel.setEnabled(true);
            rangeChannel = getIpRangeChannelDao().create(rangeChannel);
        }
        return rangeChannel;
    }

    /**
     * {@inheritDoc}
     *
     * @throws InvalidIpAddressException
     * @throws InvalidIpRangeException
     * @throws InvalidIpAddressException
     * @throws InvalidIpRangeException
     * @see com.communote.server.model.security.security.IpRange.IpRangeFilterManagementBase#handleCreateFilter(String,
     *      String, String, String)
     */
    @Override
    protected IpRangeFilterVO handleCreateFilter(String name, String includes, String excludes)
            throws InvalidIpRangeException, InvalidIpAddressException {
        SecurityHelper.assertCurrentUserIsClientManager();
        List<IpRange> includeList = IpRangeHelper.stringToRanges(includes, RANGES_DELIMITER);
        List<IpRange> excludeList = IpRangeHelper.stringToRanges(excludes, RANGES_DELIMITER);

        IpRangeFilter filter = IpRangeFilter.Factory.newInstance();

        if (includeList.size() > 0) {
            filter.setIncludes(new HashSet<IpRange>(getIpRangeDao().create(includeList)));
        }
        if (excludeList.size() > 0) {
            filter.setExcludes(new HashSet<IpRange>(getIpRangeDao().create(excludeList)));
        }
        filter.setName(name);
        dropCache();
        return entityToVO(getIpRangeFilterDao().create(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IpRangeFilterVO handleFindFilterById(Long id) {
        IpRangeFilter filter = getIpRangeFilterDao().load(id);
        if (filter == null) {
            return null;
        }
        return entityToVO(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<IpRange> handleFindIpRanges(ChannelType channel, boolean includes) {
        if (!getIpRangeChannel(channel).isEnabled()) {
            return new ArrayList<IpRange>();
        }
        return internalFindIpRanges(channel, includes, null);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagementBase#handleIsInRange(String,
     *      com.communote.server.model.security.ChannelType)
     */
    // TODO current impl fail-fast with luck (e.g. one including and one excluding)
    @Override
    protected boolean handleIsInRange(String ip, ChannelType channel)
            throws InvalidIpAddressException {
        IpRange[] excludes = getExcludes(channel);
        IpRange[] includes = getIncludes(channel);
        // check if ip ranges are set
        if (ArrayUtils.isEmpty(excludes) && ArrayUtils.isEmpty(includes)) {
            // no ipranges? is in range!
            return true;
        }
        return internalIsInRange(ip, includes, excludes);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagementBase#handleListFilter()
     */
    @Override
    protected List<IpRangeFilterVO> handleListFilter() {
        return entityCollectionToVO(getIpRangeFilterDao().loadAll());
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagementBase#handleRemoveFilter(Long,
     *      String)
     */
    @Override
    protected void handleRemoveFilter(Long id, String currentIP, ChannelType currentChannel)
            throws CurrentIpNotInRange, InvalidIpAddressException {
        SecurityHelper.assertCurrentUserIsClientManager();
        IpRangeFilter filter = getIpRangeFilterDao().load(id);
        if (filter == null) {
            throw new IllegalArgumentException("filter not found");
        }
        if (filter.getIncludes() != null) {
            IpRangeChannel rangeChannel = getIpRangeChannel(currentChannel);
            if (filter.isEnabled() && rangeChannel.isEnabled()
                    && filter.getChannels().contains(rangeChannel)) {
                IpRange[] includes = internalFindIpRanges(currentChannel, true, filter).toArray(
                        new IpRange[] { });
                if (!internalIsInRange(currentIP, includes, new IpRange[0])) {
                    throw new CurrentIpNotInRange("Current IP address would be blocked.", currentIP);
                }
            }
            getIpRangeDao().remove(filter.getIncludes());
        }
        if (filter.getExcludes() != null) {
            getIpRangeDao().remove(filter.getExcludes());
        }
        dropCache(getChannels(filter.getChannels()));
        filter.getChannels().clear();
        getIpRangeFilterDao().update(filter);
        getIpRangeFilterDao().remove(filter);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagementBase#handleSetChannelEnabled(com.communote.server.model.security.ChannelType,
     *      boolean, String)
     */
    @Override
    protected void handleSetChannelEnabled(ChannelType channel, boolean enabled, String currentIP,
            ChannelType currentChannel) throws CurrentIpNotInRange, InvalidIpAddressException {
        SecurityHelper.assertCurrentUserIsClientManager();
        IpRangeChannel entity = getIpRangeChannel(channel);
        entity.setEnabled(enabled);
        // only check when current channel matches the channel of the filter
        if (channel.equals(currentChannel) && enabled) {
            // get includes and excludes by bypassing cache
            IpRange[] includes = handleFindIpRanges(currentChannel, true)
                    .toArray(new IpRange[] { });
            IpRange[] excludes = handleFindIpRanges(currentChannel, false)
                    .toArray(new IpRange[] { });
            if (!internalIsInRange(currentIP, includes, excludes)) {
                // disable again because no rollback on checked exception
                entity.setEnabled(!enabled);
                throw new CurrentIpNotInRange("Current IP address would be blocked.", currentIP);
            }
        }
        dropCache(new ChannelType[] { channel });
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagementBase#handleSetFilterChannelEnabled(Long,
     *      com.communote.server.model.security.ChannelType, boolean, String)
     */
    @Override
    protected IpRangeFilterVO handleSetFilterChannelEnabled(Long id, ChannelType channel,
            boolean enabled, String currentIP, ChannelType currentChannel)
                    throws CurrentIpNotInRange, InvalidIpAddressException {
        SecurityHelper.assertCurrentUserIsClientManager();
        IpRangeFilter filter = getIpRangeFilterDao().load(id);
        if (filter == null) {
            throw new IllegalArgumentException("filter not found");
        }
        IpRangeChannel rangeChannel = getIpRangeChannel(channel);
        // only check when current channel matches the channel of the filter
        if (channel.equals(currentChannel) && rangeChannel.isEnabled()) {
            if (enabled) {
                if (filter.isEnabled()) {
                    internalIsInRangeOfEnabledFilter(currentIP, filter, currentChannel);
                }
            } else if (filter.getIncludes() != null) {
                // filter.getChannels().remove(rangeChannel);
                IpRange[] includes = internalFindIpRanges(currentChannel, true, filter).toArray(
                        new IpRange[] { });
                // only check includes because removing an exclude won't block the current IP
                if (!internalIsInRange(currentIP, includes, new IpRange[] { })) {
                    throw new CurrentIpNotInRange("Current IP address would be blocked.", currentIP);
                }
            }
        }

        if (enabled) {
            filter.getChannels().add(rangeChannel);
        } else {
            filter.getChannels().remove(rangeChannel);
        }
        getIpRangeFilterDao().update(filter);
        dropCache(new ChannelType[] { channel });
        return entityToVO(filter);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagementBase#handleSetFilterEnabled(Long,
     *      boolean, String)
     */
    @Override
    protected IpRangeFilterVO handleSetFilterEnabled(Long id, boolean enabled, String currentIP,
            ChannelType currentChannel) throws CurrentIpNotInRange, InvalidIpAddressException {
        SecurityHelper.assertCurrentUserIsClientManager();
        IpRangeFilter filter = getIpRangeFilterDao().load(id);
        if (filter == null) {
            throw new IllegalArgumentException("filter not found");
        }
        if (filter.isEnabled() == enabled) {
            return entityToVO(filter);
        }
        // only check when current channel matches the channel of the filter
        IpRangeChannel rangeChannel = getIpRangeChannel(currentChannel);
        if (filter.getChannels().contains(rangeChannel) && rangeChannel.isEnabled()) {
            if (enabled) {
                // check only targeted filter because existing filter set did not block the IP
                internalIsInRangeOfEnabledFilter(currentIP, filter, currentChannel);
            } else if (filter.getIncludes() != null) {
                // bypass cache to avoid unnecessary re-caching
                IpRange[] includes = internalFindIpRanges(currentChannel, true, filter).toArray(
                        new IpRange[] { });
                // only check includes because removing an exclude won't block the current IP
                if (!internalIsInRange(currentIP, includes, new IpRange[] { })) {
                    throw new CurrentIpNotInRange("Current IP address would be blocked.", currentIP);
                }
            }
        }
        filter.setEnabled(enabled);
        dropCache(getChannels(filter.getChannels()));
        return entityToVO(filter);
    }

    /**
     * {@inheritDoc}
     *
     * @throws InvalidIpAddressException
     * @throws InvalidIpRangeException
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagementBase#handleUpdateFilter(Long,
     *      String, String, String, String)
     */
    // TODO use a VO for filter data and include enable status
    @Override
    protected void handleUpdateFilter(Long id, String name, String includes, String excludes,
            String currentIP, ChannelType currentChannel) throws InvalidIpRangeException,
            InvalidIpAddressException, CurrentIpNotInRange {
        SecurityHelper.assertCurrentUserIsClientManager();
        IpRangeFilter filter = getIpRangeFilterDao().load(id);
        if (filter == null) {
            throw new IllegalArgumentException("filter not found");
        }
        Collection<IpRange> includeList = IpRangeHelper.stringToRanges(includes, RANGES_DELIMITER);
        Collection<IpRange> excludeList = IpRangeHelper.stringToRanges(excludes, RANGES_DELIMITER);
        IpRangeChannel webChannel = getIpRangeChannel(currentChannel);
        if (filter.getChannels().contains(webChannel) && webChannel.isEnabled()
                && filter.isEnabled()) {
            List<IpRange> curIncludes = internalFindIpRanges(currentChannel, true, filter);
            curIncludes.addAll(includeList);
            List<IpRange> curExcludes = internalFindIpRanges(currentChannel, false, filter);
            curExcludes.addAll(excludeList);
            if (!internalIsInRange(currentIP, curIncludes.toArray(new IpRange[curIncludes.size()]),
                    curExcludes.toArray(new IpRange[curExcludes.size()]))) {
                throw new CurrentIpNotInRange("Current IP address would be blocked.", currentIP);
            }
        }
        // name
        filter.setName(name);
        // includes
        if (filter.getIncludes() != null) {
            getIpRangeDao().remove(filter.getIncludes());
            filter.getIncludes().clear();
        }
        if (includeList.size() > 0) {
            filter.getIncludes().addAll(getIpRangeDao().create(includeList));
        }
        // excludes
        if (filter.getExcludes() != null) {
            getIpRangeDao().remove(filter.getExcludes());
            filter.getExcludes().clear();
        }
        if (excludeList.size() > 0) {
            filter.getExcludes().addAll(getIpRangeDao().create(excludeList));
        }

        getIpRangeFilterDao().update(filter);
        dropCache(getChannels(filter.getChannels()));
    }

    /**
     * Checks if an ip is in range.
     *
     * @param ip
     *            the ip
     * @param ranges
     *            the ranges
     * @return true, if successful
     */
    private boolean inRange(BigInteger ip, IpRange[] ranges) {
        // if (ranges.length == 0) {
        // return false;
        // }
        for (IpRange range : ranges) {
            if (range.isInRange(ip)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of include or exclude IpRanges for a given channel. Only enabled filters will
     * be checked.
     *
     * @param channel
     *            the channel for which the IpRanges will be returned.
     * @param includes
     *            whether to return includes or excludes
     * @param filterToIgnore
     *            IpRangeFilter that should be ignored. Can be null.
     * @return the IpRanges
     */
    private List<IpRange> internalFindIpRanges(ChannelType channel, boolean includes,
            IpRangeFilter filterToIgnore) {
        List<IpRange> ranges = new ArrayList<IpRange>();

        Collection<IpRangeFilter> filters = getIpRangeFilterDao().loadAll();

        for (IpRangeFilter filter : filters) {
            if (filter.isEnabled() && !filter.equals(filterToIgnore)) {
                Set<IpRangeChannel> ipRangeChannels = filter.getChannels();
                boolean isChannel = false;
                for (IpRangeChannel ipRangeChannel : ipRangeChannels) {
                    if (ipRangeChannel.getChannel().equals(channel)) {
                        isChannel = true;
                        break;
                    }
                }
                if (isChannel) {
                    if (includes) {
                        if (filter.getIncludes() != null) {
                            ranges.addAll(filter.getIncludes());
                        }
                    } else {
                        if (filter.getExcludes() != null) {
                            ranges.addAll(filter.getExcludes());
                        }
                    }
                }
            }
        }

        return ranges;
    }

    /**
     * Test whether an IP is in the provided range.
     *
     * @param ip
     *            the IP to test
     * @param includes
     *            inclusive IP range definition
     * @param excludes
     *            exclusive IP range definition
     * @return true if the IP is in range (i.e. in includes if defined and not in excludes if
     *         defined)
     * @throws InvalidIpAddressException
     *             if the given ip is not valid
     */
    private boolean internalIsInRange(String ip, IpRange[] includes, IpRange[] excludes)
            throws InvalidIpAddressException {
        BigInteger ipValue = IpRangeHelper.ipToInt(ip);
        // do not filter if no ranges are defined
        boolean isInRange = true;
        if (excludes.length != 0) {
            isInRange = !inRange(ipValue, excludes);
        }
        if (isInRange && includes.length != 0) {
            isInRange = inRange(ipValue, includes);
        }
        return isInRange;
    }

    /**
     * Checks whether an IP address is in the range of a newly enabled filter. This check will test
     * the excludes. The includes will only be checked if the current filter set for the channel
     * does not have any ranges.
     *
     * @param currentIP
     *            the IP to test
     * @param filter
     *            the filter
     * @param channel
     *            the (enabled) channel of the filter for which test will be done
     * @throws InvalidIpAddressException
     *             if the given IP is not valid
     * @throws CurrentIpNotInRange
     *             if the IP is not in the range of the filter
     */
    private void internalIsInRangeOfEnabledFilter(String currentIP, IpRangeFilter filter,
            ChannelType channel) throws InvalidIpAddressException, CurrentIpNotInRange {
        IpRange[] excludes = filter.getExcludes() == null ? new IpRange[0] : filter.getExcludes()
                .toArray(new IpRange[filter.getExcludes().size()]);
        IpRange[] includes = new IpRange[0];
        // no existing include means all IPs are excepted, thus a new include could block the IP
        if (getIncludes(channel).length == 0) {
            includes = filter.getIncludes() == null ? new IpRange[0] : filter.getIncludes()
                    .toArray(new IpRange[filter.getIncludes().size()]);
        }
        if (!internalIsInRange(currentIP, includes, excludes)) {
            throw new CurrentIpNotInRange("Current IP address would be blocked.", currentIP);
        }
    }

}
