package com.communote.server.web.fe.portal.user.validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.common.validation.EmailValidator;
import com.communote.server.web.fe.portal.user.forms.ForgottenPWForm;

/**
 * Validator for the forgotten password use case.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ForgottenPWValidator implements Validator {

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.validation.Validator#supports(Class)
     */
    @Override
    public boolean supports(Class clazz) {
        return ForgottenPWForm.class.equals(clazz);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.validation.Validator#validate(Object,
     *      org.springframework.validation.Errors)
     */
    @Override
    public void validate(Object target, Errors errors) {
        ForgottenPWForm form = (ForgottenPWForm) target;
        if (StringUtils.equals(ForgottenPWForm.SEND_PW_LINK, form.getAction())) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "error.valueEmpty");
            if (!errors.hasFieldErrors("email")
                    && !EmailValidator.validateEmailAddressByRegex(form.getEmail())) {
                errors.rejectValue("email", "error.email.not.valid",
                        "The entered email address is not valid!");
            }
        } else if (StringUtils.equals(ForgottenPWForm.CONFIRM_NEW_PASSWORD, form.getAction())) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "error.valueEmpty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password2", "error.valueEmpty");
            if (!errors.hasErrors() && !StringUtils.equals(form.getPassword(), form.getPassword2())) {
                errors.rejectValue("password", "user.forgotten.password.changing.error",
                        "Password changing error");
                form.setPassword("");
                form.setPassword2("");
            }
            // TODO see RegisterUserValidator
            if (!errors.hasFieldErrors("password") && form.getPassword().length() < 6) {
                errors.rejectValue("password", "error.password.must.have.at.least.6.characters",
                        "The password is too short!");
                form.setPassword("");
                form.setPassword2("");
            }
        }

    }

}
