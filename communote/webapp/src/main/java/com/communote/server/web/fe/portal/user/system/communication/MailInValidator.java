package com.communote.server.web.fe.portal.user.system.communication;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.EmailValidator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.web.commons.controller.GenericValidator;


/**
 * Validator for the {@link MailOutController}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MailInValidator extends GenericValidator<MailInForm> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void doValidate(MailInForm form, Errors errors) {
        validateServerSettings(form, errors);
        if (MailInController.MODE_SINGLE.equals(form.getMode())) {
            if (!EmailValidator.getInstance().isValid(form.getSingleModeAddress())) {
                errors.rejectValue("singleModeAddress", "error.email.not.valid");
            }
        } else if (MailInController.MODE_MULTI.equals(form.getMode())) {
            if (StringUtils.isBlank(form.getMultiModeDomain())) {
                errors.rejectValue("multiModeDomain", "string.validation.empty");
            }
            if (!EmailValidator.getInstance().isValid(
                    "a" + form.getMultiModeSuffix() + "@" + form.getMultiModeDomain())) {
                errors.rejectValue("multiModeDomain",
                        "client.system.communication.mail.in.mode.multi.error.match");
                errors.rejectValue("multiModeSuffix",
                        "client.system.communication.mail.in.mode.multi.error.match");
            }
        }
    }

    /**
     * Validates the server settings.
     * 
     * @param form
     *            The form.
     * @param errors
     *            The errors.
     */
    private void validateServerSettings(MailInForm form, Errors errors) {

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "server", "string.validation.empty");

        ValidationHelper.validatePortNumber("port", form.getPort(), false, errors);

        if (form.getProtocol() == null
                || !Pattern.matches("imap(s)?", form.getProtocol().getName())) {
            errors.rejectValue("protocol",
                    "client.system.communication.mail.in.server.protocol.hint");
        }
        if (StringUtils.isBlank(form.getReconnectionTimeout())
                || !form.getReconnectionTimeout().matches("\\d+")) {
            errors.rejectValue("reconnectionTimeout", "string.validation.numbers.positive");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mailbox", "string.validation.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "login", "string.validation.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "string.validation.empty");
    }
}
