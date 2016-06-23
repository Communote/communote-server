package com.communote.server.web.fe.portal.service;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.ssl.ChannelManagement;
import com.communote.server.model.security.ChannelType;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Special localized message returning the description of the mobile apps entry of the tools page.
 * The first argument must be the current HttpServletRequest.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MobileAppsDescriptionLocalizedMessage implements LocalizedMessage {

    /**
     * Get localized message which describes how to configure the mobile app.
     *
     * @param locale
     *            the locale
     * @param request
     *            the current request
     * @return the settings message
     */
    protected String getSettingsMessage(Locale locale, HttpServletRequest request) {
        String url = ControllerHelper.renderAbsoluteUrl(request, null, "/", false, false, false);
        url = StringUtils.substringBetween(url, "://", "/microblog");
        String sslSuffix;
        if (ServiceLocator.findService(ChannelManagement.class).isForceSsl(ChannelType.API)) {
            sslSuffix = ResourceBundleManager.instance().getText(
                    "service.apps.mobile.setting.option.https", locale);
        } else {
            sslSuffix = StringUtils.EMPTY;
        }
        return ResourceBundleManager.instance().getText("service.apps.mobile.setting", locale, url,
                sslSuffix);
    }

    @Override
    public String toString(Locale locale, Object... arguments) {
        HttpServletRequest request = (HttpServletRequest) arguments[0];

        String description = ResourceBundleManager.instance().getText(
                "service.apps.mobile.description", locale);
        return description + getSettingsMessage(locale, request);
    }

}
