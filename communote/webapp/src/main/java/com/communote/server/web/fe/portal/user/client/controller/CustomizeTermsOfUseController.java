package com.communote.server.web.fe.portal.user.client.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.web.commons.MessageHelper;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CustomizeTermsOfUseController extends MultiActionController {

    private static final String USERS_MUST_ACCEPT_TERMS_OF_USE = "usersMustAcceptTermsOfUse";
    private final static String VIEW = "main.microblog.client.customize.termsofuse";

    /**
     * Loads the available languages into the request.
     *
     * @param request
     *            The request.
     */
    private void loadDefaultData(HttpServletRequest request) {
        boolean userMustAcceptTermsOfUse = CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.TERMS_OF_USE_USERS_MUST_ACCEPT,
                        ClientProperty.DEFAULT_TERMS_OF_USE_USERS_MUST_ACCEPT);
        request.setAttribute(USERS_MUST_ACCEPT_TERMS_OF_USE, userMustAcceptTermsOfUse);
    }

    /**
     * Sets all users to not accepted the Terms of Use.
     *
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @return The {@link ModelAndView}.
     */
    public ModelAndView resetUsers(HttpServletRequest request, HttpServletResponse response) {
        try {
            ServiceLocator.findService(UserManagement.class).resetTermsOfUse();
        } catch (AuthorizationException e) {
            MessageHelper.saveErrorMessageFromKey(request, "common.not.authorized.operation");
        }
        loadDefaultData(request);
        return new ModelAndView(VIEW);
    }

    /**
     * Standard view.
     *
     * @param request
     *            Request.
     * @param response
     *            Response.
     * @return {@link ModelAndView}.
     */
    public ModelAndView show(HttpServletRequest request, HttpServletResponse response) {
        loadDefaultData(request);
        return new ModelAndView(VIEW);
    };

    /**
     * Updates the users policy.
     *
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @return The {@link ModelAndView}.
     */
    public ModelAndView updateUsersPolicy(HttpServletRequest request, HttpServletResponse response) {
        Object usersMustAccept = request.getParameterMap().get(USERS_MUST_ACCEPT_TERMS_OF_USE);
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(ClientProperty.TERMS_OF_USE_USERS_MUST_ACCEPT,
                Boolean.toString(usersMustAccept != null));
        loadDefaultData(request);
        return new ModelAndView(VIEW);
    };

}
