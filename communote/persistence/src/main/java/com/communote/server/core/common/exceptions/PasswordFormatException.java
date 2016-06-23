package com.communote.server.core.common.exceptions;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PasswordFormatException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7227187347227565918L;

    /**
     * Constructs a new instance of PasswordFormatException
     *
     */
    public PasswordFormatException(String message) {
        super(message);
    }

}
