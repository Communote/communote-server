package com.communote.server.core.user;


/**
 * <p>
 * Thrown to indicate that user deletion is disabled on the current client.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserDeletionDisabledException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 8699146307069468590L;

    /**
     * Constructs a new instance of UserDeletionDisabledException
     *
     */
    public UserDeletionDisabledException(String message) {
        super(message);
    }

}
