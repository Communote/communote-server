package com.communote.server.model.user;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserImage implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.user.UserImage}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.user.UserImage}.
         */
        public static com.communote.server.model.user.UserImage newInstance() {
            return new com.communote.server.model.user.UserImageImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.UserImage}, taking
         * all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.UserImage newInstance(byte[] image) {
            final com.communote.server.model.user.UserImage entity = new com.communote.server.model.user.UserImageImpl();
            entity.setImage(image);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 928721763654410081L;

    private byte[] image;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("image='");
        sb.append(image);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an UserImage instance and all identifiers for
     * this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof UserImage)) {
            return false;
        }
        final UserImage that = (UserImage) object;
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
     * 
     */
    public byte[] getImage() {
        return this.image;
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

    public void setImage(byte[] image) {
        this.image = image;
    }
}