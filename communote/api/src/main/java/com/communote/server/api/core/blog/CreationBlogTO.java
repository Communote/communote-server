package com.communote.server.api.core.blog;

/**
 * This is a variant of the BlogTO with special fields need only for creation of new topics.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CreationBlogTO extends BlogTO {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Long parentTopicId;

    /**
     * Default constructor.
     */
    public CreationBlogTO() {
        super();
    }

    /**
     * Create a new TO and set the provided members
     *
     * @param allCanRead
     *            true if all users are allowed to read, false otherwise
     * @param allCanWrite
     *            true if all users are allowed to write, false otherwise
     * @param title
     *            the title of the blog
     * @param createSystemNotes
     *            whether notes with creation source 'SYSTEM' can be created in the blog
     */
    public CreationBlogTO(boolean allCanRead, boolean allCanWrite, String title,
            boolean createSystemNotes) {
        super(allCanRead, allCanWrite, title, createSystemNotes);
    }

    /**
     * @return Id of the parent topic or null if none.
     */
    public Long getParentTopicId() {
        return parentTopicId;
    }

    /**
     * @param parentTopicId
     *            Id of the parent topic for this topic.
     */
    public void setParentTopicId(Long parentTopicId) {
        this.parentTopicId = parentTopicId;
    }
}
