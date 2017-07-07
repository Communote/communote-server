package com.communote.server.web.fe.portal.user.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.web.commons.controller.VelocityViewController;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Controller for rendering the profile page of the current user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserProfileController extends VelocityViewController {

    private String redirectUrl;

    /**
     * @param primaryExternalAuthentication
     *            Id of the current primary external authentication.
     * @return True, when the user is allowed to change the password or email address.
     */
    private boolean canChangePasswordAndMail(String primaryExternalAuthentication) {
        if (primaryExternalAuthentication == null) {
            return true;
        }
        return !ServiceLocator.instance().getService(UserManagement.class)
                .hasExternalAuthentication(SecurityHelper.getCurrentUserId(),
                        primaryExternalAuthentication);
    }

    /**
     * @return the redirecUrl
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * Forces https, if enabled.
     *
     * {@inheritDoc}
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (!request.isSecure()
                && Boolean.parseBoolean(ApplicationProperty.WEB_HTTPS_SUPPORTED.getValue())) {
            String url = redirectUrl;
            String queryString = request.getQueryString();
            if (queryString != null) {
                url += queryString;
            }
            response.sendRedirect(ControllerHelper.renderAbsoluteUrlIgnoreRequestProtocol(request,
                    null, url, true, false, false));
            return null;
        }
        return super.handleRequest(request, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean prepareModel(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws Exception {
        super.prepareModel(request, response, model);

        ClientConfigurationProperties clientProperties = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();

        model.put("userId", SecurityHelper.getCurrentUserId());
        model.put("userAlias", SecurityHelper.getCurrentUserAlias());
        model.put("showDelete", ClientConfigurationHelper.isUserDeletionAllowed());
        model.put("canChangePasswordAndEmail",
                canChangePasswordAndMail(clientProperties.getPrimaryExternalAuthentication()));
        return true;
    }

    /**
     * @param redirecUrl
     *            the redirecUrl to set
     */
    public void setRedirectUrl(String redirecUrl) {
        this.redirectUrl = redirecUrl;
    }
}
