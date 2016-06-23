package com.communote.server.persistence.common.security;

/**
 * Exception to be thrown if a security code does not exist.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SecurityCodeNotFoundException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4286055836584572180L;

    /**
     * The default constructor.
     */
    public SecurityCodeNotFoundException() {
    }

    /**
     * Constructs a new instance of SecurityCodeNotFoundException
     *
     */
    public SecurityCodeNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of SecurityCodeNotFoundException
     *
     */
    public SecurityCodeNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
