package com.communote.server.web.commons.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.model.security.ChannelType;
import com.communote.server.persistence.user.client.ClientUrlHelper;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.controller.StartpageRegistry;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * <p>
 * A filter which tries to resolve the client execution context of the request by looking for a
 * client ID in the request URL. In case a client ID is contained and the client exists the request
 * will be passed to a {@link ClientStatusHandler} to perform status dependent operations. The
 * handler will be looked up in the web application context and if there is none an instance of
 * {@link ClientStatusHandler} will be used. If no client ID is contained in the request URL a
 * redirect will be sent to the start page of the global client. The start page is resolved be using
 * the {@link StartpageRegistry} which is expected to be available in the web application context.
 * </p>
 * Before all of this happens a ClientContextAwareRequestResponseInitializer will be invoked to
 * allow any kind of preparations of the request and response objects so that multiple clients can
 * be supported. The initializer is looked up in the web application context and if there is non
 * this step is ignored.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientContextProcessingFilter implements Filter {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ClientContextProcessingFilter.class);
    private StartpageRegistry startpageResolver;

    private ClientStatusHandler clientStatusHandler;

    private Boolean requestResponseInitializerAvailable;
    private ClientContextAwareRequestResponseInitializer requestResponseInitializer;

    /**
     * Does nothing.
     *
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // Do nothing.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        ClientContextAwareRequestResponseInitializer initializer = getRequestResponseInitializer();
        if (initializer != null) {
            httpRequest = initializer.prepareRequest(httpRequest);
            httpResponse = initializer.prepareResponse(httpResponse);
        }

        // debugSession(httpRequest);
        // get pure servlet path without protocol, servername, port and context
        String uri = httpRequest.getServletPath();
        if (httpRequest.getPathInfo() != null) {
            uri += httpRequest.getPathInfo();
        }

        // check if redirect is required
        if (!sendRedirectToMainPage(uri, httpRequest, httpResponse)) {
            String clientId = ClientUrlHelper.getClientId(uri);
            processClient(httpRequest, httpResponse, filterChain, clientId);
        }
    }

    private ClientStatusHandler getClientStatusHandler() {
        if (this.clientStatusHandler == null) {
            try {
                this.clientStatusHandler = WebServiceLocator.findService(ClientStatusHandler.class);
            } catch (BeansException e) {
                LOGGER.info("No custom ClientStatusHandler found, using default one");
                this.clientStatusHandler = new ClientStatusHandler();
            }
        }
        return this.clientStatusHandler;
    }

    private ClientContextAwareRequestResponseInitializer getRequestResponseInitializer() {
        if (requestResponseInitializerAvailable == null) {
            try {
                this.requestResponseInitializer = WebServiceLocator
                        .findService(ClientContextAwareRequestResponseInitializer.class);
                LOGGER.debug("ClientContextAwareRequestResponseInitializer found: {}",
                        this.requestResponseInitializer.getClass().getName());
                requestResponseInitializerAvailable = Boolean.TRUE;
            } catch (BeansException e) {
                requestResponseInitializerAvailable = Boolean.FALSE;
            }
        }
        return this.requestResponseInitializer;
    }

    /**
     * @return The current startpage resolver.
     */
    private StartpageRegistry getStartpageResolver() {
        if (this.startpageResolver == null) {
            this.startpageResolver = WebServiceLocator.instance().getStartpageRegistry();
        }
        return this.startpageResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // Do nothing.
    }

    /**
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filterChain
     *            the filter chain
     * @param clientId
     *            the client id
     * @throws IOException
     *             in case of an error
     * @throws ServletException
     *             in case of an error
     */
    private void processClient(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain, String clientId) throws IOException, ServletException {
        try {
            // load client
            ClientTO client = ServiceLocator.findService(ClientRetrievalService.class).findClient(
                    clientId);
            getClientStatusHandler().processClient(request, response, filterChain, client);
        } catch (ClientNotFoundException e) {
            processNotExistingClient(response, clientId);
        }
    }

    /**
     * @param response
     *            the response
     * @param clientId
     *            the client id
     * @throws IOException
     *             in case of an error
     */
    private void processNotExistingClient(HttpServletResponse response, String clientId)
            throws IOException {
        LOGGER.debug("client '{}' does not exist", clientId);
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Account does not exist");
    }

    /**
     * Sends redirect to a entry page (recovery or main portal).
     *
     * @param uri
     *            the uri
     * @param request
     *            the request
     * @param response
     *            the response
     * @return true, if redirect to main page was sent
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private boolean sendRedirectToMainPage(String uri, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String[] uriFragments = StringUtils.split(uri, "/");
        String target = null;
        // no client id, or only client id given
        if (uriFragments.length <= 2 || uriFragments.length == 3
                && uriFragments[2].equals("portal")) {
            // no explicit target given, use default target
            target = this.getStartpageResolver().getStartpage();
        }

        if (target != null) {
            // always use the WEB channel because it is a redirect to the home page (which is WEB)
            // and the channel might not be set in the ThreadLocal because that filter is called
            // later
            target = ControllerHelper.renderAbsoluteUrl(request, ClientUrlHelper.getClientId(uri),
                    target, false, ChannelType.WEB, false, false);
            LOGGER.debug("redirect url '{}' to: '{}'", request.getRequestURL(), target);
            String language = request.getParameter("lang");
            if (!StringUtils.isEmpty(language)) {
                target += "?lang=" + language;
            }
            response.sendRedirect(response.encodeRedirectURL(target));
        }
        return target != null;
    }

}
