package com.communote.server.api.core.blog;

/**
 * Thrown if a blog/topic does not exist.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogNotFoundException extends com.communote.server.api.core.common.NotFoundException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3098707123141052520L;

    private Long blogId;

    private String blogNameId;

    /**
     *
     * @param id
     *            the ID for which no blog could be found
     */
    public BlogNotFoundException(Long id) {
        super("Blog with ID " + id + " does not exist");
        this.blogId = id;
    }

    /**
     * Constructs a new instance of BlogNotFoundException
     *
     */
    public BlogNotFoundException(String message, Long blogId, String blogNameId) {
        super(message);
        this.blogId = blogId;
        this.blogNameId = blogNameId;
    }

    /**
     * Constructs a new instance of BlogNotFoundException
     *
     */
    public BlogNotFoundException(String message, Throwable throwable, Long blogId, String blogNameId) {
        super(message, throwable);
        this.blogId = blogId;
        this.blogNameId = blogNameId;
    }

    /**
     * @return The blog ID which could not be resolved to an existing blog. This parameter will be
     *         null if the blogNameId is set.
     */
    public Long getBlogId() {
        return this.blogId;
    }

    /**
     * @return the blog name identifier/alias which could not be resolved to an existing blog. This
     *         parameter will be null if the blogId is set.
     */
    public String getBlogNameId() {
        return this.blogNameId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public void setBlogNameId(String blogNameId) {
        this.blogNameId = blogNameId;
    }

}
