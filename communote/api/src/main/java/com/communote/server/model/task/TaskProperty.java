package com.communote.server.model.task;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class TaskProperty implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.task.TaskProperty}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.task.TaskProperty}.
         */
        public static com.communote.server.model.task.TaskProperty newInstance() {
            return new com.communote.server.model.task.TaskPropertyImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.task.TaskProperty}, taking
         * all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.task.TaskProperty newInstance(String propertyKey,
                String propertyValue) {
            final com.communote.server.model.task.TaskProperty entity = new com.communote.server.model.task.TaskPropertyImpl();
            entity.setPropertyKey(propertyKey);
            entity.setPropertyValue(propertyValue);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7831939565822674441L;

    private String propertyKey;

    private String propertyValue;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("propertyKey='");
        sb.append(propertyKey);
        sb.append("', ");

        sb.append("propertyValue='");
        sb.append(propertyValue);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an TaskProperty instance and all identifiers for
     * this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TaskProperty)) {
            return false;
        }
        final TaskProperty that = (TaskProperty) object;
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
     * Key of the property.
     * </p>
     */
    public String getPropertyKey() {
        return this.propertyKey;
    }

    /**
     * <p>
     * Value of the property.
     * </p>
     */
    public String getPropertyValue() {
        return this.propertyValue;
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

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}