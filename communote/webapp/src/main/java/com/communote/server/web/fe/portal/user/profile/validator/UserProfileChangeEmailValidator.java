package com.communote.server.web.fe.portal.user.profile.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.web.fe.portal.user.profile.forms.UserProfileChangeEmailForm;

/**
 * The Class UserProfileChangeEmailValidator validates the input from the user profile form.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileChangeEmailValidator implements Validator {

    /**
     * {@inheritDoc}
     */
    public boolean supports(Class<?> clazz) {
        return UserProfileChangeEmailForm.class.equals(clazz);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Object target, Errors errors) {
        UserProfileChangeEmailForm form = (UserProfileChangeEmailForm) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "string.validation.empty");
        ValidationHelper.validateEmail("newEmail", form.getNewEmail(), true, errors);
    }
}
