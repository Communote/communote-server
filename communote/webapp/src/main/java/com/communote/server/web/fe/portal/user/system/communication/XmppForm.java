package com.communote.server.web.fe.portal.user.system.communication;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class XmppForm {

    /** time to wait for next posting */
    public static final String DEFAULT_POSTING_INTERVAL = "30000";

    private String server;
    private String port;
    private String login;
    private String password;
    private String action;
    private String priority = "100";
    private Boolean enabled = false;
    private String userSuffix;
    private String blogSuffix;
    private Boolean subscriptionEnabled = true;
    private String postingInterval;
    private boolean running;
    private boolean passwordChanged = false;

    /**
     * Does nothing.
     */
    public XmppForm() {
        // Do nothing
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @return the blogSuffix
     */
    public String getBlogSuffix() {
        return blogSuffix;
    }

    /**
     * @return the enabled
     */
    public Boolean getEnabled() {
        return enabled;
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
     * @return the postingInterval
     */
    public String getPostingInterval() {
        return postingInterval;
    }

    /**
     * @return the priority
     */
    public String getPriority() {
        return priority;
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @return the subscriptionEnabled
     */
    public Boolean getSubscriptionEnabled() {
        return subscriptionEnabled;
    }

    /**
     * @return the userSuffix
     */
    public String getUserSuffix() {
        return userSuffix;
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
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = StringUtils.trim(action);
    }

    /**
     * @param blogSuffix
     *            the blogSuffix to set
     */
    public void setBlogSuffix(String blogSuffix) {
        this.blogSuffix = StringUtils.trim(blogSuffix);
    }

    /**
     * @param enabled
     *            the enabled to set
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
     * @param postingInterval
     *            the postingInterval to set
     */
    public void setPostingInterval(String postingInterval) {
        this.postingInterval = StringUtils.trim(postingInterval);
    }

    /**
     * @param priority
     *            the priority to set
     */
    public void setPriority(String priority) {
        this.priority = StringUtils.trim(priority);
    }

    /**
     * @param running
     *            the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * @param server
     *            the server to set
     */
    public void setServer(String server) {
        this.server = StringUtils.trim(server);
    }

    /**
     * @param subscriptionEnabled
     *            the subscriptionEnabled to set
     */
    public void setSubscriptionEnabled(Boolean subscriptionEnabled) {
        this.subscriptionEnabled = subscriptionEnabled;
    }

    /**
     * @param userSuffix
     *            the userSuffix to set
     */
    public void setUserSuffix(String userSuffix) {
        this.userSuffix = StringUtils.trim(userSuffix);
    }

}
