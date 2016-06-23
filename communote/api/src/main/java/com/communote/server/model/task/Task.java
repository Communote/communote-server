package com.communote.server.model.task;

/**
 *
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class Task implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.task.Task}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.task.Task}.
         */
        public static com.communote.server.model.task.Task newInstance() {
            return new com.communote.server.model.task.TaskImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.task.Task}, taking all
         * possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.task.Task newInstance(String uniqueName,
                Boolean active, com.communote.server.model.task.TaskStatus taskStatus,
                java.util.Date nextExecution, java.util.Date lastExecution, Long taskInterval,
                String handlerClassName,
                java.util.Set<com.communote.server.model.task.TaskProperty> properties) {
            final com.communote.server.model.task.Task entity = new com.communote.server.model.task.TaskImpl();
            entity.setUniqueName(uniqueName);
            entity.setActive(active);
            entity.setTaskStatus(taskStatus);
            entity.setNextExecution(nextExecution);
            entity.setLastExecution(lastExecution);
            entity.setTaskInterval(taskInterval);
            entity.setHandlerClassName(handlerClassName);
            entity.setProperties(properties);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.task.Task}, taking all
         * required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.task.Task newInstance(String uniqueName,
                Boolean active, com.communote.server.model.task.TaskStatus taskStatus,
                java.util.Date nextExecution, String handlerClassName) {
            final com.communote.server.model.task.Task entity = new com.communote.server.model.task.TaskImpl();
            entity.setUniqueName(uniqueName);
            entity.setActive(active);
            entity.setTaskStatus(taskStatus);
            entity.setNextExecution(nextExecution);
            entity.setHandlerClassName(handlerClassName);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1752854960833985241L;

    private String uniqueName;

    private Boolean active;

    private com.communote.server.model.task.TaskStatus taskStatus;

    private java.util.Date nextExecution;

    private java.util.Date lastExecution;

    private Long taskInterval;

    private String handlerClassName;

    private Long id;

    private java.util.Set<com.communote.server.model.task.TaskProperty> properties = new java.util.HashSet<com.communote.server.model.task.TaskProperty>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("uniqueName='");
        sb.append(uniqueName);
        sb.append("', ");

        sb.append("active='");
        sb.append(active);
        sb.append("', ");

        sb.append("taskStatus='");
        sb.append(taskStatus);
        sb.append("', ");

        sb.append("nextExecution='");
        sb.append(nextExecution);
        sb.append("', ");

        sb.append("lastExecution='");
        sb.append(lastExecution);
        sb.append("', ");

        sb.append("taskInterval='");
        sb.append(taskInterval);
        sb.append("', ");

        sb.append("handlerClassName='");
        sb.append(handlerClassName);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Task instance and all identifiers for this
     * entity equal the identifiers of the argument entity. Returns <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Task)) {
            return false;
        }
        final Task that = (Task) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     *
     */
    public Boolean getActive() {
        return this.active;
    }

    /**
     * <p>
     * Name of the handler class, which will excute this task.
     * </p>
     */
    public String getHandlerClassName() {
        return this.handlerClassName;
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
    public java.util.Date getLastExecution() {
        return this.lastExecution;
    }

    /**
     *
     */
    public java.util.Date getNextExecution() {
        return this.nextExecution;
    }

    /**
     *
     */
    public java.util.Set<com.communote.server.model.task.TaskProperty> getProperties() {
        return this.properties;
    }

    /**
     * <p>
     * The interval the task should be scheduled in milliseconds. If null or less than < 1 the task
     * will only be executed once.
     * </p>
     */
    public Long getTaskInterval() {
        return this.taskInterval;
    }

    /**
     * <p>
     * Status of the task.
     * </p>
     */
    public com.communote.server.model.task.TaskStatus getTaskStatus() {
        return this.taskStatus;
    }

    /**
     *
     */
    public String getUniqueName() {
        return this.uniqueName;
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

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setHandlerClassName(String handlerClassName) {
        this.handlerClassName = handlerClassName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLastExecution(java.util.Date lastExecution) {
        this.lastExecution = lastExecution;
    }

    public void setNextExecution(java.util.Date nextExecution) {
        this.nextExecution = nextExecution;
    }

    public void setProperties(java.util.Set<com.communote.server.model.task.TaskProperty> properties) {
        this.properties = properties;
    }

    public void setTaskInterval(Long taskInterval) {
        this.taskInterval = taskInterval;
    }

    public void setTaskStatus(com.communote.server.model.task.TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }
}