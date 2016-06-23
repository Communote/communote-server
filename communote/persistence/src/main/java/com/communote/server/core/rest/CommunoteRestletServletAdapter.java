package com.communote.server.core.rest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.engine.http.HttpRequest;
import org.restlet.engine.http.HttpResponse;
import org.restlet.ext.servlet.ServletAdapter;
import org.restlet.ext.servlet.internal.ServletCall;

/**
 * Extend the restlets {@link ServletAdapter} since we need to specify the
 * {@link #getBaseRef(HttpServletRequest)} more freely and
 * {@link HttpServletRequest#getServletPath()} does not seem to fulfill our needs.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CommunoteRestletServletAdapter extends ServletAdapter {

    /**
     * The url base path the restlet will be listening too
     */
    private String urlBasePath;

    /**
     * Restlet servlet adapter
     * 
     * @param context
     *            the servlet context
     */
    public CommunoteRestletServletAdapter(ServletContext context) {
        super(context);
    }

    /**
     * {@inheritDoc} Will take the urlBasePath as basePath extension instead of
     * {@link HttpServletRequest#getServletPath()}
     */
    @Override
    public Reference getBaseRef(HttpServletRequest request) {
        Reference result = null;

        final String basePath = urlBasePath == null ? request.getServletPath() : urlBasePath;
        final String baseUri = request.getRequestURL().toString();
        // Path starts at first slash after scheme://
        final int pathStart = baseUri.indexOf("/", request.getScheme().length() + 3);
        if (basePath.length() == 0) {
            // basePath is empty in case the webapp is mounted on root context
            if (pathStart != -1) {
                result = new Reference(baseUri.substring(0, pathStart));
            } else {
                result = new Reference(baseUri);
            }
        } else {
            if (pathStart != -1) {

                String basePathPattern = basePath;
                basePathPattern = basePathPattern.replace("/", "\\/");
                basePathPattern = basePathPattern.replace("*", "\\w+");
                basePathPattern = basePathPattern + ".*";

                Pattern pattern = Pattern.compile(basePathPattern);
                Matcher matcher = pattern.matcher(baseUri);

                int baseIndex = -1;
                // = baseUri.indexOf(basePath, pathStart);
                if (matcher.find()) {
                    baseIndex = matcher.start();
                }

                if (baseIndex != -1) {
                    result = new Reference(baseUri.substring(0, baseIndex + basePath.length()));
                }
            }
        }

        return result;
    }

    /**
     * 
     * @return the url base path that will be used for {@link #getBaseRef(HttpServletRequest)}
     */
    public String getUrlBasePath() {
        return urlBasePath;
    }

    /**
     * Services a HTTP Servlet request as a Restlet request handled by the "target" Restlet.
     * 
     * @param request
     *            The HTTP Servlet request.
     * @param response
     *            The HTTP Servlet response.
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) {
        if (getNext() != null) {
            try {
                HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request,
                        false);

                // Set the current context
                Context.setCurrent(getContext());

                // Convert the Servlet call to a Restlet call
                ServletCall servletCall = new ServletCall(wrappedRequest.getLocalAddr(),
                        wrappedRequest.getLocalPort(), wrappedRequest, response);
                HttpRequest httpRequest = toRequest(servletCall);
                HttpResponse httpResponse = new HttpResponse(servletCall, httpRequest);

                // Adjust the relative reference
                httpRequest.getResourceRef().setBaseRef(getBaseRef(wrappedRequest));

                // Adjust the root reference
                httpRequest.setRootRef(getRootRef(wrappedRequest));

                // workaround for that moment to hand over the session id to the resources. this is
                // needed for the login
                // FIX: the injection of the HttpServletRequest within the ContextInjector of the
                // restlet extension
                HttpRequest.addHeader(httpRequest, "requestSessionId", wrappedRequest.getSession()
                        .getId());

                // workaround for empty post body for delete, put requests with content type of form
                if (request.getMethod().equals("POST")
                        && request.getContentType().startsWith("application/x-www-form-urlencoded")
                        && request.getContentLength() == 0) {
                    HttpRequest.addHeader(httpRequest, "Content-Length", "1");
                }

                // Handle the request and commit the response
                getNext().handle(httpRequest, httpResponse);
                commit(httpResponse);
            } finally {
                Engine.clearThreadLocalVariables();
            }
        } else {
            getLogger().warning("Unable to find the Restlet target");
        }
    }

    /**
     * Set the url base path, it will be trimmed, and preceeded by a slash if not happend so far
     * 
     * @param urlBasePath
     *            the url base path that will be used for {@link #getBaseRef(HttpServletRequest)}
     */
    public void setUrlBasePath(String urlBasePath) {
        this.urlBasePath = urlBasePath;
        if (this.urlBasePath != null) {
            this.urlBasePath = this.urlBasePath.trim();
            if (!this.urlBasePath.startsWith("/")) {
                this.urlBasePath = "/" + this.urlBasePath;
            }
        }
    }
}
