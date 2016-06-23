package com.communote.server.web.fe.portal.user.system.application;

import org.apache.commons.lang.StringUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ServerForm {

    private String context;
    private String hostname;
    private String httpPort;
    private String httpsPort;
    private Boolean httpsEnabled = false;

    /**
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return the httpPort
     */
    public String getHttpPort() {
        return httpPort;
    }

    /**
     * @return the httpsEnabled
     */
    public Boolean getHttpsEnabled() {
        return httpsEnabled;
    }

    /**
     * @return the httpsPort
     */
    public String getHttpsPort() {
        return httpsPort;
    }

    /**
     * @param context
     *            the context to set
     */
    public void setContext(String context) {
        this.context = StringUtils.trim(context);
    }

    /**
     * @param hostname
     *            the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = StringUtils.trim(hostname);
    }

    /**
     * @param httpPort
     *            the httpPort to set
     */
    public void setHttpPort(String httpPort) {
        this.httpPort = StringUtils.trim(httpPort);
    }

    /**
     * @param httpsEnabled
     *            the httpsEnabled to set
     */
    public void setHttpsEnabled(Boolean httpsEnabled) {
        this.httpsEnabled = httpsEnabled;
    }

    /**
     * @param httpsPort
     *            the httpsPort to set
     */
    public void setHttpsPort(String httpsPort) {
        this.httpsPort = StringUtils.trim(httpsPort);
    }
}
