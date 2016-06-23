package com.communote.server.web.fe.portal.user.system.application;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.core.security.ssl.ChannelManagement;
import com.communote.server.model.security.ChannelType;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ServerController extends BaseFormController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        ServerForm form = new ServerForm();
        form.setHostname(ApplicationProperty.WEB_SERVER_HOST_NAME.getValue());
        form.setHttpPort(ApplicationProperty.WEB_HTTP_PORT.getValue());
        form.setHttpsPort(ApplicationProperty.WEB_HTTPS_PORT.getValue());
        form.setContext(ApplicationProperty.WEB_SERVER_CONTEXT_NAME.getValue());
        form.setHttpsEnabled(Boolean.parseBoolean(ApplicationProperty.WEB_HTTPS_SUPPORTED
                .getValue()));
        return form;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        ServerForm form = (ServerForm) command;
        Map<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationProperty.WEB_SERVER_HOST_NAME, form.getHostname());
        settings.put(ApplicationProperty.WEB_HTTP_PORT, form.getHttpPort());
        settings.put(ApplicationProperty.WEB_HTTPS_PORT, form.getHttpsPort());
        settings.put(ApplicationProperty.WEB_SERVER_CONTEXT_NAME, form.getContext());
        settings.put(ApplicationProperty.WEB_HTTPS_SUPPORTED,
                Boolean.toString(form.getHttpsEnabled()));

        if (!form.getHttpsEnabled()) {
            ChannelManagement channelManagement = ServiceLocator
                    .findService(ChannelManagement.class);
            if (channelManagement.isForceSsl(ChannelType.WEB)
                    || channelManagement.isForceSsl(ChannelType.RSS)
                    || channelManagement.isForceSsl(ChannelType.API)) {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.system.application.server.https.enabled.error");
                return new ModelAndView(getSuccessView(), "command", form);
            }
        }

        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
            .updateApplicationConfigurationProperties(settings);
        } catch (ConfigurationUpdateException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.application.settings.error");
            return new ModelAndView(getSuccessView(), "command", form);
        }
        MessageHelper.saveMessageFromKey(request, "client.system.settings.success");
        return new ModelAndView(getSuccessView(), "command", formBackingObject(request));
    }

}
