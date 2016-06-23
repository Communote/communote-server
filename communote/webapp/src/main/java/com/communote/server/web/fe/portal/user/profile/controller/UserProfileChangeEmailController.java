package com.communote.server.web.fe.portal.user.profile.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.encryption.HashCodeGenerator;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.User;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.fe.portal.user.profile.forms.UserProfileChangeEmailForm;

/**
 * Controller for changing the user e-mail.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileChangeEmailController extends BaseFormController {

    /**
     * 
     * @param request
     *            the servlet request
     * @param response
     *            the servlet response
     * @param form
     *            the form
     * @param userId
     *            the ID of the user
     * @param errors
     *            object to bind errors
     * @return the new model and view or null in case of exception
     */
    private ModelAndView doConfigUser(HttpServletRequest request, HttpServletResponse response,
            UserProfileChangeEmailForm form, Long userId, BindException errors) {
        ModelAndView mav = null;
        User user = ServiceLocator.instance().getService(UserManagement.class)
                .findUserByUserId(userId);

        String confirm = user.getPassword();
        String password = form.getPassword();

        // checking process is copied from
        // DatabaseAuthenticationProvider.assertPasswords(String, String, String)
        if (!StringUtils.equals(confirm, password)) {
            // in case it was a plain text password try the md5 one
            if (!StringUtils.equals(confirm, HashCodeGenerator.generateMD5HashCode(password))) {
                errors.rejectValue("password", "user.profile.email.error.wrong.password",
                        "The password is incorrect.");
                ControllerHelper.setApplicationFailure(response);
            }
        }
        if (!errors.hasErrors()) {
            try {
                ServiceLocator.instance().getService(UserManagement.class)
                        .changeEmailAddress(userId, form.getNewEmail(), true);
                MessageHelper.saveMessageFromKey(request, "user.profile.email.success");
                form.setNewEmail(null);

                mav = new ModelAndView(getFormView(), getCommandName(), form);
            } catch (EmailValidationException e) {
                errors.rejectValue("newEmail", "error.email.not.valid",
                        "The email has the wrong format");
                ControllerHelper.setApplicationFailure(response);
            } catch (EmailAlreadyExistsException e) {
                errors.rejectValue("newEmail", "error.email.already.exists",
                        "The email you enter already exists.");
                ControllerHelper.setApplicationFailure(response);
            }
        }

        // always reset the password
        form.setPassword(null);

        return mav;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        SecurityHelper.assertCurrentUserId();
        return new UserProfileChangeEmailForm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        Long userId = SecurityHelper.assertCurrentUserId();

        UserProfileChangeEmailForm form = (UserProfileChangeEmailForm) command;

        ModelAndView mav = null;

        if (userId != null) {
            mav = doConfigUser(request, response, form, userId, errors);
        }

        if (errors.getErrorCount() > 0 || mav == null) {
            mav = showForm(request, errors, getFormView());
        }

        return mav;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView processFormSubmission(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors) throws Exception {
        if (errors.hasErrors()) {
            ControllerHelper.setApplicationFailure(response);
        }
        return super.processFormSubmission(request, response, command, errors);
    }
}
