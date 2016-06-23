package com.communote.server.model.user.note;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserNoteEntity implements java.io.Serializable,
        com.communote.server.model.property.Propertyable {
    /**
     * Constructs new instances of {@link com.communote.server.model.user.note.UserNoteEntity}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.user.note.UserNoteEntity}.
         */
        public static com.communote.server.model.user.note.UserNoteEntity newInstance() {
            return new com.communote.server.model.user.note.UserNoteEntityImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.note.UserNoteEntity},
         * taking all required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.user.note.UserNoteEntity newInstance(int rank,
                com.communote.server.model.note.Note note, com.communote.server.model.user.User user) {
            final com.communote.server.model.user.note.UserNoteEntity entity = new com.communote.server.model.user.note.UserNoteEntityImpl();
            entity.setRank(rank);
            entity.setNote(note);
            entity.setUser(user);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.note.UserNoteEntity},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.note.UserNoteEntity newInstance(int rank,
                com.communote.server.model.note.Note note,
                com.communote.server.model.user.User user,
                java.util.Set<com.communote.server.model.user.UserNoteProperty> properties) {
            final com.communote.server.model.user.note.UserNoteEntity entity = new com.communote.server.model.user.note.UserNoteEntityImpl();
            entity.setRank(rank);
            entity.setNote(note);
            entity.setUser(user);
            entity.setProperties(properties);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4815540309924957288L;

    private int rank;

    private Long id;

    private com.communote.server.model.note.Note note;

    private com.communote.server.model.user.User user;

    private java.util.Set<com.communote.server.model.user.UserNoteProperty> properties = new java.util.HashSet<com.communote.server.model.user.UserNoteProperty>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("rank='");
        sb.append(rank);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an UserNoteEntity instance and all identifiers
     * for this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof UserNoteEntity)) {
            return false;
        }
        final UserNoteEntity that = (UserNoteEntity) object;
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
    public com.communote.server.model.note.Note getNote() {
        return this.note;
    }

    /**
     * 
     */
    public java.util.Set<com.communote.server.model.user.UserNoteProperty> getProperties() {
        return this.properties;
    }

    /**
     * <p>
     * The rank for this user and note. The rank is bound based on a precision defined in the
     * specific implementation. The precision is used to convert the rank back to a value of 1 but
     * allows to store it as a numeric for better database integration.
     * </p>
     * <p>
     * To be save always use the setRankNormalized instead of setRank
     * </p>
     */
    public int getRank() {
        return this.rank;
    }

    /**
     * <p>
     * Get the normalized rank between 0..1 based on the stored rank and the RANK_PRECISION.
     * </p>
     */
    public abstract double getRankNormalized();

    /**
     * 
     */
    public com.communote.server.model.user.User getUser() {
        return this.user;
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

    public void setNote(com.communote.server.model.note.Note note) {
        this.note = note;
    }

    public void setProperties(
            java.util.Set<com.communote.server.model.user.UserNoteProperty> properties) {
        this.properties = properties;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * 
     */
    public abstract void setRankNormalized(double normalizedRank);

    public void setUser(com.communote.server.model.user.User user) {
        this.user = user;
    }
}