package com.communote.server.web.fe.portal.user.system.communication;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.mail.fetching.MailInProtocolType;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MailInForm {

    /** */
    public static final MailInProtocolType DEFAULT_PROTOCOL = MailInProtocolType.IMAP;
    /** */
    public static final String DEFAULT_FETCH_TIMEOUT = "30000";
    /** */
    public static final String DEFAULT_PRECONNECT_TIMEOUT = "120000";
    private String server;
    private String port;
    private MailInProtocolType protocol;
    private String login;
    private String password;
    private boolean startTls;
    private String mailbox;
    private String fetchingTimeout;
    private String reconnectionTimeout;
    private String action;
    private String mode;
    private boolean running;

    private String singleModeAddress;

    private String multiModeDomain;

    private String multiModeSuffix;

    private Boolean multiModeUseAccount;
    private boolean passwordChanged = false;

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @return the fetchingTimeout
     */
    public String getFetchingTimeout() {
        return fetchingTimeout;
    }

    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return the mailbox
     */
    public String getMailbox() {
        return mailbox;
    }

    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * @return the multiModeDomain
     */
    public String getMultiModeDomain() {
        return multiModeDomain;
    }

    /**
     * @return the multiModeSuffix
     */
    public String getMultiModeSuffix() {
        return multiModeSuffix;
    }

    /**
     * @return the multiModeUseAccount
     */
    public Boolean getMultiModeUseAccount() {
        return multiModeUseAccount;
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
        // if (StringUtils.isBlank(port)) {
        // return secureConnection ? "993" : "143";
        // }
        return port;
    }

    /**
     * @return the protocol
     */
    public MailInProtocolType getProtocol() {
        if (protocol == null) {
            return DEFAULT_PROTOCOL;
        }

        return protocol;
    }

    /**
     * @return the reconnectionTimeout
     */
    public String getReconnectionTimeout() {
        return reconnectionTimeout;
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @return the singleModeAddress
     */
    public String getSingleModeAddress() {
        return singleModeAddress;
    }

    /**
     * @return the passwordChanged
     */
    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    /**
     * @return the running
     */
    public boolean isRunning() {
        return running;
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
     * @param fetchingTimeout
     *            the fetchingTimeout to set
     */
    public void setFetchingTimeout(String fetchingTimeout) {
        this.fetchingTimeout = StringUtils.trim(fetchingTimeout);
    }

    /**
     * @param login
     *            the login to set
     */
    public void setLogin(String login) {
        this.login = StringUtils.trim(login);
    }

    /**
     * @param mailbox
     *            the mailbox to set
     */
    public void setMailbox(String mailbox) {
        this.mailbox = StringUtils.trim(mailbox);
    }

    /**
     * @param mode
     *            the mode to set
     */
    public void setMode(String mode) {
        this.mode = StringUtils.trim(mode);
    }

    /**
     * @param multiModeDomain
     *            the multiModeDomain to set
     */
    public void setMultiModeDomain(String multiModeDomain) {
        this.multiModeDomain = StringUtils.trim(multiModeDomain);
    }

    /**
     * @param multiModeSuffix
     *            the multiModeSuffix to set
     */
    public void setMultiModeSuffix(String multiModeSuffix) {
        this.multiModeSuffix = StringUtils.trim(multiModeSuffix);
    }

    /**
     * @param multiModeUseAccount
     *            the multiModeUseAccount to set
     */
    public void setMultiModeUseAccount(Boolean multiModeUseAccount) {
        this.multiModeUseAccount = multiModeUseAccount;
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
        if (StringUtils.isEmpty(port)) {
            port = null;
        }
        this.port = StringUtils.trim(port);
    }

    /**
     * @param protocol
     *            the protocol to set
     */
    public void setProtocol(MailInProtocolType protocol) {
        this.protocol = protocol;
    }

    /**
     * @param reconnectionTimeout
     *            the reconnectionTimeout to set
     */
    public void setReconnectionTimeout(String reconnectionTimeout) {
        this.reconnectionTimeout = StringUtils.trim(reconnectionTimeout);
    }

    /**
     * @param isRunning
     *            the running to set
     */
    public void setRunning(boolean isRunning) {
        this.running = isRunning;
    }

    /**
     * @param server
     *            the server to set
     */
    public void setServer(String server) {
        this.server = StringUtils.trim(server);
    }

    /**
     * @param singleModeAddress
     *            the singleModeAddress to set
     */
    public void setSingleModeAddress(String singleModeAddress) {
        this.singleModeAddress = StringUtils.trim(singleModeAddress);
    }

    /**
     * @param startTls
     *            the startTls to set
     */
    public void setStartTls(boolean startTls) {
        this.startTls = startTls;
    }
}