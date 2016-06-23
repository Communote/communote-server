package com.communote.server.web.commons.viewtool;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class for tools that need the request.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class RequestAwareTool {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext application;

    /**
     * @return the request
     */
    public HttpServletRequest getRequest() {
        return this.request;
    }

    /**
     * @return the response
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * @return the servlet context
     */
    public ServletContext getServletContext() {
        return application;
    }

    /**
     * Sets the current {@link HttpServletRequest}. This is required for this tool to operate and
     * will throw a NullPointerException if this is not set or is set to {@code null}.
     * 
     * @param request
     *            the request
     */
    public void setRequest(HttpServletRequest request) {
        if (request == null) {
            throw new NullPointerException("request should not be null");
        }
        this.request = request;
    }

    /**
     * Sets the current {@link HttpServletResponse}. This is required for this tool to operate and
     * will throw a NullPointerException if this is not set or is set to {@code null}.
     * 
     * @param response
     *            the response
     */
    public void setResponse(HttpServletResponse response) {
        if (response == null) {
            throw new NullPointerException("response should not be null");
        }
        this.response = response;
    }

    /**
     * Sets the current {@link ServletContext}. This is required for this tool to operate and will
     * throw a NullPointerException if this is not set or is set to {@code null}.
     * 
     * @param application
     *            the servlet context
     */
    public void setServletContext(ServletContext application) {
        if (application == null) {
            throw new NullPointerException("servletContext should not be null");
        }
        this.application = application;
    }
}
