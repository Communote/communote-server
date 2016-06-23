package com.communote.server.web.api.to.user;

/**
 * Transfer object for the login information
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LoginInfo {
    private String sessionId;
    private Long userId;
    private String latestMobileVersion;

    /**
     * @param sessionId
     *            the assigned session id
     * @param userId
     *            the id of the user
     */
    public LoginInfo(String sessionId, Long userId) {
        this.sessionId = sessionId;
        this.userId = userId;
    }

    /**
     * @param sessionId
     *            the assigned session id
     * @param userId
     *            the id of the user
     * @param latestMobileVersion
     *            the latest version of the mobile client
     */
    public LoginInfo(String sessionId, Long userId, String latestMobileVersion) {
        this(sessionId, userId);
        this.latestMobileVersion = latestMobileVersion;
    }

    /**
     * @return the latest available version of the mobile midp client
     */
    public String getLatestMobileVersion() {
        return latestMobileVersion;
    }

    /**
     * @return the assigned session id
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @return the id of the user
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param latestMobileVersion
     *            the latest midp version
     */
    public void setLatestMobileVersion(String latestMobileVersion) {
        this.latestMobileVersion = latestMobileVersion;
    }

    /**
     * @param sessionId
     *            the assigned session id
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @param userId
     *            the id of the user
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
