package com.communote.server.web.fe.portal.user.client.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.web.fe.portal.user.client.forms.ClientGeneralSettingsForm;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClientGeneralSettingsValidator implements Validator {
    private static final int MAX_INPUT_STRING_LENGTH = 255;
    private static final int MIN_INPUT_STRING_LENGTH = 1;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(ClientGeneralSettingsForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ClientGeneralSettingsForm form = (ClientGeneralSettingsForm) target;
        ValidationHelper.validateString("clientName", form.getClientName(), true,
                MAX_INPUT_STRING_LENGTH, MIN_INPUT_STRING_LENGTH,
                ValidationPatterns.Client.PATTERN_CLIENT_NAME, errors);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "timeZoneId", "error.valueEmpty");
    }

}
