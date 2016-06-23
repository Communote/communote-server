package com.communote.server.web.fe.portal.user.client.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.ssl.ChannelManagement;
import com.communote.server.model.security.ChannelConfiguration;
import com.communote.server.model.security.ChannelType;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.fe.portal.user.client.forms.SslChannelConfigurationForm;

/**
 * List all defined channel types for the client and provides a form to define which channel type
 * for.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SslChannelConfigurationController extends BaseFormController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        List<ChannelConfiguration> channels = ServiceLocator.findService(ChannelManagement.class)
                .loadAll();
        SslChannelConfigurationForm form = new SslChannelConfigurationForm(channels);
        return form;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        SslChannelConfigurationForm form = (SslChannelConfigurationForm) command;
        updateSslChannelConfiguration(form);
        MessageHelper.saveMessage(request, MessageHelper.getText(request,
                "client.security.ssl.update.success"));
        return showForm(request, errors, getFormView());
    }

    /**
     *
     * @param form
     *            The form data.
     */
    private void updateSslChannelConfiguration(SslChannelConfigurationForm form) {
        List<ChannelConfiguration> newChannelConfig = new ArrayList<ChannelConfiguration>();
        if (form.getWeb()) {
            ChannelConfiguration channelConfiguration = ChannelConfiguration.Factory.newInstance(
                    form.getWeb(), ChannelType.WEB);
            newChannelConfig.add(channelConfiguration);
        }

        if (form.getApi()) {
            ChannelConfiguration channelConfiguration = ChannelConfiguration.Factory.newInstance(
                    form.getApi(), ChannelType.API);
            newChannelConfig.add(channelConfiguration);
        }

        if (form.getRss()) {
            ChannelConfiguration channelConfiguration = ChannelConfiguration.Factory.newInstance(
                    form.getRss(), ChannelType.RSS);
            newChannelConfig.add(channelConfiguration);
        }
        ServiceLocator.findService(ChannelManagement.class).update(newChannelConfig);
    }
}