package com.communote.server.web.fe.portal.user.client.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.common.exceptions.InvalidOperationException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.InvalidUserStatusTransitionException;
import com.communote.server.core.user.NoClientManagerLeftException;
import com.communote.server.core.user.UserDeletionDisabledException;
import com.communote.server.core.user.UserGroupMemberManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementException;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.user.group.GroupOperationNotPermittedException;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.core.user.validation.UserActivationValidationException;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.commons.helper.JsonRequestHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientUserManagementController extends MultiActionController {

    /** parameter name for the user id */
    public static final String PARAM_USER_ID = "userId";

    /** parameter name for the kenmei group entity id */
    public static final String PARAM_GROUP_ID = "groupEntityId";

    /** parameter name for the user role */
    public static final String PARAM_USER_ROLE = "role";

    /**
     * parameter name for the deletion mode
     */
    private static final String PARAM_BECOME_MANAGER = "becomeManager";

    private static final String PARAM_CONFIRMED_BLOG_IDS = "confirmedBlogIds";

    /**
     * parameter name for the deletion mode
     */
    private static final String PARAM_DELETE_MODE = "deleteMode";

    private static final String DELETE_MODE_ANONYMIZE = "anonymize";
    private static final String DELETE_MODE_DISABLE = "disable";

    private final static Logger LOGGER = LoggerFactory
            .getLogger(ClientUserManagementController.class);

    /**
     * Activates a user.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             in case of IO error when writing response
     */
    public void activateUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        String message = null;
        jsonResponse.put("status", ApiResult.ResultStatus.ERROR.name());
        Long userId = ParameterHelper.getParameterAsLong(request.getParameterMap(), PARAM_USER_ID);
        if (userId == null) {
            message = MessageHelper.getText(request,
                    "client.user.management.error.no.user.selected");
        } else {

            User user = ServiceLocator.instance().getService(UserManagement.class)
                    .findUserByUserId(userId);
            Object[] messageArguments = null;

            try {
                if (user != null) {
                    messageArguments = new Object[] { user.getAlias() };
                    ServiceLocator.instance().getService(UserManagement.class)
                            .changeUserStatusByManager(userId, UserStatus.ACTIVE);

                    message = MessageHelper.getText(request,
                            "client.user.management.activate.user.success", messageArguments);

                    jsonResponse.put("status", ApiResult.ResultStatus.OK.name());

                } else {
                    LOGGER.error("user (id = " + userId + ") not found.");
                    message = MessageHelper.getText(request,
                            "client.user.management.error.user.not.found", new Long[] { userId });
                }
            } catch (UserActivationValidationException e) {
                LOGGER.debug("activate user (id = {}) failed, {}", userId, e.getMessage());
                message = MessageHelper.getText(request, e.getReason(
                        "client.user.management.activate.user.error.", messageArguments));
            } catch (InvalidUserStatusTransitionException e) {
                LOGGER.error("activate user (id = " + userId + ") failed ", e);
                message = MessageHelper.getText(request,
                        "client.user.management.activate.user.error.not.activated",
                        messageArguments);
            } catch (AuthorizationException e) {
                LOGGER.error("user (id = " + userId + ") not found on calling activateUser");
                message = MessageHelper.getText(request,
                        "client.user.management.activate.user.error.not.admin");
            } catch (NoClientManagerLeftException e) {
                LOGGER.error("NoClientManagerLeftException while trying to activate user with id "
                        + userId);
                message = MessageHelper.getText(request,
                        "client.user.management.activate.user.error.not.activated",
                        messageArguments);
            } catch (UserNotFoundException e) {
                LOGGER.error("user (id = " + userId + ") not found on calling activateUser");
                message = MessageHelper.getText(request,
                        "client.user.management.error.user.not.found", messageArguments);
            } catch (UserManagementException e) {
                LOGGER.error("activating user (id = " + userId + ") failed", e);
                message = MessageHelper.getText(request,
                        "client.user.management.activate.user.error.not.activated",
                        messageArguments);
            }
        }

        jsonResponse.put("message", message);
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }

    /**
     * Assigns a role to a user.
     *
     * @param request
     *            the http servlet request
     * @param response
     *            the servlet response
     * @throws IOException
     *             in case of IO error when writing response
     */
    public void assignRole(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long userId = ParameterHelper.getParameterAsLong(request.getParameterMap(), PARAM_USER_ID);
        UserRole role = getRole(request);
        if (role == null || userId == null) {
            return;
        }
        try {
            ServiceLocator.instance().getService(UserManagement.class).assignUserRole(userId, role);
        } catch (AuthorizationException e) {
            LOGGER.error("Current user is not allowed to assign a role", e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Operation not allowed");
        } catch (InvalidOperationException e) {
            LOGGER.error("The role " + role.getValue() + " cannot be assigned", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Operation invalid");
        }
    }

    /**
     * Handles a request for confirming the user deletion.
     *
     * @param request
     *            the http servlet request
     * @param response
     *            the servlet response
     * @throws IOException
     *             in case an exception occurs while writing the response
     */
    public void confirmUserDeletion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long userId = ParameterHelper.getParameterAsLong(request.getParameterMap(), PARAM_USER_ID);
        User user = null;

        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        String message = null;

        if (userId != null) {
            user = ServiceLocator.instance().getService(UserManagement.class)
                    .findUserByUserId(userId);
        }
        if (user == null) {
            message = MessageHelper.getText(request,
                    "client.user.management.delete.user.error.not.existing");

            jsonResponse.put("status", ApiResult.ResultStatus.ERROR.name());

        } else {
            String deleteMode = getDeleteMode(request);

            if (deleteMode == null) {
                message = MessageHelper.getText(request,
                        "client.user.management.delete.user.error.no.mode");

                jsonResponse.put("status", ApiResult.ResultStatus.ERROR.name());

            } else {
                jsonResponse.put("status", ApiResult.ResultStatus.OK.name());

                jsonResponse.put("title", MessageHelper.getText(request,
                        "client.user.management.delete.user.confirm.title"));
                jsonResponse.put("confirmMessage", MessageHelper.getText(request,
                        "client.user.management.delete.user.confirm." + deleteMode,
                        new String[] { UserNameHelper.getDetailedUserSignature(user) }));
            }
        }

        jsonResponse.put("message", message);
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }

    /**
     * This method deletes the requested user by setting its status to {@link UserStatus#DELETED}.
     *
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @throws IOException
     *             in case of IO error when writing response
     */
    public void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ObjectNode jsonResponse = null;

        String messageKey = null;
        ApiResult.ResultStatus status = ApiResult.ResultStatus.ERROR;

        Long userId = ParameterHelper.getParameterAsLong(request.getParameterMap(), PARAM_USER_ID);

        if (userId == null) {
            messageKey = "client.user.management.error.no.user.selected";
        } else if (getDeleteMode(request) == null) {
            messageKey = "client.user.management.delete.user.error.no.mode";
        } else {

            String deletionMode = getDeleteMode(request);

            Boolean becomeManager = ServletRequestUtils.getBooleanParameter(request,
                    PARAM_BECOME_MANAGER, Boolean.TRUE);
            Long[] confirmedBlogIds = ParameterHelper.getParameterAsLongArray(
                    request.getParameterMap(), PARAM_CONFIRMED_BLOG_IDS, new Long[0]);

            try {
                internalDeleteUser(userId, deletionMode, confirmedBlogIds, becomeManager);

                messageKey = "client.user.management.delete.user.success";

                status = ApiResult.ResultStatus.OK;

                // log user out if userId == currentUserId
                if (userId.equals(SecurityHelper.getCurrentUserId())) {
                    ControllerHelper.sendInternalRedirectToLogoutUrl(request, response);
                    return;
                }

            } catch (AuthorizationException e) {
                messageKey = "client.user.management.delete.user.error.no.auth";
            } catch (NoClientManagerLeftException e) {
                messageKey = "client.user.management.delete.user.error.last.client.manager";
            } catch (UserDeletionDisabledException e) {
                // should not occur cause we are manager
                LOGGER.error("Unexpected exception.", e);
                messageKey = "client.user.management.delete.user.error.application.error";
            } catch (InvalidUserStatusTransitionException e) {
                // user has status DELETED
                messageKey = "client.user.management.delete.user.error.already.deleted";
            } catch (NoBlogManagerLeftException e) {
                jsonResponse = handleNoBlogManagerLeftException(request, response, e, userId);
            } catch (UserManagementException e) {
                messageKey = handleUserManagementExceptionOnUserDelete(request, response, e);
            }
        }
        if (jsonResponse == null) {
            jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
            jsonResponse.put("status", status.name());
            jsonResponse.put("message", MessageHelper.getText(request, messageKey));
        }
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }

    /**
     * This method temporarily disables the selected user. In case of an error an JSON object
     * describing the error is returned.
     *
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @throws IOException
     *             in case an exception occurs while writing the response
     */
    public void disableUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        String message = null;

        jsonResponse.put("status", ApiResult.ResultStatus.ERROR.name());

        Long userId = ParameterHelper.getParameterAsLong(request.getParameterMap(), PARAM_USER_ID);
        if (userId == null) {

            message = MessageHelper.getText(request,
                    "client.user.management.error.no.user.selected");
        } else {
            try {
                ServiceLocator.instance().getService(UserManagement.class)
                .changeUserStatusByManager(userId, UserStatus.TEMPORARILY_DISABLED);

                message = MessageHelper.getText(request,
                        "widget.user.management.profile.action.disable.success");

                jsonResponse.put("status", ApiResult.ResultStatus.OK.name());

                // log current user out if necessary
                if (userId.equals(SecurityHelper.getCurrentUserId())) {
                    ControllerHelper.sendInternalRedirectToLogoutUrl(request, response);
                }
            } catch (UserActivationValidationException e) {
                LOGGER.error("Unexpected exception while temporarily disabling user {}", userId, e);
                message = MessageHelper.getText(request,
                        "widget.user.management.profile.action.disable.failed");
            } catch (InvalidUserStatusTransitionException e) {
                LOGGER.error("temporarily disabling of user (id = " + userId + ") failed");
                message = MessageHelper.getText(request,
                        "widget.user.management.profile.action.disable.failed");
            } catch (UserNotFoundException e) {
                message = MessageHelper.getText(request,
                        "widget.user.management.profile.action.disable.not.found");
            } catch (NoClientManagerLeftException e) {
                message = MessageHelper.getText(request,
                        "widget.user.management.profile.action.disable.no.admin.left");
            } catch (AuthorizationException e) {
                message = MessageHelper.getText(request,
                        "widget.user.management.profile.action.disable.not.admin");
            }
        }

        jsonResponse.put("message", message);
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }

    /**
     * Returns the deleteMode from the request.
     *
     * @param request
     *            the request
     * @return the provided deletion mode or null if not found or the mode does not match
     */
    private String getDeleteMode(HttpServletRequest request) {
        String deleteMode = request.getParameter(PARAM_DELETE_MODE);
        if (!StringUtils.isBlank(deleteMode)) {
            if (deleteMode.equals(DELETE_MODE_ANONYMIZE) || deleteMode.equals(DELETE_MODE_DISABLE)) {
                return deleteMode;
            }
        }
        return null;
    }

    /**
     * Extracts a UserRole from the request.
     *
     * @param request
     *            the request
     * @return the extracted role or null if the provided role is not valid or no role is provided
     */
    private UserRole getRole(HttpServletRequest request) {
        String roleStr = request.getParameter(PARAM_USER_ROLE);
        UserRole role = null;
        if (!StringUtils.isBlank(roleStr)) {
            try {
                role = UserRole.fromString(roleStr);
            } catch (IllegalArgumentException e) {
                LOGGER.error("invalid user role: " + roleStr);
            }
        }
        return role;
    }

    /**
     * Creates a JSON object with details about the blogs/groups that would become manager less if
     * the user is deleted.
     *
     * @param request
     *            the servlet request
     * @param response
     *            the servlet response
     * @param exception
     *            the exception to handle
     * @param userId
     *            the user to delete
     *
     * @return The message.
     */
    private ObjectNode handleNoBlogManagerLeftException(HttpServletRequest request,
            HttpServletResponse response, NoBlogManagerLeftException exception, Long userId) {
        ControllerHelper.setApplicationSuccess(response);
        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        String messageKey = null;
        String concatedTitles = StringUtils.join(exception.getBlogIdsToTitleMapping().values(),
                ", ");
        String concatedIDs = StringUtils.join(exception.getBlogIdsToTitleMapping().keySet(), ",");
        String optionsMsgKey = null;
        String confirmMsgKey = null;

        jsonResponse.put("title",
                MessageHelper.getText(request, "client.user.management.delete.user.confirm.title"));
        jsonResponse.put("ids", concatedIDs);
        if (userId.equals(SecurityHelper.getCurrentUserId())) {
            // special messages if admin tries to delete himself
            jsonResponse.put("showBecomeManagerRadio", false);
            messageKey = "client.user.management.delete.oneself.managerless.blogs.message";
            optionsMsgKey = "client.user.management.delete.oneself.managerless.options";
            confirmMsgKey = "client.user.management.delete.oneself.managerless.options.confirm";
        } else {
            jsonResponse.put("showBecomeManagerRadio", true);
            messageKey = "client.user.management.delete.user.managerless.blogs.message";
            optionsMsgKey = "client.user.management.delete.user.managerless.options";
            confirmMsgKey = "client.user.management.delete.user.managerless.options.confirm";
        }
        jsonResponse.put("message",
                MessageHelper.getText(request, messageKey, new String[] { concatedTitles }));
        jsonResponse.put("optionsMessage", MessageHelper.getText(request, optionsMsgKey));
        jsonResponse.put("confirmMessage", MessageHelper.getText(request, confirmMsgKey));
        jsonResponse.put("status", ApiResult.ResultStatus.OK.name());

        return jsonResponse;
    }

    /**
     * Converts the exception into an error message
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param e
     *            the exception to handle
     * @return the message key of the error message
     */
    private String handleUserManagementExceptionOnUserDelete(HttpServletRequest request,
            HttpServletResponse response, UserManagementException e) {
        Throwable cause = e.getCause();
        String errorMsgKey = "client.user.management.delete.user.error.application.error";
        if (cause != null) {
            if (cause instanceof AuthorizationException) {
                errorMsgKey = "client.user.management.delete.user.error.delete.notes";
            } else {
                LOGGER.error("User deletion by client manager failed.", cause);
            }
        } else {
            LOGGER.error("User deletion by client manager failed.", e);
        }
        return errorMsgKey;
    }

    /**
     * Invokes user anonymization or permanently disabling.
     *
     * @param userId
     *            the user to delete
     * @param deleteMode
     *            the delete mode
     * @param confirmedBlogIds
     *            blog IDs the user confirmed to be handled according to the becomeManager parameter
     * @param becomeManager
     *            whether to make current user to manager of confirmed blogs or to delete them. Can
     *            be null which leads to ignoring the confirmedBlogIds.
     * @throws NoClientManagerLeftException
     *             in case deletion would lead to client without manager
     * @throws AuthorizationException
     *             in case the current user is not allowed to delete the user
     * @throws UserDeletionDisabledException
     *             when user deletion is disabled
     * @throws InvalidUserStatusTransitionException
     *             in case the user is already DELETED while try to permanently disable him
     * @throws NoBlogManagerLeftException
     *             in case deletion would lead to blogs without manager
     */
    private void internalDeleteUser(Long userId, String deleteMode, Long[] confirmedBlogIds,
            Boolean becomeManager) throws NoClientManagerLeftException, AuthorizationException,
            UserDeletionDisabledException, InvalidUserStatusTransitionException,
            NoBlogManagerLeftException {
        if (deleteMode.equals(DELETE_MODE_DISABLE)) {
            ServiceLocator.instance().getService(UserManagement.class)
            .permanentlyDisableUser(userId, confirmedBlogIds, becomeManager);
        } else {
            ServiceLocator.instance().getService(UserManagement.class)
            .anonymizeUser(userId, confirmedBlogIds, becomeManager);
        }
    }

    /**
     * Tries to remove a role from a user. Writes an error message as JSON object (with key
     * 'errorMessage') to the response when the role cannot be removed.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             in case of an IO exception
     */
    public void removeRole(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long userId = ParameterHelper.getParameterAsLong(request.getParameterMap(), PARAM_USER_ID);
        UserRole role = getRole(request);
        if (role != null && userId != null) {
            try {
                ServiceLocator.instance().getService(UserManagement.class)
                .removeUserRole(userId, role);
            } catch (NoClientManagerLeftException e) {
                ObjectNode jsonResponse = JsonRequestHelper.createJsonErrorResponse(MessageHelper
                        .getText(request,
                                "widget.user.management.profile.action.right.no.admin.left"));
                JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
            } catch (AuthorizationException e) {
                LOGGER.error("Current user is not allowed to remove a role", e);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Operation not allowed");
            } catch (InvalidOperationException e) {
                LOGGER.error("The role " + role.getValue() + " cannot be removed", e);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Operation invalid");
            }
        }
    }

    /**
     * Removes a CommunoteEntity from a KenmeiGroupEntity and sends a JSON response to the client.
     * The response could be {"message":"my personal error text","status":"ERROR"}.
     *
     * @param request
     *            the http servlet request
     * @param response
     *            the servlet response
     * @throws IOException
     *             in case of a IO error
     */
    public void removeUserFromGroupEntity(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        String message = null;

        if (!SecurityHelper.isClientManager()) {
            message = MessageHelper.getText(request,
                    "client.user.management.remove.user.from.group.nopermissions");
            jsonResponse.put("status", ApiResult.ResultStatus.ERROR.name());
        } else {

            Long userId = ParameterHelper.getParameterAsLong(request.getParameterMap(),
                    PARAM_USER_ID);
            Long groupId = ParameterHelper.getParameterAsLong(request.getParameterMap(),
                    PARAM_GROUP_ID);

            try {
                ServiceLocator.findService(UserGroupMemberManagement.class).removeEntityFromGroup(
                        groupId, userId);

                message = MessageHelper.getText(request,
                        "client.user.management.remove.user.from.group.success");

                jsonResponse.put("status", ApiResult.ResultStatus.OK.name());

            } catch (GroupOperationNotPermittedException e) {

                message = MessageHelper.getText(request,
                        "client.user.management.remove.user.from.group.error");

                jsonResponse.put("status", ApiResult.ResultStatus.ERROR.name());
            } catch (GroupNotFoundException e) {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.user.group.management.group.removefrom.error");
                LOGGER.warn("Illegal access.", e);
            }
        }

        jsonResponse.put("message", message);
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }

}
