package com.communote.server.web.fe.portal.user.system.application;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class VirusScanningForm {
    /** the type of the virus scanner */
    private SupportedVirusScannerTypes scannerType;

    /** */
    private String action;
    /** */
    private boolean enabled;

    /** */
    private boolean clamAVScanner;
    /** */
    private boolean cmdLineScanner;

    /** */
    private String clamHost;
    /** */
    private String clamPort;
    /** */
    private String clamTempDir;
    /** */
    private String clamConnectionTimeout;

    /** */
    private String cmdCommand;
    /** */
    private String cmdExitCode;
    /** */
    private String cmdTempDir;
    /** */
    private String cmdTempFilePrefix;
    /** */
    private String cmdTempFileSuffix;
    /** */
    private String cmdProcessTimeout;

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @return the clamConnectionTimeout
     */
    public String getClamConnectionTimeout() {
        return clamConnectionTimeout;
    }

    /**
     * @return the clamHost
     */
    public String getClamHost() {
        return clamHost;
    }

    /**
     * @return the clamPort
     */
    public String getClamPort() {
        return clamPort;
    }

    /**
     * @return the clamTempDir
     */
    public String getClamTempDir() {
        return clamTempDir;
    }

    /**
     * @return the cmdCommand
     */
    public String getCmdCommand() {
        return cmdCommand;
    }

    /**
     * @return the cmdExitCode
     */
    public String getCmdExitCode() {
        return cmdExitCode;
    }

    /**
     * @return the cmdProcessTimeout
     */
    public String getCmdProcessTimeout() {
        return cmdProcessTimeout;
    }

    /**
     * @return the cmdTempDir
     */
    public String getCmdTempDir() {
        return cmdTempDir;
    }

    /**
     * @return the cmdTempFilePrefix
     */
    public String getCmdTempFilePrefix() {
        return cmdTempFilePrefix;
    }

    /**
     * @return the cmdTempFileSuffix
     */
    public String getCmdTempFileSuffix() {
        return cmdTempFileSuffix;
    }

    /**
     * @return the scannerType
     */
    public SupportedVirusScannerTypes getScannerType() {
        return scannerType;
    }

    /**
     * @return {@code true} if the current scanner type is Clam Anti Virus.
     */
    public boolean isClamAVScanner() {
        if (SupportedVirusScannerTypes.CLAMAV.equals(scannerType)) {
            clamAVScanner = true;
        } else {
            clamAVScanner = false;
        }

        return clamAVScanner;
    }

    /**
     * @return {@code true} if the current scanner type is Clam Anti Virus.
     */
    public boolean isCmdLineScanner() {
        if (SupportedVirusScannerTypes.CMDLINE.equals(scannerType)) {
            cmdLineScanner = true;
        } else {
            cmdLineScanner = false;
        }

        return cmdLineScanner;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @param clamConnectionTimeout
     *            the clamConnectionTimeout to set
     */
    public void setClamConnectionTimeout(String clamConnectionTimeout) {
        this.clamConnectionTimeout = StringUtils.trim(clamConnectionTimeout);
    }

    /**
     * @param clamHost
     *            the clamHost to set
     */
    public void setClamHost(String clamHost) {
        this.clamHost = StringUtils.trim(clamHost);
    }

    /**
     * @param clamPort
     *            the clamPort to set
     */
    public void setClamPort(String clamPort) {
        this.clamPort = StringUtils.trim(clamPort);
    }

    /**
     * @param clamTempDir
     *            the clamTempDir to set
     */
    public void setClamTempDir(String clamTempDir) {
        this.clamTempDir = StringUtils.trim(clamTempDir);
    }

    /**
     * @param cmdCommand
     *            the cmdCommand to set
     */
    public void setCmdCommand(String cmdCommand) {
        this.cmdCommand = StringUtils.trim(cmdCommand);
    }

    /**
     * @param cmdExitCode
     *            the cmdExitCode to set
     */
    public void setCmdExitCode(String cmdExitCode) {
        this.cmdExitCode = StringUtils.trim(cmdExitCode);
    }

    /**
     * @param cmdProcessTimeout
     *            the cmdProcessTimeout to set
     */
    public void setCmdProcessTimeout(String cmdProcessTimeout) {
        this.cmdProcessTimeout = StringUtils.trim(cmdProcessTimeout);
    }

    /**
     * @param cmdTempDir
     *            the cmdTempDir to set
     */
    public void setCmdTempDir(String cmdTempDir) {
        this.cmdTempDir = StringUtils.trim(cmdTempDir);
    }

    /**
     * @param cmdTempFilePrefix
     *            the cmdTempFilePrefix to set
     */
    public void setCmdTempFilePrefix(String cmdTempFilePrefix) {
        this.cmdTempFilePrefix = StringUtils.trim(cmdTempFilePrefix);
    }

    /**
     * @param cmdTempFileSuffix
     *            the cmdTempFileSuffix to set
     */
    public void setCmdTempFileSuffix(String cmdTempFileSuffix) {
        this.cmdTempFileSuffix = StringUtils.trim(cmdTempFileSuffix);
    }

    /**
     * @param enabled
     *            the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @param scannerType
     *            the scannerType to set
     */
    public void setScannerType(SupportedVirusScannerTypes scannerType) {
        this.scannerType = scannerType;
    }

}