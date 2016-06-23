package com.communote.server.core.user.group;

/**
 * Thrown to indicate that an operation on a group is not permitted.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GroupOperationNotPermittedException extends
com.communote.server.api.core.security.AuthorizationException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 4142880040075121845L;

    /**
     * Constructs a new instance of GroupOperationNotPermittedException
     *
     */
    public GroupOperationNotPermittedException(String message) {
        super(message);
    }

}
