package com.communote.server.core.crc;

import com.communote.server.persistence.crc.ContentRepositoryException;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RepositoryConnectorNotFoundException extends ContentRepositoryException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String connectorId;
    private final Class<? extends RepositoryConnector> targetClass;

    public RepositoryConnectorNotFoundException(String connectorId,
            Class<? extends RepositoryConnector> targetClass, String message) {
        super(message);
        this.connectorId = connectorId;
        this.targetClass = targetClass;
    }

    public RepositoryConnectorNotFoundException(String connectorId, String message) {
        this(connectorId, null, message);
    }

    public String getConnectorId() {
        return connectorId;
    }

    /**
     * 
     * @return if set the repo has been found but not for this target class
     */
    public Class<? extends RepositoryConnector> getTargetClass() {
        return targetClass;
    }

}