package com.communote.server.model.client;

/**
 * <p>
 * Stores statistical information of the client.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ClientStatistic implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.client.ClientStatistic}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.client.ClientStatistic}.
         */
        public static com.communote.server.model.client.ClientStatistic newInstance() {
            return new com.communote.server.model.client.ClientStatisticImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.client.ClientStatistic},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.client.ClientStatistic newInstance(
                long repositorySize) {
            final com.communote.server.model.client.ClientStatistic entity = new com.communote.server.model.client.ClientStatisticImpl();
            entity.setRepositorySize(repositorySize);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5728586327227185205L;

    private long repositorySize;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("repositorySize='");
        sb.append(repositorySize);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an ClientStatistic instance and all identifiers
     * for this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ClientStatistic)) {
            return false;
        }
        final ClientStatistic that = (ClientStatistic) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * Holds the current size of the repository in MB.
     * </p>
     */
    public long getRepositorySize() {
        return this.repositorySize;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRepositorySize(long repositorySize) {
        this.repositorySize = repositorySize;
    }
}