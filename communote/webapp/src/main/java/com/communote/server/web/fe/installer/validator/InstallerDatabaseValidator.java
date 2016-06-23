package com.communote.server.web.fe.installer.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.web.fe.installer.forms.InstallerForm;

/**
 * The database validator validates the database selection form for application installation.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InstallerDatabaseValidator implements Validator {

    /**
     * {@inheritDoc}
     */
    public boolean supports(Class<?> clazz) {
        return InstallerForm.class.equals(clazz);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Object target, Errors errors) {
        InstallerForm form = (InstallerForm) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "databaseType", "error.valueEmpty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "databaseHost", "error.valueEmpty");
        ValidationHelper.validatePortNumber("databasePort", form.getDatabasePort(), false, errors);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "databaseName", "error.valueEmpty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "databaseUser", "error.valueEmpty");
    }
}
