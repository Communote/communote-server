package com.communote.server.external.acegi;

/**
 * Exception to be thrown if user is not yet activated.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserAccountNotActivatedException extends UserAccountException {

    private static final long serialVersionUID = -4362523734593416154L;

    /**
     * @param msg
     *            The message.
     * @param username
     *            The username.
     */
    public UserAccountNotActivatedException(String msg, String username) {
        super(msg, username);
    }
}
