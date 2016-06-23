package com.communote.server.web.fe.portal.user.profile.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.messaging.NotificationDefinition;
import com.communote.server.core.messaging.NotificationManagement;
import com.communote.server.core.messaging.NotificationManagementConstants;
import com.communote.server.core.messaging.NotificationScheduleTypes;
import com.communote.server.core.messaging.NotificationService;
import com.communote.server.core.messaging.vo.MessagerConnectorConfigTO;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.core.user.UserProfileManagementException;
import com.communote.server.model.messaging.MessagerConnectorType;
import com.communote.server.web.commons.FormAction;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.fe.portal.user.profile.forms.UserProfileNotificationsForm;

/**
 * Controller for modifying the notification configuration of a user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileNotificationsController extends BaseFormController {

    private static final String NOTIFICATION_SCHEDULE_PREFIX = "notificationSchedule_";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(UserProfileNotificationsController.class);

    /**
     *
     * @param userId
     *            the ID of the user
     */
    private void doRequestFriendShip(Long userId) {
        ServiceLocator.findService(NotificationManagement.class)
        .enableUser(userId, MessagerConnectorType.XMPP.toString());
    }

    /**
     *
     * @param request
     *            the servlet request
     * @param response
     *            the servlet response
     * @param form
     *            the form object
     * @param errors
     *            object to bind errors
     */
    private void doUpdateNotifications(HttpServletRequest request, HttpServletResponse response,
            UserProfileNotificationsForm form, BindException errors) {
        if (form.isXmppFail()) {
            // force an XMPP connector if the XMPP fail is set
            form.setXmpp(true);
        }
        Map<MessagerConnectorType, MessagerConnectorConfigTO> connectorConfigs;
        connectorConfigs = new HashMap<MessagerConnectorType, MessagerConnectorConfigTO>();
        if (form.isXmpp()) {
            MessagerConnectorConfigTO configTO = new MessagerConnectorConfigTO(
                    MessagerConnectorType.XMPP, NotificationManagementConstants.XMPP_PRIORITY);
            configTO.getProperties().put(NotificationManagementConstants.FALLBACK_ON_FAIL_LITERAL,
                    String.valueOf(form.isXmppFail()));
            connectorConfigs.put(MessagerConnectorType.XMPP, configTO);
        }
        if (form.isMail()) {
            MessagerConnectorConfigTO configTO = new MessagerConnectorConfigTO(
                    MessagerConnectorType.MAIL, NotificationManagementConstants.MAIL_PRIORITY);
            connectorConfigs.put(MessagerConnectorType.MAIL, configTO);
        }
        try {
            ServiceLocator.findService(UserProfileManagement.class).updateNotificationConfig(
                    connectorConfigs);
        } catch (UserProfileManagementException e) {
            LOGGER.error("Error changing notification config: " + e.getMessage(), e);
            errors.reject("user.profile.notification.change.failed");
            ControllerHelper.setApplicationFailure(response);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        Map<NotificationDefinition, NotificationScheduleTypes> schedules = ServiceLocator
                .findService(NotificationService.class)
                .getUserNotificationSchedules(SecurityHelper.getCurrentUserId());
        request.setAttribute("userNotificationSchedules", schedules);
        UserProfileNotificationsForm form = new UserProfileNotificationsForm();
        initNotification(request, form);
        return form;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {

        UserProfileNotificationsForm form = (UserProfileNotificationsForm) command;
        Long userId = SecurityHelper.getCurrentUserId();

        if (userId != null) {
            if (form.getAction().equals(FormAction.UPDATE_USER_PROFILE)) {
                doUpdateNotifications(request, response, form, errors);
            } else if (form.getAction().equals(FormAction.XMPP_REQUEST_FRIENDSHIP)) {
                doRequestFriendShip(userId);
            }
            saveNotificationSchedules(userId, request);
        }
        if (errors.hasErrors()) {
            return showForm(request, errors, getFormView());
        } else {
            MessageHelper.saveMessageFromKey(request, "user.profile.notification.save.success");
            return new ModelAndView(getSuccessView(), getCommandName(), command);
        }
    }

    /**
     * Initializes the notification.
     *
     * @param request
     *            The HttpServletRequest object
     * @param form
     *            The form object
     */
    private void initNotification(HttpServletRequest request, UserProfileNotificationsForm form) {
        List<MessagerConnectorConfigTO> connectorConfigs = ServiceLocator.findService(
                UserProfileManagement.class).getMessagerConnectorConfigs();
        if (connectorConfigs != null) {
            for (MessagerConnectorConfigTO config : connectorConfigs) {
                if (config.getType().equals(MessagerConnectorType.MAIL)) {
                    form.setMail(true);
                } else if (config.getType().equals(MessagerConnectorType.XMPP)) {
                    form.setXmpp(true);
                    String fallback = config.getProperties().get(
                            NotificationManagementConstants.FALLBACK_ON_FAIL_LITERAL);
                    if ("true".equals(fallback)) {
                        form.setXmppFail(true);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView processFormSubmission(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors) throws Exception {
        if (errors.hasErrors()) {
            ControllerHelper.setApplicationFailure(response);
        }
        return super.processFormSubmission(request, response, command, errors);
    }

    /**
     * Method to save users settings for notification definitions.
     *
     * @param userId
     *            The users id.
     * @param request
     *            The request.
     */
    private void saveNotificationSchedules(Long userId, HttpServletRequest request) {
        NotificationService notificationDefinitionService = ServiceLocator
                .findService(NotificationService.class);
        Map<String, String[]> parameters = request.getParameterMap();
        Map<NotificationDefinition, NotificationScheduleTypes> mappings =
                new HashMap<NotificationDefinition, NotificationScheduleTypes>();
        for (NotificationDefinition definition : notificationDefinitionService
                .getRegisteredDefinitions()) {
            boolean definitionEnabled = ParameterHelper.getParameterAsBoolean(parameters,
                    NOTIFICATION_SCHEDULE_PREFIX + definition.getId(), false);
            mappings.put(definition, definitionEnabled ? NotificationScheduleTypes.IMMEDIATE
                    : NotificationScheduleTypes.NEVER);
        }
        try {
            notificationDefinitionService.saveUserNotificationSchedules(userId, mappings);
            Map<NotificationDefinition, NotificationScheduleTypes> schedules = ServiceLocator
                    .findService(NotificationService.class)
                    .getUserNotificationSchedules(SecurityHelper.getCurrentUserId());
            request.setAttribute("userNotificationSchedules", schedules);
        } catch (NotFoundException e) { // Should never occur.
            MessageHelper
            .saveErrorMessageFromKey(request, "error.message.code.unknown.description");
            LOGGER.error("Error updating notification definitions", e);
        } catch (AuthorizationException e) { // Should never occur.
            MessageHelper
            .saveErrorMessageFromKey(request, "error.message.code.unknown.description");
            LOGGER.error("Error updating notification definitions", e);
        }
    }
}
