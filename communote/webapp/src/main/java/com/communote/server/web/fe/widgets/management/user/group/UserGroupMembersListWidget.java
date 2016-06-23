package com.communote.server.web.fe.widgets.management.user.group;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.listitems.blog.member.CommunoteEntityData;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.UserGroupMemberManagement;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.user.CommunoteEntityQuery;
import com.communote.server.core.vo.query.user.CommunoteEntityQueryParameters;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;

/**
 * Widget for displaying group members.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserGroupMembersListWidget extends AbstractPagedListWidget<CommunoteEntityData> {

    private final static FilterWidgetParameterNameProvider NAME_PROVIDER = FilterWidgetParameterNameProvider.INSTANCE;
    private final static int PAGING_DEFAULT_OFFSET = 0;
    private final static int PAGING_MAX_COUNT = 20;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "widget.client.management.group.user.list.html";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PageableList<CommunoteEntityData> handleQueryList() {
        int offset = getIntParameter(NAME_PROVIDER.getNameForOffset(), PAGING_DEFAULT_OFFSET);
        int maxCount = getIntParameter(NAME_PROVIDER.getNameForMaxCount(), PAGING_MAX_COUNT);
        Long groupId = getLongParameter(UserGroupAddMemberWidget.PARAMETER_GROUP_ID, -1);
        if (groupId > -1) {
            CommunoteEntityQuery query = new CommunoteEntityQuery();
            CommunoteEntityQueryParameters parameters = query.createInstance();
            parameters.setDirectGroupMembershipFilteringGroupId(groupId);
            ResultSpecification resultSpecification = new ResultSpecification(offset, maxCount);
            parameters.setResultSpecification(resultSpecification);

            PageableList<CommunoteEntityData> result = ServiceLocator.findService(
                    QueryManagement.class).query(query, parameters);
            result.setMinNumberOfElements(ServiceLocator.findService(
                    UserGroupMemberManagement.class).countMembers(groupId));
            setPageInformation(result, resultSpecification);

            getRequest().setAttribute("isExternalGroup",
                    ServiceLocator.findService(UserGroupManagement.class).isExternalGroup(groupId));
            getRequest().setAttribute(UserGroupAddMemberWidget.PARAMETER_GROUP_ID,
                    groupId);
            return result;
        }
        return PageableList.emptyList();
    }

}
