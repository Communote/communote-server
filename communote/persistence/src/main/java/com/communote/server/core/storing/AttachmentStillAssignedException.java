package com.communote.server.core.storing;

/**
 * Thrown to indicate that an attachment cannot be deleted because it is still assigned to a note.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AttachmentStillAssignedException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final Long attachmentId;
    private final Long noteId;

    /**
     * Creates a new instance of the exception
     * 
     * @param message
     *            a detailed message
     * @param attachmentId
     *            the ID of the attachment that is still assigned to a note
     * @param noteId
     *            the ID of the note the attachment is still assigned to
     */
    public AttachmentStillAssignedException(String message, Long attachmentId, Long noteId) {
        super(message);
        this.attachmentId = attachmentId;
        this.noteId = noteId;
    }

    /**
     * @return the ID of the attachment that is still assigned to a note
     */
    public Long getAttachmentId() {
        return this.attachmentId;
    }

    /**
     * @return the ID of the note the attachment is still assigned to
     */
    public Long getNoteId() {
        return this.noteId;
    }
}
