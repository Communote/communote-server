package com.communote.server.core.exception;

/**
 * This interface contains a series of default error codes.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ErrorCodes {

    /** Authentication error. */
    public final static String AUTHENTICATION_ERROR = "AUTHENTICATION_ERROR";
    /** Authorization error. */
    public final static String AUTHORIZATION_ERROR = "AUTHORIZATION_ERROR";
    /** Illegal parameters. */
    public final static String ILLEGAL_PARAMETERS_ERROR = "ILLEGAL_PARAMETERS";
    /** Validation Error. */
    public final static String VALIDATION_ERROR = "VALIDATION_ERROR";
    /** An unknown error. */
    public final static String UNKNOWN_ERROR = "UNKNOWN";
    /** Okay. */
    public final static String OKAY = "OKAY";
    /** Okay, but with warnings. */
    public final static String WARNING = "WARNING";
    /** Forbidden. */
    public final static String FORBIDDEN = "FORBIDDEN";
    /** Not found. */
    public final static String NOT_FOUND = "NOT_FOUND";
    /** Bad request. */
    public final static String BAD_REQUEST = "BAD_REQUEST";
    /** Internal Error. */
    public final static String INTERNAL_ERROR = "INTERNAL_ERROR";

    /** Not acceptable. */
    public final static String NOT_ACCEPTABLE = "NOT_ACCEPTABLE";

}
