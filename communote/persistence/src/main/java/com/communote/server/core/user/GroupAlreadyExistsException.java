package com.communote.server.core.user;

/**
 * Thrown to indicate that a group which should not exist already exists.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class GroupAlreadyExistsException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception
     *
     * @param message
     *            a detail message
     */
    public GroupAlreadyExistsException(String message) {
        super(message);
    }
}
