package com.communote.server.web.fe.portal.user.system.communication;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationPropertyXmpp;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class XmppAdvancedController extends BaseFormController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        ClientHelper.assertIsCurrentClientGlobal();
        XmppForm xmppForm = new XmppForm();

        String defaultSuffix = StringUtils.EMPTY;

        if (StringUtils.isNotBlank(ApplicationPropertyXmpp.HOST.getValue())) {
            defaultSuffix = "@" + ApplicationPropertyXmpp.HOST.getValue();
        }

        xmppForm.setBlogSuffix(ApplicationPropertyXmpp.BLOG_SUFFIX.getValue(defaultSuffix));
        xmppForm.setUserSuffix(ApplicationPropertyXmpp.USER_SUFFIX.getValue(defaultSuffix));
        xmppForm.setPostingInterval(ApplicationPropertyXmpp.TIME_TO_WAIT
                .getValue(XmppForm.DEFAULT_POSTING_INTERVAL));

        return xmppForm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        ClientHelper.assertIsCurrentClientGlobal();
        XmppForm form = (XmppForm) command;
        Map<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationPropertyXmpp.BLOG_SUFFIX, form.getBlogSuffix());
        settings.put(ApplicationPropertyXmpp.HANDLE_SUBSCRIPTION_REQUESTS,
                Boolean.toString(form.getSubscriptionEnabled()));
        settings.put(ApplicationPropertyXmpp.TIME_TO_WAIT, form.getPostingInterval());
        settings.put(ApplicationPropertyXmpp.USER_SUFFIX, form.getUserSuffix());
        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
                    .updateApplicationConfigurationProperties(settings);
        } catch (ConfigurationUpdateException e) {
            MessageHelper.saveErrorMessageFromKey(request, "client.system.settings.error");
            return new ModelAndView(getSuccessView(), "command", form);
        }
        MessageHelper.saveMessageFromKey(request, "client.system.settings.success");
        return new ModelAndView(getSuccessView(), "command", formBackingObject(request));
    }
}
