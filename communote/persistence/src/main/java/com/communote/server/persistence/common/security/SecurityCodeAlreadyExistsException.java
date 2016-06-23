package com.communote.server.persistence.common.security;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SecurityCodeAlreadyExistsException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5471480849570129954L;

    /**
     * Constructs a new instance of SecurityCodeAlreadyExistsException
     *
     */
    public SecurityCodeAlreadyExistsException(String message) {
        super(message);
    }

}
