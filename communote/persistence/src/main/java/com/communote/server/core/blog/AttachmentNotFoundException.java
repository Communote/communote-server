package com.communote.server.core.blog;

import com.communote.server.api.core.common.NotFoundException;

/**
 * Exception to be thrown if an attachment cannot be found.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentNotFoundException extends NotFoundException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6151922644770525610L;

    private final Long attachmentId;

    /**
     * Constructs a new exception with default message
     * 
     * @param attachmentId
     *            The ID of the attachment that was not found
     */
    public AttachmentNotFoundException(Long attachmentId) {
        this(attachmentId, (Throwable) null);
    }

    /**
     * Construct a new exception with details
     * 
     * @param attachmentId
     *            The ID of the attachment that was not found
     * @param message
     *            the detail message
     */
    public AttachmentNotFoundException(Long attachmentId, String message) {
        this(attachmentId, message, null);
    }

    /**
     * Construct a new exception with details
     * 
     * @param attachmentId
     *            The ID of the attachment that was not found
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public AttachmentNotFoundException(Long attachmentId, String message, Throwable throwable) {
        super(message, throwable);
        this.attachmentId = attachmentId;
    }

    /**
     * Constructs a new exception with default message
     * 
     * @param attachmentId
     *            The ID of the attachment that was not found
     * @param cause
     *            the cause of the exception
     */
    public AttachmentNotFoundException(Long attachmentId, Throwable cause) {
        this(attachmentId, "Attachment with ID " + attachmentId + " not found", cause);
    }

    /**
     * @return ID of the attachment that was not found
     */
    public Long getAttachmentId() {
        return attachmentId;
    }

}
