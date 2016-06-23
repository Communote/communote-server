package com.communote.server.web.fe.portal.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.persistence.common.security.SecurityCodeNotFoundException;

/**
 * Handler for confirming a specific security code.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class SecurityCodeConfirmationHandler {

    private final SecurityCodeAction action;

    /**
     * Create a new handler for the given action
     *
     * @param action
     *            the action of the security code this handler can handle
     */
    public SecurityCodeConfirmationHandler(SecurityCodeAction action) {
        this.action = action;
    }

    /**
     * Confirm the code.
     *
     * @param request
     *            the current request
     * @param response
     *            the current response
     * @param code
     *            the code to confirm
     * @return the result of the confirmation or null if the handler has already send an appropriate
     *         response within this method
     * @throws SecurityCodeNotFoundException
     *             in case the code was removed in the meantime
     * @throws IOException
     *             in case this handler wrote to the response and this caused an error
     *
     */
    public abstract SecurityCodeConfirmationResult confirm(HttpServletRequest request,
            HttpServletResponse response, SecurityCode code) throws SecurityCodeNotFoundException,
            IOException;

    /**
     * @return the action of the security code this handler can handle
     */
    public SecurityCodeAction getAction() {
        return this.action;
    }

    /**
     * Return a message that should be shown as warning to the user if the action of this handler
     * was provided but the code did not exist. This can for instance occur if a confirmation link
     * is opened a second time.
     *
     * @param request
     *            the current request
     * @param code
     *            the code that could not be found
     * @return the message or null if this case should not be handled specifically
     */
    public LocalizedMessage getWarningForMissingCode(HttpServletRequest request, String code) {
        return null;
    }

}
