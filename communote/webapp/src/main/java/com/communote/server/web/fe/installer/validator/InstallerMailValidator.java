package com.communote.server.web.fe.installer.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.web.fe.installer.forms.InstallerForm;


/**
 * The mail setting validator validates the settings for outgoing messages for application
 * installation.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InstallerMailValidator implements Validator {

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

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "smtpHost", "string.validation.empty");
        ValidationHelper.validatePortNumber("smtpPort", form.getSmtpPort(), false, errors);

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "senderName", "string.validation.empty");
        ValidationHelper.validateEmail("senderAddress", form.getSenderAddress(), true, errors);

        ValidationHelper.validateEmail("supportAddress", form.getSupportAddress(), false, errors);
    }
}
