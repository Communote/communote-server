package com.communote.server.core.user;

import com.communote.server.core.user.exception.UserValidationException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EmailAlreadyExistsException extends UserValidationException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2756877146500150672L;

    /**
     * Constructs a new instance of EmailAlreadyExistsException
     *
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }

}
