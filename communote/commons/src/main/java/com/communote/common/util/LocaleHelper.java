package com.communote.common.util;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LocaleHelper {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleHelper.class);

    /**
     * @param languageCode
     *            The language code in the form LANGUAGE_COUNTRY_VARIANT
     * @return A locale for this language code.
     */
    public static Locale toLocale(String languageCode) {
        String[] languageFragments = StringUtils.splitPreserveAllTokens(languageCode, "_");
        switch (languageFragments.length) {
        case 1:
            return new Locale(languageFragments[0]);
        case 2:
            return new Locale(languageFragments[0], languageFragments[1]);
        case 3:
            return new Locale(languageFragments[0], languageFragments[1], languageFragments[2]);
        default:
            LOGGER.error("The given language is not valid: {} ", languageCode);
            throw new IllegalArgumentException("The given language is not valid: " + languageCode);
        }
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private LocaleHelper() {
        // Do nothing
    }
}
