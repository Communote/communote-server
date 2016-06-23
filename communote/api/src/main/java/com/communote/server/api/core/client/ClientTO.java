package com.communote.server.api.core.client;

import java.util.Date;

import com.communote.server.model.client.ClientStatus;

/**
 * TO for Clients.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientTO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6773865095683414758L;

    private String clientId;

    private String name;

    private ClientStatus clientStatus;

    private String creationVersion;

    private Date creationDate;

    private String creationRevision;

    private Long id;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ClientTO)) {
            return false;
        }
        final ClientTO that = (ClientTO) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @return the clientStatus
     */
    public ClientStatus getClientStatus() {
        return clientStatus;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @return the creationRevision
     */
    public String getCreationRevision() {
        return creationRevision;
    }

    /**
     * @return the creationVersion
     */
    public String getCreationVersion() {
        return creationVersion;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return A hash code based on this entity's identifiers.
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());
        return hashCode;
    }

    /**
     * Method to check if this client has the given status.
     *
     * @param status
     *            The status to check.
     * @return True, if the given status is not null and equals the clients status.
     */
    public boolean hasStatus(ClientStatus status) {
        return status != null && status.equals(clientStatus);
    }

    /**
     * Convenience method to test if a client is active
     *
     * @return true if the client has the status ACTIVE
     */
    public boolean isActive() {
        return hasStatus(ClientStatus.ACTIVE);
    }

    /**
     * @param clientId
     *            the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @param clientStatus
     *            the clientStatus to set
     */
    public void setClientStatus(ClientStatus clientStatus) {
        this.clientStatus = clientStatus;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @param creationRevision
     *            the creationRevision to set
     */
    public void setCreationRevision(String creationRevision) {
        this.creationRevision = creationRevision;
    }

    /**
     * @param creationVersion
     *            the creationVersion to set
     */
    public void setCreationVersion(String creationVersion) {
        this.creationVersion = creationVersion;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}