package com.communote.server.core.blog;

/**
 * Thrown to indicate that an attachment cannot be connected with a note because it is already
 * assigned to another note.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentAlreadyAssignedException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final Long attachmentId;
    private final String filename;

    /**
     * Creates a new instance.
     * 
     * @param message
     *            a message
     * 
     * @param attachmentId
     *            the ID of the attachment
     * @param filename
     *            the filename of the attachment
     */
    public AttachmentAlreadyAssignedException(String message, Long attachmentId, String filename) {
        super(message);
        this.attachmentId = attachmentId;
        this.filename = filename;
    }

    /**
     * @return the ID of the attachment
     */
    public Long getAttachmentId() {
        return this.attachmentId;
    }

    /**
     * @return the filename of the attachment
     */
    public String getFilename() {
        return this.filename;
    }
}
