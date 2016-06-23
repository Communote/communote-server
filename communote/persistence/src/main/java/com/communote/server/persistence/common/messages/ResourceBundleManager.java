package com.communote.server.persistence.common.messages;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.StaticMessageSource;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventDispatcher;

/**
 * The resource bundle manager controls the message source and takes care of providing it.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResourceBundleManager {

    private static ResourceBundleManager INSTANCE = new ResourceBundleManager();

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleManager.class);

    /** Logger. */
    private static final Logger LOCALIZATION_LOGGER = LoggerFactory
            .getLogger("missingLocalization." + ResourceBundleManager.class.getName());

    /* represents the prefix of a custom message key */
    private static final String DEFAULT_PREFIX_OF_CUSTOM_MSG_PROPERTY = "custom.message";

    private static final Set<String> MISSING_KEYS_NOT_TO_LOG = new HashSet<String>();
    static {
        MISSING_KEYS_NOT_TO_LOG.add("error.valueEmpty.alias");
        MISSING_KEYS_NOT_TO_LOG.add("error.valueEmpty.command.alias");
        MISSING_KEYS_NOT_TO_LOG.add("error.valueEmpty.command.newClientId");
        MISSING_KEYS_NOT_TO_LOG.add("error.valueEmpty.String");
        MISSING_KEYS_NOT_TO_LOG.add("error.valueEmpty.newClientId");
        MISSING_KEYS_NOT_TO_LOG
                .add("string.validation.no.regex.matches.command.contactPhoneNumber");
        MISSING_KEYS_NOT_TO_LOG.add("string.validation.no.regex.matches.contactPhoneNumber");
    }

    /**
     * Convenience method for getting a i18n key's value. Use this method to check if a database
     * value represents a message key.
     *
     * @param msgKey
     *            The possible message key
     * @param locale
     *            the current locale
     * @return The localized text
     */
    public static String i18NCustomText(String msgKey, Locale locale) {
        if (isCustomMessageKey(msgKey)) {
            msgKey = ResourceBundleManager.instance().getText(msgKey, locale, (Object[]) null);
        }
        return msgKey;
    }

    /**
     * @return the instance of the manager
     */
    public static ResourceBundleManager instance() {
        return INSTANCE;
    }

    /**
     * Test whether the key belongs to a message that can be overridden in LocalizationManagement.
     *
     * @param msgKey
     *            the key to test
     * @return true if the key belongs to a message that can be overridden in LocalizationManagement
     */
    public static boolean isCustomMessageKey(String msgKey) {
        return msgKey != null && msgKey.startsWith(DEFAULT_PREFIX_OF_CUSTOM_MSG_PROPERTY);
    }

    private final Map<String, StaticMessageSource> bundlesToLocalizations = new LinkedHashMap<String, StaticMessageSource>();
    private final Map<String, Set<String>> languageCodesToKeys = new ConcurrentHashMap<String, Set<String>>();
    private List<StaticMessageSource> localizations = new ArrayList<StaticMessageSource>();
    private final List<String> basenames = new ArrayList<String>();
    private final ReloadableResourceBundleMessageSource messageSource;

    /**
     * default constructor
     */
    private ResourceBundleManager() {
        basenames.add("classpath:com/communote/server/core/i18n/messages");
        basenames.add("classpath:com/communote/server/core/i18n/administration-messages");
        basenames.add("classpath:com/communote/server/core/i18n/installer-messages");
        messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasenames(basenames.toArray(new String[basenames.size()]));
        messageSource.setFallbackToSystemLocale(false);
    }

    /**
     * Add a basename pointing to resource bundles with translations. Resource bundles are searched
     * sequentially for a matching translation. Bundles that were added earlier can therefore
     * override translations provided by bundles that were added later. Resource bundles provided
     * with this method will be searched for translations after those provided by
     * {@link #addLocalizations(String, Locale, Map)}
     *
     * @param basename
     *            The basename of the resource bundle without file extension or language code. If
     *            the resource bundles should be searched on the classpath the basename has to start
     *            with <code>classpath:</code>.
     */
    public synchronized void addBasename(String basename) {
        basenames.add(basename);
        messageSource.setBasenames(basenames.toArray(new String[basenames.size()]));
        messageSource.clearCache();
    }

    /**
     * Like {@link #addBasename(String)} but adds the resource bundles referenced by the basename to
     * the top so that they will be searched before those provided by {@link #addBasename(String)}.
     * Translations provided by {@link #addLocalizations(String, Locale, Map)} are still having a
     * higher precedence.
     *
     * @param basename
     *            The basename of the resource bundle without file extension or language code. If
     *            the resource bundles should be searched on the classpath the basename has to start
     *            with <code>classpath:</code>.
     */
    public synchronized void addBasenameToTop(String basename) {
        basenames.add(0, basename);
        messageSource.setBasenames(basenames.toArray(new String[basenames.size()]));
        messageSource.clearCache();
    }

    /**
     * Adds a map with additional localizations for a specified local to this manager.
     *
     * @param key
     *            Key of the localizations. If there is already a dictionary for this key, it will
     *            be overwritten.
     * @param locale
     *            The locale the localizations are for.
     * @param localizations
     *            The localizations. The key is the message key and and value the value.
     */
    public synchronized void addLocalizations(String key, Locale locale,
            Map<String, String> localizations) {
        if (localizations == null || localizations.isEmpty()) {
            LOGGER.debug("You can't add empty localizations for {}. The key was {}", locale, key);
            return;
        }
        String languageCode = locale.toString();
        StaticMessageSource innerLocalizations = bundlesToLocalizations.containsKey(key) ? bundlesToLocalizations
                .get(key) : new StaticMessageSource();
        for (Entry<String, String> localization : localizations.entrySet()) {
            innerLocalizations.addMessage(localization.getKey(), locale, localization.getValue());
        }
        Set<String> keys = languageCodesToKeys.get(languageCode);
        if (keys == null) {
            keys = new HashSet<String>();
            languageCodesToKeys.put(languageCode, keys);
        }
        keys.add(key);
        this.bundlesToLocalizations.put(key, innerLocalizations);
        ArrayList<StaticMessageSource> newLocalizations = new ArrayList<StaticMessageSource>(
                this.localizations);
        newLocalizations.remove(innerLocalizations);
        newLocalizations.add(0, innerLocalizations);
        this.localizations = newLocalizations;
        ServiceLocator.findService(EventDispatcher.class).fire(new ResourceBundleChangedEvent());
    }

    /**
     * Method to get a message and let it parse through Velocity.
     *
     * @param messageKey
     *            Message key to load.
     * @param locale
     *            The locale.
     * @param model
     *            The model for the Velocity context.
     * @return The final text.
     */
    public String evaluateText(String messageKey, Locale locale, Map<String, Object> model) {
        StringWriter messageWriter = new StringWriter();
        VelocityEngine velocityEngine = ServiceLocator.findService(VelocityEngine.class);
        try {
            velocityEngine.evaluate(new VelocityContext(model), messageWriter, messageKey,
                    getText(messageKey, locale));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return messageWriter.toString().trim();
    }

    /**
     * Convenience method for getting a i18n key's value. Use this method to check if a database
     * value represents a message key.
     *
     * @param msgKey
     *            The possible message key
     * @param locale
     *            the current locale
     * @return The localized text
     */
    public String getI18NCustomText(String msgKey, Locale locale) {
        if (StringUtils.indexOf(msgKey, DEFAULT_PREFIX_OF_CUSTOM_MSG_PROPERTY, 0) > -1) {
            msgKey = getText(msgKey, locale);
        }
        return msgKey;
    }

    /**
     * @param locale
     *            The locale to use.
     * @return All valid locales.
     */
    private List<Locale> getLocales(Locale locale) {
        List<Locale> locales = new ArrayList<Locale>();
        if (locale != null) {
            locales.add(locale);
            boolean hasVariant = StringUtils.isNotBlank(locale.getVariant());
            boolean hasCountry = StringUtils.isNotBlank(locale.getCountry());
            if (hasVariant && hasCountry) { // "de_DE_SN" -> Add "de_DE"
                locales.add(new Locale(locale.getLanguage(), locale.getCountry()));
            }
            if (hasVariant || hasCountry) { // "de__SN" || "de_DE" -> Add "de"
                locales.add(new Locale(locale.getLanguage()));
            }
        }
        locales.add(Locale.ENGLISH);
        return locales;
    }

    /**
     * Convenience method for getting a i18n key's value.
     *
     * @param msgKey
     *            The message key
     * @param locale
     *            the current locale.
     * @param arguments
     *            Argument for the message.
     * @return The localized text
     */
    public String getText(String msgKey, Locale locale, Object... arguments) {
        for (Locale innerLocale : getLocales(locale)) {
            for (StaticMessageSource localization : localizations) {
                try {
                    String message = localization.getMessage(msgKey, arguments, innerLocale);
                    if (message != null) {
                        return message;
                    }
                } catch (NoSuchMessageException e) {
                    // Ignore. Will be logged later.
                }
            }
        }
        String message = StringUtils.EMPTY;
        try {
            message = messageSource.getMessage(msgKey, arguments, locale);
        } catch (NoSuchMessageException e) {
            try { // Fallback to English.
                message = messageSource.getMessage(msgKey, arguments, Locale.ENGLISH);
            } catch (NoSuchMessageException e1) {
                if (LOCALIZATION_LOGGER.isWarnEnabled()
                        && !MISSING_KEYS_NOT_TO_LOG.contains(msgKey)) {
                    LOCALIZATION_LOGGER.warn("{}", msgKey);
                } else {
                    LOCALIZATION_LOGGER.debug("{}", msgKey);
                }
            }
        }
        return message;
    }

    /**
     * @return A set of language codes and localization exists.
     */
    public Set<String> getUsedLanguageCodes() {
        Set<String> result = new HashSet<String>(languageCodesToKeys.keySet());
        // We always support ENGLISH and GERMAN
        result.add(Locale.ENGLISH.getLanguage());
        result.add(Locale.GERMAN.getLanguage());
        return result;
    }

    /**
     * @param languageCode
     *            The language code to check.
     * @return <code>True</code>, if there are localizations for the given language code are
     *         available.
     */
    public boolean isUsedLanguage(String languageCode) {
        return languageCodesToKeys.get(languageCode) != null;
    }

    /**
     * @param msgKey
     *            The message key
     * @param args
     *            the arguments for the message
     * @param locale
     *            the current locale
     * @return true if the message exists
     */
    public boolean knowsMessageKey(String msgKey, Object[] args, Locale locale) {
        try {
            messageSource.getMessage(msgKey, args, locale);
        } catch (NoSuchMessageException e) {
            return false;
        }
        return true;
    }

    /**
     * Removes the given basename.
     *
     * @param basename
     *            The basename.
     */
    public synchronized void removeBasename(String basename) {
        basenames.remove(basename);
        messageSource.setBasenames(basenames.toArray(new String[basenames.size()]));
    }

    /**
     * Removes the additional localization for the given key.
     *
     * @param key
     *            Key of the localizations.
     */
    public synchronized void removeLocalizations(String key) {
        StaticMessageSource removedDictionary = bundlesToLocalizations.remove(key);
        if (removedDictionary != null) {
            ArrayList<StaticMessageSource> newLocalizations = new ArrayList<StaticMessageSource>(
                    localizations);
            newLocalizations.remove(removedDictionary);
            this.localizations = newLocalizations;
        }
        Iterator<Entry<String, Set<String>>> iterator = languageCodesToKeys.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Set<String>> languageCodeToKeys = iterator.next();
            Set<String> keys = languageCodeToKeys.getValue();
            keys.remove(key);
            if (keys.isEmpty()) {
                iterator.remove();
            }
        }
        ServiceLocator.findService(EventDispatcher.class).fire(new ResourceBundleChangedEvent());
    }
}
