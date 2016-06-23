package com.communote.server.model.task;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class TaskExecution implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.task.TaskExecution}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.task.TaskExecution}.
         */
        public static com.communote.server.model.task.TaskExecution newInstance() {
            return new com.communote.server.model.task.TaskExecutionImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.task.TaskExecution},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.task.TaskExecution newInstance(
                String instanceName, com.communote.server.model.task.Task task) {
            final com.communote.server.model.task.TaskExecution entity = new com.communote.server.model.task.TaskExecutionImpl();
            entity.setInstanceName(instanceName);
            entity.setTask(task);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 4919724621731848536L;

    private String instanceName;

    private Long id;

    private com.communote.server.model.task.Task task;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("instanceName='");
        sb.append(instanceName);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an TaskExecution instance and all identifiers
     * for this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TaskExecution)) {
            return false;
        }
        final TaskExecution that = (TaskExecution) object;
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
     * Name of the node the task is running on.
     * </p>
     */
    public String getInstanceName() {
        return this.instanceName;
    }

    /**
     * 
     */
    public com.communote.server.model.task.Task getTask() {
        return this.task;
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

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public void setTask(com.communote.server.model.task.Task task) {
        this.task = task;
    }
}