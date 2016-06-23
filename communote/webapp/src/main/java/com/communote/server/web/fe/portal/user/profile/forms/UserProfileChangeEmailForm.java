package com.communote.server.web.fe.portal.user.profile.forms;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.core.security.SecurityHelper;

/**
 * The Class UserProfileChangeEmailForm.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileChangeEmailForm {

    private String newEmail;
    private String password;

    /**
     * @return the current e-mail of the user
     */
    public String getCurrentEmail() {
        return SecurityHelper.assertCurrentKenmeiUser().getEmail();
    }

    /**
     * Gets the new email.
     * 
     * @return the new email
     */
    public String getNewEmail() {
        return newEmail;
    }

    /**
     * Gets the password
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the new email
     * 
     * @param newEmail
     *            the new email to set
     */
    public void setNewEmail(String newEmail) {
        this.newEmail = StringUtils.trim(newEmail);
    }

    /**
     * Sets the password.
     * 
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = StringUtils.trim(password);
    }
}