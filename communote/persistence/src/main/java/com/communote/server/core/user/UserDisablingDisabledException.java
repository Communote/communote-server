package com.communote.server.core.user;

/**
 * <p>
 * Thrown to indicate that permanently disabling is disabled for the current client.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserDisablingDisabledException extends UserDeletionDisabledException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4265099642545521628L;

    /**
     * Constructs a new instance of UserDisablingDisabledException
     *
     */
    public UserDisablingDisabledException(String message) {
        super(message);
    }

}
