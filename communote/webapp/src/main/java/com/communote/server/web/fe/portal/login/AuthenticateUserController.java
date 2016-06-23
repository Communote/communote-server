package com.communote.server.web.fe.portal.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.communote.server.core.security.SecurityHelper;

/**
 * Controller that shows the login form or in case of an authentication failure a login failed view.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AuthenticateUserController extends AbstractController {
    private String loginView;
    private String loginFailedView;

    private AuthenticationSuccessHandler authenticationSuccessHandler;

    public AuthenticationSuccessHandler getAuthenticationSuccessHandler() {
        return authenticationSuccessHandler;
    }

    /**
     * @return the loginFailedView
     */
    public String getLoginFailedView() {
        return loginFailedView;
    }

    /**
     * @return the loginView
     */
    public String getLoginView() {
        return loginView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView mav = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (SecurityHelper.getCurrentUserId() != null && authentication != null) {

            /**
             * if an authenticated user got that far handle it as authentication success (which some
             * authentication filters do themselves)
             * 
             * this handle will redirect to the home page or to the target url if provided
             */
            authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        } else {
            HttpSession session = request.getSession(false);
            if (session != null
                    && session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) != null) {
                // remove attribute for further calls
                session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
                mav = new ModelAndView(getLoginFailedView());
            } else {
                mav = new ModelAndView(getLoginView());

            }
        }
        return mav;
    }

    public void setAuthenticationSuccessHandler(
            AuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    /**
     * @param loginFailedView
     *            the loginFailedView to set
     */
    public void setLoginFailedView(String loginFailedView) {
        this.loginFailedView = loginFailedView;
    }

    /**
     * @param loginView
     *            the loginView to set
     */
    public void setLoginView(String loginView) {
        this.loginView = loginView;
    }

}
