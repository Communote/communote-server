package com.communote.plugins.mq.message.core.data.topic;

/**
 * 
 * External object of a topic
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

public class ExternalObject {

    private String externalObjectId;

    private String externalObjectName;

    /**
     * @return the identifier of the external object
     */
    public String getExternalObjectId() {
        return externalObjectId;
    }

    /**
     * @return the name of external object
     */
    public String getExternalObjectName() {
        return externalObjectName;
    }

    /**
     * @param externalObjectId
     *            the identifier of the external object
     */
    public void setExternalObjectId(String externalObjectId) {
        this.externalObjectId = externalObjectId;
    }

    /**
     * @param externalObjectName
     *            the name of external object
     */
    public void setExternalObjectName(String externalObjectName) {
        this.externalObjectName = externalObjectName;
    }

}
