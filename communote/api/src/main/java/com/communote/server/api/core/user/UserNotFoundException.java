package com.communote.server.api.core.user;


/**
 * Thrown if a user does not exist.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserNotFoundException extends CommunoteEntityNotFoundException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8022511198475946018L;

    /**
     * Constructs a new instance of UserNotFoundException
     *
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of UserNotFoundException
     *
     */
    public UserNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
