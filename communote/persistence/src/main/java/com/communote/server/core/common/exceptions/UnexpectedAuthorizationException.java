package com.communote.server.core.common.exceptions;

/**
 * Thrown to indicate that the current user does not have the appropriate rights to execute an
 * action. This Exception has the same semantics as the
 * {@link com.communote.server.api.core.security.AuthorizationException} but is unchecked.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO This class is a workaround for cases where the checked {@link
// com.communote.server.core.common.exceptions.AuthorizationException} should
// but cannot be thrown because of too much refactoring this would produce in the calling code.
public class UnexpectedAuthorizationException extends RuntimeException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 8134260495191018534L;

    /**
     * Constructs a new instance of AuthorizationException
     *
     */
    public UnexpectedAuthorizationException(String message) {
        super(message);
    }

}
