package com.communote.server.web.fe.portal.user.client.controller.security;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AuthenticationForm {

    private String riskLevel;
    private String lockInterval;
    private String failedAttemptsBeforeTemporaryLock;
    private String failedAttemptsBeforePermanentLock;

    /**
     * @return the failedAttemptsBeforePermanentLock
     */
    public String getFailedAttemptsBeforePermanentLock() {
        return failedAttemptsBeforePermanentLock;
    }

    /**
     * @return the failedAttempsBeforeTemporaryLock
     */
    public String getFailedAttemptsBeforeTemporaryLock() {
        return failedAttemptsBeforeTemporaryLock;
    }

    /**
     * @return the lockInterval
     */
    public String getLockInterval() {
        return lockInterval;
    }

    /**
     * @return the riskLevel
     */
    public String getRiskLevel() {
        return riskLevel;
    }

    /**
     * @param failedAttemptsBeforePermanentLock
     *            the failedAttemptsBeforePermanentLock to set
     */
    public void setFailedAttemptsBeforePermanentLock(String failedAttemptsBeforePermanentLock) {
        this.failedAttemptsBeforePermanentLock = failedAttemptsBeforePermanentLock == null ? null
                : failedAttemptsBeforePermanentLock.trim();
    }

    /**
     * @param failedAttemptsBeforeTemporaryLock
     *            the failedAttemptsBeforeTemporaryLock to set
     */
    public void setFailedAttemptsBeforeTemporaryLock(String failedAttemptsBeforeTemporaryLock) {
        this.failedAttemptsBeforeTemporaryLock = failedAttemptsBeforeTemporaryLock == null ? null
                : failedAttemptsBeforeTemporaryLock.trim();
    }

    /**
     * @param lockInterval
     *            the lockInterval to set
     */
    public void setLockInterval(String lockInterval) {
        this.lockInterval = lockInterval == null ? null : lockInterval.trim();
    }

    /**
     * @param riskLevel
     *            the riskLevel to set
     */
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel == null ? null : riskLevel.trim();
    }
}
