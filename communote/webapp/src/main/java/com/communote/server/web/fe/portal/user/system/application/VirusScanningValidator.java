package com.communote.server.web.fe.portal.user.system.application;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.web.commons.controller.GenericValidator;


/**
 * Validator for the {@link VirusScanningController}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class VirusScanningValidator extends GenericValidator<VirusScanningForm> {
    /**
     * {@inheritDoc}
     */
    @Override
    public void doValidate(VirusScanningForm form, Errors errors) {

        if (SupportedVirusScannerTypes.CLAMAV.equals(form.getScannerType())) {
            ValidationUtils
                    .rejectIfEmptyOrWhitespace(errors, "clamHost", "string.validation.empty");
            ValidationHelper.validatePortNumber("clamPort", form.getClamPort(), true, errors);

            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "clamTempDir",
                    "string.validation.empty");

            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "clamConnectionTimeout",
                    "string.validation.empty");
            if (!errors.hasFieldErrors("clamConnectionTimeout")) {
                // only digits allowed
                ValidationHelper.validateStringByRegex(form.getClamConnectionTimeout(),
                        ValidationHelper.REGEX_DIGITS, "clamConnectionTimeout", errors);
            }
        }

        if (SupportedVirusScannerTypes.CMDLINE.equals(form.getScannerType())) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cmdCommand",
                    "string.validation.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cmdExitCode",
                    "string.validation.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cmdProcessTimeout",
                    "string.validation.empty");
            if (!errors.hasFieldErrors("cmdProcessTimeout")) {
                // only digits allowed
                ValidationHelper.validateStringByRegex(form.getCmdProcessTimeout(),
                        ValidationHelper.REGEX_DIGITS, "cmdProcessTimeout", errors);
            }
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cmdTempDir",
                    "string.validation.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cmdTempFilePrefix",
                    "string.validation.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cmdTempFileSuffix",
                    "string.validation.empty");
        }

    }
}
