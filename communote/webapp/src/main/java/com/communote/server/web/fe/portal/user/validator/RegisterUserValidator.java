package com.communote.server.web.fe.portal.user.validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.common.validation.EmailValidator;
import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.web.fe.portal.user.forms.RegistrationForm;

/**
 * The register user validator validates the form {@link RegistrationForm} for user registration.
 * This includes email and the password.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RegisterUserValidator implements Validator {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return RegistrationForm.class.equals(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Object target, Errors errors) {
        RegistrationForm form = (RegistrationForm) target;
        validateFirstName(errors, form);
        validateLastName(errors, form);
        validateAlias(errors, form);
        validateEmail(errors, form);
        validatePassword(errors, form);
        validateUserTimeZoneId(errors, form);
    }

    /**
     *
     * @param errors
     *            Errors.
     * @param form
     *            The form.
     */
    private void validateAlias(Errors errors, RegistrationForm form) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "alias", "error.valueEmpty");
        if (!errors.hasFieldErrors("alias")
                && !form.getAlias().matches(ValidationPatterns.PATTERN_ALIAS)) {
            errors.rejectValue("alias", "error.alias.not.valid", "The entered login is not valid!");
        }
    }

    /**
     *
     * @param errors
     *            Errors.
     * @param form
     *            The form.
     */
    private void validateEmail(Errors errors, RegistrationForm form) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "error.valueEmpty");
        if (!errors.hasFieldErrors("email")
                && !EmailValidator.validateEmailAddressByRegex(form.getEmail())) {
            errors.rejectValue("email", "error.email.not.valid",
                    "The entered email address is not valid!");
        }
    }

    /**
     *
     * @param errors
     *            The errors.
     * @param form
     *            The form containign the data.
     */
    private void validateFirstName(Errors errors, RegistrationForm form) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "error.valueEmpty");
        if (StringUtils.isNotBlank(form.getFirstName())
                && !form.getFirstName().matches(ValidationPatterns.PATTERN_FIRSTNAME)) {
            errors.rejectValue("firstName", "error.firstname.not.valid",
                    new Object[] { ValidationPatterns.FIRSTNAME_UPPER_BOUND },
                    "Your chosen forename is not valid.");
        }
    }

    /**
     *
     * @param errors
     *            The errors.
     * @param form
     *            The form containign the data.
     */
    private void validateLastName(Errors errors, RegistrationForm form) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "error.valueEmpty");
        if (StringUtils.isNotBlank(form.getLastName())
                && !form.getLastName().matches(ValidationPatterns.PATTERN_LASTNAME)) {
            errors.rejectValue("lastName", "error.lastname.not.valid",
                    new Object[] { ValidationPatterns.LASTNAME_UPPER_BOUND },
                    "Your chosen surename is not valid.");
        }
    }

    /**
     *
     * @param errors
     *            Errors.
     * @param form
     *            The form.
     */
    private void validatePassword(Errors errors, RegistrationForm form) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "error.valueEmpty");
        if (!errors.hasFieldErrors("password")
                && !StringUtils.equals(form.getPassword(), form.getPassword2())) {
            form.setPassword("");
            form.setPassword2("");
            errors.rejectValue("password", "error.passwords.are.not.matching",
                    "The passwords are not matching!");
        }
        // TODO also used in ForgottenPWValidator ,
        // should be outsourced in single helper method
        if (!errors.hasFieldErrors("password") && form.getPassword().length() < 6) {
            form.setPassword("");
            form.setPassword2("");
            errors.rejectValue("password", "error.password.must.have.at.least.6.characters",
                    "The password is too short!");
        }
    }

    /**
     * Checks that the time zone is not empty.
     *
     * @param errors
     *            Errors.
     * @param form
     *            The form.
     */
    private void validateUserTimeZoneId(Errors errors, RegistrationForm form) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "timeZoneId", "error.valueEmpty");
    }
}
