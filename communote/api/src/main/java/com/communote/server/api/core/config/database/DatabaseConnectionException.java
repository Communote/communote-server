package com.communote.server.api.core.config.database;

/**
 * Exception to be thrown if a database connection cannot be established.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DatabaseConnectionException extends Exception {

    private static final long serialVersionUID = 1L;

    public DatabaseConnectionException(String message) {
        super(message);
    }

    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
