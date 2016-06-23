package com.communote.server.core.user;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.properties.PropertiesUtils;
import com.communote.server.core.common.time.SimplifiedTimeZone;
import com.communote.server.core.common.time.TimeZoneProvider;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.user.Country;
import com.communote.server.model.user.CountryConstants;
import com.communote.server.model.user.User;
import com.communote.server.model.user.Language;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.CountryDao;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.LanguageDao;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Implementation of com.communote.server.core.user.MasterDataManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("masterDataManagement")
public class MasterDataManagementImpl extends MasterDataManagementBase {

    private final static Pattern LANGUAGE_CODE_PATTERN = Pattern
            .compile("(?:[a-zA-Z]+__[a-zA-Z]+)|(?:[a-zA-Z]+(?:(?:_[a-zA-Z]+)?_[a-zA-Z]+)?)");
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MasterDataManagementImpl.class);
    /**
     * The path of the country property file
     */
    private static final String COUNTRIES_PROPERTIES = "com/communote/server/core/i18n/countries/"
            + "countries{0}.properties";

    /**
     * The path of the language property file
     */
    private static final String LANGUAGES_PROPERTIES = "com/communote/server/core/i18n/languages/languages.properties";

    /**
     * The property prefix for the country values
     */
    private static final String MASTERDATA_COUNTRY_PREFIX = "masterdata.countries.";

    @Autowired
    private CountryDao countryDao;

    @Autowired
    private LanguageDao languageDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TimeZoneProvider timeZoneProvider;

    private final Map<Locale, List<Country>> countries = new HashMap<Locale, List<Country>>();

    /** Mapping of client id to languages. */
    private final Map<String, Collection<Language>> availableLanguages = new HashMap<String, Collection<Language>>();

    /**
     * Adds the given language to the collection of available languages for the current client.
     *
     * @param language
     *            The language to add.
     */
    private void addAvailableLanguage(Language language) {
        // TODO not thread safe!
        Collection<Language> languages = availableLanguages.get(ClientHelper.getCurrentClientId());
        if (languages == null) {
            languages = initAvailableLanguages();
        }
        languages.add(language);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO this method claims to support languages in the format code_country_variant but it
    // doesn't. The Language should be converted to a Locale so that isAvailableLanguage(Locale) can
    // be implemented correctly.
    public synchronized void addLanguage(String languageCode, String name) {
        if (!LANGUAGE_CODE_PATTERN.matcher(languageCode).matches()) {
            throw new IllegalArgumentException("The given language code is not valid: "
                    + languageCode + " ");
        }
        Language language = languageDao.findByLanguageCode(languageCode);
        if (language == null) {
            language = Language.Factory.newInstance(languageCode, name, name);
            languageDao.create(language);
        } else {
            language.setName(name);
            languageDao.update(language);
        }
        addAvailableLanguage(language);
        LOGGER.info("Language added or updated: {} {}", languageCode, name);
    }

    /**
     * Iterate through the properties and only use the ones having the given prefix in the key. The
     * keys in the resulting map are the same as the given ones WITHOUT the prefix. The values are
     * staying untouched. Null keys or vales will be ignored and not used in the result map.
     *
     * @param properties
     *            The properties to filter
     * @param prefix
     *            The prefix to filter, a "." will be append if necessary
     * @return The filtered map
     */
    private Map<String, String> filterProperties(Properties properties, String prefix) {
        Map<String, String> propertyNames = new Hashtable<String, String>();

        if (!prefix.endsWith(".")) {
            prefix += ".";
        }

        for (Entry<Object, Object> propertyEntry : properties.entrySet()) {
            if (propertyEntry.getKey() != null && propertyEntry.getValue() != null) {
                String key = propertyEntry.getKey().toString();

                if (key.startsWith(prefix)) {
                    propertyNames.put(key.substring(prefix.length()), propertyEntry.getValue()
                            .toString());
                }
            }
        }
        return propertyNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAllLanguages() {
        Map<String, String> languages = new HashMap<String, String>();
        for (Language language : languageDao.loadAll()) {
            languages.put(language.getLanguageCode(), language.getName());
        }
        return languages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Language> getAvailableLanguages() {
        Collection<Language> languages = availableLanguages.get(ClientHelper.getCurrentClientId());
        if (languages == null) {
            languages = initAvailableLanguages();
        }
        return new HashSet<Language>(languages);
    }

    /**
     * The comparator to use for sorting the resulting items. If null they will not be sorted.
     *
     * @param locale
     *            the locale
     * @return The comparator
     */
    @SuppressWarnings("rawtypes")
    private Comparator getComparator(Locale locale) {
        Collator primaryCollator = Collator.getInstance(locale);
        primaryCollator.setStrength(Collator.SECONDARY);
        Collator secondaryCollator = Collator.getInstance(locale);
        secondaryCollator.setStrength(Collator.TERTIARY);
        ComparatorChain chain = new ComparatorChain();
        chain.addComparator(new BeanComparator(CountryConstants.NAME, primaryCollator));
        chain.addComparator(new BeanComparator(CountryConstants.NAME, secondaryCollator));
        return chain;
    }

    /**
     * Loades the property file for country names.
     *
     * @param locale
     *            the locale for the country list
     * @return the localized country properties
     */
    private Map<String, String> getCountryProperties(Locale locale) {
        String propertiesFile = COUNTRIES_PROPERTIES.replace("{0}",
                Locale.ENGLISH.equals(locale) ? "" : "_" + locale.getLanguage());

        try {
            Properties countryProperties = PropertiesUtils.load(propertiesFile);

            if (countryProperties == null && !Locale.ENGLISH.equals(locale)) {
                propertiesFile = COUNTRIES_PROPERTIES.replace("{0}", "");
                countryProperties = PropertiesUtils.load(propertiesFile);
            }

            if (countryProperties == null) {
                throw new MasterDataManagementException(
                        "Could not load country properties for locale=" + locale
                                + " propertiesFile=" + propertiesFile);
            }
            Map<String, String> countryPropertyNames = filterProperties(countryProperties,
                    MASTERDATA_COUNTRY_PREFIX);
            return countryPropertyNames;
        } catch (IOException e) {
            throw new MasterDataManagementException("Could not load country properties for locale="
                    + locale + " propertiesFile=" + propertiesFile, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Language> getUsedLanguages() {
        Collection<Language> usedLanguages = new HashSet<Language>();
        Set<String> usedLanguageCodes = ResourceBundleManager.instance().getUsedLanguageCodes();
        for (Language language : getAvailableLanguages()) {
            if (usedLanguageCodes.contains(language.getLanguageCode())) {
                usedLanguages.add(language);
            }
        }
        Long currentUserId = SecurityHelper.getCurrentUserId();
        User currentUser = null;
        if (currentUserId != null) {
            currentUser = userDao.load(currentUserId);
        }
        if (currentUser != null && currentUser.getLanguageCode() != null) {
            Language language = languageDao.findByLanguageCode(currentUser.getLanguageCode());
            if (language != null) {
                usedLanguages.add(language);
            }
        }
        return usedLanguages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Country handleFindCountryByCode(String countryCode) {
        return countryDao.findCountryByCode(countryCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Language handleFindLanguageByCode(String languageCode) {
        return languageDao.findByLanguageCode(languageCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Country> handleGetCountries(Locale locale) {
        List<Country> localeCountries = countries.get(locale);
        if (localeCountries == null) {
            localeCountries = loadCountries(locale);
        }
        return localeCountries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Language> handleGetLanguages() {
        return (List<Language>) languageDao.loadAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<SimplifiedTimeZone> handleGetTimeZones() {
        return timeZoneProvider.getTimeZones();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handlePostInitialization() {
        LOGGER.info("Init master data");
        updateLanguages();
        updateCountries();
    }

    /**
     *
     * synchronized to avoid accessing the available languages concurrently
     *
     * @return A collection of the available languages.
     */
    private synchronized Collection<Language> initAvailableLanguages() {
        Collection<Language> languages = availableLanguages.get(ClientHelper.getCurrentClientId());
        if (languages == null) {
            languages = new HashSet<Language>();
            availableLanguages.put(ClientHelper.getCurrentClientId(), languages);
            // get all languages and add it
            for (String languageCodes : ResourceBundleManager.instance().getUsedLanguageCodes()) {
                Language language = languageDao.findByLanguageCode(languageCodes);
                if (language != null) {
                    languages.add(language);
                }
            }
        }
        return languages;
    }

    @Override
    // TODO this is not correct! Language.getLanguageCode() can be something like
    // code_country_variant thus, the comparison against locale.getLanguage is wrong. Besides this,
    // the documentation of Locale states that the Locale or its components should not be compared
    // against a string but always against another (constructed) Locale.
    public boolean isAvailableLanguage(Locale locale) {
        Collection<Language> knownLanguages = getAvailableLanguages();
        for (Language language : knownLanguages) {
            if (locale.getLanguage().equals(language.getLanguageCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads the localized country list.
     *
     * @param locale
     *            the locale for the country list
     * @return the localized country list
     */
    private synchronized List<Country> loadCountries(Locale locale) {
        List<Country> dbCountries = (List<Country>) countryDao.loadAll();
        List<Country> localeCountries = new ArrayList<Country>(dbCountries.size());
        Map<String, String> codeToName = getCountryProperties(locale);

        for (Country dbCountry : dbCountries) {
            Country c = Country.Factory.newInstance();
            c.setCountryCode(dbCountry.getCountryCode());
            String name = codeToName.get(dbCountry.getCountryCode());
            if (name == null) {
                // TODO correct this warning output
                LOGGER.warn("Can not fname for countryCode = " + dbCountry.getCountryCode()
                        + " for the current locale = " + locale);
                name = dbCountry.getName();
            }
            c.setName(name);
            localeCountries.add(c);
        }
        Collections.sort(localeCountries, getComparator(locale));
        countries.put(locale, localeCountries);
        return localeCountries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeLanguage(String languageCode) {
        if (Locale.ENGLISH.toString().equals(languageCode)
                || Locale.GERMAN.toString().equals(languageCode)) {
            throw new IllegalArgumentException("The given language is not allowed to be removed: "
                    + languageCode);
        }
        Language language = languageDao.findByLanguageCode(languageCode);
        if (language != null) {
            Collection<Language> languages = availableLanguages.get(ClientHelper
                    .getCurrentClientId());
            languageDao.remove(language.getId());
            languages.remove(language);
        }
    }

    /**
     * Update the countries in the database.
     */
    private void updateCountries() {
        LOGGER.info("Update countries");
        Collection<Country> collection = countryDao.loadAll();
        Map<String, Country> databaseCountries = new Hashtable<String, Country>(collection.size());
        for (Country country : collection) {
            databaseCountries.put(country.getCountryCode(), country);
        }

        Map<String, String> countryPropertyNames = getCountryProperties(Locale.ENGLISH);

        for (Entry<String, String> entry : countryPropertyNames.entrySet()) {
            if (!databaseCountries.containsKey(entry.getKey())) {
                Country temp = Country.Factory.newInstance();
                // TODO don't store the name in the database because it still needs to be localized
                temp.setName(entry.getValue());
                temp.setCountryCode(entry.getKey());
                countryDao.create(temp);
            } else {
                Country temp = databaseCountries.get(entry.getKey());
                if (!temp.getName().equals(entry.getValue())) {
                    temp.setName(entry.getValue());
                    countryDao.update(temp);
                }
            }
        }
    }

    /**
     * Updates the languages in the database.
     */
    private void updateLanguages() {
        LOGGER.info("Update languages");
        Collection<Language> collection = languageDao.loadAll();
        Map<String, Language> languageCodesToLanguages = new HashMap<String, Language>();
        for (Language language : collection) {
            languageCodesToLanguages.put(language.getLanguageCode(), language);
        }
        Properties languageProperties;
        try {
            languageProperties = PropertiesUtils.load(LANGUAGES_PROPERTIES);
        } catch (IOException e) {
            throw new MasterDataManagementException("Loading language.properties failed", e);
        }
        if (languageProperties == null) {
            throw new MasterDataManagementException("Loading language.properties failed");
        }
        Map<String, String> languagePropertyNames = filterProperties(languageProperties,
                MASTERDATA_LANGUAGE_PREFIX);

        for (Entry<String, String> entry : languagePropertyNames.entrySet()) {
            if (!languageCodesToLanguages.containsKey(entry.getKey())) {
                Language temp = Language.Factory.newInstance();
                // TODO don't store the name in the database because it still needs to be localized
                temp.setName(entry.getValue());
                temp.setLanguageCode(entry.getKey());
                languageCodesToLanguages.put(temp.getLanguageCode(), temp);
                languageDao.create(temp);
            } else {
                Language temp = languageCodesToLanguages.get(entry.getKey());
                if (!temp.getName().equals(entry.getValue())) {
                    temp.setName(entry.getValue());
                    languageDao.update(temp);
                }
            }
        }
    }

}
