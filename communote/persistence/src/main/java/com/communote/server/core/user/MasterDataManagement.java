package com.communote.server.core.user;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.communote.server.model.user.Country;
import com.communote.server.model.user.Language;

/**
 * <p>
 * Management class for master data.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface MasterDataManagement {

    /** The property prefix for the language values. */
    public static final String MASTERDATA_LANGUAGE_PREFIX = "masterdata.languages.";

    /**
     * Adds a new language or updates an existing language for the given code.
     *
     * @param languageCode
     *            The language code as LANGUAGE_COUNTY_VARIANT
     * @param name
     *            The name of the language (Should be the foreign name).
     */
    public void addLanguage(String languageCode, String name);

    /**
     * <p>
     * Finds a country by the country code.
     * </p>
     */
    public Country findCountryByCode(String countryCode);

    /**
     * <p>
     * Finds a language by the language code.
     * </p>
     */
    public Language findLanguageByCode(
            String languageCode);

    /**
     * Returns all languages for the client.
     *
     * @return Map of all language codes with their names.
     */
    @Transactional(readOnly = true)
    public Map<String, String> getAllLanguages();

    /**
     * This method returns all available languages. In contrast to {@link #getUsedLanguages()} it
     * will not include an inactive language of the current user.
     *
     * Will maintain the languages itself and not use the database.
     *
     * @return All languages which currently have at least one active localization.
     *
     * @see #getUsedLanguages()
     */
    @Transactional(readOnly = true)
    public Collection<Language> getAvailableLanguages();

    /**
     * <p>
     * Returns a list of all countries in the database.
     * </p>
     */
    public List<com.communote.server.model.user.Country> getCountries(
            java.util.Locale locale);

    /**
     * @return All known languages. Includes also languages, there is currently no localization
     *         active.
     */
    public List<Language> getLanguages();

    /**
     * <p>
     *
     * @return The available list of time zones.
     *         </p>
     */
    public List<com.communote.server.core.common.time.SimplifiedTimeZone> getTimeZones();

    /**
     * This method returns all available languages plus the language of the current user. For only
     * retrieving the available languages use {@link #getAvailableLanguages()}
     *
     * Will use the database only for getting the language of the current user.
     *
     * @return All languages which currently have at least one active localization. Includes the
     *         current users language, even if there is no localization for it.
     *
     * @see #getAvailableLanguages()
     */
    @Transactional(readOnly = true)
    public Collection<Language> getUsedLanguages();

    /**
     * Test whether the language of the locale belongs to the available languages.
     *
     * @param locale
     *            the locale to test
     * @return true if the language of the locale is among the available languages
     */
    @Transactional(readOnly = true)
    boolean isAvailableLanguage(Locale locale);

    /**
     *
     */
    public void postInitialization();

    /**
     * Removes the language for the given code.
     *
     * @param languageCode
     *            The code of the language to remove.
     */
    public void removeLanguage(String languageCode);

}
