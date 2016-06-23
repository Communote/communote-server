package com.communote.server.model.user;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class NavigationItem implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.user.NavigationItem}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.user.NavigationItem}.
         */
        public static com.communote.server.model.user.NavigationItem newInstance() {
            return new com.communote.server.model.user.NavigationItemImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.NavigationItem},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.NavigationItem newInstance(String data,
                int itemIndex, java.sql.Timestamp lastAccessDate, String name,
                com.communote.server.model.user.User owner) {
            final com.communote.server.model.user.NavigationItem entity = new com.communote.server.model.user.NavigationItemImpl();
            entity.setData(data);
            entity.setItemIndex(itemIndex);
            entity.setLastAccessDate(lastAccessDate);
            entity.setName(name);
            entity.setOwner(owner);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1953109113128398178L;

    private String data;

    private int itemIndex = 0;

    private java.sql.Timestamp lastAccessDate;

    private String name;

    private Long id;

    private com.communote.server.model.user.User owner;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("data='");
        sb.append(data);
        sb.append("', ");

        sb.append("itemIndex='");
        sb.append(itemIndex);
        sb.append("', ");

        sb.append("lastAccessDate='");
        sb.append(lastAccessDate);
        sb.append("', ");

        sb.append("name='");
        sb.append(name);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an NavigationItem instance and all identifiers
     * for this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof NavigationItem)) {
            return false;
        }
        final NavigationItem that = (NavigationItem) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public String getData() {
        return this.data;
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
    public int getItemIndex() {
        return this.itemIndex;
    }

    /**
     * 
     */
    public java.sql.Timestamp getLastAccessDate() {
        return this.lastAccessDate;
    }

    /**
     * 
     */
    public String getName() {
        return this.name;
    }

    /**
     * 
     */
    public com.communote.server.model.user.User getOwner() {
        return this.owner;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public void setLastAccessDate(java.sql.Timestamp lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(com.communote.server.model.user.User owner) {
        this.owner = owner;
    }
}