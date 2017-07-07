package com.communote.server.web.fe.widgets.user.profile;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.exceptions.PasswordLengthException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.security.UserPasswordManagement;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.portal.user.profile.forms.UserProfileChangePasswordForm;
import com.communote.server.web.fe.portal.user.profile.validator.UserProfileChangePasswordValidator;
import com.communote.server.widgets.springmvc.SpringFormWidget;

/**
 * Widget for displaying the form to change the user password
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileChangePasswordWidget
        extends SpringFormWidget<UserProfileChangePasswordForm> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(UserProfileChangePasswordWidget.class);

    private final boolean currentPasswordRequired;

    public UserProfileChangePasswordWidget() {
        this(true);
    }

    public UserProfileChangePasswordWidget(boolean currentPasswordRequired) {
        this.currentPasswordRequired = currentPasswordRequired;
        this.setValidator(new UserProfileChangePasswordValidator(
                ServiceLocator.findService(UserPasswordManagement.class), currentPasswordRequired));
    }

    @Override
    protected UserProfileChangePasswordForm formBackingObject(HttpServletRequest request) {
        return new UserProfileChangePasswordForm();
    }

    @Override
    public String getTile(String outputType) {
        return "core.widget.user.profile.change.password";
    }

    /**
     * Return the ID of the user for which the password should be changed
     *
     * @param request
     *            the servlet request
     * @return the ID of the current user
     */
    protected Long getUserId(HttpServletRequest request) {
        return SecurityHelper.assertCurrentUserId();
    }

    /**
     * @return true if the user has to provide the old password to change it
     */
    public boolean isCurrentPasswordRequired() {
        return currentPasswordRequired;
    }

    @Override
    protected void onSubmit(HttpServletRequest request,
            UserProfileChangePasswordForm formBackingObject, BindingResult errors) {
        String newPassword = formBackingObject.getNewPassword();
        UserPasswordManagement userPasswordManagement = ServiceLocator
                .findService(UserPasswordManagement.class);
        Long userId = getUserId(request);
        if (userId == null) {
            LOGGER.error("No user ID provided");
            errors.reject("error.application.exception.unspecified", "No user ID provided");
        } else {
            if (isCurrentPasswordRequired()) {
                String oldPassword = formBackingObject.getOldPassword();
                if (oldPassword == null
                        || !userPasswordManagement.checkPassword(userId, oldPassword)) {
                    errors.rejectValue("oldPassword", "user.profile.password.error.wrong.password",
                            "The password is incorrect.");
                }
            }
        }
        if (!errors.hasErrors()) {
            try {
                userPasswordManagement.changePassword(userId, newPassword);
                MessageHelper.saveMessage(request,
                        MessageHelper.getText(request, "user.profile.password.success"));
            } catch (PasswordLengthException e) {
                errors.rejectValue("newPassword", "error.password.must.have.at.least.6.characters",
                        "The password is not long enough.");
            } catch (Exception e) {
                LOGGER.error("Updating password of user with ID " + userId + " failed", e);
                errors.rejectValue("newPassword", "user.profile.password.error",
                        "Changing the password failed!");
            }
        }

        // always reset the password
        formBackingObject.setOldPassword(null);
        formBackingObject.setNewPassword(null);
        formBackingObject.setNewPasswordConfirm(null);

    }

}
