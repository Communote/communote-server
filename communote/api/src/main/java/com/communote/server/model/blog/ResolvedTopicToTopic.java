package com.communote.server.model.blog;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ResolvedTopicToTopic implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.blog.ResolvedTopicToTopic}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.blog.ResolvedTopicToTopic}
         * .
         */
        public static com.communote.server.model.blog.ResolvedTopicToTopic newInstance() {
            return new com.communote.server.model.blog.ResolvedTopicToTopicImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.blog.ResolvedTopicToTopic}
         * , taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.blog.ResolvedTopicToTopic newInstance(
                Long parentTopicId, Long childTopicId, String topicPath) {
            final com.communote.server.model.blog.ResolvedTopicToTopic entity = new com.communote.server.model.blog.ResolvedTopicToTopicImpl();
            entity.setParentTopicId(parentTopicId);
            entity.setChildTopicId(childTopicId);
            entity.setTopicPath(topicPath);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3101250506076873312L;

    private Long parentTopicId;

    private Long childTopicId;

    private String topicPath;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("parentTopicId='");
        sb.append(parentTopicId);
        sb.append("', ");

        sb.append("childTopicId='");
        sb.append(childTopicId);
        sb.append("', ");

        sb.append("topicPath='");
        sb.append(topicPath);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an ResolvedTopicToTopic instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ResolvedTopicToTopic)) {
            return false;
        }
        final ResolvedTopicToTopic that = (ResolvedTopicToTopic) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public Long getChildTopicId() {
        return this.childTopicId;
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
    public Long getParentTopicId() {
        return this.parentTopicId;
    }

    /**
     * 
     */
    public String getTopicPath() {
        return this.topicPath;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setChildTopicId(Long childTopicId) {
        this.childTopicId = childTopicId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setParentTopicId(Long parentTopicId) {
        this.parentTopicId = parentTopicId;
    }

    public void setTopicPath(String topicPath) {
        this.topicPath = topicPath;
    }
}