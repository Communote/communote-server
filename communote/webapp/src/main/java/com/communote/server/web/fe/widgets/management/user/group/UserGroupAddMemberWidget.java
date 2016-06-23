package com.communote.server.web.fe.widgets.management.user.group;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.user.UserGroupMemberManagement;
import com.communote.server.core.user.group.CantAddParentAsChildException;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.user.group.GroupOperationNotPermittedException;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.widgets.AbstractWidget;

/**
 * This widget adds a member to a group.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserGroupAddMemberWidget extends AbstractWidget {
    /** Parameter name for isGroup. */
    public static final String PARAMETER_IS_GROUP = "isGroup";
    /** Parameter name for groupId. */
    public static final String PARAMETER_GROUP_ID = "groupId";
    /** Parameter name for entityId. */
    public static final String PARAMETER_ENTITY_ID = "entityId";
    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(UserGroupAddMemberWidget.class);

    /**
     * This method adds a new entity to the group.
     * 
     * @param groupId
     *            ID of the group to add the entity to
     * @param entityId
     *            The entity to add.
     * @return true if the entity was added successfully
     */
    private boolean addEntity(Long groupId, Long entityId) {

        boolean isGroup = ParameterHelper.getParameterAsBoolean(getParameters(),
                PARAMETER_IS_GROUP, false);
        try {
            String messageKey;
            if (isGroup) {
                ServiceLocator.findService(UserGroupMemberManagement.class)
                        .addGroup(groupId, entityId);
                messageKey = "client.user.group.management.group.addto.success.group";
            } else {
                ServiceLocator.findService(UserGroupMemberManagement.class).addUser(groupId, entityId);
                messageKey = "client.user.group.management.group.addto.success.user";
            }
            MessageHelper.saveMessageFromKey(getRequest(), messageKey);
            return true;
        } catch (GroupNotFoundException e) {
            LOGGER.debug("There was an error adding a member to a group.", e);
            MessageHelper.saveErrorMessageFromKey(getRequest(),
                    "client.user.group.management.group.addto.failed");
        } catch (UserNotFoundException e) {
            LOGGER.debug("There was an error adding a member to a group.", e);
            MessageHelper.saveErrorMessageFromKey(getRequest(),
                    "client.user.group.management.group.addto.failed");
        } catch (GroupOperationNotPermittedException e) {
            LOGGER.debug("There was an error adding a member to a group.", e);
            MessageHelper.saveErrorMessageFromKey(getRequest(),
                    "client.user.group.management.group.addto.failed.external");
        } catch (CantAddParentAsChildException e) {
            LOGGER.debug("Parent<->Child constraint violated", e);
            MessageHelper.saveErrorMessageFromKey(getRequest(),
                    "client.user.group.management.group.addto.failed.child");
        } catch (AuthorizationException e) {
            MessageHelper.saveErrorMessageFromKey(getRequest(), "common.not.authorized.operation");
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "widget.user.group.usergroup.add.member." + outputType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        Long groupId = ParameterHelper.getParameterAsLong(getParameters(), PARAMETER_GROUP_ID);
        String submit = getParameter("submit");
        if (submit != null) {
            boolean success = false;
            String entityIdAsString = getParameter(PARAMETER_ENTITY_ID);
            if (StringUtils.isNotEmpty(entityIdAsString) && StringUtils.isNumeric(entityIdAsString)) {
                success = addEntity(groupId, Long.parseLong(entityIdAsString));
            } else {
                MessageHelper.saveErrorMessageFromKey(getRequest(),
                        "client.user.group.management.group.addto.noentry");
            }
            if (!success) {
                setSuccess(false);
            }
        }
        return groupId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }

}
