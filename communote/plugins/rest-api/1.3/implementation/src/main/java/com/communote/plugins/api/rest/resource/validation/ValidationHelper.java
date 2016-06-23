package com.communote.plugins.api.rest.resource.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ValidationHelper {

    /**
     * @param source
     *            string to be checked
     * @param regEx
     *            regular expression
     * @return list of errors
     */
    public static List<ParameterValidationError> checkAgainstRegularExpression(String source,
            String regEx) {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(source);
        if (!m.matches()) {
            ParameterValidationError error = new ParameterValidationError();
            error.setSource("nameIdentifier");
            error.setMessageKey("string.validation.no.regex.matches");
            errors.add(error);
        }
        return errors;
    }

    /**
     * @param source
     *            string to be checked
     * @param maxLength
     *            max length
     * @return list of errors
     */
    public static List<ParameterValidationError> checkMaxStringLengthLimit(String source,
            int maxLength) {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        if (source.length() > maxLength) {
            ParameterValidationError error = new ParameterValidationError();
            error.setMessageKey("string.validation.length.greater.max");
            errors.add(error);
        }
        return errors;
    }

    /**
     * @param source
     *            string to be checked
     * @param minLength
     *            max length
     * @return list of errors
     */
    public static List<ParameterValidationError> checkMinStringLengthLimit(String source,
            int minLength) {
        List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
        if (source.length() < minLength) {
            ParameterValidationError error = new ParameterValidationError();
            error.setMessageKey("string.validation.length.smaller.min");
            errors.add(error);
        }
        return errors;
    }

    /**
     * 
     */
    private ValidationHelper() {

    }

}
