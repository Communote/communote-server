package com.communote.server.model.tag;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractTagCategory implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -5942496379570663269L;

    private String name;

    private String prefix;

    private String description;

    private boolean multipleTags;

    private Long id;

    private java.util.List<com.communote.server.model.tag.CategorizedTag> tags = new java.util.ArrayList<com.communote.server.model.tag.CategorizedTag>();

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

        sb.append("prefix='");
        sb.append(prefix);
        sb.append("', ");

        sb.append("description='");
        sb.append(description);
        sb.append("', ");

        sb.append("multipleTags='");
        sb.append(multipleTags);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an AbstractTagCategory instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof AbstractTagCategory)) {
            return false;
        }
        final AbstractTagCategory that = (AbstractTagCategory) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public String getDescription() {
        return this.description;
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
    public String getName() {
        return this.name;
    }

    /**
     * 
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * 
     */
    public java.util.List<com.communote.server.model.tag.CategorizedTag> getTags() {
        return this.tags;
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
    public boolean isMultipleTags() {
        return this.multipleTags;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMultipleTags(boolean multipleTags) {
        this.multipleTags = multipleTags;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setTags(java.util.List<com.communote.server.model.tag.CategorizedTag> tags) {
        this.tags = tags;
    }
}