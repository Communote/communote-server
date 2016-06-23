package com.communote.server.web.fe.portal.user.forms;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ForgottenPWForm {

    /**
     * Constants for send a new email action
     */
    public final static String SEND_PW_LINK = "sendpwemail";

    /**
     * Constants for a confirm new email action
     */
    public final static String CONFIRM_NEW_PASSWORD = "confirmnewpassword";

    private String action;
    private String password1;
    private String password2;
    private String email;
    private String code;

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the password.
     * 
     * @return the password
     */
    public String getPassword() {
        return password1;
    }

    /**
     * @return the password2
     */
    public String getPassword2() {
        return password2;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = action == null ? null : action.trim();
    }

    /**
     * @param code
     *            the code to set
     */
    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    /**
     * Returns the password.
     * 
     * @param password
     *            The password.
     */
    public void setPassword(String password) {
        this.password1 = password;
    }

    /**
     * @param password2
     *            the password2 to set
     */
    public void setPassword2(String password2) {
        this.password2 = password2;
    }

}
