package com.communote.server.web.fe.portal.user.system.communication;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.server.core.helper.ValidationHelper;

/**
 * Validator for the {@link MailOutController}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MailOutValidator implements Validator {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        return MailOutForm.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Object target, Errors errors) {
        MailOutForm form = (MailOutForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "server", "string.validation.empty");
        ValidationHelper.validatePortNumber("port", form.getPort(), false, errors);

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "senderName", "string.validation.empty");
        ValidationHelper.validateEmail("senderAddress", form.getSenderAddress(), true, errors);
    }
}
