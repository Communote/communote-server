package com.communote.server.core.user;

import com.communote.server.core.user.exception.UserValidationException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalUserPasswordChangeNotAllowedException extends UserValidationException {

    private static final long serialVersionUID = -5447506926413117490L;
    private final String username;
    private final String systemId;

    /**
     * @param username
     *            The username.
     * @param systemId
     *            The systemId.
     */
    public ExternalUserPasswordChangeNotAllowedException(String username, String systemId) {
        super("An external user can't change his password, when the external system is primary. User: "
                + username + "; System: " + systemId);
        this.username = username;
        this.systemId = systemId;
    }

    /**
     * @return the systemId
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
}
