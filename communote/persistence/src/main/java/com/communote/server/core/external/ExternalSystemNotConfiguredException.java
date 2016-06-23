package com.communote.server.core.external;

/**
 * Exception indicating that an external system is not configured
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ExternalSystemNotConfiguredException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String externalSystemId;

    public ExternalSystemNotConfiguredException(String message, String externalSystemId) {
        super(message);
        this.externalSystemId = externalSystemId;
    }

    public String getExternalSystemId() {
        return externalSystemId;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

}
