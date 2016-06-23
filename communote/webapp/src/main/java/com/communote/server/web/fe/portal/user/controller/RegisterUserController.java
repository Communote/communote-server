package com.communote.server.web.fe.portal.user.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagement.RegistrationType;
import com.communote.server.model.user.User;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Controller to register a user by given email.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RegisterUserController extends AbstractController {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterUserController.class);

    /** The Constant PARAM_EMAIL. */
    public final static String PARAM_EMAIL = "email";

    /** The view. */
    private String view;

    /**
     * Gets the view.
     *
     * @return the view
     */
    public String getView() {
        return view;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String email = request.getParameter(PARAM_EMAIL);
        if (ServletRequestUtils.getBooleanParameter(request, "resend", false)) {
            User user = ServiceLocator.instance().getService(UserManagement.class)
                    .findUserByEmailAlias(email);
            if (user != null) {
                email = user.getEmail();
            }
        }
        ResourceBundleManager resources = ResourceBundleManager.instance();
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        if (StringUtils.isBlank(email)) {
            LOGGER.debug("No email found.");
            MessageHelper.saveErrorMessage(request,
                    resources.getText("error.email.not.valid", locale));
        } else if (!CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().isRegistrationAllowed()) {
            LOGGER.debug("Registration is disabled.");
            MessageHelper.saveErrorMessage(request,
                    resources.getText("error.local.registration.disabled", locale));
        } else {
            try {
                ServiceLocator.instance().getService(UserManagement.class)
                .registerUser(email, locale, RegistrationType.SELF);
                MessageHelper.saveMessage(request,
                        resources.getText("user.register.success", locale));
            } catch (EmailValidationException e) {
                MessageHelper.saveErrorMessage(request,
                        resources.getText("error.email.not.valid", locale));
            } catch (EmailAlreadyExistsException e) {
                MessageHelper.saveErrorMessage(request,
                        resources.getText("error.email.already.exists", locale));
            }
        }
        request.setAttribute("pageTitle", resources.getText("blog.message.page.title", locale));
        return ControllerHelper.replaceModuleInMAV(new ModelAndView(getView()));
    }

    /**
     * Sets the view.
     *
     * @param view
     *            the new view
     */
    public void setView(String view) {
        this.view = view;
    }

}
