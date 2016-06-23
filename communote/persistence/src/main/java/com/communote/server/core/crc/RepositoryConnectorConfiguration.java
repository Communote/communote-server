package com.communote.server.core.crc;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RepositoryConnectorConfiguration {

    private String connectorId;
    private boolean supportsMetadata;

    /**
     * Create a new configuration.
     * 
     * @param connectorId
     *            the ID of the connector
     * @param supportsMetadata
     *            whether the connector supports additional metadata
     */
    public RepositoryConnectorConfiguration(String connectorId, boolean supportsMetadata) {
        this.supportsMetadata = supportsMetadata;
        this.connectorId = connectorId;
    }

    /**
     * @return the ID of the connector
     */
    // TODO this should probably be a method of the connector itself and not of the configuration
    public String getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public void setSupportsMetadata(boolean supports) {
        this.supportsMetadata = supports;
    }

}
