package com.communote.server.api.core.attachment;

import com.communote.server.api.core.common.IdentifiableEntityData;

/**
 * Value object for attachments.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentData extends IdentifiableEntityData {
    private static final long serialVersionUID = -2127376532550324997L;

    private String contentId;

    private String mimeTyp;

    private String fileName;

    private String repositoryId;

    private Long size;

    private Long noteId;

    /**
     * Does nothing.
     */
    public AttachmentData() {
        // Do nothing.
    }

    public AttachmentData(String contentId, String mimeTyp, String fileName, String repositoryId) {
        this.contentId = contentId;
        this.mimeTyp = mimeTyp;
        this.fileName = fileName;
        this.repositoryId = repositoryId;
    }

    /**
     * @return the contentId
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * @return the file name of the attachment
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the MIME type of the attachment
     */
    public String getMimeTyp() {
        return mimeTyp;
    }

    /**
     *
     * @return id of the note the attachment is associated with. null if no note connected.
     */
    public Long getNoteId() {
        return noteId;
    }

    /**
     * @return the repositoryId
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * @return the size of the attachment in bytes
     */
    public Long getSize() {
        return size;
    }

    /**
     * @param contentId
     *            the contentId to set
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @param mimeTyp
     *            the mimeTyp to set
     */
    public void setMimeTyp(String mimeTyp) {
        this.mimeTyp = mimeTyp;
    }

    /**
     *
     * @param noteId
     *            id of the note the attachment is associated with. null if no note connected.
     */
    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    /**
     * @param repositoryId
     *            the repositoryId to set
     */
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    /**
     * @param size
     *            the size to set
     */
    public void setSize(Long size) {
        this.size = size;
    }

}