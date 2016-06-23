package com.communote.server.core.security.iprange;

/**
 * <p>
 * Spring Service base class for
 * <code>com.communote.server.core.api.security.iprange.IpRangeFilterManagement</code>,
 * provides access to all services and entities referenced by this service.
 * </p>
 * 
 * @see com.communote.server.core.security.iprange.IpRangeFilterManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class IpRangeFilterManagementBase
        implements com.communote.server.core.security.iprange.IpRangeFilterManagement {

    private com.communote.server.persistence.security.iprange.IpRangeDao ipRangeDao;

    /**
     * Sets the reference to <code>ipRange</code>'s DAO.
     */
    public void setIpRangeDao(
            com.communote.server.persistence.security.iprange.IpRangeDao ipRangeDao) {
        this.ipRangeDao = ipRangeDao;
    }

    /**
     * Gets the reference to <code>ipRange</code>'s DAO.
     */
    protected com.communote.server.persistence.security.iprange.IpRangeDao getIpRangeDao() {
        return this.ipRangeDao;
    }

    private com.communote.server.persistence.security.iprange.IpRangeChannelDao ipRangeChannelDao;

    /**
     * Sets the reference to <code>ipRangeChannel</code>'s DAO.
     */
    public void setIpRangeChannelDao(
            com.communote.server.persistence.security.iprange.IpRangeChannelDao ipRangeChannelDao) {
        this.ipRangeChannelDao = ipRangeChannelDao;
    }

    /**
     * Gets the reference to <code>ipRangeChannel</code>'s DAO.
     */
    protected com.communote.server.persistence.security.iprange.IpRangeChannelDao getIpRangeChannelDao() {
        return this.ipRangeChannelDao;
    }

    private com.communote.server.persistence.security.iprange.IpRangeFilterDao ipRangeFilterDao;

    /**
     * Sets the reference to <code>ipRangeFilter</code>'s DAO.
     */
    public void setIpRangeFilterDao(
            com.communote.server.persistence.security.iprange.IpRangeFilterDao ipRangeFilterDao) {
        this.ipRangeFilterDao = ipRangeFilterDao;
    }

    /**
     * Gets the reference to <code>ipRangeFilter</code>'s DAO.
     */
    protected com.communote.server.persistence.security.iprange.IpRangeFilterDao getIpRangeFilterDao() {
        return this.ipRangeFilterDao;
    }

    /**
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagement#createFilter(String,
     *      String, String)
     */
    public com.communote.server.persistence.security.iprange.IpRangeFilterVO createFilter(
            String name, String includes, String excludes)
            throws com.communote.server.core.security.iprange.InvalidIpAddressException,
            com.communote.server.core.security.iprange.InvalidIpRangeException {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.createFilter(String name, String includes, String excludes) - 'name' can not be null or empty");
        }
        try {
            return this.handleCreateFilter(name, includes, excludes);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.iprange.IpRangeFilterManagementException(
                    "Error performing 'com.communote.server.core.api.security.iprange.IpRangeFilterManagement.createFilter(String name, String includes, String excludes)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #createFilter(String, String, String)}
     */
    protected abstract com.communote.server.persistence.security.iprange.IpRangeFilterVO handleCreateFilter(
            String name, String includes, String excludes)
            throws com.communote.server.core.security.iprange.InvalidIpAddressException,
            com.communote.server.core.security.iprange.InvalidIpRangeException;

    /**
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagement#updateFilter(Long,
     *      String, String, String, String,
     *      com.communote.server.model.security.ChannelType)
     */
    public void updateFilter(Long id, String name, String includes,
            String excludes, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.InvalidIpAddressException,
            com.communote.server.core.security.iprange.InvalidIpRangeException,
            com.communote.server.core.security.iprange.CurrentIpNotInRange {
        if (id == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.updateFilter(Long id, String name, String includes, String excludes, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'id' can not be null");
        }
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.updateFilter(Long id, String name, String includes, String excludes, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'name' can not be null or empty");
        }
        if (currentIP == null || currentIP.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.updateFilter(Long id, String name, String includes, String excludes, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'currentIP' can not be null or empty");
        }
        if (currentChannel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.updateFilter(Long id, String name, String includes, String excludes, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'currentChannel' can not be null");
        }
        try {
            this.handleUpdateFilter(id, name, includes, excludes, currentIP, currentChannel);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.iprange.IpRangeFilterManagementException(
                    "Error performing 'com.communote.server.core.api.security.iprange.IpRangeFilterManagement.updateFilter(Long id, String name, String includes, String excludes, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #updateFilter(Long, String, String, String, String, com.communote.server.model.security.ChannelType)}
     */
    protected abstract void handleUpdateFilter(Long id, String name,
            String includes, String excludes, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.InvalidIpAddressException,
            com.communote.server.core.security.iprange.InvalidIpRangeException,
            com.communote.server.core.security.iprange.CurrentIpNotInRange;

    /**
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagement#removeFilter(Long,
     *      String, com.communote.server.model.security.ChannelType)
     */
    public void removeFilter(Long id, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException {
        if (id == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.removeFilter(Long id, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'id' can not be null");
        }
        if (currentIP == null || currentIP.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.removeFilter(Long id, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'currentIP' can not be null or empty");
        }
        if (currentChannel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.removeFilter(Long id, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'currentChannel' can not be null");
        }
        try {
            this.handleRemoveFilter(id, currentIP, currentChannel);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.iprange.IpRangeFilterManagementException(
                    "Error performing 'com.communote.server.core.api.security.iprange.IpRangeFilterManagement.removeFilter(Long id, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #removeFilter(Long, String, com.communote.server.model.security.ChannelType)}
     */
    protected abstract void handleRemoveFilter(Long id, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException;

    /**
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagement#listFilter()
     */
    public java.util.List<com.communote.server.persistence.security.iprange.IpRangeFilterVO> listFilter() {
        try {
            return this.handleListFilter();
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.iprange.IpRangeFilterManagementException(
                    "Error performing 'com.communote.server.core.api.security.iprange.IpRangeFilterManagement.listFilter()' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for {@link #listFilter()}
     */
    protected abstract java.util.List<com.communote.server.persistence.security.iprange.IpRangeFilterVO> handleListFilter();

    /**
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagement#setFilterEnabled(Long,
     *      boolean, String, com.communote.server.model.security.ChannelType)
     */
    public com.communote.server.persistence.security.iprange.IpRangeFilterVO setFilterEnabled(
            Long id, boolean enabled, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException {
        if (id == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setFilterEnabled(Long id, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'id' can not be null");
        }
        if (currentIP == null || currentIP.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setFilterEnabled(Long id, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'currentIP' can not be null or empty");
        }
        if (currentChannel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setFilterEnabled(Long id, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'currentChannel' can not be null");
        }
        try {
            return this.handleSetFilterEnabled(id, enabled, currentIP, currentChannel);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.iprange.IpRangeFilterManagementException(
                    "Error performing 'com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setFilterEnabled(Long id, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #setFilterEnabled(Long, boolean, String, com.communote.server.model.security.ChannelType)}
     */
    protected abstract com.communote.server.persistence.security.iprange.IpRangeFilterVO handleSetFilterEnabled(
            Long id, boolean enabled, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException;

    /**
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagement#setFilterChannelEnabled(Long,
     *      com.communote.server.model.security.ChannelType, boolean, String,
     *      com.communote.server.model.security.ChannelType)
     */
    public com.communote.server.persistence.security.iprange.IpRangeFilterVO setFilterChannelEnabled(
            Long id, com.communote.server.model.security.ChannelType channel,
            boolean enabled, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException {
        if (id == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setFilterChannelEnabled(Long id, com.communote.server.persistence.security.ChannelType channel, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'id' can not be null");
        }
        if (channel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setFilterChannelEnabled(Long id, com.communote.server.persistence.security.ChannelType channel, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'channel' can not be null");
        }
        if (currentIP == null || currentIP.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setFilterChannelEnabled(Long id, com.communote.server.persistence.security.ChannelType channel, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'currentIP' can not be null or empty");
        }
        if (currentChannel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setFilterChannelEnabled(Long id, com.communote.server.persistence.security.ChannelType channel, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'currentChannel' can not be null");
        }
        try {
            return this.handleSetFilterChannelEnabled(id, channel, enabled, currentIP,
                    currentChannel);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.iprange.IpRangeFilterManagementException(
                    "Error performing 'com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setFilterChannelEnabled(Long id, com.communote.server.persistence.security.ChannelType channel, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #setFilterChannelEnabled(Long, com.communote.server.model.security.ChannelType, boolean, String, com.communote.server.model.security.ChannelType)}
     */
    protected abstract com.communote.server.persistence.security.iprange.IpRangeFilterVO handleSetFilterChannelEnabled(
            Long id, com.communote.server.model.security.ChannelType channel,
            boolean enabled, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException;

    /**
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagement#isInRange(String,
     *      com.communote.server.model.security.ChannelType)
     */
    public boolean isInRange(String ip,
            com.communote.server.model.security.ChannelType channel)
            throws com.communote.server.core.security.iprange.InvalidIpAddressException {
        if (ip == null || ip.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.isInRange(String ip, com.communote.server.persistence.security.ChannelType channel) - 'ip' can not be null or empty");
        }
        if (channel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.isInRange(String ip, com.communote.server.persistence.security.ChannelType channel) - 'channel' can not be null");
        }
        try {
            return this.handleIsInRange(ip, channel);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.iprange.IpRangeFilterManagementException(
                    "Error performing 'com.communote.server.core.api.security.iprange.IpRangeFilterManagement.isInRange(String ip, com.communote.server.persistence.security.ChannelType channel)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #isInRange(String, com.communote.server.model.security.ChannelType)}
     */
    protected abstract boolean handleIsInRange(String ip,
            com.communote.server.model.security.ChannelType channel)
            throws com.communote.server.core.security.iprange.InvalidIpAddressException;

    /**
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagement#setChannelEnabled(com.communote.server.model.security.ChannelType,
     *      boolean, String, com.communote.server.model.security.ChannelType)
     */
    public void setChannelEnabled(com.communote.server.model.security.ChannelType channel,
            boolean enabled, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException {
        if (channel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setChannelEnabled(com.communote.server.persistence.security.ChannelType channel, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'channel' can not be null");
        }
        if (currentIP == null || currentIP.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setChannelEnabled(com.communote.server.persistence.security.ChannelType channel, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'currentIP' can not be null or empty");
        }
        if (currentChannel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setChannelEnabled(com.communote.server.persistence.security.ChannelType channel, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel) - 'currentChannel' can not be null");
        }
        try {
            this.handleSetChannelEnabled(channel, enabled, currentIP, currentChannel);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.iprange.IpRangeFilterManagementException(
                    "Error performing 'com.communote.server.core.api.security.iprange.IpRangeFilterManagement.setChannelEnabled(com.communote.server.persistence.security.ChannelType channel, boolean enabled, String currentIP, com.communote.server.persistence.security.ChannelType currentChannel)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #setChannelEnabled(com.communote.server.model.security.ChannelType, boolean, String, com.communote.server.model.security.ChannelType)}
     */
    protected abstract void handleSetChannelEnabled(
            com.communote.server.model.security.ChannelType channel, boolean enabled,
            String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException;

    /**
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagement#findFilterById(Long)
     */
    public com.communote.server.persistence.security.iprange.IpRangeFilterVO findFilterById(
            Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.findFilterById(Long id) - 'id' can not be null");
        }
        try {
            return this.handleFindFilterById(id);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.iprange.IpRangeFilterManagementException(
                    "Error performing 'com.communote.server.core.api.security.iprange.IpRangeFilterManagement.findFilterById(Long id)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for {@link #findFilterById(Long)}
     */
    protected abstract com.communote.server.persistence.security.iprange.IpRangeFilterVO handleFindFilterById(
            Long id);

    /**
     * @see com.communote.server.core.security.iprange.IpRangeFilterManagement#findIpRanges(com.communote.server.model.security.ChannelType,
     *      boolean)
     */
    public java.util.List<com.communote.server.model.security.IpRange> findIpRanges(
            com.communote.server.model.security.ChannelType channel, boolean includes) {
        if (channel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.iprange.IpRangeFilterManagement.findIpRanges(com.communote.server.persistence.security.ChannelType channel, boolean includes) - 'channel' can not be null");
        }
        try {
            return this.handleFindIpRanges(channel, includes);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.iprange.IpRangeFilterManagementException(
                    "Error performing 'com.communote.server.core.api.security.iprange.IpRangeFilterManagement.findIpRanges(com.communote.server.persistence.security.ChannelType channel, boolean includes)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #findIpRanges(com.communote.server.model.security.ChannelType, boolean)}
     */
    protected abstract java.util.List<com.communote.server.model.security.IpRange> handleFindIpRanges(
            com.communote.server.model.security.ChannelType channel, boolean includes);

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     * 
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }
}