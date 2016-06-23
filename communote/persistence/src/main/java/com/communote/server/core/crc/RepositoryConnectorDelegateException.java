package com.communote.server.core.crc;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.crc.RepositoryConnectorDelegate}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RepositoryConnectorDelegateException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -5949520129615310093L;

    /**
     * Constructs a new instance of <code>RepositoryConnectorDelegateException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public RepositoryConnectorDelegateException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>RepositoryConnectorDelegateException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public RepositoryConnectorDelegateException(String message, Throwable throwable) {
        super(message, throwable);
    }
}