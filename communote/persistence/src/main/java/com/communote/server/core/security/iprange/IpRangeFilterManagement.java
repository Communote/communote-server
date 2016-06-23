package com.communote.server.core.security.iprange;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface IpRangeFilterManagement {

    /**
     * 
     */
    public com.communote.server.persistence.security.iprange.IpRangeFilterVO createFilter(
            String name, String includes, String excludes)
            throws com.communote.server.core.security.iprange.InvalidIpAddressException,
            com.communote.server.core.security.iprange.InvalidIpRangeException;

    /**
     * 
     */
    public void updateFilter(Long id, String name, String includes,
            String excludes, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.InvalidIpAddressException,
            com.communote.server.core.security.iprange.InvalidIpRangeException,
            com.communote.server.core.security.iprange.CurrentIpNotInRange;

    /**
     * 
     */
    public void removeFilter(Long id, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException;

    /**
     * 
     */
    public java.util.List<com.communote.server.persistence.security.iprange.IpRangeFilterVO> listFilter();

    /**
     * 
     */
    public com.communote.server.persistence.security.iprange.IpRangeFilterVO setFilterEnabled(
            Long id, boolean enabled, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException;

    /**
     * 
     */
    public com.communote.server.persistence.security.iprange.IpRangeFilterVO setFilterChannelEnabled(
            Long id, com.communote.server.model.security.ChannelType channel,
            boolean enabled, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException;

    /**
     * 
     */
    public boolean isInRange(String ip,
            com.communote.server.model.security.ChannelType channel)
            throws com.communote.server.core.security.iprange.InvalidIpAddressException;

    /**
     * 
     */
    public void setChannelEnabled(com.communote.server.model.security.ChannelType channel,
            boolean enabled, String currentIP,
            com.communote.server.model.security.ChannelType currentChannel)
            throws com.communote.server.core.security.iprange.CurrentIpNotInRange,
            com.communote.server.core.security.iprange.InvalidIpAddressException;

    /**
     * 
     */
    public com.communote.server.persistence.security.iprange.IpRangeFilterVO findFilterById(
            Long id);

    /**
     * 
     */
    public java.util.List<com.communote.server.model.security.IpRange> findIpRanges(
            com.communote.server.model.security.ChannelType channel, boolean includes);

}
