package com.communote.server.core.user;

import java.util.List;
import java.util.Locale;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.core.common.time.SimplifiedTimeZone;
import com.communote.server.model.user.Country;
import com.communote.server.model.user.Language;

/**
 * <p>
 * Spring Service base class for <code>MasterDataManagement</code> , provides access to all services
 * and entities referenced by this service.
 * </p>
 *
 * @see MasterDataManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class MasterDataManagementBase implements MasterDataManagement {

    /**
     * @see MasterDataManagement#findCountryByCode(String)
     */
    @Override
    @Transactional(readOnly = true)
    public Country findCountryByCode(String countryCode) {
        if (countryCode == null || countryCode.trim().length() == 0) {
            throw new IllegalArgumentException("MasterDataManagement.findCountryByCode"
                    + "(String countryCode) - 'countryCode' can not be null or empty");
        }
        try {
            return this.handleFindCountryByCode(countryCode);
        } catch (RuntimeException rt) {
            throw new MasterDataManagementException(
                    "Error performing 'MasterDataManagement.findCountryByCode(String countryCode)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see MasterDataManagement#findLanguageByCode(String)
     */
    @Override
    @Transactional(readOnly = true)
    public Language findLanguageByCode(
            String languageCode) {
        if (languageCode == null || languageCode.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "MasterDataManagement.findLanguageByCode(String languageCode) - 'languageCode' can not be null or empty");
        }
        try {
            return this.handleFindLanguageByCode(languageCode);
        } catch (RuntimeException rt) {
            throw new MasterDataManagementException(
                    "Error performing 'MasterDataManagement.findLanguageByCode(String languageCode)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see MasterDataManagement#getCountries(Locale)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Country> getCountries(
            Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException(
                    "MasterDataManagement.getCountries(Locale locale) - 'locale' can not be null");
        }
        try {
            return this.handleGetCountries(locale);
        } catch (RuntimeException rt) {
            throw new MasterDataManagementException(
                    "Error performing 'MasterDataManagement.getCountries(Locale locale)' --> " + rt,
                    rt);
        }
    }

    /**
     * @see MasterDataManagement#getLanguages()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Language> getLanguages() {
        try {
            return this.handleGetLanguages();
        } catch (RuntimeException rt) {
            throw new MasterDataManagementException(
                    "Error performing 'MasterDataManagement.getLanguages()' --> " + rt, rt);
        }
    }

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     *
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }

    /**
     * @see MasterDataManagement#getTimeZones()
     */
    @Override
    @Transactional(readOnly = true)
    public List<SimplifiedTimeZone> getTimeZones() {
        try {
            return this.handleGetTimeZones();
        } catch (RuntimeException rt) {
            throw new MasterDataManagementException(
                    "Error performing 'MasterDataManagement.getTimeZones()' --> " + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findCountryByCode(String)}
     */
    protected abstract Country handleFindCountryByCode(
            String countryCode);

    /**
     * Performs the core logic for {@link #findLanguageByCode(String)}
     */
    protected abstract Language handleFindLanguageByCode(
            String languageCode);

    /**
     * Performs the core logic for {@link #getCountries(Locale)}
     */
    protected abstract List<Country> handleGetCountries(
            Locale locale);

    /**
     * Performs the core logic for {@link #getLanguages()}
     */
    protected abstract List<Language> handleGetLanguages();

    /**
     * Performs the core logic for {@link #getTimeZones()}
     */
    protected abstract List<SimplifiedTimeZone> handleGetTimeZones();

    /**
     * Performs the core logic for {@link #postInitialization()}
     */
    protected abstract void handlePostInitialization();

    /**
     * @see MasterDataManagement#postInitialization()
     */
    @Override
    public void postInitialization() {
        try {
            this.handlePostInitialization();
        } catch (RuntimeException rt) {
            throw new MasterDataManagementException(
                    "Error performing 'MasterDataManagement.postInitialization()' --> " + rt, rt);
        }
    }

}