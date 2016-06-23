package com.communote.server.web.commons.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import com.communote.server.persistence.common.messages.ResourceBundleManager;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResourceBundleManagerMessageSource implements MessageSource {

    private ResourceBundleManager resourceBundleManager;

    /**
     * {@inheritDoc}
     */
    public String getMessage(MessageSourceResolvable resolvable, Locale locale)
            throws NoSuchMessageException {
        for (String code : resolvable.getCodes()) {
            String message = getMessage(code, resolvable.getArguments(), locale);
            if (message != null && message.length() > 0) {
                return message;
            }
        }
        return resolvable.getDefaultMessage();
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage(String messageKey, Object[] arguments, Locale locale)
            throws NoSuchMessageException {
        return getMessage(messageKey, arguments, null, locale);
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage(String messageKey, Object[] arguments, String defaultMessage,
            Locale locale) {
        String text = resourceBundleManager.getText(messageKey, locale, arguments);
        if (text == null) {
            return defaultMessage;
        }
        return text;
    }

    /**
     * @return the resourceBundleManager
     */
    public ResourceBundleManager getResourceBundleManager() {
        return resourceBundleManager;
    }

    /**
     * @param resourceBundleManager
     *            the resourceBundleManager to set
     */
    public void setResourceBundleManager(ResourceBundleManager resourceBundleManager) {
        this.resourceBundleManager = resourceBundleManager;
    }

}
