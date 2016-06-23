package com.communote.server.persistence.security.iprange;

import java.util.List;

import com.communote.server.model.security.ChannelType;
import com.communote.server.model.security.IpRange;
import com.communote.server.model.security.IpRangeChannelConstants;
import com.communote.server.model.security.IpRangeFilterConstants;


/**
 * The Class IpRangeChannelDaoImpl.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeChannelDaoImpl extends
        com.communote.server.persistence.security.iprange.IpRangeChannelDaoBase {

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDaoBase#handleFindEnabledExcludeRanges(com.communote.server.model.security.ChannelType)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<IpRange> handleFindEnabledExcludeRanges(ChannelType channel) {
        List<IpRange> list = getHibernateTemplate().find(
                "select filter.excludes from " + IpRangeFilterConstants.CLASS_NAME
                        + " filter left join filter." + IpRangeFilterConstants.EXCLUDES
                        + " ip left join filter." + IpRangeFilterConstants.CHANNELS
                        + " channel where channel." + IpRangeChannelConstants.TYPE
                        + " = ? AND filter." + IpRangeFilterConstants.ENABLED + " = TRUE AND "
                        + IpRangeChannelConstants.ENABLED + " = TRUE ", channel.toString());
        return list;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDaoBase#handleFindEnabledExcludeRanges(com.communote.server.model.security.ChannelType)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<IpRange> handleFindEnabledIncludeRanges(ChannelType channel) {
        List<IpRange> list = getHibernateTemplate().find(
                "select filter.includes from " + IpRangeFilterConstants.CLASS_NAME
                        + " filter left join filter." + IpRangeFilterConstants.INCLUDES
                        + " ip left join filter." + IpRangeFilterConstants.CHANNELS
                        + " channel where channel." + IpRangeChannelConstants.TYPE
                        + " = ? AND filter." + IpRangeFilterConstants.ENABLED + " = TRUE AND "
                        + IpRangeChannelConstants.ENABLED + " = TRUE ", channel.toString());
        return list;
    }

}
