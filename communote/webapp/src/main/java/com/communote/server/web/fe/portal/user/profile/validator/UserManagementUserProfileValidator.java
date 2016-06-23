package com.communote.server.web.fe.portal.user.profile.validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.persistence.user.UserProfileVO;
import com.communote.server.web.fe.portal.user.profile.forms.UserProfileForm;

/**
 * The Class UserManagementUserProfileValidator validates the input from the user profile formular.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserManagementUserProfileValidator implements Validator {

    /** The Constant MAX_INPUT_STRING_LENGTH. */
    private static final int MAX_INPUT_STRING_LENGTH = 1024;

    /**
     * {@inheritDoc}
     */
    public boolean supports(@SuppressWarnings("rawtypes") Class clazz) {
        return UserProfileForm.class.equals(clazz);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Object target, Errors errors) {
        UserProfileForm form = (UserProfileForm) target;

        UserProfileVO profile = form.getUserProfile();
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userProfile.firstName",
                "error.valueEmpty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userProfile.lastName",
                "error.valueEmpty");

        validateString(profile.getFirstName(), "userProfile.firstName", errors);
        validateString(profile.getLastName(), "userProfile.lastName", errors);
        validateString(profile.getSalutation(), "userProfile.salutation", errors);
        validateString(profile.getPosition(), "userProfile.position", errors);
        validateString(profile.getCompany(), "userProfile.company", errors);

        validateString(profile.getStreet(), "userProfile.street", errors);
        validateString(profile.getZip(), "userProfile.zip", errors);
        validateString(profile.getCity(), "userProfile.city", errors);
        ValidationHelper.validatePhoneNumber(profile.getPhone(), "phone", errors, false);
        ValidationHelper.validatePhoneNumber(profile.getFax(), "fax", errors, false);
        if (StringUtils.isEmpty(form.getLanguageCode())) {
            errors.rejectValue("languageCode", "error.valueEmpty");
        }
    }

    /**
     * @param toValidate
     *            the string to valdiate
     * @param key
     *            the key/field of validation
     * @param errors
     *            the errors
     */
    private void validateString(String toValidate, String key, Errors errors) {
        if (toValidate != null) {
            ValidationHelper.validateString(key, toValidate, false,
                    MAX_INPUT_STRING_LENGTH, 0, ValidationPatterns.DEFAULT_REGEX, errors);
        }
    }
}
