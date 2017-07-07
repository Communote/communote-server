package com.communote.server.core.security.authentication;

/**
 * Indicates that a user was trying to authenticate against the internal DB, while having a config
 * for an activated external system.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AuthAgainstInternalDBWhileExternalUserAccountException extends Exception {

    private static final long serialVersionUID = 4307530171030267935L;

    private final String externalSystemId;

    private final String username;

    /**
     * @param message
     *            The message.
     * @param username
     *            the login name of the user that tried to log in
     * @param externalSystemId
     *            ID of the external system the user is from
     */
    public AuthAgainstInternalDBWhileExternalUserAccountException(String message, String username,
            String externalSystemId) {
        super(message);
        this.username = username;
        this.externalSystemId = externalSystemId;
    }

    /**
     * @return the ID of the external system the user is from
     */
    public String getExternalSystemId() {
        return externalSystemId;
    }

    /**
     * @return the login name of the user that tried to log in
     */
    public String getUsername() {
        return username;
    }

}
