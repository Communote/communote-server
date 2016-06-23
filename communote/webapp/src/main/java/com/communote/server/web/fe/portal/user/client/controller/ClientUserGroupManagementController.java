package com.communote.server.web.fe.portal.user.client.controller;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.api.core.task.TaskStatusException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.UserGroupMemberManagement;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.user.group.GroupOperationNotPermittedException;
import com.communote.server.model.task.TaskStatus;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.widgets.management.user.group.UserGroupAddMemberWidget;

/**
 * This controller handles user group actions.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientUserGroupManagementController extends MultiActionController {
    private final static Logger LOGGER = LoggerFactory
            .getLogger(ClientUserGroupManagementController.class);

    /**
     * Deletes the group.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             in case of an IO exception
     * @throws ServletException
     *             Exception.
     */
    public void deleteGroup(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Long groupId = ServletRequestUtils.getLongParameter(request,
                UserGroupAddMemberWidget.PARAMETER_GROUP_ID);

        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        jsonResponse.put("status", ApiResult.ResultStatus.ERROR.name());

        String message = null;

        if (groupId == null) {
            message = MessageHelper.getText(request,
                    "client.user.group.management.error.no.group.selected");
        } else {

            try {
                ServiceLocator.findService(UserGroupManagement.class).deleteGroup(groupId);

                message = MessageHelper.getText(request,
                        "client.user.group.management.delete.success");

                jsonResponse.put("status", ApiResult.ResultStatus.OK.name());

            } catch (GroupOperationNotPermittedException e) {
                LOGGER.debug("Error deleting a group.", e);
                message = MessageHelper.getText(request,
                        "client.user.group.management.delete.failed");
            } catch (AuthorizationException e) {
                message = MessageHelper.getText(request, "common.not.authorized.operation");
            }
        }

        jsonResponse.put("message", message);
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }

    /**
     * Removes the given entity from the group.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             in case of an IO exception
     * @throws ServletException
     *             Exception
     */
    public void removeGroupMember(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Long groupId = ServletRequestUtils.getLongParameter(request,
                UserGroupAddMemberWidget.PARAMETER_GROUP_ID);
        Long entityId = ServletRequestUtils.getLongParameter(request,
                UserGroupAddMemberWidget.PARAMETER_ENTITY_ID);

        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        jsonResponse.put("status", ApiResult.ResultStatus.ERROR.name());

        String message = null;

        if (groupId == null || entityId == null) {
            message = MessageHelper.getText(request,
                    "client.user.group.management.group.removefrom.error");
        } else {

            try {
                ServiceLocator.findService(UserGroupMemberManagement.class).removeEntityFromGroup(
                        groupId, entityId);
                message = MessageHelper.getText(request,
                        "client.user.group.management.group.removefrom.success");
                jsonResponse.put("status", ApiResult.ResultStatus.OK.name());

            } catch (NumberFormatException e) {
                message = MessageHelper.getText(request,
                        "client.user.group.management.group.removefrom.error");
                LOGGER.warn("Not a number.", e);
            } catch (GroupOperationNotPermittedException e) {
                message = MessageHelper.getText(request,
                        "client.user.group.management.group.addto.failed.external");
                LOGGER.warn("Illegal access.", e);
            } catch (GroupNotFoundException e) {
                message = MessageHelper.getText(request,
                        "client.user.group.management.group.removefrom.error");
                LOGGER.warn("Illegal access.", e);
            }
        }

        jsonResponse.put("message", message);
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }

    /**
     * This method starts the synchronization and returns a JSON response.
     *
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @throws IOException
     *             in case writing the JSON response failed
     *
     */
    public void startSynchronization(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        TaskManagement taskManagement = ServiceLocator.instance().getService(TaskManagement.class);

        // do not run on clients other than the global to avoid DOS attacks
        // (because the job runs through all clients)
        String message = null;
        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        if (ClientHelper.isCurrentClientGlobal()) {
            try {
                CommunoteRuntime
                        .getInstance()
                        .getConfigurationManager()
                        .updateClientConfigurationProperty(
                                ClientProperty.GROUP_SYNCHRONIZATION_DO_FULL_SYNC,
                                Boolean.TRUE.toString());
                taskManagement.rescheduleTask("SynchronizeGroups", new Date());
                message = MessageHelper.getText(request,
                        "client.user.group.management.sync.start.success");
                jsonResponse.put("status", ApiResult.ResultStatus.OK.name());
            } catch (TaskStatusException e) {
                jsonResponse.put("status", ApiResult.ResultStatus.ERROR.name());
                if (TaskStatus.RUNNING.equals(e.getActualStatus())) {
                    message = MessageHelper.getText(request,
                            "client.user.group.management.sync.start.error.running");
                } else {
                    message = MessageHelper.getText(request,
                            "client.user.group.management.sync.start.error.failed");
                }
            }
        } else {
            jsonResponse.put("status", ApiResult.ResultStatus.ERROR.name());
            message = "Not supported!";
        }
        jsonResponse.put("message", message);
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }
}
