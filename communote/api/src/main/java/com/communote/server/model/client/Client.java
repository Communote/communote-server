package com.communote.server.model.client;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Locale;

/**
 * A client represents an enterprise account (or tenant) of Communote. A standalone installation has
 * only one client, but an installation with support for multitenancy, like the Communote SaaS
 * service, can have an arbitrary amount of clients.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Client implements Serializable {
    /**
     * Constructs new instances of {@link Client}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link Client}.
         */
        public static Client newInstance() {
            return new Client();
        }

        /**
         * Constructs a new instance of {@link Client}, taking all required and/or read-only
         * properties as arguments.
         */
        public static Client newInstance(String clientId, String name, ClientStatus clientStatus) {
            final Client entity = new Client();
            entity.setClientId(clientId);
            entity.setName(name);
            entity.setClientStatus(clientStatus);
            return entity;
        }

        /**
         * Constructs a new instance of {@link Client}, taking all possible properties (except the
         * identifier(s))as arguments.
         */
        public static Client newInstance(String clientId, String name, ClientStatus clientStatus,
                String creationVersion, Timestamp creationTime, String creationRevision) {
            final Client entity = new Client();
            entity.setClientId(clientId);
            entity.setName(name);
            entity.setClientStatus(clientStatus);
            entity.setCreationVersion(creationVersion);
            entity.setCreationTime(creationTime);
            entity.setCreationRevision(creationRevision);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -823966614744779089L;

    private String clientId;

    private String name;

    private ClientStatus clientStatus;

    private String creationVersion;

    private Timestamp creationTime;

    private String creationRevision;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("clientId='");
        sb.append(clientId);
        sb.append("', ");

        sb.append("name='");
        sb.append(name);
        sb.append("', ");

        sb.append("clientStatus='");
        sb.append(clientStatus);
        sb.append("', ");

        sb.append("creationVersion='");
        sb.append(creationVersion);
        sb.append("', ");

        sb.append("creationTime='");
        sb.append(creationTime);
        sb.append("', ");

        sb.append("creationRevision='");
        sb.append(creationRevision);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Client instance and all identifiers for this
     * entity equal the identifiers of the argument entity. Returns <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Client)) {
            return false;
        }
        final Client that = (Client) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * @return a unique identifier of the client
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * @return the current status of the client
     */
    public ClientStatus getClientStatus() {
        return this.clientStatus;
    }

    /**
     * @return the revision of Communote at the time the client was created. This can be null for
     *         older installations.
     */
    public String getCreationRevision() {
        return this.creationRevision;
    }

    /**
     * @return the time the client was created
     */
    public Timestamp getCreationTime() {
        return this.creationTime;
    }

    /**
     * @return the version of Communote (e.g. 1.0.1.2222) at the time the client was created. This
     *         can be null for older installations.
     */
    public String getCreationVersion() {
        return this.creationVersion;
    }

    /**
     * @return unique numeric ID of the client
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @return a short name describing the client. This name can for instance be used as title in
     *         frontend pages.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    /**
     * Test whether the client has the given status
     *
     * @param status
     *            the status to test
     * @return true if the client has the status
     */
    public boolean hasStatus(ClientStatus status) {
        return getClientStatus() != null && getClientStatus().equals(status);
    }

    public void setClientId(String clientId) {
        // force lower case client ID
        if (clientId != null) {
            this.clientId = clientId.toLowerCase(Locale.ENGLISH);
        }
    }

    public void setClientStatus(ClientStatus clientStatus) {
        this.clientStatus = clientStatus;
    }

    public void setCreationRevision(String creationRevision) {
        this.creationRevision = creationRevision;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public void setCreationVersion(String creationVersion) {
        this.creationVersion = creationVersion;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

}