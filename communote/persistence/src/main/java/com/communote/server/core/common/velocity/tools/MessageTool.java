package com.communote.server.core.common.velocity.tools;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.i18n.LocalizationManagement;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.model.i18n.Message;
import com.communote.server.persistence.common.messages.CustomMessageKeyConstant;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.client.ClientUrlHelper;

/**
 * Tool for messages.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MessageTool {

    /**
     * Get imprint with fallback behavior if no imprint is configured.
     *
     * @param request
     *            the current request
     * @return the localized imprint
     */
    public String getImprint(HttpServletRequest request) {
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        return getImprint(locale);
    }

    /**
     * Get imprint with fallback behavior if no imprint is configured.
     *
     * @param locale
     *            the locale to use for localization
     * @return the localized imprint
     */
    public String getImprint(Locale locale) {
        String fallbackMsgKey;
        Object[] fallbackMsgArgs;

        fallbackMsgKey = CustomMessageKeyConstant.IMPRINT;
        // TODO if request is available we could create a request aware url but logic resides in
        // webapp's ControllerHelper...
        fallbackMsgArgs = new Object[] { ClientUrlHelper.renderConfiguredAbsoluteUrl(
                "admin/client/customize/imprint", true) };

        return getMessage(CustomMessageKeyConstant.IMPRINT, locale, fallbackMsgKey, fallbackMsgArgs);
    }

    /**
     *
     * @param key
     *            The key of the message.
     * @param locale
     *            The locale.
     * @param width
     *            Used, when the imprint is not html to determine the with of the pre-Element.
     * @param fallbackMsgKey
     *            Key of the fallback message, if message not found.
     * @param fallbackMsgArgs
     *            Optional arguments for message key. Can be null.
     * @return The message, if found or the message for the given fallback.
     */
    private String getMessage(String key, Locale locale, String fallbackMsgKey,
            Object[] fallbackMsgArgs) {
        LocalizationManagement localizationManagement = ServiceLocator
                .findService(LocalizationManagement.class);
        Message message = localizationManagement.getMessage(key, locale);
        String messageAsString;
        if (message == null) {
            LocalizedMessage fallback = localizationManagement.getCustomMessageFallback(key);
            if (fallback == null) {
                messageAsString = ResourceBundleManager.instance().getText(fallbackMsgKey, locale,
                        fallbackMsgArgs);
            } else {
                messageAsString = fallback.toString(locale);
            }
        } else {
            messageAsString = message.getMessage();
        }
        if (message != null && !message.isIsHtml()) {
            messageAsString = "<pre>" + messageAsString + "</pre>";
        }
        return messageAsString;
    }

    /**
     * Get terms of use with fallback behavior if no terms of use are configured.
     *
     * @param request
     *            the current request
     * @return the localized terms of use
     */
    public String getTermsOfUse(HttpServletRequest request) {
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        return getTermsOfUse(locale);
    }

    /**
     * Get terms of use with fallback behavior if no terms of use are configured.
     *
     * @param locale
     *            the locale to use for localization
     * @return the localized terms of use
     */
    public String getTermsOfUse(Locale locale) {
        String fallbackMsgKey;
        Object[] fallbackMsgArgs;

        fallbackMsgKey = CustomMessageKeyConstant.TERMS_OF_USE;
        fallbackMsgArgs = new Object[] { ClientUrlHelper.renderConfiguredAbsoluteUrl(
                "admin/client/customize/termsofuse/show", true) };
        return getMessage(CustomMessageKeyConstant.TERMS_OF_USE, locale, fallbackMsgKey,
                fallbackMsgArgs);
    }
}
