package com.communote.server.model.config;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ClientConfiguration implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.config.ClientConfiguration}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.ClientConfiguration}.
         */
        public static com.communote.server.model.config.ClientConfiguration newInstance() {
            return new com.communote.server.model.config.ClientConfigurationImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.ClientConfiguration}, taking all possible
         * properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.config.ClientConfiguration newInstance(
                byte[] logoImage, java.sql.Timestamp lastLogoImageModificationDate,
                String timeZoneId, com.communote.server.model.blog.Blog defaultBlog) {
            final com.communote.server.model.config.ClientConfiguration entity = new com.communote.server.model.config.ClientConfigurationImpl();
            entity.setLogoImage(logoImage);
            entity.setLastLogoImageModificationDate(lastLogoImageModificationDate);
            entity.setTimeZoneId(timeZoneId);
            entity.setDefaultBlog(defaultBlog);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7010006590367935155L;

    private byte[] logoImage;

    private java.sql.Timestamp lastLogoImageModificationDate;

    private String timeZoneId;

    private Long id;

    private com.communote.server.model.blog.Blog defaultBlog;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("logoImage='");
        sb.append(logoImage);
        sb.append("', ");

        sb.append("lastLogoImageModificationDate='");
        sb.append(lastLogoImageModificationDate);
        sb.append("', ");

        sb.append("timeZoneId='");
        sb.append(timeZoneId);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an ClientConfiguration instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ClientConfiguration)) {
            return false;
        }
        final ClientConfiguration that = (ClientConfiguration) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public com.communote.server.model.blog.Blog getDefaultBlog() {
        return this.defaultBlog;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * The last modification date of the client logo
     * </p>
     */
    public java.sql.Timestamp getLastLogoImageModificationDate() {
        return this.lastLogoImageModificationDate;
    }

    /**
     * 
     */
    public byte[] getLogoImage() {
        return this.logoImage;
    }

    /**
     * <p>
     * The time zone id used for the client.
     * </p>
     */
    public String getTimeZoneId() {
        return this.timeZoneId;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setDefaultBlog(com.communote.server.model.blog.Blog defaultBlog) {
        this.defaultBlog = defaultBlog;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLastLogoImageModificationDate(java.sql.Timestamp lastLogoImageModificationDate) {
        this.lastLogoImageModificationDate = lastLogoImageModificationDate;
    }

    public void setLogoImage(byte[] logoImage) {
        this.logoImage = logoImage;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }
}