package com.communote.server.core.user.group;

import com.communote.server.core.user.exception.UserValidationException;

/**
 * Thrown to indicate that an alias does not conform to the supported format.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AliasValidationException extends UserValidationException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 9020527883478032763L;

    /**
     * Constructs a new instance of AliasValidationException
     *
     */
    public AliasValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of AliasValidationException
     *
     */
    public AliasValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
