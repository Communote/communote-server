package com.communote.server.web.fe.portal.user.client.forms;

/**
 * A form the email settings.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ClientProfileEmailForm {
    private String clientEmail = "";

    private String clientEmailName = "";

    private String clientSupportEmailAddress;

    /**
     * Returns the client email address used in reply-to email header.
     * 
     * @return the clientEmail
     */
    public String getClientEmail() {
        return clientEmail;
    }

    /**
     * Returns the personal name of the client email address used in reply-to email header.
     * 
     * @return the clientEmailName
     */
    public String getClientEmailName() {
        return clientEmailName;
    }

    /**
     * @return the clientSupportEmailAddress
     */
    public String getClientSupportEmailAddress() {
        return clientSupportEmailAddress;
    }

    /**
     * Sets the client email address used in reply-to email header.
     * 
     * @param clientEmail
     *            the clientEmail to set
     */
    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail == null ? null : clientEmail.trim();
    }

    /**
     * Sets the personal name of the client email address used in reply-to email header.
     * 
     * @param clientEmailName
     *            the clientEmailName to set
     */
    public void setClientEmailName(String clientEmailName) {
        this.clientEmailName = clientEmailName == null ? null : clientEmailName.trim();
    }

    /**
     * @param clientSupportEmailAddress
     *            the clientSupportEmailAddress to set
     */
    public void setClientSupportEmailAddress(String clientSupportEmailAddress) {
        this.clientSupportEmailAddress = clientSupportEmailAddress == null ? null
                : clientSupportEmailAddress.trim();
    }

}
