package com.communote.server.api.core.external;

import com.communote.server.model.blog.BlogRole;

/**
 * Thrown to indicate that a topic has to be assigned to an external object, but isn't.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalObjectNotAssignedException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5152688816528749416L;

    private String externalId;

    private String externalSystemId;

    /**
     * Create a new exception
     *
     * @param message
     *            The message.
     * @param blogId
     *            The blogId.
     * @param userRole
     *            The users role.
     * @param externalId
     *            identifier of external object in external system
     * @param externalSystemId
     *            identifier of external system
     */
    public ExternalObjectNotAssignedException(String message, Long blogId, BlogRole userRole,
            String externalId, String externalSystemId) {
        super(message);
        this.externalId = externalId;
        this.externalSystemId = externalSystemId;
    }

    /**
     * Get the identifier of the external object.
     *
     * @return identifier of the external object in the external system that should have been
     *         assigned to the topic
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Get the external system identifier.
     *
     * @return identifier of the external system.
     */
    public String getExternalSystemId() {
        return externalSystemId;
    }

    /**
     * Set the external identifier.
     *
     * @param externalId
     *            identifier of the external object in the external system that should have been
     *            assigned to the topic
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * Set the external system identifier.
     *
     * @param externalSystemId
     *            identifier of the external system.
     */
    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }
}
