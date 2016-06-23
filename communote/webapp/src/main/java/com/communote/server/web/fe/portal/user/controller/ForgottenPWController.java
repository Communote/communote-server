package com.communote.server.web.fe.portal.user.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.SecurityCodeManagement;
import com.communote.server.core.common.exceptions.PasswordLengthException;
import com.communote.server.core.user.ExternalUsersMayNotChangeTheirPasswordException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.ForgottenPasswordSecurityCode;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.fe.portal.user.forms.ForgottenPWForm;

/**
 * Controller to request a new and confirm a password
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ForgottenPWController extends BaseFormController {

    /**
     * @param request
     *            the request
     * @param errors
     *            the errors
     * @param form
     *            the form with the password
     * @param secCode
     *            the security code
     * @return the mav to show, is null on error
     * @throws Exception
     *             in case of an show form error
     */
    private ModelAndView confirmNewPassword(HttpServletRequest request, BindException errors,
            ForgottenPWForm form, SecurityCode secCode) throws Exception {
        ModelAndView mav = null;
        try {
            User user = secCode.getUser();
            // assume the pasword is plain, if not provided the code to the
            // cgange password
            // method
            ServiceLocator.instance().getService(UserManagement.class).changePassword(user.getId(),
                    form.getPassword());
            // mav = new ModelAndView(getFormView(), getCommandName(),
            // command);
            mav = new ModelAndView(getSuccessView());
            MessageHelper.saveMessage(request, ResourceBundleManager.instance().getText(
                    "user.forgotten.password.changing.successful", user.getLanguageLocale()));
            ServiceLocator.instance().getService(SecurityCodeManagement.class)
                    .deleteAllCodesByUser(
                            user.getId(), ForgottenPasswordSecurityCode.class);
        } catch (PasswordLengthException e) {
            errors.rejectValue("password", "error.password.must.have.at.least.6.characters",
                    "The password is too short!");
            mav = showForm(request, errors, getFormView());
        }

        return mav;
    }

    /**
     * Extracts the security code from the request object.
     * 
     * @param request
     *            the request
     * @return the security code or null if not found
     */
    private SecurityCode extractSecurityCode(HttpServletRequest request) {
        SecurityCode secCode = null;
        String code = request.getParameter("code");
        if (StringUtils.isNotBlank(code)) {
            secCode = ServiceLocator.instance().getService(SecurityCodeManagement.class)
                    .findByCode(code);
        }
        return secCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        String action = request.getParameter("pwaction");
        if (ForgottenPWForm.SEND_PW_LINK.equals(action)
                || ForgottenPWForm.CONFIRM_NEW_PASSWORD.equals(action)) {
            return new ForgottenPWForm();
        } else {
            throw new NotFoundException("Action " + action + " is not supported");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        ModelAndView mav = null;
        ForgottenPWForm form = (ForgottenPWForm) command;
        if (!errors.hasErrors()) {
            if (StringUtils.equals(ForgottenPWForm.SEND_PW_LINK, form.getAction())) {
                mav = sendPasswordLink(request, errors, form);
            } else if (StringUtils.equals(ForgottenPWForm.CONFIRM_NEW_PASSWORD, form.getAction())) {
                SecurityCode secCode = extractSecurityCode(request);
                if (secCode != null) {
                    mav = confirmNewPassword(request, errors, form, secCode);
                } else {
                    errors.rejectValue("password1",
                            "user.forgotten.password.no.securitycode.found",
                            "The given security code is null");
                }
            }
        }
        if (errors.hasErrors()) {
            mav = showForm(request, errors, getFormView());
        }
        if (mav == null) {
            mav = new ModelAndView(getFormView());

        }
        return mav;
    }

    /**
     * Sends the password link to the user
     * 
     * @param request
     *            the request
     * @param errors
     *            the errors
     * @param form
     *            the formular
     * @return the mav to show, is null on error
     */
    private ModelAndView sendPasswordLink(HttpServletRequest request, BindException errors,
            ForgottenPWForm form) {
        ModelAndView mav = null;
        UserManagement userManagement = ServiceLocator.instance().getService(UserManagement.class);
        User user = userManagement.findUserByEmail(
                form.getEmail());
        if (user != null) {
            try {
                userManagement.sendNewPWLink(user);
                mav = new ModelAndView(getSuccessView());
                MessageHelper.saveMessage(request, ResourceBundleManager.instance().getText(
                        "user.forgotten.password.email.sending.successful",
                        user.getLanguageLocale()));
            } catch (ExternalUsersMayNotChangeTheirPasswordException e) {
                errors.rejectValue("email", "user.forgotten.password.user.is.external",
                        "No user for this email");
            }
        } else {
            errors.rejectValue("email", "user.forgotten.password.user.not.exist",
                    "No user for this email");
        }
        return mav;
    }
}
