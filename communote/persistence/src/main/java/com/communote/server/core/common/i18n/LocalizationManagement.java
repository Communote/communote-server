package com.communote.server.core.common.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.common.string.StringEscapeHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.MasterDataManagement;
import com.communote.server.model.i18n.Message;
import com.communote.server.model.user.Language;
import com.communote.server.persistence.common.messages.MessageDao;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * This class is responsible for managing custom messages within the database.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class LocalizationManagement {

    private final Map<String, LocalizedMessage> customMessageFallbacks = new HashMap<>();

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private MasterDataManagement masterDataManagement;

    /**
     * Return for a given message key the localized message for each of the currently available
     * languages
     *
     * @param messageKey
     *            The message key to load.
     * @param resourceBundlefallback
     *            whether to return a fallback with the same key from the resource bundle if there
     *            is no message for the given key in the database
     * @return Mapping from language to localized message. If no fallback should be returned or no
     *         fallback exists the message will be null.
     */
    @Transactional(readOnly = true)
    public Map<Language, Message> getAvailableLanguages(String messageKey,
            boolean resourceBundleFallback) {
        Map<Language, Message> languages = new HashMap<Language, Message>();
        for (Language language : masterDataManagement.getUsedLanguages()) {
            Message currentMessage = getMessage(language, messageKey, resourceBundleFallback);
            languages.put(language, currentMessage);
        }
        return languages;
    }

    /**
     * Method to get a custom localized message from the database.
     *
     * @param messageKey
     *            The message key.
     * @param locale
     *            The locale.
     * @return The message from the database or a fallback value which was set via
     *         {@link #setCustomMessageFallback(String, LocalizedMessage)} or from the local
     *         resources.
     */
    @Transactional(readOnly = true)
    public String getCustomMessage(String messageKey, Locale locale) {
        Message message = messageDao.find(messageKey, locale.getLanguage(),
                Locale.ENGLISH.getLanguage());
        if (message != null) {
            if (message.isIsHtml()) {
                return message.getMessage();
            }
            return StringEscapeHelper.escapeXml(message.getMessage());
        }
        LocalizedMessage fallback = customMessageFallbacks.get(messageKey);
        if (fallback != null) {
            return fallback.toString(locale);
        }
        return ResourceBundleManager.instance().getText(messageKey, locale);
    }

    /**
     * Get a fallback that was set with {@link #setCustomMessageFallback(String, LocalizedMessage)}
     *
     * @param messageKey
     *            the key of the message
     * @return the fallback or null if there is no fallback
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public LocalizedMessage getCustomMessageFallback(String messageKey) {
        return customMessageFallbacks.get(messageKey);
    }

    /**
     * Returns the message for the specified language from database or if there is none optionally
     * return it from the resource bundle.
     *
     * @param language
     *            the language
     * @param messageKey
     *            The message key to load.
     * @param resourceBundlefallback
     *            whether to return a fallback with the same key from the resource bundle if there
     *            is no message for the given key in the database
     * @return the message or null if not contained in the database or resource bundle
     */
    private Message getMessage(Language language, String messageKey, boolean resourceBundlefallback) {
        Message actualMessage = messageDao.find(messageKey, language.getLanguageCode());
        if (actualMessage != null || !resourceBundlefallback) {
            return actualMessage;
        }
        // get from resource bundle if contained
        Locale locale = new Locale(language.getLanguageCode());
        if (ResourceBundleManager.instance().knowsMessageKey(messageKey, null, locale)) {
            actualMessage = Message.Factory.newInstance();
            actualMessage.setIsHtml(true);
            actualMessage.setLanguage(language);
            actualMessage.setMessageKey(messageKey);
            actualMessage.setMessage(ResourceBundleManager.i18NCustomText(messageKey, locale));
        }
        return actualMessage;
    }

    /**
     * Method to get a custom localized message from the database.
     *
     * @param messageKey
     *            The message key.
     * @param locale
     *            The locale.
     * @return The message from the database or null.
     */
    @Transactional(readOnly = true)
    public Message getMessage(String messageKey, Locale locale) {
        return messageDao.find(messageKey, locale.getLanguage(), Locale.ENGLISH.getLanguage());
    }

    /**
     * Remove a fallback which was set with
     * {@link #setCustomMessageFallback(String, LocalizedMessage)}
     * 
     * @param messageKey
     *            the key of the fallback to remove
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void removeCustomMessageFallback(String messageKey) {
        customMessageFallbacks.remove(messageKey);
    }

    /**
     * Set a falback for a custom message. If there is already a fallback for that key it will be
     * overridden.
     *
     * @param messageKey
     *            the key of the fallback
     * @param fallback
     *            the fallback
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void setCustomMessageFallback(String messageKey, LocalizedMessage fallback) {
        if (!ResourceBundleManager.isCustomMessageKey(messageKey)) {
            throw new IllegalArgumentException("Message key " + messageKey
                    + " does not belong to a custom message.");
        }
        customMessageFallbacks.put(messageKey, fallback);
    }

    /**
     * Method to update or create message.
     *
     * @param messageKey
     *            Key of the message.
     * @param messageValue
     *            Value of the message.
     * @param languageCode
     *            Language code.
     * @param isHtml
     *            True, if this is html.
     */
    public void setMessage(String messageKey, String messageValue, String languageCode,
            boolean isHtml) {
        if (!ResourceBundleManager.isCustomMessageKey(messageKey)) {
            throw new IllegalArgumentException("Message key " + messageKey
                    + " does not belong to a custom message.");
        }
        SecurityHelper.assertCurrentUserIsClientManager();
        Message message = messageDao.find(messageKey, languageCode);
        if (message == null) {
            Language language = masterDataManagement.findLanguageByCode(languageCode);
            message = Message.Factory.newInstance(messageKey, messageValue, isHtml, language);
            messageDao.create(message);
        } else {
            message.setMessage(messageValue);
            message.setIsHtml(isHtml);
            messageDao.update(message);
        }
    }
}
