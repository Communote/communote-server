package com.communote.server.core.external;

/**
 * Thrown to indicate that the external object is already assigned to a blog.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ExternalObjectAlreadyAssignedException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final String message;
    private final String externalSystemId;

    private final String externalId;

    private final Long blogId;

    /**
     * Create a new exception
     * 
     * @param message
     *            a detail message
     * @param externalSystemId
     *            the ID of the external system of the external object
     * @param externalId
     *            the ID of the external object within the external system
     * @param blogId
     *            the ID of the blog to which the external object is already assigned
     */
    public ExternalObjectAlreadyAssignedException(String message, String externalSystemId,
            String externalId, Long blogId) {
        this.message = message;
        this.externalSystemId = externalSystemId;
        this.externalId = externalId;
        this.blogId = blogId;
    }

    /**
     * @return the ID of the blog to which the external object is already assigned
     */
    public Long getBlogId() {
        return blogId;
    }

    /**
     * @return the ID of the external object within the external system
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * @return the ID of the external system of the external object
     */
    public String getExternalSystemId() {
        return externalSystemId;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
