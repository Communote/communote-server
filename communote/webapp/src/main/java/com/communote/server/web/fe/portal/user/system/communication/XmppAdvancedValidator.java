package com.communote.server.web.fe.portal.user.system.communication;

import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.communote.server.web.commons.controller.GenericValidator;


/**
 * Validator for the {@link XmppController}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class XmppAdvancedValidator extends GenericValidator<XmppForm> {

    private final static Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    /**
     * {@inheritDoc}
     */
    @Override
    public void doValidate(XmppForm form, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userSuffix", "string.validation.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "blogSuffix", "string.validation.empty");

        if (!NUMBER_PATTERN.matcher(form.getPostingInterval()).matches()) {
            errors.rejectValue("postingInterval", "string.validation.numbers.positive");
        }
    }
}
