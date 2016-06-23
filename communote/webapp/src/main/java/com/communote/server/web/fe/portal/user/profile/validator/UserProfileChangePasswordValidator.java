package com.communote.server.web.fe.portal.user.profile.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.web.fe.portal.user.profile.forms.UserProfileChangePasswordForm;


/**
 * The Class UserProfileChangePasswordValidator validates the input from the user profile form.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileChangePasswordValidator implements Validator {

    private boolean currentPasswordRequired = true;

    /**
     * Default constructor
     */
    public UserProfileChangePasswordValidator() {
        super();
    }

    /**
     * Constructor to define whether the current password is required
     * 
     * @param currentPasswordRequired
     *            set to false if the old one is not required to change the password
     */
    public UserProfileChangePasswordValidator(boolean currentPasswordRequired) {
        this.setCurrentPasswordRequired(currentPasswordRequired);
    }

    /**
     * @return true if the old one is required to change the password
     */
    public boolean isCurrentPasswordRequired() {
        return currentPasswordRequired;
    }

    /**
     * @param currentPasswordRequired
     *            set to false if the old one is not required to change the password
     */
    public void setCurrentPasswordRequired(boolean currentPasswordRequired) {
        this.currentPasswordRequired = currentPasswordRequired;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(@SuppressWarnings("rawtypes") Class clazz) {
        return UserProfileChangePasswordForm.class.equals(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Object target, Errors errors) {
        UserProfileChangePasswordForm form = (UserProfileChangePasswordForm) target;

        if (isCurrentPasswordRequired()) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "oldPassword",
                    "string.validation.empty");
        }
        ValidationHelper.validatePasswords("newPassword", form.getNewPassword(),
                "newPasswordConfirm", form.getNewPasswordConfirm(), errors);
    }
}
