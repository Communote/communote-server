package com.communote.server.web.fe.portal.user.client.forms;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientGeneralSettingsForm {
    /** The client name. */
    private String clientName = "";
    /** The time zone of the client */
    private String timeZoneId = "";

    /**
     * Gets the client name.
     *
     * @return the clientName
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @return the timeZoneId
     */
    public String getTimeZoneId() {
        return timeZoneId;
    }

    /**
     * Sets the client name.
     *
     * @param clientName
     *            the clientName to set
     */
    public void setClientName(String clientName) {
        this.clientName = clientName == null ? null : clientName.trim();
    }

    /**
     * @param timeZoneId
     *            the timeZoneId to set
     */
    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId == null ? null : timeZoneId.trim();
    }
}
