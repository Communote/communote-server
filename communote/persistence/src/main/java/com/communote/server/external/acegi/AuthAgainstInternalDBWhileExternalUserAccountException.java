package com.communote.server.external.acegi;

/**
 * Indicates that a user was trying to authenticate against the internal DB, while having a config
 * for an activated external system.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AuthAgainstInternalDBWhileExternalUserAccountException extends UserAccountException {

    private static final long serialVersionUID = 4307530171030267935L;

    private final String externalSystemId;

    /**
     * @param message
     *            The message.
     * @param username
     *            The users name.
     * @param externalSystemId
     *            Id of the external system.
     */
    public AuthAgainstInternalDBWhileExternalUserAccountException(String message, String username,
            String externalSystemId) {
        super(message, username);
        this.externalSystemId = externalSystemId;
    }

    /**
     * @return the externalSystemId
     */
    public String getExternalSystemId() {
        return externalSystemId;
    }

}
