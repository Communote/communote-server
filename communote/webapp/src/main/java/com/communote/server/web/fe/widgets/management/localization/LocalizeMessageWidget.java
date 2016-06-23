package com.communote.server.web.fe.widgets.management.localization;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.i18n.LocalizationManagement;
import com.communote.server.model.i18n.Message;
import com.communote.server.model.user.Language;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.portal.user.client.forms.CustomizeMessageForm;
import com.communote.server.widgets.annotations.AnnotatedSingleResultWidget;
import com.communote.server.widgets.annotations.ViewIdentifier;
import com.communote.server.widgets.annotations.WidgetAction;

/**
 * This Widget can be used to localize a specific message.
 * <p>
 * <b>Note</b>: All message keys should start with "custom.message".
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@ViewIdentifier("widget.management.localization.message")
public class LocalizeMessageWidget extends AnnotatedSingleResultWidget {

    private static final String PARAMETER_MESSAGE_KEY = "messageKey";
    private static final String PARAMETER_RESOURCE_BUNDLE_FALLBACK = "resourceBundleFallback";

    private final LocalizationManagement localizationManagement = ServiceLocator.instance()
            .getService(LocalizationManagement.class);

    /**
     * Does nothing.
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object processSingleResult() {
        String messageKey = getParameter(PARAMETER_MESSAGE_KEY);
        boolean bundleFallback = getBooleanParameter(PARAMETER_RESOURCE_BUNDLE_FALLBACK, false);
        String languageCode = getParameter("languageCode", Locale.ENGLISH.getLanguage());
        CustomizeMessageForm customizeMessageForm = new CustomizeMessageForm();
        customizeMessageForm.setKey(messageKey);
        customizeMessageForm.setLanguageCode(languageCode);
        Message message = localizationManagement.getMessage(messageKey, new Locale(languageCode));
        if (message != null) {
            customizeMessageForm.setMessage(message.getMessage());
            customizeMessageForm.setIsHtml(message.isIsHtml());
        } else if (bundleFallback) {
            customizeMessageForm.setMessage(ResourceBundleManager.i18NCustomText(messageKey,
                    new Locale(languageCode)));
            customizeMessageForm.setIsHtml(true);
        } else {
            customizeMessageForm.setMessage("");
            customizeMessageForm.setIsHtml(false);
        }
        Map<Language, Message> languages = localizationManagement.getAvailableLanguages(messageKey,
                bundleFallback);
        for (Entry<Language, Message> language : languages.entrySet()) {
            Message localizedMessage = language.getValue();
            setResponseMetadata(language.getKey().getLanguageCode() + "_value",
                    localizedMessage == null ? "" : localizedMessage.getMessage());
            setResponseMetadata(language.getKey().getLanguageCode() + "_isHtml",
                    localizedMessage == null ? Boolean.FALSE : localizedMessage.isIsHtml());
        }
        getRequest().setAttribute("languages", languages.keySet());
        getRequest().setAttribute("showIsHtml", getBooleanParameter("showIsHtml", true));
        return customizeMessageForm;
    }

    /**
     * This message is called, when the user saves the message.
     */
    @WidgetAction("SAVE_MESSAGE")
    public void save() {
        String messageKey = getParameter(PARAMETER_MESSAGE_KEY);
        String messageString = getParameter("message", "");
        String languageCode = getParameter("languageCode", Locale.ENGLISH.getLanguage());
        boolean isHtml = getBooleanParameter("isHtml", false);
        localizationManagement.setMessage(messageKey, messageString, languageCode, isHtml);
        MessageHelper.saveMessageFromKey(getRequest(),
                "client.customization.localization.save.success");
    }
}
