package com.communote.server.core.user.client;


/**
 * <p>
 * This exception is thrown if the result is not unique.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NonUniqueResultException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 8739110372411044125L;

    /**
     * Constructs a new instance of NonUniqueResultException
     *
     */
    public NonUniqueResultException(String message) {
        super(message);
    }

}
