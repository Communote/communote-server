package com.communote.server.web.fe.widgets.management.user.group;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.communote.common.paging.PageInformation;
import com.communote.common.util.PageableList;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.group.GroupDao;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;
import com.communote.server.web.fe.widgets.management.user.GroupItem;

/**
 * Widget to represent user groups.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserGroupListWidget extends AbstractPagedListWidget<GroupItem> {

    private static final String PARAMETER_FILTER = "filter";
    private final static FilterWidgetParameterNameProvider NAME_PROVIDER = FilterWidgetParameterNameProvider.INSTANCE;
    private final static int PAGING_INTERVAL = 5;
    private final static int PAGING_DEFAULT_OFFSET = 0;
    private final static int PAGING_MAX_COUNT = 10;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "widget.user.group.usergrouplist." + outputType;
    }

    @Override
    protected PageableList<GroupItem> handleQueryList() {
        int offset = ParameterHelper.getParameterAsInteger(getParameters(),
                NAME_PROVIDER.getNameForOffset(), PAGING_DEFAULT_OFFSET);
        int maxCount = ParameterHelper.getParameterAsInteger(getParameters(),
                NAME_PROVIDER.getNameForMaxCount(), PAGING_MAX_COUNT);
        GroupDao groupDao = ServiceLocator.findService(GroupDao.class);
        // TODO do not use DAO but expose count method to management
        PageInformation information = new PageInformation(offset, maxCount,
                groupDao.count(getParameter(PARAMETER_FILTER)), PAGING_INTERVAL);
        setPageInformation(information);

        // TODO do not use DAO but appropriate query (and a converter) instead
        Collection<Group> groups = groupDao.loadWithReferences(offset, maxCount, ParameterHelper
                .getParameterAsString(getParameters(), PARAMETER_FILTER, StringUtils.EMPTY));

        PageableList<GroupItem> usersGroups = new PageableList<GroupItem>(
                new ArrayList<GroupItem>());

        usersGroups.setMinNumberOfElements(groups.size());

        for (Group group : groups) {
            GroupItem groupItem = new GroupItem();
            groupItem.setGroup(group);
            groupItem.setIsExternal(group instanceof ExternalUserGroup);
            usersGroups.add(groupItem);
        }

        return usersGroups;
    }

    /**
     * Does nothing.
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }
}
