package com.communote.server.web.fe.installer.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.web.fe.installer.forms.InstallerForm;


/**
 * The mail setting validator validates the settings for outgoing messages for application
 * installation.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InstallerAdminAccountValidator implements Validator {

    /**
     * {@inheritDoc}
     */
    public boolean supports(Class clazz) {
        return InstallerForm.class.equals(clazz);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Object target, Errors errors) {
        InstallerForm form = (InstallerForm) target;

        // validate email address
        ValidationHelper.validateEmail("userEmail", form.getUserEmail(), true, errors);

        // check if firstname and lastname contains content
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userFirstName", "error.valueEmpty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userLastName", "error.valueEmpty");

        // check if language code contains content
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userLanguageCode", "error.valueEmpty");

        // check user alias
        validateAlias("userAlias", form.getUserAlias(), errors);

        // check user password
        ValidationHelper.validatePasswords("userPassword", form.getUserPassword(),
                "userPasswordConfirmation", form.getUserPasswordConfirmation(), errors);
    }

    // TODO swap this method out to a central location
    /**
     * Validate the alias of the form
     * 
     * @param fieldName
     *            the field name.
     * @param input
     *            the input of the field.
     * @param errors
     *            the error object.
     */
    private void validateAlias(String fieldName, String input, Errors errors) {

        // check if field contains content
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldName, "error.valueEmpty");

        // validate the field input against a regular expression
        if (!errors.hasFieldErrors(fieldName)
                && !input.matches(ValidationPatterns.PATTERN_ALIAS)) {
            errors.rejectValue(fieldName, "error.alias.not.valid",
                    "The entered login is not valid!");
        }
    }
}
