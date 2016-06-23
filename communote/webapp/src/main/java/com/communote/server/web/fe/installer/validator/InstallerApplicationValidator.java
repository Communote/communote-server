package com.communote.server.web.fe.installer.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.server.web.fe.installer.forms.InstallerForm;


/**
 * The application validator verifies some general application settings made during the
 * installation.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InstallerApplicationValidator implements Validator {

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
        // check if required fields contains content
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "accountName", "error.valueEmpty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "accountTimeZoneId", "error.valueEmpty");
    }
}
