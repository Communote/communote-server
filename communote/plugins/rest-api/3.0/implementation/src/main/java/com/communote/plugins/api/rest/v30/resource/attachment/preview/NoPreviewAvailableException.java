package com.communote.plugins.api.rest.v30.resource.attachment.preview;

/**
 * Exception to note, that no preview is available for the requested attachment.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoPreviewAvailableException extends Exception {

    private static final long serialVersionUID = -8970023348833537496L;

    private final Long attachmentId;

    /**
     * Constructor.
     * 
     * @param attachmentId
     *            Id of the invalid attachment.
     */
    public NoPreviewAvailableException(Long attachmentId) {
        super("For the given attachment with id " + attachmentId + " is no preview available.");
        this.attachmentId = attachmentId;
    }

    /**
     * @return the attachmentId
     */
    public Long getAttachmentId() {
        return attachmentId;
    }
}
