package com.communote.server.web.fe.portal.service;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.client.ClientUrlHelper;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ContactLocalizedMessage implements LocalizedMessage {

    @Override
    public String toString(Locale locale, Object... arguments) {
        String supportEmailAddress = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.SUPPORT_EMAIL_ADDRESS);
        if (StringUtils.isBlank(supportEmailAddress)) {
            HttpServletRequest request = null;
            if (arguments != null && arguments.length > 0) {
                if (arguments[0] instanceof HttpServletRequest) {
                    request = (HttpServletRequest) arguments[0];
                }
            }
            String adminUrl;
            if (request == null) {
                adminUrl = ClientUrlHelper.renderConfiguredAbsoluteUrl("user/client/profile/email",
                        true);
            } else {
                adminUrl = ControllerHelper.renderUrl(request, "user/client/profile/email", null,
                        false, false, null, null, false, false);
            }
            return ResourceBundleManager.instance().getText(
                    "service.about.contact.missing-support-address", locale, adminUrl);
        } else {
            return ResourceBundleManager.instance().getText("service.about.contact.description",
                    locale, supportEmailAddress);
        }
    }

}
