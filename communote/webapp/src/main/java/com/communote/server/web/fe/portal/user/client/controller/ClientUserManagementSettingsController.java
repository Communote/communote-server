package com.communote.server.web.fe.portal.user.client.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.config.type.ClientProperty.PRESELECTED_TABS_VALUES;
import com.communote.server.api.core.config.type.ClientProperty.PRESELECTED_TOPIC_OVERVIEW_TABS_VALUES;
import com.communote.server.core.ConfigurationManagementException;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.fe.portal.user.client.forms.ClientUserManagementSettingsForm;

/**
 * Controller for setting general user settings.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientUserManagementSettingsController extends BaseFormController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        ClientTO client = ClientAndChannelContextHolder.getClient();
        ClientUserManagementSettingsForm form = new ClientUserManagementSettingsForm(client);
        form.setAutomaticUserActivation(ClientProperty.AUTOMATIC_USER_ACTIVATION
                .getValue(ClientConfigurationHelper.DEFAULT_AUTOMATIC_USER_ACTIVATION));
        return form;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        ClientTO client = ClientAndChannelContextHolder.getClient();
        ClientUserManagementSettingsForm form = (ClientUserManagementSettingsForm) command;
        Map<ClientConfigurationPropertyConstant, String> map;
        map = new HashMap<ClientConfigurationPropertyConstant, String>();
        map.put(ClientProperty.AUTOMATIC_USER_ACTIVATION,
                Boolean.toString(form.isAutomaticUserActivation()));
        map.put(ClientProperty.CREATE_EXTERNAL_USER_AUTOMATICALLY,
                Boolean.toString(form.isCreateExternalUserAutomatically()));
        map.put(ClientProperty.NO_REGISTRATION_USER_NOTIFY_EMAILS_WHEN_EXTERNAL_AUTH,
                Boolean.toString(form.isNoNotifyEmailsToUserWhenExternalAuth()));
        map.put(ClientProperty.DELETE_USER_BY_ANONYMIZE_ENABLED,
                Boolean.toString(form.isAllowAnonymizeUserAccount()));
        map.put(ClientProperty.DELETE_USER_BY_DISABLE_ENABLED,
                Boolean.toString(form.isAllowDisableUserAccount()));
        map.put(ClientProperty.USER_REGISTRATION_ALLOWED,
                Boolean.toString(form.isUserRegistrationOnDBAuthAllowed()));
        map.put(ClientProperty.PRESELECTED_TAB,
                PRESELECTED_TABS_VALUES.valueOf(form.getPreselectedTab()).name());
        map.put(ClientProperty.PRESELECTED_TOPIC_OVERVIEW_TAB,
                PRESELECTED_TOPIC_OVERVIEW_TABS_VALUES.valueOf(
                        form.getPreselectedTopicOverviewTab()).name());
        map.put(ClientProperty.PRESELECTED_VIEW, form.getPreselectedView().name());
        map.put(ClientProperty.DEFAULT_LANGUAGE, form.getDefaultLanguage());

        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
                    .updateClientConfigurationProperties(map);
            MessageHelper.saveMessageFromKey(request, "client.user.management.save.success");
        } catch (ConfigurationManagementException e) {
            MessageHelper.saveErrorMessageFromKey(request, "client.configure.user.deletion.failed");
            // TODO reset values?
        }
        return new ModelAndView(getSuccessView(), getCommandName(),
                new ClientUserManagementSettingsForm(client));
    }
}
