package com.communote.server.persistence.security.iprange;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.springframework.dao.support.DataAccessUtils;

import com.communote.server.core.security.iprange.IpRangeHelper;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.security.IpRangeChannel;
import com.communote.server.model.security.IpRangeFilter;
import com.communote.server.model.security.IpRangeFilterConstants;
import com.communote.server.persistence.security.iprange.IpRangeFilterVO;


/**
 * @see com.communote.server.model.security.IpRangeFilter
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeFilterDaoImpl
        extends com.communote.server.persistence.security.iprange.IpRangeFilterDaoBase {

    /** The Constant RANGES_DELIMITER. */
    private static final String RANGES_DELIMITER = ",";

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
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#count()
     * @return Number of elements.
     */
    @Override
    protected int handleCount() {
        return DataAccessUtils.intResult(getHibernateTemplate().find(
                "select count(*) from " + IpRangeFilterConstants.CLASS_NAME));
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#getIpRanges(int, int)
     *      {@inheritDoc}
     */
    @Override
    protected List<IpRangeFilterVO> handleGetIpFilters(int offset, int maxCount) {
        Criteria criteria = getSession().createCriteria(IpRangeFilter.class);
        criteria.setFirstResult(offset);
        criteria.setMaxResults(maxCount);
        criteria.addOrder(Order.asc(IpRangeFilterConstants.NAME));
        List<IpRangeFilter> filters = criteria.list();
        List<IpRangeFilterVO> result = new ArrayList<IpRangeFilterVO>();
        if (filters != null) {
            for (IpRangeFilter filter : filters) {
                result.add(entityToVO(filter));
            }
        }
        return result;
    }
}