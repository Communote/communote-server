package com.communote.common.validation;

import java.util.regex.Pattern;

/**
 * Static class for eMail validation
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class EmailValidator {


    // German umlauts in Unicode.
    private static final String UMLAUTE = "\u00E4\u00FC\u00F6\u00C4\u00DC\u00D6";

    private static final String REG_EXPR_DOMAIN_PART = "[a-zA-Z0-9" + UMLAUTE
            + "][a-zA-Z0-9\\-\\_" + UMLAUTE + "]*" + "(\\.[a-zA-Z0-9\\-\\_"
            + UMLAUTE + "]+)*[a-zA-Z0-9" + UMLAUTE + "]|[a-zA-Z0-9" + UMLAUTE
            + "]";

    private static final Pattern PATTERN_LOCAL_PART = Pattern
            .compile("[0-9a-zA-Z!#$%&'*+\\-=?^_`{}|~]+(\\.[0-9a-zA-Z!#$%&'*+\\-=?^_`{}|~]+)*");
    private static final Pattern PATTERN_DOMAIN_PART = Pattern
            .compile(REG_EXPR_DOMAIN_PART);

    /** the email local part (before '@') must not exceed 64 characters! */
    public static final int MAX_LENGTH_LOCAL_PART = 64;

    /** The maximal size of the domain part. */
    public static final int MAX_LENGTH_DOMAIN_PART = 255;

    public static final int MAX_SAFE_LENGTH_LOCAL_PART = MAX_LENGTH_LOCAL_PART - 10;

    /**
     * Validating a email address with Regular expressions
     * 
     * @param email
     *            The email to check
     * @return if the email address format correct then returns true
     */
    public static boolean validateEmailAddressByRegex(String email) {
        String[] split = email.split("@");
        if (split.length != 2 || split[0].length() > MAX_LENGTH_LOCAL_PART
                || split[1].length() > MAX_LENGTH_DOMAIN_PART) {
            return false;
        }
        return PATTERN_LOCAL_PART.matcher(split[0]).matches()
                && PATTERN_DOMAIN_PART.matcher(split[1]).matches();
    }

    /**
     * Do not construct me
     */
    private EmailValidator() {
        // Do nothing.
    }
}
