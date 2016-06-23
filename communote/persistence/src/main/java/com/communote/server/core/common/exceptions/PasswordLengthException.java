package com.communote.server.core.common.exceptions;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PasswordLengthException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8822567868836579664L;

    /**
     * Constructs a new instance of PasswordLengthException
     *
     */
    public PasswordLengthException(String message) {
        super(message);
    }

}
