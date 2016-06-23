package com.communote.server.core.blog;

/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.service.NoteService}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3375416465512877293L;

    /**
     * Constructs a new instance of <code>NoteManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public NoteManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>NoteManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public NoteManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }

}