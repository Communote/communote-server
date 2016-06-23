package com.communote.server.core.user;

import com.communote.server.core.user.exception.ExternalUserRepositoryException;

/**
 * <p>
 * Exception indicating that the update of an existing external user, identified by the external
 * username, is not possible because the permanentId is not matching the saved value.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PermanentIdMissmatchException extends ExternalUserRepositoryException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3550159076173834071L;

    /**
     * Constructs a new instance of PermanentIdMissmatchException
     *
     */
    public PermanentIdMissmatchException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of PermanentIdMissmatchException
     *
     */
    public PermanentIdMissmatchException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
