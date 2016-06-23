package com.communote.server.core.user.exception;

import com.communote.server.persistence.common.security.CommunoteException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GeneralUserManagementException extends CommunoteException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 1444425003385537430L;

    /**
     * Constructs a new instance of GeneralUserManagementException
     *
     * @param message
     *            the throwable message.
     */
    public GeneralUserManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of GeneralUserManagementException
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public GeneralUserManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
