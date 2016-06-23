package com.communote.server.core.common.exceptions;

/**
 * <p>
 * This is exception is thrown if the an illegal state in the data base has been detected, e.g. more
 * than one user with same email and password.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IllegalDatabaseState extends RuntimeException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 1349620060563033007L;

    /**
     * The default constructor.
     */
    public IllegalDatabaseState() {
    }

    /**
     * Constructs a new instance of IllegalDatabaseState
     *
     */
    public IllegalDatabaseState(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of IllegalDatabaseState
     *
     */
    public IllegalDatabaseState(String message, Throwable throwable) {
        super(message, throwable);
    }

}
