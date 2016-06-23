package com.communote.server.core.security;

import com.communote.server.core.user.exception.GeneralUserManagementException;

/**
 * <p>
 * Thrown to indicate that the user account is not in state ACTIVE.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AccountNotActivatedException extends GeneralUserManagementException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1519688126292924895L;

    /**
     * Constructs a new instance of AccountNotActivatedException
     *
     */
    public AccountNotActivatedException(String message) {
        super(message);
    }

}
