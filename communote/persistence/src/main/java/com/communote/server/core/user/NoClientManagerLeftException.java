package com.communote.server.core.user;

import com.communote.server.core.user.exception.GeneralUserManagementException;

/**
 * <p>
 * Thrown to indicate that an operation would lead to removing or disabling the last client manager.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoClientManagerLeftException extends GeneralUserManagementException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6591199878856534331L;

    /**
     * Constructs a new instance of NoClientManagerLeftException
     *
     */
    public NoClientManagerLeftException(String message) {
        super(message);
    }

}
