package com.communote.server.web.commons.helper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.util.UrlHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.blog.export.NoteWriterFactory;
import com.communote.server.core.security.ssl.ChannelManagement;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.model.security.ChannelType;
import com.communote.server.persistence.user.client.ClientUrlHelper;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.filter.KenmeiForwardFilter;
import com.communote.server.web.fe.widgets.user.news.RssSupportWidget;

/**
 * Helper class for common controller functionality.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ControllerHelper {

    /**
     * Name of the request attribute to provide the target URL for the "Next" button on the error
     * page
     */
    public static final String ATTRIBUTE_NAME_ERROR_PAGE_NEXT_TARGET = "com.communote.errorPage.next";
    /**
     * Name of the request attribute to provide a custom localized error message.
     */
    public static final String ATTRIBUTE_NAME_ERROR_PAGE_CUSTOM_MESSAGE = "com.communote.errorPage.message";

    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(ControllerHelper.class);

    /** The Constant SLASH. */
    private static final String SLASH = "/";

    /** placeholder for the view name replacement to be used in the tiles def. */
    private static final String VIEW_NAME_PLACEHOLDER_MODULE = "MODULE";

    /** The Constant HEADER_X_APPLICATION_RESULT. */
    private static final String HEADER_X_APPLICATION_RESULT = "X-APPLICATION-RESULT";

    /**
     * Attribute name for storing secure URL prefix
     */
    private static final String ATTR_URL_PREFIX_SECURE = "kenmei_url_prefix_secure";
    /**
     * Attribute name for storing URL prefix
     */
    private static final String ATTR_URL_PREFIX = "kenmei_url_prefix";

    /**
     * Append the static resource parameter to the url
     *
     * @param url
     *            the url to append to
     */
    private static void appendStaticResourceParameter(StringBuilder url) {
        url.append("?");
        url.append(KenmeiForwardFilter.PARAMETER_NAME_STATIC_RESOURCE);
        url.append("=true");
    }

    /**
     * Builds an absolute URL
     *
     * @param request
     *            the current request
     * @param clientId
     *            the client ID to include. Can be null which will lead to including the ID of the
     *            current client.
     * @param urlPath
     *            the URL path referencing a resource. The path doesn't have to start with a '/' and
     *            can be null.
     * @param isSecure
     *            whether to use HTTPS.
     * @param staticResource
     *            true if the url should be rendered as a resource not to be delivered by the
     *            dispatcher servlet
     * @param staticResourceByDispatcher
     *            true if the url should be rendered as a resource BUT delivered by the dispatcher
     *            servlet (see KenmeiForwardFilter)
     * @return the absolute URL
     */
    private static String buildAbsoluteUrl(HttpServletRequest request, String clientId,
            String urlPath, boolean isSecure, boolean staticResource,
            boolean staticResourceByDispatcher) {
        StringBuilder url = new StringBuilder();
        if (isSecure) {
            url.append(getUrlPrefixSecure(request));
        } else {
            url.append(getUrlPrefix(request));
        }
        if (staticResource && !staticResourceByDispatcher) {
            ClientUrlHelper.appendUrlPath(url, urlPath);
        } else {
            url.append(ClientUrlHelper.prependModuleClientPart(clientId, urlPath));
            if (staticResourceByDispatcher) {
                appendStaticResourceParameter(url);
            }
        }
        return url.toString();
    }

    /**
     * Builds the URL prefix consisting of protocol, server name, port and context path (without
     * trailing '/') from request and the provided port.
     *
     * @param request
     *            the current request
     * @param port
     *            the port to use
     * @param https
     *            whether the prefix should be used for HTTPS URLs
     * @return the prefix
     */
    private static String buildUrlPrefix(HttpServletRequest request, int port, boolean https) {
        int defaultPort = https ? ApplicationProperty.DEFAULT_WEB_HTTPS_PORT
                : ApplicationProperty.DEFAULT_WEB_HTTP_PORT;
        StringBuilder url = new StringBuilder(https ? "https://" : "http://");
        url.append(request.getServerName());
        if (port != defaultPort) {
            url.append(":");
            url.append(port);
        }
        url.append(request.getContextPath());
        return url.toString();
    }

    /**
     * Dumps the session.
     *
     * @param request
     *            the request
     */
    @SuppressWarnings("unchecked")
    public static synchronized void dumpSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        LOG.debug("###### DUMP SESSION ############");
        if (session != null) {
            List<String> names = Collections.list(session.getAttributeNames());
            if (!names.isEmpty()) {
                Collections.sort(names);
                for (String key : names) {
                    LOG.debug(key + ": "
                            + ToStringBuilder.reflectionToString(session.getAttribute(key)));
                }
            } else {
                LOG.debug("no attributes found in session");
            }
        } else {
            LOG.debug("session not set");
        }
        LOG.debug("###### DUMP SESSION FINISHED ############");
    }

    /**
     * Makes a forward on the current client to the specified relative URL path.
     *
     * @param request
     *            the current request to be forwarded
     * @param response
     *            the response for forwarding
     * @param urlPath
     *            path leading to the target resource. Client and module information will be added.
     * @throws IOException
     *             in case the forward fails
     * @throws ServletException
     *             in case the forward fails
     */
    public static void forwardRequest(ServletRequest request, ServletResponse response,
            String urlPath) throws ServletException, IOException {
        // target of forward is relative to current context
        String target = ClientUrlHelper.prependModuleClientPart(urlPath);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(target);
        requestDispatcher.forward(request, response);
    }

    /**
     * Gets the rss feed link.
     *
     * @param rssSupportWidget
     *            the rss support widget
     * @return the rss link for the rss widget
     */
    public static String getRssFeedLink(RssSupportWidget rssSupportWidget) {
        StringBuilder url = new StringBuilder();
        url.append("?format=rss&");
        url.append(FilterWidgetParameterNameProvider.INSTANCE.getNameForMaxCount());
        url.append("=");
        url.append(NoteWriterFactory.EXPORT_DEFAULT_MAX_POSTS);

        String url2 = "";
        url2 += rssSupportWidget.getRssParameters();

        if (url2.length() > 0) {
            url.append("&");
            url.append(url2);
        }
        return url.toString();
    }

    /**
     * Returns the URL prefix for a HTTP connection. The prefix consists of protocol, server name,
     * port and context path (without trailing '/').
     *
     * @param request
     *            the current request
     * @return the URL prefix
     */
    private static String getUrlPrefix(HttpServletRequest request) {
        String prefix = (String) request.getAttribute(ATTR_URL_PREFIX);
        if (prefix == null) {
            if (!request.isSecure()) {
                prefix = buildUrlPrefix(request, request.getServerPort(), false);
                request.setAttribute(ATTR_URL_PREFIX, prefix);
            } else {
                int port = CommunoteRuntime
                        .getInstance()
                        .getConfigurationManager()
                        .getApplicationConfigurationProperties()
                        .getProperty(ApplicationProperty.WEB_HTTP_PORT,
                                ApplicationProperty.DEFAULT_WEB_HTTP_PORT);
                return buildUrlPrefix(request, port, false);
            }
        }
        return prefix;
    }

    /**
     * Returns the URL prefix for a HTTPS connection. The prefix consists of protocol, server name,
     * port and context path (without trailing '/').
     *
     * @param request
     *            the current request
     * @return the URL prefix
     */
    private static String getUrlPrefixSecure(HttpServletRequest request) {
        String prefix = (String) request.getAttribute(ATTR_URL_PREFIX_SECURE);
        if (prefix == null) {
            if (request.isSecure()) {
                // use port from the request
                // store it for later because we know it works (the URL was reached)
                prefix = buildUrlPrefix(request, request.getServerPort(), true);
                request.setAttribute(ATTR_URL_PREFIX_SECURE, prefix);
            } else {
                String url;
                ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                        .getConfigurationManager().getApplicationConfigurationProperties();
                if (!props.getProperty(ApplicationProperty.WEB_HTTPS_SUPPORTED,
                        ApplicationProperty.DEFAULT_WEB_HTTPS_SUPPORTED)) {
                    url = getUrlPrefix(request);
                } else {
                    int port = props.getProperty(ApplicationProperty.WEB_HTTPS_PORT,
                            ApplicationProperty.DEFAULT_WEB_HTTPS_PORT);
                    url = buildUrlPrefix(request, port, true);
                }
                return url;
            }
        }
        return prefix;
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
     *            the current request
     * @param location
     *            Absolute URL to be validated.
     * @param baseUrl
     *            the baseURL can be null
     * @param renderSessionId
     *            true if rendering the session ID is forced; can be null
     * @return <code>true</code> if the specified URL should be encoded with a session identifier
     */
    private static boolean isEncodeable(HttpServletRequest request, String location,
            String baseUrl, Boolean renderSessionId) {

        if (location == null || baseUrl != null) {
            return false;
        }

        boolean isEncodeable = true;

        if (renderSessionId != null && renderSessionId) {
            isEncodeable = true;
        } else if (renderSessionId != null && !renderSessionId) {
            isEncodeable = false;
        } else if (location.startsWith("#")) {
            isEncodeable = false; // Is this an intra-document reference?
        } else if (request.isRequestedSessionIdFromCookie()) {
            isEncodeable = false;
        } else if (request.getSession(false) == null) {
            isEncodeable = false; // Are we in a valid session that is not using cookies?
        }

        // This URL belongs to our web application, so it is encodeable
        return isEncodeable;
    }

    /**
     * Prepare a model and view with a view named "core.ajax.response". The method will set a
     * request attribute htmlResponse to true if the Accept header does not contain
     * application/json. In case it contains application/json the content-type will be set to
     * application/json and the attribute will be set to false or the response is written directly
     * if writeJsonDirectly is true. The view can access the jsonObject via the request attribute
     * jsonResponse.
     *
     * @param request
     *            the current request
     * @param response
     *            the response
     * @param json
     *            the JSON to write
     * @param writeJsonDirectly
     *            whether to write the JSON directly or let the view handle it. In case this is
     *            false the view must check for the attribute htmlResponse and if set to false
     *            directly write the JSON object.
     * @return the model and view or null if the response has been written directly
     * @throws IOException
     *             in case of an exception while serializing the JSON object to the response
     */
    public static ModelAndView prepareModelAndViewForJsonResponse(HttpServletRequest request,
            HttpServletResponse response, ObjectNode json, boolean writeJsonDirectly)
                    throws IOException {
        // TODO use JsonRequestHelper.isJsonRequested?
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            if (writeJsonDirectly) {
                JsonRequestHelper.writeJsonResponse(response, json);
                return null;
            }
            response.setContentType("application/json");
            request.setAttribute("htmlResponse", "false");
        } else {
            request.setAttribute("htmlResponse", "true");
        }
        request.setAttribute("jsonResponse", JsonHelper.writeJsonTreeAsString(json));
        return new ModelAndView("core.ajax.response");
    }

    /**
     * Renders an absolute URL for the provided URL path. The returned URL will be a using the HTTPS
     * protocol if the forceHttps parameter is true or
     * {@link com.communote.server.core.security.ssl.ChannelManagement#isForceSsl()} returns true or
     * the current request was received via HTTPS. If the latter condition does not hold but one of
     * the others does, the returned URL might still be using the HTTP protocol if the property
     * {@link ApplicationProperty#WEB_HTTPS_SUPPORTED} is set to false.
     *
     * @param request
     *            the current request
     * @param clientId
     *            the client ID to include. Can be null which will lead to including the ID of the
     *            current client.
     * @param urlPath
     *            the URL path referencing a resource. The path doesn't have to start with a '/' and
     *            can be null.
     * @param forceHttps
     *            whether to force an HTTPS URL
     * @param staticResource
     *            true if the url should be rendered as a resource not to be delivered by the
     *            dispatcher servlet
     * @param staticResourceByDispatcher
     *            true if the url should be rendered as a resource BUT delivered by the dispatcher
     *            servlet (see KenmeiForwardFilter)
     * @return the absolute URL
     */
    public static String renderAbsoluteUrl(HttpServletRequest request, String clientId,
            String urlPath, boolean forceHttps, boolean staticResource,
            boolean staticResourceByDispatcher) {
        boolean isSecure = forceHttps || request.isSecure()
                || ServiceLocator.findService(ChannelManagement.class).isForceSsl();
        return buildAbsoluteUrl(request, clientId, urlPath, isSecure, staticResource,
                staticResourceByDispatcher);
    }

    /**
     * Renders an absolute URL for the provided URL path. The returned URL will be a using the HTTPS
     * protocol if the forceHttps parameter is true or
     * {@link ChannelManagement#isForceSsl(ChannelType))} returns true for the provided channel or
     * the current request was received via HTTPS. If the latter condition does not hold but one of
     * the others does, the returned URL might still be using the HTTP protocol if the property
     * {@link ApplicationProperty#WEB_HTTPS_SUPPORTED} is set to false.
     *
     * @param request
     *            the current request
     * @param clientId
     *            the client ID to include. Can be null which will lead to including the ID of the
     *            current client.
     * @param urlPath
     *            the URL path referencing a resource. The path doesn't have to start with a '/' and
     *            can be null.
     * @param forceHttps
     *            whether to force an HTTPS URL
     * @param channelTyp
     *            the channel to check for a forced SSL connection, instead of using the current
     *            channel
     * @param staticResource
     *            true if the url should be rendered as a resource not to be delivered by the
     *            dispatcher servlet
     * @param staticResourceByDispatcher
     *            true if the url should be rendered as a resource BUT delivered by the dispatcher
     *            servlet (see KenmeiForwardFilter)
     * @return the absolute URL
     */
    public static String renderAbsoluteUrl(HttpServletRequest request, String clientId,
            String urlPath, boolean forceHttps, ChannelType channelTyp, boolean staticResource,
            boolean staticResourceByDispatcher) {
        boolean isSecure = forceHttps || request.isSecure()
                || ServiceLocator.findService(ChannelManagement.class).isForceSsl(channelTyp);
        return buildAbsoluteUrl(request, clientId, urlPath, isSecure, staticResource,
                staticResourceByDispatcher);
    }

    /**
     * Renders an absolute URL for the provided URL path. The returned URL will be a using the HTTPS
     * protocol if the forceHttps parameter is true or
     * {@link com.communote.server.core.security.ssl.ChannelManagement#isForceSsl()} returns true.
     * the returned URL might still be using the HTTP protocol if the property
     * {@link ApplicationProperty#WEB_HTTPS_SUPPORTED} is set to false. In contrast to
     * {@link #renderAbsoluteUrl(HttpServletRequest, String, String, boolean)} the protocol used for
     * the current request will not be evaluated.
     *
     * @param request
     *            the current request
     * @param clientId
     *            the client ID to include. Can be null which will lead to including the ID of the
     *            current client.
     * @param urlPath
     *            the URL path referencing a resource. The path doesn't have to start with a '/' and
     *            can be null.
     * @param forceHttps
     *            whether to force an HTTPS URL
     * @param staticResource
     *            true if the url should be rendered as a resource not to be delivered by the
     *            dispatcher servlet
     * @param staticResourceByDispatcher
     *            true if the url should be rendered as a resource BUT delivered by the dispatcher
     *            servlet (see KenmeiForwardFilter)
     * @return the absolute URL
     */
    public static String renderAbsoluteUrlIgnoreRequestProtocol(HttpServletRequest request,
            String clientId, String urlPath, boolean forceHttps, boolean staticResource,
            boolean staticResourceByDispatcher) {
        boolean isSecure = forceHttps
                || ServiceLocator.findService(ChannelManagement.class).isForceSsl();
        return buildAbsoluteUrl(request, clientId, urlPath, isSecure, staticResource,
                staticResourceByDispatcher);
    }

    /**
     * Renders a relative URL for the provided URL path that leads to the referenced resource on the
     * current client. For example 'portal/home.do' becomes '/microblog/global/portal/home.do' if
     * the current client is named 'global'. The context is included.
     *
     * @param request
     *            the current request
     * @param urlPath
     *            the URL path referencing a resource. The path doesn't have to start with a '/' and
     *            can be null.
     * @param staticResource
     *            true if the url should be rendered as a resource not to be delivered by the
     *            dispatcher servlet
     * @param staticResourceByDispatcher
     *            true if the url should be rendered as a resource BUT delivered by the dispatcher
     *            servlet (see KenmeiForwardFilter)
     * @return the relative URL, starting with a '/'
     */
    public static String renderRelativeUrl(HttpServletRequest request, String urlPath,
            boolean staticResource, boolean staticResourceByDispatcher) {
        return ControllerHelper.renderRelativeUrl(request, null, urlPath, staticResource,
                staticResourceByDispatcher);
    }

    /**
     * Renders a relative URL for the provided URL path that leads to the referenced resource on the
     * given client. For example 'portal/home.do' becomes '/microblog/global/portal/home.do' if the
     * provided clientId is 'global'. The context is included.
     *
     * @param request
     *            the current request
     * @param clientId
     *            the client ID to include. Can be null which will lead to including the ID of the
     *            current client.
     * @param urlPath
     *            the URL path referencing a resource. The path doesn't have to start with a '/' and
     *            can be null.
     * @param staticResource
     *            true if the URL should be rendered as a resource which will not be delivered by
     *            the dispatcher servlet
     * @param staticResourceByDispatcher
     *            true if the url should be rendered as a resource BUT delivered by the dispatcher
     *            servlet (see KenmeiForwardFilter)
     * @return the relative URL, starting with a '/'
     */
    public static String renderRelativeUrl(HttpServletRequest request, String clientId,
            String urlPath, boolean staticResource, boolean staticResourceByDispatcher) {
        StringBuilder urlBuilder = new StringBuilder(request.getContextPath());
        if (staticResource && !staticResourceByDispatcher) {
            ClientUrlHelper.appendUrlPath(urlBuilder, urlPath);
        } else {
            urlBuilder.append(ClientUrlHelper.prependModuleClientPart(clientId, urlPath));
            if (staticResourceByDispatcher) {
                appendStaticResourceParameter(urlBuilder);
            }
        }

        return urlBuilder.toString();

    }

    /**
     * Renders the URL for the specified urlPath.
     *
     * @param request
     *            the current request to retrieve session data
     * @param urlPath
     *            the URL path
     * @param baseUrl
     *            optional string with a URL that should be prefixed to the urlPath. If set, the
     *            parameters absolute, secure and clientId will be ignored.
     * @param absolute
     *            whether the URL should be rendered as an absolute URL. Will be ignored if baseUrl
     *            is not null.
     * @param secure
     *            whether the URL should be rendered with the HTTPS protocol. Will be ignored if
     *            absolute is false or baseUrl is not null.
     * @param renderSessionId
     *            optional parameter to force inclusion of the current session ID. If null the
     *            session ID will be included if the request asked for an existing session and the
     *            session ID was not received via a cookie. If not null and true the ID of the
     *            current session will be included. In case there is no session the session will be
     *            created. If baseUrl is not null the session ID won't be included.
     * @param clientId
     *            the ID of the client to be included in the URL. If null the ID of the current
     *            client will be used. Will be ignored if baseUrl is not null.
     * @param staticResource
     *            true if the url should be rendered as a resource not to be delivered by the
     *            dispatcher servlet
     * @param staticResourceByDispatcher
     *            true if the url should be rendered as a resource BUT delivered by the dispatcher
     *            servlet (see KenmeiForwardFilter)
     *
     * @return the rendered URL
     */
    public static String renderUrl(HttpServletRequest request, String urlPath, String baseUrl,
            boolean absolute, boolean secure, Boolean renderSessionId, String clientId,
            boolean staticResource, boolean staticResourceByDispatcher) {
        String url = null;
        if (baseUrl != null) {
            StringBuilder urlSb = new StringBuilder(baseUrl);
            if (!baseUrl.endsWith("/")) {
                if (!urlPath.startsWith("/")) {
                    urlSb.append("/");
                }
                urlSb.append(urlPath);
            } else {
                if (urlPath.startsWith("/")) {
                    urlSb.append(urlPath.substring(1));
                } else {
                    urlSb.append(urlPath);
                }
            }
            if (staticResourceByDispatcher) {
                appendStaticResourceParameter(urlSb);
            }
            url = urlSb.toString();
        } else {
            if (absolute) {
                url = ControllerHelper.renderAbsoluteUrl(request, clientId, urlPath, secure,
                        staticResource, staticResourceByDispatcher);
            } else {
                url = ControllerHelper.renderRelativeUrl(request, clientId, urlPath,
                        staticResource, staticResourceByDispatcher);
            }
        }
        if (isEncodeable(request, url, baseUrl, renderSessionId)) {
            // always create session if not there
            url = UrlHelper.insertSessionIdInUrl(url, request.getSession().getId());
        }
        return url;
    }

    /**
     * Replace 'MODULE' in the view name of the MAV with the current module.
     *
     * @param mav
     *            the mav to check
     * @return the model and view
     */
    public static ModelAndView replaceModuleInMAV(ModelAndView mav) {
        if (mav != null) {
            mav.setViewName(replaceModuleInViewName(mav.getViewName()));
        }
        return mav;
    }

    /**
     * Replace the module in the view name.
     *
     * @param viewName
     *            the view name
     * @return the view name with replaced module
     */
    public static String replaceModuleInViewName(String viewName) {
        if (viewName != null && viewName.contains(VIEW_NAME_PLACEHOLDER_MODULE)) {
            viewName = viewName.replace(VIEW_NAME_PLACEHOLDER_MODULE,
                    ClientUrlHelper.MODULE_MICROBLOG);
        }
        return viewName;
    }

    /**
     * Send a redirect within the web application. The redirect will be sent to the current client.
     *
     * @param request
     *            the current request
     * @param response
     *            the response for the redirect
     * @param relativeUrl
     *            a relative URL to append. can be null
     * @throws IOException
     *             in case the redirect fails
     */
    public static void sendInternalRedirect(HttpServletRequest request,
            HttpServletResponse response, String relativeUrl) throws IOException {
        ControllerHelper.sendInternalRedirect(request, response, null, relativeUrl);
    }

    /**
     * Send a redirect within the web application. The redirect will be sent to the provided client
     * or if not specified to the current client.
     *
     * @param request
     *            the current request
     * @param response
     *            the response for the redirect
     * @param clientId
     *            the ID of the client to redirect to. If this parameter is null the redirect will
     *            be sent to the current client.
     * @param relativeUrl
     *            a relative URL to append. can be null
     * @throws IOException
     *             in case the redirect fails
     */
    public static void sendInternalRedirect(HttpServletRequest request,
            HttpServletResponse response, String clientId, String relativeUrl) throws IOException {
        String redirectLink = renderAbsoluteUrl(request, clientId, relativeUrl, false, false, false);

        if (LOG.isDebugEnabled()) {
            LOG.debug("send redirect to '" + redirectLink.toString() + "'");
        }
        response.sendRedirect(response.encodeRedirectURL(redirectLink.toString()));
    }

    /**
     * Sends a redirect to the logout URL of the current client.
     *
     * @param request
     *            the current request
     * @param response
     *            the response for the redirect
     * @throws IOException
     *             in case the redirect fails
     */
    public static void sendInternalRedirectToLogoutUrl(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        ControllerHelper.sendInternalRedirect(request, response, null, "/logout.do");
    }

    /**
     * Send a redirect within the web application. The redirect will be sent to the start page of
     * the current client.
     *
     * @param request
     *            the current request
     * @param response
     *            the response for the redirect
     * @throws IOException
     *             in case the redirect fails
     */
    public static void sendInternalRedirectToStartPage(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String relativeUrl = WebServiceLocator.instance().getStartpageRegistry().getStartpage();
        ControllerHelper.sendInternalRedirect(request, response, null, relativeUrl);
    }

    /**
     * Send a redirect using the provided URL.
     *
     * @param request
     *            the current request
     * @param response
     *            the response for the redirect
     * @param url
     *            the URL. Can be absolute or relative. In case it is relative the redirect will be
     *            on the server, but no client ID will be included.
     * @throws IOException
     *             in case of an error
     */
    public static void sendRedirect(HttpServletRequest request, HttpServletResponse response,
            String url) throws IOException {
        StringBuilder redirectLink = new StringBuilder();
        if (StringUtils.isBlank(url)) {
            redirectLink.append(request.getContextPath());
            redirectLink.append(SLASH);
        } else {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                redirectLink.append(request.getContextPath());
                if (!url.startsWith(SLASH)) {
                    redirectLink.append(SLASH);
                }
            }
            redirectLink.append(url);
        }
        response.sendRedirect(response.encodeRedirectURL(redirectLink.toString()));
    }

    /**
     * Sets the application failure.
     *
     * @param response
     *            the response
     */
    public static void setApplicationFailure(HttpServletResponse response) {
        response.setHeader(HEADER_X_APPLICATION_RESULT, "ERROR");
    }

    /**
     * Sets the application success.
     *
     * @param response
     *            the response
     */
    public static void setApplicationSuccess(HttpServletResponse response) {
        response.setHeader(HEADER_X_APPLICATION_RESULT, "OK");
    }

    /**
     * do not use me this way.
     */
    private ControllerHelper() {

    }
}
