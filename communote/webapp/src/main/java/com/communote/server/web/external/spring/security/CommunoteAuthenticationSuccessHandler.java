package com.communote.server.web.external.spring.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import com.communote.common.util.UrlHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.persistence.user.client.ClientUrlHelper;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommunoteAuthenticationSuccessHandler.class);

    /** The prefix of the URL query string. */
    private static final String QUERY_STRING_PREFIX = "?";

    private final Set<String> urlsToRediretTo = Collections
            .newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private RequestCache requestCache = new HttpSessionRequestCache();

    /**
     * Add a URL which can be used as redirection target after successful authentication. The URL
     * should be relative and start after the client. It will be checked against the redirection
     * target which is taken from the request parameter returned by {@link #getTargetUrlParameter()}
     * or a cashed request. The found redirection target will only be used if it starts with one of
     * the URLs added via this or the {@link #setRedirectionTargets(List)} method.
     *
     * @param urlToInclude
     *            the URL to include
     * @param strict
     *            true to take the URL as is, if false the URL will be trimmed and a "/" will be
     *            prepended if missing
     * @return the added URL or null if the URL was null or absolute
     */
    public String addRedirectionTarget(final String urlToInclude, final boolean strict) {
        if (urlToInclude == null || UrlHelper.isAbsoluteUrl(urlToInclude)) {
            LOGGER.debug("Ignoring absolute or invalid URL '{}'", urlToInclude);
            return null;
        }
        String finalUrl = urlToInclude;
        if (!strict) {
            finalUrl = finalUrl.trim();
            if (!finalUrl.startsWith("/")) {
                finalUrl = "/" + finalUrl;
            }
        }
        boolean added;
        synchronized (this) {
            added = this.urlsToRediretTo.add(finalUrl);
        }
        if (LOGGER.isDebugEnabled()) {
            boolean modified = !urlToInclude.equals(finalUrl);
            if (added) {
                if (modified) {
                    LOGGER.debug(
                            "Added the URL '{}' (provided as '{}') to the list of URLs to include.",
                            finalUrl, urlToInclude);
                } else {
                    LOGGER.debug("Added the URL '{}' to the list of URLs to include.", finalUrl);
                }
            } else {
                if (modified) {
                    LOGGER.debug("URL ignored '{}' because the final URL '{}' already exists",
                            urlToInclude, finalUrl);
                } else {
                    LOGGER.debug("URL ignored '{}' because it already exists", finalUrl);
                }
            }
        }
        return finalUrl;
    }

    /**
     * <p>
     * Get the targetUrl from the request parameter returned by {@link #getTargetUrlParameter()}.
     * The referer or the default URL are not considered.
     * </p>
     * Note: this method does not encode the client ID into the URL
     *
     * @param request
     *            the current request
     * @param response
     *            the current response
     * @return the URL or null if no URL was set in the parameter
     */
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        String targetUrlParameter = getTargetUrlParameter();
        String targetUrl = null;
        if (targetUrlParameter != null) {
            targetUrl = request.getParameter(targetUrlParameter);
            if (StringUtils.isBlank(targetUrl)) {
                targetUrl = null;
            } else {
                LOGGER.debug("Found target URL in request parameter: {}", targetUrl);
            }
        }
        return targetUrl;
    }

    /**
     * Insert the session ID in the URL in the form ";jessionid=SESSION_ID". Rendering is only done
     * if the browser deactivates cookies. Method takes care of query strings.
     * <p>
     * <b>Modification</b> of HttpServletRequest#encodeURL
     * </p>
     *
     * @param request
     *            The request object.
     * @param url
     *            The url to add the parameter to
     * @return the url with the session id
     */
    private String encodeAbsoluteURL(HttpServletRequest request, String url) {
        if (!isEncodeable(request, url)) {
            return url;
        }
        String jSessionId = ";jsessionid=" + request.getSession().getId();
        if (url.indexOf(QUERY_STRING_PREFIX) > 0) {
            url = url.replace(QUERY_STRING_PREFIX, jSessionId + QUERY_STRING_PREFIX);
        } else {
            url += jSessionId;
        }
        return url;
    }

    @Override
    protected void handle(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        // reset overridden locale to enforce usage of users locale
        SessionHandler.instance().resetOverriddenCurrentUserLocale(request);
        String targetUrl = determineTargetUrl(request, response);
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (targetUrl == null) {
            if (savedRequest != null) {
                targetUrl = savedRequest.getRedirectUrl();
                LOGGER.debug("Found target URL in saved request: {}", targetUrl);
            }
        }
        if (savedRequest != null) {
            requestCache.removeRequest(request, response);
        }
        String moduleClientPart = ClientUrlHelper.prependModuleClientPart(StringUtils.EMPTY);
        if (!isTargetUrlValid(request, targetUrl, moduleClientPart)) {
            targetUrl = WebServiceLocator.instance().getStartpageRegistry().getStartpage();
            LOGGER.debug("Using startpage as target URL: {}", targetUrl);
        }

        if (response.isCommitted()) {
            LOGGER.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        if (!UrlHelper.isAbsoluteHttpUrl(targetUrl)) {
            // if module client part is already contained, the URL can be rendered as static URL
            boolean renderStatic = targetUrl.startsWith(moduleClientPart);
            targetUrl = ControllerHelper.renderAbsoluteUrl(request, null, targetUrl, false,
                    renderStatic, false);

            targetUrl = encodeAbsoluteURL(request, targetUrl);
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Return <code>true</code> if the specified URL should be encoded with a session identifier.
     * This will be true if all of the following conditions are met:
     * <ul>
     * <li>The request we are responding to asked for a valid session
     * <li>The requested session ID was not received via a cookie
     * </ul>
     * <p>
     * <b>Modification</b> of org.apache.coyote.tomcat4.CoyoteResponse#isEncodeable
     * </p>
     *
     * @param request
     *            The request object.
     * @param location
     *            Absolute URL to be validated.
     * @return <code>true</code> if the specified URL should be encoded with a session identifier
     */
    private boolean isEncodeable(HttpServletRequest request, String location) {
        if (location == null) {
            return false;
        }
        HttpSession session = request.getSession(false);
        return !(session == null || location.startsWith("#") || request
                .isRequestedSessionIdFromCookie());
    }

    private boolean isTargetUrlValid(HttpServletRequest request, String targetUrl,
            String moduleClientPart) {
        if (targetUrl == null) {
            return false;
        }
        String relativeUrl;
        if (UrlHelper.isAbsoluteUrl(targetUrl)) {
            // URL must have same host, port and context as current request or configured URL
            String url = targetUrl.toLowerCase(Locale.ENGLISH);
            boolean targetUrlSecure = url.startsWith("https:");
            if (!targetUrlSecure && !url.startsWith("http:")) {
                LOGGER.debug("Target URL is not a valid HTTP URL: {}", targetUrl);
                return false;
            }
            int length = url.length();
            url = stripDefaultPort(url, targetUrlSecure);
            int offset = length - url.length();
            // create absolute URL with context path
            String absoluteUrl = ControllerHelper.renderAbsoluteUrl(request, null,
                    StringUtils.EMPTY, targetUrlSecure, true, false);
            if (!url.startsWith(absoluteUrl)) {

                absoluteUrl = ClientUrlHelper.renderConfiguredAbsoluteUrl(null, StringUtils.EMPTY,
                        targetUrlSecure, true);
                if (!url.startsWith(absoluteUrl)) {
                    LOGGER.debug("Target URL is not a Communote URL: {}", targetUrl);
                    return false;
                }
            }
            // create relative url but preserve case
            relativeUrl = targetUrl.substring(absoluteUrl.length() + offset);
        } else {
            // relative target URLs are expected to start after the contextPath
            relativeUrl = targetUrl;
        }
        if (relativeUrl.startsWith(moduleClientPart)) {
            relativeUrl = relativeUrl.substring(moduleClientPart.length());
        }
        if (!StringUtils.isEmpty(relativeUrl)) {
            for (String urlToInclude : urlsToRediretTo) {
                if (relativeUrl.startsWith(urlToInclude)) {
                    return true;
                }
            }
        }
        LOGGER.debug("Target URL '{}' did not match the URLs to include", targetUrl);
        return false;
    }

    /**
     * Remove a URL which was previously added with {@link #addRedirectionTarget(String, boolean)}
     * or {@link #setRedirectionTargets(List)}
     *
     * @param url
     *            the URL to remove
     * @return true if the URL was removed
     */
    public boolean removeRedirectionTarget(String url) {
        synchronized (this) {
            return this.urlsToRediretTo.remove(url);
        }
    }

    /**
     * Set the URLs which can be used as redirection target after successful authentication. The
     * URLs should be relative and start after the client. They will be checked against the
     * redirection target which is taken from the request parameter returned by
     * {@link #getTargetUrlParameter()} or a cashed request. The found redirection target will only
     * be used if it starts with one of the URLs added via this or the
     * {@link #addRedirectionTarget(String, boolean)} method.
     *
     * @param urlsToInclude
     *            the URLs to include
     */
    public synchronized void setRedirectionTargets(List<String> urlsToInclude) {
        this.urlsToRediretTo.clear();
        if (urlsToInclude != null) {
            this.urlsToRediretTo.addAll(urlsToInclude);
        }
    }

    /**
     * Set a request cache to look for a redirection target if no target URL was provided by a
     * request parameter. By default a {@link HttpSessionRequestCache} will be used.
     *
     * @param requestCache
     *            the cache to use
     */
    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

    private String stripDefaultPort(String url, boolean secure) {
        int schemeOffset = secure ? 9 : 8;
        int colonIdx = url.indexOf(':', schemeOffset);
        int slashIdx = url.indexOf('/', schemeOffset);
        if (colonIdx >= 0 && (slashIdx == -1 || slashIdx > colonIdx)) {
            String port = null;
            if (slashIdx == -1) {
                // url ends with port
                port = url.substring(colonIdx);
            } else if (slashIdx > colonIdx) {
                port = url.substring(colonIdx, slashIdx);
            }
            if (port != null) {
                boolean defaultPort;
                if (secure) {
                    defaultPort = port.equals(":443");
                } else {
                    defaultPort = port.equals(":80");
                }
                if (defaultPort) {
                    String orgUrl = url;
                    url = orgUrl.substring(0, colonIdx);
                    if (slashIdx != -1) {
                        url = url + orgUrl.substring(slashIdx);
                    }
                }
            }
        }
        return url;
    }
}
