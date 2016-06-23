package com.communote.server.web.fe.portal.user.client.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.service.ClientCreationService;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.fe.portal.user.client.forms.ClientGeneralSettingsForm;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClientGeneralSettingsController extends BaseFormController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ClientGeneralSettingsController.class);

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        ClientGeneralSettingsForm form = new ClientGeneralSettingsForm();
        ClientTO client = ClientHelper.getCurrentClient();
        form.setClientName(client.getName());
        form.setTimeZoneId(CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getClientTimeZoneId());
        return form;
    }

    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        ClientGeneralSettingsForm form = (ClientGeneralSettingsForm) command;
        boolean success = true;
        try {
            ServiceLocator.findService(ClientCreationService.class).updateClientName(
                    form.getClientName());
        } catch (ClientNotFoundException e) {
            LOGGER.error("Unexpected exception updating client name", e);
            MessageHelper.saveErrorMessageFromKey(request, "common.error.unspecified");
            success = false;
        } catch (AuthorizationException e) {
            MessageHelper.saveErrorMessageFromKey(request, "common.not.authorized.operation");
            success = false;
        }
        if (success) {
            CommunoteRuntime.getInstance().getConfigurationManager()
                    .updateClientTimeZone(form.getTimeZoneId());
            MessageHelper.saveMessageFromKey(request, "common.changes.save.success");
            return new ModelAndView(getSuccessView(), getCommandName(), form);
        } else {
            return showForm(request, errors, getFormView());
        }
    }

}
