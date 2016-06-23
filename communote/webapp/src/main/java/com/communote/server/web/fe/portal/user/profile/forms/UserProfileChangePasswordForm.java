package com.communote.server.web.fe.portal.user.profile.forms;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class UserProfileChangePasswordForm.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileChangePasswordForm {

    private String oldPassword;

    private String newPassword;
    private String newPasswordConfirm;

    /**
     * @return the newPassword
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * @return the newPasswordConfirm
     */
    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }

    /**
     * @return the oldPassword
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * @param newPassword
     *            the newPassword to set
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = StringUtils.trim(newPassword);
    }

    /**
     * @param newPasswordConfirm
     *            the newPasswordConfirm to set
     */
    public void setNewPasswordConfirm(String newPasswordConfirm) {
        this.newPasswordConfirm = StringUtils.trim(newPasswordConfirm);
    }

    /**
     * @param oldPassword
     *            the oldPassword to set
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = StringUtils.trim(oldPassword);
    }
}
