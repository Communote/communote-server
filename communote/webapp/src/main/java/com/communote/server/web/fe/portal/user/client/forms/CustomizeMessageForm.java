package com.communote.server.web.fe.portal.user.client.forms;

/**
 * Imprint form.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CustomizeMessageForm {

    private String key;

    private String message;

    private String languageCode;

    private boolean html = true;

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the languageCode
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the isHtml
     */
    public boolean isIsHtml() {
        return html;
    }

    /**
     * @param isHtml
     *            the isHtml to set
     */
    public void setIsHtml(boolean isHtml) {
        this.html = isHtml;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key == null ? null : key.trim();
    }

    /**
     * @param languageCode
     *            the languageCode to set
     */
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode == null ? null : languageCode.trim();
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message == null ? null : message.trim();
    }

}
