package com.communote.server.web.commons.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.LocaleResolver;

import com.communote.server.core.common.session.SessionHandler;

/**
 * A LocaleResolver that uses the
 * {@link SessionHandler#getCurrentLocale(HttpServletRequest)} to get the locale of
 * the current user.
 * 
 * Does NOT set the locale in the request.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class SessionHandlerLocaleResolver implements LocaleResolver {

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.web.servlet.LocaleResolver#resolveLocale(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return SessionHandler.instance().getCurrentLocale(request);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.web.servlet.LocaleResolver#setLocale(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, java.util.Locale)
     */
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        /**
         * Do nothing since the locale should only be set in the LanguageFilter
         */
    }

}
