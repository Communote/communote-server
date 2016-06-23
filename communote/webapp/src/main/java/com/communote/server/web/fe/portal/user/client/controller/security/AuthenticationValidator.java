package com.communote.server.web.fe.portal.user.client.controller.security;

import java.util.regex.Pattern;

import org.springframework.validation.Errors;

import com.communote.server.web.commons.controller.GenericValidator;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AuthenticationValidator extends GenericValidator<AuthenticationForm> {

    private final static Pattern POSITIVE_NUMBER = Pattern.compile("\\d+");

    private final static Pattern NUMBER_1_TO_3 = Pattern.compile("[1-3]");

    /**
     * {@inheritDoc}
     */
    @Override
    public void doValidate(AuthenticationForm form, Errors errors) {
        if (!POSITIVE_NUMBER.matcher(form.getFailedAttemptsBeforeTemporaryLock()).matches()) {
            errors.rejectValue("failedAttemptsBeforeTemporaryLock",
                    "string.validation.numbers.positive");
        }
        if (!POSITIVE_NUMBER.matcher(form.getFailedAttemptsBeforePermanentLock()).matches()) {
            errors.rejectValue("failedAttemptsBeforePermanentLock",
                    "string.validation.numbers.positive");
        }
        if (!POSITIVE_NUMBER.matcher(form.getLockInterval()).matches()) {
            errors.rejectValue("lockInterval", "string.validation.numbers.positive");
        }
        if (!NUMBER_1_TO_3.matcher(form.getRiskLevel()).matches()) {
            errors.rejectValue("riskLevel", "client.security.authentication.error.risk.level");
        }
    }

}
