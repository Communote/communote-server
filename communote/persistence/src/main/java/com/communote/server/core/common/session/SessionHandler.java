package com.communote.server.core.common.session;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * The session handler provides access to common used session attributes
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO the locale stuff should be handled more generically. Something like Springs
// LocaleContextHolder could be possible. There would be special CommunoteLocaleContext which has
// the methods getLocale, currentUserLocaleChanged, userAuthenticated and userLoggedOut (and maybe
// an override). An request aware implementation of this context could evaluate a session attribute
// like the SessionHandler. Another implementation could just check the current user. The different
// channels (web, mq, ...) would be responsible for exposing a suitable LocaleContext. And client
// code would just use LocaleContextHolder.getLocale().
public final class SessionHandler {

    private final static Logger LOG = Logger.getLogger(SessionHandler.class);

    private static final String FIRST_REQUEST_WAS_SECURE = "firstRequestedWasSecure";

    private static SessionHandler INSTANCE = null;

    /** Attribute for the current user locale. */
    public static final String CURRENT_USER_LOCALE = "currentUserLocale";

    /**
     * The one and only instance
     *
     * @return the instance
     */
    public static SessionHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new SessionHandler();
        }
        return INSTANCE;
    }

    private final IdentityConverter<User> converter;

    private UserManagement userManagement;

    /**
     * Do not construct me, i m just helping
     */
    private SessionHandler() {
        converter = new IdentityConverter<>();
    }

    /**
     * Method to be called to notify the session handler that the user changed his profile language.
     *
     * @param request
     *            the current request
     */
    public void currentUserLocaleChanged(HttpServletRequest request) {
        // use changed the language and want's to use it, thus remove any override
        resetOverriddenCurrentUserLocale(request);
    }

    /**
     * Get the authenticated user if any.
     *
     * @return the user or null
     */
    private User getCurrentKenmeiUser() {
        Long id = SecurityHelper.getCurrentUserId();
        User user = null;
        if (id != null) {
            user = getUserManagement().getUserById(id, converter);
            if (user == null) {
                LOG.error("User was not found, id: " + id, new Exception());
            }
        } else {
            // might happen if user is not authenticated or requests are not sent through the auth
            // filter chain
            if (LOG.isTraceEnabled()) {
                LOG.trace("Currently no user in session");
            }
        }
        return user;
    }

    /**
     * Get the current locale. If the locale has been overridden in the current session this locale
     * will be returned. Otherwise the locale of the authenticated user is returned. In case there
     * is no authenticated user the locale of the current request is returned. If this locale isn't
     * set either, the configured locale of the current client is returned. In case the application
     * is not yet initialized this method falls back to English.
     *
     * @param request
     *            the current request to use
     * @return a locale, never null
     */
    public Locale getCurrentLocale(HttpServletRequest request) {
        Locale locale = getCurrentUserLocale(request);
        if (locale == null && (locale = request.getLocale()) == null) {
            if (CommunoteRuntime.getInstance().isCoreInitialized()) {
                locale = ClientHelper.getDefaultLanguage();
            } else {
                locale = Locale.ENGLISH;
            }
        }
        return locale;
    }

    /**
     * Get the locale of the current user. This will respect temporary overrides for the current
     * session.
     *
     * @param request
     *            the request to use
     * @return The locale, or null if there is no user
     */
    private Locale getCurrentUserLocale(HttpServletRequest request) {
        Locale userLocale = null;
        if ((userLocale = getOverriddenUserLocale(request.getSession(false))) == null) {
            User user = getCurrentKenmeiUser();
            if (user != null) {
                return user.getLanguageLocale();
            }
        }
        return userLocale;
    }

    /**
     * Stores if the first request for the user was over http or https.
     *
     * @param request
     *            the current request
     * @return true if the first request was over a secure channel
     */
    public Boolean getFirstRequestedWasSecure(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Boolean) session.getAttribute(FIRST_REQUEST_WAS_SECURE);
    }

    /**
     * Get the overridden locale of the current user
     *
     * @param session
     *            the session to use
     * @return The locale, or null no current user
     */
    private Locale getOverriddenUserLocale(HttpSession session) {
        return session == null ? null : (Locale) session.getAttribute(CURRENT_USER_LOCALE);
    }

    /**
     * @return lazily initialized user management
     */
    private UserManagement getUserManagement() {
        if (this.userManagement == null) {
            this.userManagement = ServiceLocator.instance().getService(UserManagement.class);
        }
        return userManagement;
    }

    /**
     * Stores the locale in the session to override the locale of the current user. If the current
     * user has the same locale this call is ignored.
     *
     * @param request
     *            the current request
     * @param locale
     *            the locale to set, if null
     *            {@link #resetOverriddenCurrentUserLocale(HttpServletRequest)} will be called
     */
    public void overrideCurrentUserLocale(HttpServletRequest request, Locale locale) {
        if (locale == null) {
            resetOverriddenCurrentUserLocale(request);
        }
        User user = getCurrentKenmeiUser();
        if (user == null || !locale.equals(user.getLanguageLocale())) {
            HttpSession session = request.getSession(true);
            session.setAttribute(CURRENT_USER_LOCALE, locale);
        }
    }

    /**
     * Stores the locale in the session to override the locale of the current user. If the current
     * user has the same locale, the provided locale is null or locale has already been overridden
     * this call is ignored and false is returned.
     *
     * @param request
     *            the current request
     * @param locale
     *            the locale to set
     * @return whether the locale was set
     */
    public boolean overrideCurrentUserLocaleIfNotOverridden(HttpServletRequest request,
            Locale locale) {
        if (locale != null) {
            User user = getCurrentKenmeiUser();
            if (user == null || !locale.equals(user.getLanguageLocale())) {
                HttpSession session = request.getSession(true);
                if (session.getAttribute(CURRENT_USER_LOCALE) == null) {
                    session.setAttribute(CURRENT_USER_LOCALE, locale);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove the locale from the session if it was set with a call to
     * {@link #overrideCurrentUserLocale(HttpServletRequest, Locale)}
     *
     * @param request
     *            The current request
     */
    public void resetOverriddenCurrentUserLocale(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(CURRENT_USER_LOCALE);
        }
    }

    /**
     * Stores if the first request for the user was over http or https.
     *
     * @param request
     *            the current request
     * @param firstRequestedWasSecure
     *            Whether the request was secure or not. If null, a previously stored secure flag is
     *            removed.
     */
    public void setFirstRequestedWasSecure(HttpServletRequest request,
            Boolean firstRequestedWasSecure) {
        HttpSession session = request.getSession(firstRequestedWasSecure != null);
        if (session != null) {
            if (firstRequestedWasSecure == null) {
                session.removeAttribute(FIRST_REQUEST_WAS_SECURE);
            } else {
                session.setAttribute(FIRST_REQUEST_WAS_SECURE, firstRequestedWasSecure);
            }
        }
    }

}
