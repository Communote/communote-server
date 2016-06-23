package com.communote.server.core.tag;

import com.communote.server.persistence.common.security.CommunoteRuntimeException;

/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.tag.TagManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagManagementException extends CommunoteRuntimeException {

    private static final long serialVersionUID = -7515603578669101670L;

    /**
     * Constructs a new instance of <code>TagManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public TagManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of CommunoteException
     *
     * @param message
     *            the throwable message.
     * @param messageKey
     *            A message key to be used for a specific error message.
     * @param arguments
     *            Arguments to be used for the specific error message.
     */
    public TagManagementException(String message, String messageKey, Object... arguments) {
        super(message, null, messageKey, arguments);
    }

    /**
     * Constructs a new instance of <code>TagManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the cause of the exception
     */
    public TagManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructs a new instance of CommunoteException
     *
     * @param message
     *            detail message
     * @param throwable
     *            the cause of the exception
     *
     * @param messageKey
     *            A message key to be used for a specific error message.
     * @param arguments
     *            Arguments to be used for the specific error message.
     */
    public TagManagementException(String message, Throwable throwable, String messageKey,
            Object... arguments) {
        super(message, throwable, messageKey, arguments);
    }
}