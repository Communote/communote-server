package com.communote.server.web.fe.portal.user.client.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.common.validation.EmailValidator;
import com.communote.server.web.fe.portal.user.client.forms.ClientProfileEmailForm;

/**
 * Validator for the email configuration.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ClientProfileEmailValidator implements Validator {
    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.validation.Validator#supports(Class)
     */
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(ClientProfileEmailForm.class);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.validation.Validator#validate(Object,
     *      org.springframework.validation.Errors)
     */
    public void validate(Object target, Errors errors) {
        ClientProfileEmailForm form = (ClientProfileEmailForm) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "clientEmail", "error.valueEmpty");
        if (!errors.hasFieldErrors("clientEmail")) {
            if (!EmailValidator.validateEmailAddressByRegex(form.getClientEmail())) {
                errors.rejectValue("clientEmail", "error.email.not.valid",
                        "The entered email address is not valid!");
            }
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "clientEmailName", "error.valueEmpty");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "clientSupportEmailAddress",
                "error.valueEmpty");
        if (!errors.hasFieldErrors("clientSupportEmailAddress")) {
            if (!EmailValidator.validateEmailAddressByRegex(form.getClientSupportEmailAddress())) {
                errors.rejectValue("clientSupportEmailAddress", "error.email.not.valid",
                        "The entered email address is not valid!");
            }
        }
    }
}
