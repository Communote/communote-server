package com.communote.server.core.helper;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.communote.common.validation.EmailValidator;
import com.communote.server.core.common.exceptions.PasswordValidationException;
import com.communote.server.core.user.security.UserPasswordManagement;
import com.communote.server.model.user.PhoneNumber;

/**
 * Helper class for validation.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ValidationHelper {

    /**
     * Contains a message property. ("string.validation.length.smaller.min")
     */
    public static final String STRING_SMALLER_MIN = "string.validation.length.smaller.min";
    /**
     * Contains a message property. ("string.validation.length.greater.max")
     */
    public static final String STRING_GREATER_MAX = "string.validation.length.greater.max";
    /**
     * Contains a message property. ("string.validation.no.regex.matches")
     */
    public static final String STRING_WRONG_REGEX_FORMAT = "string.validation.no.regex.matches";

    /** */
    public static final String STRING_WRONG_REGEX_FORMAT_ALL_LETTERS = "string.validation.no.regex.matches.letters.only";
    /** */
    public static final String STRING_WRONG_REGEX_FORMAT_ALL_LETTERS_AND_OTHER = "string.validation.no.regex.matches.letters.more";
    /** */
    public static final String STRING_WRONG_REGEX_FORMAT_ALL_DIGITS = "string.validation.no.regex.matches.digits.only";

    private static final String ALL_LETTERS_REGEX_CLASS = "\\p{L}";

    /** regex class for all digits */
    private static final String ALL_DIGIT_REGEX_CLASS = "\\d";

    /** regex for numbers only */
    public static final String REGEX_DIGITS = "[\\d]*";

    /** regex for port numbers */
    public static final String PORT_NUMBERS_REGEX = "^[\\d]{1,5}$";

    /** min length for port numbers */
    public static final int PORT_NUMBERS_MIN_LENGTH = 1;

    /** max length for port numbers */
    public static final int PORT_NUMBERS_MAX_LENGTH = 5;

    /**
     * Validates a string by regex expression
     *
     * @param input
     *            the input of a form field.
     * @param regex
     *            a regular expression for the given string, can be {@code null}
     * @param fieldName
     *            Name of the field the check is against.
     * @param errors
     *            The errors to put the error into.
     * @return The message key for the error.
     */
    private static String doValidationAgainstRegex(String input, String regex, String fieldName,
            Errors errors) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input == null ? "" : input);
        if (!matcher.matches()) {
            errors.rejectValue(fieldName, getRegexErrorMessageKey(regex),
                    new Object[] { getRegexForDisplay(regex) }, "");
            return getRegexErrorMessageKey(regex);
        }
        MatchResult res = matcher.toMatchResult();
        if (res.start() != 0 || res.end() != input.length()) {
            errors.rejectValue(fieldName, getRegexErrorMessageKey(regex),
                    new Object[] { getRegexForDisplay(regex) }, "");
            return getRegexErrorMessageKey(regex);
        }
        return null;
    }

    /**
     * Returns a suited error message key for a given regex.
     *
     * @param regex
     *            the regex
     * @return the message key
     */
    private static String getRegexErrorMessageKey(String regex) {
        String message = STRING_WRONG_REGEX_FORMAT;

        if (regex.contains(ALL_LETTERS_REGEX_CLASS)) {
            if (regex.length() > ALL_LETTERS_REGEX_CLASS.length()) {
                message = STRING_WRONG_REGEX_FORMAT_ALL_LETTERS_AND_OTHER;
            } else {
                message = STRING_WRONG_REGEX_FORMAT_ALL_LETTERS;
            }
        } else if (regex.contains(ALL_DIGIT_REGEX_CLASS)) {
            message = STRING_WRONG_REGEX_FORMAT_ALL_DIGITS;
        }

        return message;
    }

    /**
     * Modifies the regex Expression to show it the user with the characters which are allowed.
     *
     * @param regex
     *            Regex to modify
     * @return Regex without starting [ or ending ]* and without escaping characters
     */
    public static String getRegexForDisplay(String regex) {
        String str = regex.replaceAll("\\\\s", " ");
        str = str.replaceAll("\\\\", "");
        str = str.replaceFirst("\\[", "");
        str = str.substring(0, str.lastIndexOf("]"));
        str = str.replaceAll("p\\{L}", "");
        return str;
    }

    /**
     * Validate the e-mail address.
     *
     * @param fieldName
     *            the field name.
     * @param input
     *            the input of the field.
     * @param isRequired
     *            {@code true} if the input must contain a value
     * @param errors
     *            the error object.
     * @return {@code true} if the e-mail address is valid otherwise {@code false}
     */
    public static boolean validateEmail(String fieldName, String input, boolean isRequired,
            Errors errors) {

        boolean isValid = true;

        if (StringUtils.isNotBlank(input)) {
            // validate the input against a regular expression
            if (!errors.hasFieldErrors(fieldName)
                    && !EmailValidator.validateEmailAddressByRegex(input)) {
                errors.rejectValue(fieldName, "error.email.not.valid",
                        "The entered e-mail address is not valid!");
            }
        } else if (isRequired) {
            errors.rejectValue(fieldName, "string.validation.empty");
        }

        if (errors.hasFieldErrors(fieldName)) {
            isValid = false;
        }

        return isValid;
    }

    /**
     * @param userPasswordManagement
     *            the user password management service
     * @param passwordField
     *            the name of the password field.
     * @param passwordInput
     *            the password.
     * @param passwordConfirmField
     *            the name of the field to confirm the password.
     * @param passwordConfirmInput
     *            the password confirmation
     * @param errors
     *            Errors.
     * @return {@code true} if the password is valid otherwise {@code false}
     */
    public static boolean validatePasswords(UserPasswordManagement userPasswordManagement,
            String passwordField, String passwordInput, String passwordConfirmField,
            String passwordConfirmInput, Errors errors) {

        boolean isValid = true;

        // check if field contains content
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, passwordField, "string.validation.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, passwordConfirmField,
                "string.validation.empty");

        // check if both strings are equal
        if (!errors.hasFieldErrors(passwordField)
                && !StringUtils.equals(passwordInput, passwordConfirmInput)) {

            errors.rejectValue(passwordField, "error.passwords.are.not.matching",
                    "The passwords are not matching!");
        }

        if (!errors.hasFieldErrors(passwordField)) {

            try {
                userPasswordManagement.validatePassword(passwordInput);
            } catch (PasswordValidationException e) {
                errors.rejectValue(passwordField, "error.password.must.have.at.least.6.characters",
                        "The password is too short!");
            }
        }

        if (errors.hasFieldErrors(passwordField)) {
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validates a phone number.
     *
     * @param phoneNumber
     *            the phone number to validate
     * @param field
     *            the field to check
     * @param errors
     *            the erros
     * @param required
     *            If set, a number must be set, else a number can be set.
     */
    public static void validatePhoneNumber(PhoneNumber phoneNumber, String field, Errors errors,
            boolean required) {
        if (phoneNumber == null) {
            return;
        }
        String pattern = "[\\d]" + (required ? "+" : "*");
        validateStringByRegex(phoneNumber.getCountryCode(), pattern, field, errors);
        // to show the error message only once
        if (!errors.hasFieldErrors(field)) {
            validateStringByRegex(phoneNumber.getAreaCode(), pattern, field, errors);
        }
        // to show the error message only once
        if (!errors.hasFieldErrors(field)) {
            validateStringByRegex(phoneNumber.getPhoneNumber(), pattern, field, errors);
        }
    }

    /**
     * Validate port numbers. Checks if the input consists only of digits and if the number is a
     * value between 0 and 65535.
     *
     * @param fieldName
     *            the field name.
     * @param input
     *            the input of the field.
     * @param isRequired
     *            {@code true} if the input must contain a value
     * @param errors
     *            the error object.
     * @return {@code true} if the port number is valid otherwise {@code false}
     */
    public static boolean validatePortNumber(String fieldName, String input, boolean isRequired,
            Errors errors) {

        if (StringUtils.isNotBlank(input)) {
            // only digits allowed
            if (!errors.hasFieldErrors(fieldName)) {
                validateStringByRegex(input, ValidationHelper.REGEX_DIGITS, fieldName, errors);
            }

            if (!errors.hasFieldErrors(fieldName)) {
                int port = NumberUtils.toInt(input, -1);

                if (port < 0 || port > 65535) {
                    errors.rejectValue(fieldName, "string.validation.numbers.port");
                }
            }
        } else if (isRequired) {
            errors.rejectValue(fieldName, "string.validation.numbers.port");
        }

        // return the success of the validation process
        return !errors.hasFieldErrors(fieldName);
    }

    /**
     * Validates a string.
     *
     * @param field
     *            the field
     * @param errors
     *            the errors
     * @param pattern
     *            The regular expression to use.
     * @param minLength
     *            Minimal length of the field.
     * @param maxLength
     *            Maximal length of the field.
     */
    public static void validateString(String field, Errors errors, String pattern, int minLength,
            int maxLength) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, field, "error.valueEmpty");
        if (!errors.hasFieldErrors(field)) {
            validateString(field, (String) errors.getFieldValue(field), true, maxLength, minLength,
                    pattern, errors);
        }
    }

    /**
     * Validates a string.
     *
     * @param fieldName
     *            the field name.
     * @param input
     *            the input of the field.
     * @param isRequired
     *            {@code true} if the input must contain a value
     * @param maxLen
     *            the max length of chars, can be a negative number to skip this test
     * @param minLen
     *            the minimum length of chars, can be zero to skip this test
     * @param regex
     *            A regular expression for the given string, can be {@code null} to skip this test
     * @param errors
     *            the error object (reference to a {@code BindException} object).
     *
     * @return {@code true} if the string is valid otherwise {@code false}
     */
    public static boolean validateString(String fieldName, String input, boolean isRequired,
            int maxLen, int minLen, String regex, Errors errors) {

        if (StringUtils.isNotBlank(input)) {
            if (minLen > 0 && input.length() < minLen) {
                errors.rejectValue(fieldName, STRING_SMALLER_MIN, new Object[] { minLen }, "");
            } else if (maxLen >= 0 && input.length() > maxLen) {
                errors.rejectValue(fieldName, STRING_GREATER_MAX, new Object[] { maxLen }, "");
            } else if (regex != null) {
                doValidationAgainstRegex(input, regex, fieldName, errors);
            }

            // return the success of the validation process
            return !errors.hasFieldErrors(fieldName);
        }

        // if field is blank but required
        if (isRequired) {
            // hint for the user that the field is mandatory
            if (minLen > 0) {
                errors.rejectValue(fieldName, STRING_SMALLER_MIN, new Object[] { minLen }, "");
            } else {
                errors.rejectValue(fieldName, "string.validation.empty");
            }
        }

        // return the success of the validation process
        return !errors.hasFieldErrors(fieldName);
    }

    /**
     * Validates a string by regular expression.
     *
     * @param inputStr
     *            String
     * @param regex
     *            A regular expression for the given string, can be null
     * @param field
     *            the field name (may be null or empty String)
     * @param errors
     *            Reference to a BindException object. If this parameter null then field will be not
     *            rejected
     * @return Returns the message property (error code) or null
     */
    public static String validateStringByRegex(String inputStr, String regex, String field,
            Errors errors) {
        return doValidationAgainstRegex(inputStr, regex, field, errors);
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private ValidationHelper() {
        // Do nothing
    }
}
