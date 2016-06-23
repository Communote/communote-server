package com.communote.server.web.fe.portal.user.system.communication;

import org.apache.commons.lang.StringUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MailOutForm {

    private String server;
    private String port;
    private boolean startTls;
    private String login;
    private String password;
    private String senderName;
    private String senderAddress;
    private String action;
    private boolean passwordChanged = false;

    /**
     * Does nothing.
     */
    public MailOutForm() {
        // Do nothing.
    }

    /**
     * @param server
     *            Server.
     * @param port
     *            Port.
     * @param login
     *            Login.
     * @param password
     *            Password.
     * @param senderName
     *            Sender name.
     * @param senderAddress
     *            Sender address.
     */
    public MailOutForm(String server, String port, String login, String password,
            String senderName, String senderAddress) {
        super();
        this.server = server;
        this.port = port;
        this.login = login;
        this.password = password;
        this.senderName = senderName;
        this.senderAddress = senderAddress;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @return the senderAddress
     */
    public String getSenderAddress() {
        return senderAddress;
    }

    /**
     * @return the senderName
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @return the passwordChanged
     */
    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    /**
     * @return the startTls
     */
    public boolean isStartTls() {
        return startTls;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = StringUtils.trim(action);
    }

    /**
     * @param login
     *            the login to set
     */
    public void setLogin(String login) {
        this.login = StringUtils.trim(login);
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param passwordChanged
     *            the passwordChanged to set
     */
    public void setPasswordChanged(boolean passwordChanged) {
        this.passwordChanged = passwordChanged;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(String port) {
        this.port = StringUtils.trim(port);
    }

    /**
     * @param senderAddress
     *            the senderAddress to set
     */
    public void setSenderAddress(String senderAddress) {
        this.senderAddress = StringUtils.trim(senderAddress);
    }

    /**
     * @param senderName
     *            the senderName to set
     */
    public void setSenderName(String senderName) {
        this.senderName = StringUtils.trim(senderName);
    }

    /**
     * @param server
     *            the server to set
     */
    public void setServer(String server) {
        this.server = StringUtils.trim(server);
    }

    /**
     * @param startTls
     *            the startTls to set
     */
    public void setStartTls(boolean startTls) {
        this.startTls = startTls;
    }

}
