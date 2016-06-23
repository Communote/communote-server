package com.communote.server.web.fe.portal.user.system.communication;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.web.commons.controller.GenericValidator;

/**
 * Validator for the {@link XmppController}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class XmppValidator extends GenericValidator<XmppForm> {
    /**
     * {@inheritDoc}
     */
    @Override
    public void doValidate(XmppForm form, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "server", "string.validation.empty");
        ValidationHelper.validatePortNumber("port", form.getPort(), true, errors);

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "login", "string.validation.empty");
        if (form.isPasswordChanged()) {
            ValidationUtils
                    .rejectIfEmptyOrWhitespace(errors, "password", "string.validation.empty");
        }
    }
}
