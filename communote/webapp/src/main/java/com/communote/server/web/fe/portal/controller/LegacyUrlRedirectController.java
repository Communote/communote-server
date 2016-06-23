package com.communote.server.web.fe.portal.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * This controller can be used to redirect "legacy" urls to their newer ones using String based
 * replacing.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LegacyUrlRedirectController implements Controller {

    private final String replace;
    private final String replacement;

    /**
     * Constructor.
     * 
     * @param replace
     *            The String to be replaced.
     * @param replacement
     *            The replacement for the replaced String.
     */
    public LegacyUrlRedirectController(String replace, String replacement) {
        this.replace = replace;
        this.replacement = replacement;
    }

    /**
     * Does the replacement and redirect.
     * 
     * {@inheritDoc}
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String requestURI = request.getRequestURI();
        requestURI = requestURI.replaceFirst(replace, replacement);
        String queryString = request.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            requestURI += "?" + request.getQueryString();
        }
        response.sendRedirect(requestURI);
        return null;
    }
}
