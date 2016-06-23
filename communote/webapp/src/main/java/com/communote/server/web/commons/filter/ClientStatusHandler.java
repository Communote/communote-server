package com.communote.server.web.commons.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.model.client.ClientStatus;
import com.communote.server.web.commons.MessageHelper;

/**
 * Handler to perform actions like forwards or redirects depending of the status of the current
 * client. This default implementation, which only supports the status ACTIVE, will associate the
 * client with the current thread.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClientStatusHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientStatusHandler.class);

    /**
     * Handles the case of a client in status ACTIVE by associating the client with the current
     * thread and continuing the filter chain.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filterChain
     *            the filter chain
     * @param client
     *            the client
     * @throws IOException
     *             in case of an IO error writing the response
     * @throws ServletException
     *             in case of an filter chain error
     */
    protected void processActiveClient(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain, ClientTO client) throws IOException, ServletException {
        try {
            // set client && module
            ClientAndChannelContextHolder.setClient(client);
            // execute filter chain
            filterChain.doFilter(request, response);
        } finally {
            // reset kenmei thread local but expose current clientId to a request attribute so that
            // it can be accessed by the error page
            ClientAndChannelContextHolder.clear();
            request.setAttribute(MessageHelper.CLIENT_ID_REQUEST_ATTRIBUTE, client.getClientId());
        }
    }

    /**
     * Perform some status dependent operation.
     *
     * @param request
     *            the current request
     * @param response
     *            the current response
     * @param filterChain
     *            the filter chain
     * @param client
     *            the extracted client
     * @throws IOException
     *             in case of an IO error while writing a response, redirecting, forwarding or
     *             continuing the filter chain
     * @throws ServletException
     *             in case of a servlet error while forwarding or continuing the filter chain
     */
    public void processClient(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain, ClientTO client) throws IOException, ServletException {
        if (ClientStatus.ACTIVE.equals(client.getClientStatus())) {
            processActiveClient(request, response, filterChain, client);
        } else {
            LOGGER.error("Client {} has unsupported status {}", client.getClientId(),
                    client.getClientStatus());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Client status not supported");
        }
    }
}
