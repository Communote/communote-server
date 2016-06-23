package com.communote.server.core.external;

import com.communote.server.core.user.exception.GeneralUserManagementException;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ExternalRepositoryException extends GeneralUserManagementException {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a detail message.
     * 
     * @param message
     *            the detail message
     */
    public ExternalRepositoryException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a detail message and a cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public ExternalRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

}
