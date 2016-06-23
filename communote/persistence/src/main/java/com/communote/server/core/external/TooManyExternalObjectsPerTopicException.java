package com.communote.server.core.external;

/**
 * Exception when there are too many external objects for a single topic for a specific system id
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TooManyExternalObjectsPerTopicException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String externalSystemId;
    private int numberOfMaximumExternalObjectsPerTopic;

    public TooManyExternalObjectsPerTopicException(String message, String externalSystemId,
            int numberOfMaximumExternalObjectsPerTopic) {
        super(message);
        this.numberOfMaximumExternalObjectsPerTopic = numberOfMaximumExternalObjectsPerTopic;
    }

    public String getExternalSystemId() {
        return externalSystemId;
    }

    public int getNumberOfMaximumExternalObjectsPerTopic() {
        return numberOfMaximumExternalObjectsPerTopic;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    public void setNumberOfMaximumExternalObjectsPerTopic(int numberOfMaximumExternalObjectsPerTopic) {
        this.numberOfMaximumExternalObjectsPerTopic = numberOfMaximumExternalObjectsPerTopic;
    }

}
