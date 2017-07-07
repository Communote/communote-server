package com.communote.server.web.fe.portal.user.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.common.exceptions.PasswordLengthException;
import com.communote.server.core.user.ExternalUserPasswordChangeNotAllowedException;
import com.communote.server.core.user.security.UserPasswordManagement;
import com.communote.server.persistence.common.security.SecurityCodeNotFoundException;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.fe.portal.user.forms.ForgottenPWForm;

/**
 * Controller to request a new and confirm a password
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ForgottenPWController extends BaseFormController {

    private static final String MSG_KEY_NO_SECURITYCODE_FOUND = "user.forgotten.password.no.securitycode.found";

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
            ForgottenPWForm form, String secCode) throws Exception {
        ModelAndView mav = null;
        if (secCode == null) {
            errors.rejectValue("password1", MSG_KEY_NO_SECURITYCODE_FOUND,
                    "The given security code is null");
        } else {
            try {
                ServiceLocator.instance().getService(UserPasswordManagement.class)
                        .changePassword(secCode, form.getPassword());
                mav = new ModelAndView(getSuccessView());
                MessageHelper.saveMessageFromKey(request,
                        "user.forgotten.password.changing.successful");

            } catch (SecurityCodeNotFoundException e) {
                errors.rejectValue("password1", MSG_KEY_NO_SECURITYCODE_FOUND,
                        "The given security code does not exist");
            } catch (PasswordLengthException e) {
                errors.rejectValue("password", "error.password.must.have.at.least.6.characters",
                        "The password is too short!");
                mav = showForm(request, errors, getFormView());
            }
        }
        return mav;
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
                String code = request.getParameter("code");
                mav = confirmNewPassword(request, errors, form, code);
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
        UserPasswordManagement userPasswordManagement = ServiceLocator.instance()
                .getService(UserPasswordManagement.class);
        try {
            userPasswordManagement.requestPasswordChange(form.getEmail());
            mav = new ModelAndView(getSuccessView());
            MessageHelper.saveMessageFromKey(request,
                    "user.forgotten.password.email.sending.successful");
        } catch (ExternalUserPasswordChangeNotAllowedException e) {
            errors.rejectValue("email", "user.forgotten.password.user.is.external",
                    "No user for this email");
        } catch (UserNotFoundException e) {
            errors.rejectValue("email", "user.forgotten.password.user.not.exist",
                    "No user for this email");
        }
        return mav;
    }
}
