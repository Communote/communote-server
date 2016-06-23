package com.communote.plugins.api.rest.request;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Request;

import org.restlet.engine.http.HttpRequest;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.servlet.ServletUtils;

/**
 * RequestHelper contains all functions for getting informations from {@link Request}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class RequestHelper {

    /**
     * Get the {@link HttpServletRequest} of {@link Request}
     * 
     * @param request
     *            {@link Request}
     * @return {@link HttpServletRequest}
     */
    public static HttpServletRequest getHttpServletRequest(org.restlet.Request request) {
        return ((org.restlet.ext.servlet.internal.ServletCall) ((HttpRequest) request)
                .getHttpCall()).getRequest();
    }

    /**
     * Get the {@link HttpServletRequest} of {@link Request}
     * 
     * @param request
     *            {@link Request}
     * @return {@link HttpServletRequest}
     */
    public static HttpServletRequest getHttpServletRequest(Request request) {
        if (request == null) {
            return null;
        }
        HttpServletRequest httpServletRequest = null;
        if (request instanceof CallContext) {
            CallContext callContext = (CallContext) request;
            httpServletRequest = ServletUtils.getRequest(callContext.getRequest());
        } else if (request instanceof ThreadLocalizedContext) {
            ThreadLocalizedContext threadLocalizedContext = (ThreadLocalizedContext) request;
            httpServletRequest = ServletUtils.getRequest(threadLocalizedContext.get().getRequest());
        }

        return httpServletRequest;
    }

    /**
     * Default constructor
     */
    private RequestHelper() {
        // not implemented
    }

}
