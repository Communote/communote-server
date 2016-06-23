package com.communote.server.core.user;


/**
 * <p>
 * Thrown to indicate that the anonymization is disabled for the current client.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserAnonymizationDisabledException extends
        com.communote.server.core.user.UserDeletionDisabledException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 810007241154666691L;

    /**
     * Constructs a new instance of UserAnonymizationDisabledException
     *
     */
    public UserAnonymizationDisabledException(String message) {
        super(message);
    }

}
