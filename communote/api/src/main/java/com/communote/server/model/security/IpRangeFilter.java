package com.communote.server.model.security;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class IpRangeFilter implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.security.IpRangeFilter}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.security.IpRangeFilter}.
         */
        public static com.communote.server.model.security.IpRangeFilter newInstance() {
            return new com.communote.server.model.security.IpRangeFilterImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.security.IpRangeFilter},
         * taking all required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.security.IpRangeFilter newInstance(String name,
                boolean enabled) {
            final com.communote.server.model.security.IpRangeFilter entity = new com.communote.server.model.security.IpRangeFilterImpl();
            entity.setName(name);
            entity.setEnabled(enabled);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.security.IpRangeFilter},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.security.IpRangeFilter newInstance(String name,
                boolean enabled,
                java.util.Set<com.communote.server.model.security.IpRange> includes,
                java.util.Set<com.communote.server.model.security.IpRangeChannel> channels,
                java.util.Set<com.communote.server.model.security.IpRange> excludes) {
            final com.communote.server.model.security.IpRangeFilter entity = new com.communote.server.model.security.IpRangeFilterImpl();
            entity.setName(name);
            entity.setEnabled(enabled);
            entity.setIncludes(includes);
            entity.setChannels(channels);
            entity.setExcludes(excludes);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -5741365785728960391L;

    private String name;

    private boolean enabled;

    private Long id;

    private java.util.Set<com.communote.server.model.security.IpRange> includes = new java.util.HashSet<com.communote.server.model.security.IpRange>();

    private java.util.Set<com.communote.server.model.security.IpRangeChannel> channels = new java.util.HashSet<com.communote.server.model.security.IpRangeChannel>();

    private java.util.Set<com.communote.server.model.security.IpRange> excludes = new java.util.HashSet<com.communote.server.model.security.IpRange>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("name='");
        sb.append(name);
        sb.append("', ");

        sb.append("enabled='");
        sb.append(enabled);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an IpRangeFilter instance and all identifiers
     * for this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof IpRangeFilter)) {
            return false;
        }
        final IpRangeFilter that = (IpRangeFilter) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public java.util.Set<com.communote.server.model.security.IpRangeChannel> getChannels() {
        return this.channels;
    }

    /**
     * 
     */
    public java.util.Set<com.communote.server.model.security.IpRange> getExcludes() {
        return this.excludes;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * 
     */
    public java.util.Set<com.communote.server.model.security.IpRange> getIncludes() {
        return this.includes;
    }

    /**
     * 
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    /**
     * 
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setChannels(
            java.util.Set<com.communote.server.model.security.IpRangeChannel> channels) {
        this.channels = channels;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setExcludes(java.util.Set<com.communote.server.model.security.IpRange> excludes) {
        this.excludes = excludes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIncludes(java.util.Set<com.communote.server.model.security.IpRange> includes) {
        this.includes = includes;
    }

    public void setName(String name) {
        this.name = name;
    }
}