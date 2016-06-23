package com.communote.server.core.common.database;

import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;

/**
 * An exception indicating an error during the update of a database
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DatabaseUpdateException extends Exception {

    private static final long serialVersionUID = 1L;

    private ClientTO client;

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to {@link #initCause}.
     * 
     * @param message
     *            the detail message. The detail message is saved for later retrieval by the
     *            {@link #getMessage()} method.
     */
    public DatabaseUpdateException(String message) {
        this(message, null);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with <code>cause</code> is <i>not</i> automatically
     * incorporated in this exception's detail message.
     * 
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            {@link #getMessage()} method).
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *            (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent
     *            or unknown.)
     */
    public DatabaseUpdateException(String message, Throwable cause) {
        super(message, cause);
        this.client = ClientAndChannelContextHolder.getClient();
    }

    /**
     * @return the client
     */
    public ClientTO getClient() {
        return client;
    }

    /**
     * @param client
     *            the client to set
     */
    public void setClient(ClientTO client) {
        this.client = client;
    }
}
