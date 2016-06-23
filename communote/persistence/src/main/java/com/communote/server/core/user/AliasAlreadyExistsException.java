package com.communote.server.core.user;

import com.communote.server.core.user.exception.UserValidationException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AliasAlreadyExistsException extends UserValidationException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6142808901313538800L;

    /**
     * Constructs a new instance of AliasAlreadyExistsException
     *
     */
    public AliasAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of AliasAlreadyExistsException
     *
     */
    public AliasAlreadyExistsException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
