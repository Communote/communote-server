package com.communote.server.core.common.exceptions;

/**
 * IncorrectResultTypeException is thrown if an unexpected result type occured.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IncorrectResultTypeException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7901125682729849686L;

    /**
     * Instantiates a new incorrect result type exception.
     * 
     * @param expectedClass
     *            the something
     * @param actually
     *            the object
     */
    public IncorrectResultTypeException(Class<?> expectedClass, Object actually) {
        super("Unexpected result type, expepected type: " + expectedClass + ", actually type: "
                + (actually == null ? "null" : actually.getClass()));
    }
}
