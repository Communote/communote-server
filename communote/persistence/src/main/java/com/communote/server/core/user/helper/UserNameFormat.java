package com.communote.server.core.user.helper;

/**
 * This enum provides the format definitions for a user signature.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public enum UserNameFormat {
    /**
     * The signature of the user is "ALIAS"
     */
    ALIAS,
    /**
     * The signature of the user is "FIRSTNAME LASTNAME". If firstname and lastname are not defined
     * the string will look like "ALIAS".
     */
    SHORT,
    /**
     * The signature of the user is "FIRSTNAME LASTNAME (ALIAS)". If firstname and lastname are not
     * defined the string will look like "ALIAS".
     */
    MEDIUM,
    /**
     * The signature of the user is "SALUTATIONS FIRSTNAME LASTNAME". If firstname and lastname are
     * not defined the string will look like "ALIAS".
     */
    LONG
}