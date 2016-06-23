package com.communote.server.core.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * TODO implement javax.servlet.http.HttpServletRequestWrapper instead of {@link HttpServletRequest}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class HttpServletRequestWrapper implements HttpServletRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServletRequestWrapper.class);

    private final HttpServletRequest request;
    private final boolean removeFileExtensionInRequestUri;

    /**
     * Wrapps the request. by default the fileextension of the request uri should be removed (see
     * {@link #getRequestURI()}
     *
     * @param request
     *            the request to wrap
     */
    public HttpServletRequestWrapper(HttpServletRequest request) {
        this(request, true);
    }

    /**
     *
     * @param request
     *            the request to wrap
     * @param removeFileExtensionInRequestUri
     *            determines if the fileextension of the request uri should be removed (see
     *            {@link #getRequestURI()}
     */
    public HttpServletRequestWrapper(HttpServletRequest request,
            boolean removeFileExtensionInRequestUri) {
        this.request = request;
        this.removeFileExtensionInRequestUri = removeFileExtensionInRequestUri;
    }

    /**
     * Append the parameter with its values to the request parameter string.
     *
     * @param parameterBuilder
     *            the builder representing the parameter string to append to
     * @param parameterKey
     *            the key of the parameter
     * @param values
     *            the values of the parameter, can be null if no values were specified
     * @param urlEncode
     *            whether to encode parameter key and values using URL encoding
     * @param encoding
     *            the character encoding to use when urlEncode is true
     * @throws UnsupportedEncodingException
     *             in case the character encoding is not supported
     */
    private void appendParameter(StringBuilder parameterBuilder, String parameterKey,
            String[] values,
            boolean urlEncode, String encoding) throws UnsupportedEncodingException {
        if (values != null) {
            if (urlEncode) {
                parameterKey = URLEncoder.encode(parameterKey, encoding);
            }
            // a key can have multiple values assigned so iterate over this values
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                if (!StringUtils.isEmpty(value)) {
                    if (urlEncode) {
                        value = URLEncoder.encode(value, encoding);
                    }
                    parameterBuilder.append(parameterKey);
                    parameterBuilder.append("=");
                    parameterBuilder.append(value);
                    parameterBuilder.append("&");
                }
            }
        }
    }

    @Override
    public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {
        return request.authenticate(arg0);
    }

    @Override
    public AsyncContext getAsyncContext() {
        return request.getAsyncContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(String name) {
        return request.getAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getAttributeNames() {
        return request.getAttributeNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthType() {
        return request.getAuthType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return request.getContentType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContextPath() {
        return request.getContextPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cookie[] getCookies() {
        return request.getCookies();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDateHeader(String name) {
        return request.getDateHeader(name);
    }

    @Override
    public DispatcherType getDispatcherType() {
        return request.getDispatcherType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getHeaderNames() {
        return request.getHeaderNames();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getHeaders(String name) {
        return request.getHeaders(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        String contentType = request.getContentType();
        // return input stream directly if content type begins with application/json or
        // multipart/form-data since restlet can handle it
        if (contentType != null) {
            if (contentType.startsWith(MediaType.APPLICATION_JSON)
                    || request.getContentType().startsWith(MediaType.MULTIPART_FORM_DATA)
                    || request.getContentType().startsWith(MediaType.APPLICATION_OCTET_STREAM)) {
                return request.getInputStream();
            }
            if (contentType.toLowerCase().startsWith(MediaType.APPLICATION_FORM_URLENCODED)) {
                return getInputStreamFromParameterMap(true);
            } else {
                // TODO shouldn't we return "415 Unsupported Media Type"?
                LOGGER.warn("Received unsupported content type {}"
                        + ". This might lead to unexpected results.", getContentType());
            }
        }
        return getInputStreamFromParameterMap(false);
    }

    /**
     *
     * @param urlEncode
     *            encoding of url
     * @return the inputstream for the parameters
     * @throws UnsupportedEncodingException
     *             can not encode parameter
     */
    private ServletInputStream getInputStreamFromParameterMap(boolean urlEncode)
            throws UnsupportedEncodingException {
        @SuppressWarnings("unchecked")
        Map<String, String[]> parameters = request.getParameterMap();

        String encoding = getCharacterEncoding();
        if (encoding == null) {
            // fallback to default encoding
            encoding = Charset.defaultCharset().name();
        }
        String parametersAsString = StringUtils.EMPTY;
        if (parameters != null && !parameters.isEmpty()) {
            StringBuilder parameterBuilder = new StringBuilder();
            for (String mapKey : parameters.keySet()) { // iterate over map
                appendParameter(parameterBuilder, mapKey, parameters.get(mapKey), urlEncode,
                        encoding);
            }
            if (parameterBuilder.length() > 0) {
                parametersAsString = parameterBuilder.substring(0, parameterBuilder.length() - 1);
            } else {
                parametersAsString = parameterBuilder.toString();
            }
        }

        return new RestApiByteArrayInputStream(parametersAsString.getBytes(encoding));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIntHeader(String name) {
        return request.getIntHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocalAddr() {
        return request.getLocalAddr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return request.getLocale();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getLocales() {
        return request.getLocales();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocalName() {
        return request.getLocalName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLocalPort() {
        return request.getLocalPort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethod() {
        return request.getMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return request.getParameterMap();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getParameterNames() {
        return request.getParameterNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getParameterValues(String name) {
        return request.getParameterValues(name);
    }

    @Override
    public Part getPart(String arg0) throws IOException, ServletException {
        return request.getPart(arg0);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return request.getParts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathInfo() {
        return request.getPathInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathTranslated() {
        return request.getPathTranslated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProtocol() {
        return request.getProtocol();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return request.getReader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRealPath(String path) {
        return request.getRealPath(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRemotePort() {
        return request.getRemotePort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    /**
     * @return {@link HttpServletRequest}
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return request.getRequestDispatcher(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestURI() {
        // TODO this is a short-before-release hack to allow identifiers with "." but also the html
        // extension for attachments", see KENMEI-5279 and related issues for details why
        if (removeFileExtensionInRequestUri || request.getRequestURI().contains("attachments.html")) {
            return request.getRequestURI().replaceFirst("\\.[a-z]+", "");
        }
        return request.getRequestURI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuffer getRequestURL() {
        return request.getRequestURL();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScheme() {
        return request.getScheme();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerName() {
        return request.getServerName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getServerPort() {
        return request.getServerPort();
    }

    @Override
    public ServletContext getServletContext() {
        return request.getServletContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletPath() {
        return request.getServletPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpSession getSession() {
        return request.getSession();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return request.getSession();
    }

    @Override
    public Principal getUserPrincipal() {
        return request.getUserPrincipal();
    }

    @Override
    public boolean isAsyncStarted() {
        return request.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return request.isAsyncSupported();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return request.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return request.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return request.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }

    @Override
    public boolean isSecure() {
        return request.isSecure();
    }

    @Override
    public boolean isUserInRole(String role) {
        return request.isUserInRole(role);
    }

    @Override
    public void login(String arg0, String arg1) throws ServletException {
        request.login(arg0, arg1);

    }

    @Override
    public void logout() throws ServletException {
        request.logout();

    }

    @Override
    public void removeAttribute(String name) {
        request.removeAttribute(name);

    }

    @Override
    public void setAttribute(String name, Object o) {
        request.setAttribute(name, o);

    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        request.setCharacterEncoding(env);

    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return request.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
            throws IllegalStateException {
        return request.startAsync(arg0, arg1);
    }

}
