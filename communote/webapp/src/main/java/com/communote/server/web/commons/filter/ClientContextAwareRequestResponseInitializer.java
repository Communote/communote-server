package com.communote.server.web.commons.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Will be called by the {@link ClientContextProcessingFilter} to allow preparations of the request
 * and response objects so that they can handle multiple clients.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ClientContextAwareRequestResponseInitializer {

    public HttpServletRequest prepareRequest(HttpServletRequest request);

    public HttpServletResponse prepareResponse(HttpServletResponse response);
}
