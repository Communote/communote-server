package com.communote.plugins.api.rest.resource.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.communote.plugins.api.rest.resource.DefaultParameter;
import com.communote.plugins.api.rest.resource.validation.DefaultValidator;
import com.communote.plugins.api.rest.resource.validation.ParameterValidationError;
import com.communote.plugins.api.rest.resource.validation.ParameterValidationException;
import com.communote.plugins.api.rest.resource.validation.ValidationHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserResourceValidator
        extends
        DefaultValidator<DefaultParameter, EditUserParameter,
        DefaultParameter, GetUserParameter, GetCollectionUserParameter> {

    /** The Constant DEFAULT_REGEX. */
    private static final String DEFAULT_REGEX = "[\\p{L}\\-\\.\\(\\) 0-9]*";

    /** The Constant MAX_INPUT_STRING_LENGTH. */
    private static final int MAX_INPUT_STRING_LENGTH = 1024;

    /**
     * checks string against length limits and regular expression
     * 
     * @param string
     *            string to be checked
     * @return list of errors, that have been found
     */
    private List<ParameterValidationError> checkLengthLimitsAndAgainstRegEx(String string) {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        if (StringUtils.isNotBlank(string)) {
            errors.addAll(ValidationHelper.checkMaxStringLengthLimit(string,
                    MAX_INPUT_STRING_LENGTH));
            errors.addAll(ValidationHelper.checkAgainstRegularExpression(string, DEFAULT_REGEX));
        }
        return errors;
    }

    /**
     * @param name
     *            The name to check
     * @param field
     *            The field this error is for.
     * @return list of errors, found during the last name checking
     */
    private List<ParameterValidationError> checkName(String name, String field) {
        List<ParameterValidationError> errors = checkLengthLimitsAndAgainstRegEx(name);
        for (ParameterValidationError parameterValidationError : errors) {
            parameterValidationError.setSource(field);
        }
        return errors;
    }

    @Override
    public void validateEdit(EditUserParameter editPrameter) throws ParameterValidationException {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        errors.addAll(checkName(editPrameter.getFirstName(), "firstName"));
        errors.addAll(checkName(editPrameter.getLastName(), "lastName"));
        if (errors.size() > 0) {
            ParameterValidationException exception = new ParameterValidationException();
            exception.setErrors(errors);
            throw exception;
        }
    }
}
