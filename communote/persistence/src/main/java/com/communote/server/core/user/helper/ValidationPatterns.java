package com.communote.server.core.user.helper;

/**
 * This interface contains patterns for validating user data.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ValidationPatterns {
    /**
     * Client specific patterns.
     */
    public interface Client {
        /** Pattern for the company. */
        final static String PATTERN_COMPANY = DEFAULT_REGEX;
        /** Pattern for the clients name. */
        final static String PATTERN_CLIENT_NAME = DEFAULT_REGEX;
    }

    /** Default pattern for a single symbol. */
    static final String DEFAULT_SYMBOL_REGEX = "[^\\p{Cntrl}]";

    /** Default Regex . */
    public static final String DEFAULT_REGEX = DEFAULT_SYMBOL_REGEX + "*";

    /** The regex for a valid alias. */
    public final static String PATTERN_ALIAS = "[a-zA-Z0-9][\\.\\w-]*[a-zA-Z0-9]|[a-zA-Z0-9]";

    /** Regex describing the characters not supported inside an alias. */
    public final static String UNSUPPORTED_CHARACTERS_IN_ALIAS = "[^.A-Za-z0-9_-]";
    /** Minimal number of symbols */
    public final static int FIRSTNAME_LOWER_BOUND = 1;
    /** Maximal number of symbols */
    public final static int FIRSTNAME_UPPER_BOUND = 255;
    /** The regex for a valid forname. */
    public final static String PATTERN_FIRSTNAME = DEFAULT_SYMBOL_REGEX + "{"
            + FIRSTNAME_LOWER_BOUND + ","
            + FIRSTNAME_UPPER_BOUND + "}";
    /** Minimal number of symbols */
    public final static int LASTNAME_LOWER_BOUND = 1;
    /** Maximal number of symbols */
    public final static int LASTNAME_UPPER_BOUND = 255;;

    /** The regex for a valid surname. */
    public final static String PATTERN_LASTNAME = DEFAULT_SYMBOL_REGEX + "{" + LASTNAME_LOWER_BOUND
            + "," + LASTNAME_UPPER_BOUND + "}";

}
