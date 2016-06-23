package com.communote.server.web.fe.widgets.management.security.iprange;

import java.util.List;

import com.communote.common.paging.PageInformation;
import com.communote.common.util.PageableList;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.persistence.security.iprange.IpRangeFilterDao;
import com.communote.server.persistence.security.iprange.IpRangeFilterVO;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;

/**
 * Widget for showing existing ip range filter.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class IpRangeListWidget extends AbstractPagedListWidget<IpRangeFilterVO> {

    private final static int PAGING_INTERVAL = 5;
    private final static int PAGING_DEFAULT_OFFSET = 0;
    private final static int PAGING_MAX_COUNT = 4;
    private final static FilterWidgetParameterNameProvider NAME_PROVIDER = FilterWidgetParameterNameProvider.INSTANCE;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "widget.client.management.security.iprange.list." + outputType;
    }

    /**
     * @return List of {@link IpRangeFilterVO}.
     */
    @Override
    protected PageableList<IpRangeFilterVO> handleQueryList() {
        SecurityHelper.assertCurrentUserIsClientManager();
        int offset = ParameterHelper.getParameterAsInteger(getParameters(), NAME_PROVIDER
                .getNameForOffset(), PAGING_DEFAULT_OFFSET);
        int maxCount = ParameterHelper.getParameterAsInteger(getParameters(), NAME_PROVIDER
                .getNameForMaxCount(), PAGING_MAX_COUNT);
        IpRangeFilterDao ipRangeFilterDao = ServiceLocator.findService(IpRangeFilterDao.class);
        PageInformation information = new PageInformation(offset, maxCount,
                ipRangeFilterDao.count(), PAGING_INTERVAL);
        setPageInformation(information);
        List<IpRangeFilterVO> filters = ipRangeFilterDao.getIpFilters(offset, maxCount);
        for (IpRangeFilterVO filter : filters) {
            filter.setName(filter.getName().replace("\"", "&quot;"));
        }
        return new PageableList<IpRangeFilterVO>(filters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }

}
