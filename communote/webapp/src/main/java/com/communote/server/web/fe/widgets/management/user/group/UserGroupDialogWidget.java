package com.communote.server.web.fe.widgets.management.user.group;

import java.util.HashMap;
import java.util.Map;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.CommunoteEntityDao;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget for editing and creating new groups.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserGroupDialogWidget extends AbstractWidget {

    /** Parameter name for id. */
    private static final String PARAMETER_ID = "groupId";

    /**
     * Extracts the group from the given data.
     *
     *
     * @return A {@link Group} object.
     */
    private Group extractGroup() {
        Long groupId = ParameterHelper.getParameterAsLong(getParameters(), PARAMETER_ID);
        if (groupId != null) {
            CommunoteEntity entity = ServiceLocator.findService(CommunoteEntityDao.class)
                    .loadWithImplementation(groupId);
            if (entity instanceof Group) {
                return (Group) entity;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "widget.user.group.usergroupdialog." + outputType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        Map<String, Object> model = new HashMap<String, Object>();
        Group group = extractGroup();
        if (group != null) {
            model.put("groupId", group.getId());
            model.put("isNotExternalGroup", !(group instanceof ExternalUserGroup));
        }
        return model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }

}
