package com.communote.server.web.fe.portal.user.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.security.AuthenticationManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.validation.UserActivationValidationException;
import com.communote.server.model.user.User;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.commons.controller.StartpageRegistry;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.external.spring.security.AuthenticationSuccessFailureFilter;
import com.communote.server.web.fe.portal.user.forms.AcceptTermsForm;

/**
 * Controller to accept the terms of use and the privacy policy. Gets a user id from session
 * attribute and shows a form with terms of use and privacy policy.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AcceptTermsController extends BaseFormController {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(AcceptTermsController.class);

    /** value for the hidden form field. */
    public final static String ACCEPT_TERMS_SEND = "acceptTermsSend";

    /** param to determine if the form was sent. */
    public final static String PARAM_SEND = "send";

    private StartpageRegistry startpageRegistry;

    /** The redirect target if the terms were accepted. */
    private String successRedirectTarget = null;

    /** The redirect target if an error occurred. */
    private String errorRedirectTarget = null;

    /**
     * Request parameter that if set will be used to redirect to in success and error case.
     */
    private String targetUrlParameter;

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AcceptTermsForm form = new AcceptTermsForm();
        return form;
    }

    /**
     * Gets the user id of the user who should accept the terms.
     *
     * @param request
     *            the request
     * @return the user id
     */
    private Long getAcceptTermsUser(HttpServletRequest request) {
        Long result = (Long) request.getSession().getAttribute(
                AuthenticationSuccessFailureFilter.SESSION_ATTR_USER_ACCEPT_TERMS);
        return result;
    }

    private String getErrorRedirectTarget() {
        if (this.errorRedirectTarget == null) {
            if (this.startpageRegistry != null) {
                return startpageRegistry.getStartpage();
            }
            return "/";
        }
        return this.errorRedirectTarget;
    }

    /**
     * Get the redirect target URL which is the one provided by the request targetUrlParameter or if
     * missing the fallback.
     *
     * @param request
     *            the current request
     * @param fallbackUrl
     *            the fallback to return if the parameter is not set
     * @return the target URL
     */
    private String getRedirectTargetUrl(HttpServletRequest request, String fallbackUrl) {
        if (targetUrlParameter != null) {
            String targetUrl = request.getParameter(targetUrlParameter);
            if (StringUtils.isNotBlank(targetUrl)) {
                return targetUrl;
            }
        }
        return fallbackUrl;
    }

    private String getSuccessRedirectTarget() {
        if (this.successRedirectTarget == null) {
            if (this.startpageRegistry != null) {
                return startpageRegistry.getStartpage();
            }
            return "/";
        }
        return this.successRedirectTarget;
    }

    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        ModelAndView result = null;
        Long userId = getAcceptTermsUser(request);
        removeUserFromSession(request);
        if (request.getParameter("_finish") != null) {
            try {
                ServiceLocator.findService(UserManagement.class).acceptTermsOfUse(userId);
                // TODO we should try to reuse the logic of
                // UsernamePasswordFormAuthenticationProcessingFilter.successfulAuthentication and
                // CommunoteAuthenticationSuccessHandler
                // call login and clear overridden locale
                SessionHandler.instance().resetOverriddenCurrentUserLocale(request);
                ServiceLocator.findService(AuthenticationManagement.class)
                .onSuccessfulAuthentication(userId);
                String targetUrl = getRedirectTargetUrl(request,
                        this.getSuccessRedirectTarget());
                sendRedirect(request, response, targetUrl);
            } catch (AuthorizationException e) {
                request.setAttribute(ControllerHelper.ATTRIBUTE_NAME_ERROR_PAGE_NEXT_TARGET,
                        getRedirectTargetUrl(request, this.getErrorRedirectTarget()));
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (UserActivationValidationException e) {
                request.setAttribute(ControllerHelper.ATTRIBUTE_NAME_ERROR_PAGE_NEXT_TARGET,
                        getRedirectTargetUrl(request, this.getErrorRedirectTarget()));
                request.setAttribute(ControllerHelper.ATTRIBUTE_NAME_ERROR_PAGE_CUSTOM_MESSAGE,
                        MessageHelper.getText(request, e.getReason("user.register.terms.error.")));
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (UserNotFoundException e) {
                String targetUrl = getRedirectTargetUrl(request, this.getErrorRedirectTarget());
                LOG.error("user with id '" + userId
                        + "' not found while accepting terms, redirect to " + targetUrl);
                sendRedirect(request, response, targetUrl);
            }
        } else {
            // assume cancel
            String cancelUrl = getRedirectTargetUrl(request,
                    ControllerHelper.renderUrl(request, "", null, false, false,
                            null, null, false, false));
            sendRedirect(request, response, cancelUrl);
        }
        return result;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView result = null;
        Long userId = getAcceptTermsUser(request);
        if (userId == null) {
            String targetUrl = getRedirectTargetUrl(request, getErrorRedirectTarget());
            LOG.debug("user id not set, redirect to " + targetUrl);
            sendRedirect(request, response, targetUrl);
        } else {
            User user = ServiceLocator.instance().getService(UserManagement.class)
                    .findUserByUserId(userId);
            if (user != null) {
                result = super.handleRequestInternal(request, response);
            } else {
                String targetUrl = getRedirectTargetUrl(request, this.getErrorRedirectTarget());
                LOG.debug("user with id '" + userId + "' not found, redirect to "
                        + targetUrl);
                removeUserFromSession(request);
                sendRedirect(request, response, targetUrl);
            }
        }
        return result;
    }

    /**
     * Removes the user id from session.
     *
     * @param request
     *            the request
     */
    private void removeUserFromSession(HttpServletRequest request) {
        request.getSession()
        .removeAttribute(AuthenticationSuccessFailureFilter.SESSION_ATTR_USER_ACCEPT_TERMS);
    }

    /**
     * send internal or normal redirect.
     *
     * @param request
     *            the current request
     * @param response
     *            the response of the current request
     * @param url
     *            relative or absolute URL to redirect to
     * @throws IOException
     *             in case the redirect failed
     */
    private void sendRedirect(HttpServletRequest request,
            HttpServletResponse response, String url) throws IOException {
        String urlLower = url.toLowerCase();
        if (urlLower.startsWith("http://") || urlLower.startsWith("https://")) {
            ControllerHelper.sendRedirect(request, response, url);
        } else {
            ControllerHelper.sendInternalRedirect(request, response, url);
        }
    }

    /**
     * Sets the error redirect target.
     *
     * @param errorTarget
     *            the new error redirect target
     */
    public void setErrorRedirectTarget(String errorTarget) {
        this.errorRedirectTarget = errorTarget;
    }

    /**
     * Set the start-page registry which will be used to resolve the redirect targets if
     * successRedirectTarget or errorRedirectTarget are undefined.
     *
     * @param startpageRegistry
     *            the registry
     */
    public void setStartpageRegistry(StartpageRegistry startpageRegistry) {
        this.startpageRegistry = startpageRegistry;
    }

    /**
     * Sets the success redirect target.
     *
     * @param successTarget
     *            the new success redirect target
     */
    public void setSuccessRedirectTarget(String successTarget) {
        this.successRedirectTarget = successTarget;
    }

    /**
     * Name of a request parameter to check for a URL to redirect to instead of the configured
     * static successRedirectTarget or errorRedirectTarget.
     *
     * @param targetUrlParameter
     *            name of the parameter that holds the target URL. The parameter name is expected to
     *            consist only of ASCII alpha-numeric characters.
     */
    public void setTargetUrlParameter(String targetUrlParameter) {
        this.targetUrlParameter = targetUrlParameter;
    }

    @Override
    protected boolean suppressValidation(HttpServletRequest request, Object command) {
        // don't validate in case of cancel
        return request.getParameter("_cancel") != null;
    }
}
