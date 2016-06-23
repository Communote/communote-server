package com.communote.server.core.vo.blog;

import java.util.Collection;
import java.util.HashSet;

/**
 * Contains details about the post creation / modification result.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteModificationResult {

    private NoteModificationStatus status;

    private Collection<String> unresolvableBlogs = new HashSet<String>();

    private Collection<String> unwritableBlogs = new HashSet<String>();

    private Collection<String> tagsWithProblems = new HashSet<String>();

    private Long noteId;

    private Long version;

    private boolean direct;

    private com.communote.server.core.vo.uti.UserNotificationResult userNotificationResult;

    private Throwable errorCause;

    private String messageKey;

    /**
     * Creates an empty result object
     */
    public NoteModificationResult() {
        this.status = null;
        this.noteId = null;
        this.version = null;
        this.direct = false;
        this.userNotificationResult = null;
    }

    /**
     * Creates an result object.
     * 
     * @param status
     *            the status of the creation / modification
     * @param unresolvableBlogs
     *            collection of blog aliases that could not be resolved to existing blogs
     * @param unwritableBlogs
     *            collection of cross post target blog aliases the user has no write access to.
     * @param noteId
     *            ID of the created note
     * @param version
     *            the version of the note
     * @param direct
     *            true if the note is a direct message
     * @param userNotificationResult
     *            the notification result
     */
    public NoteModificationResult(
            com.communote.server.core.vo.blog.NoteModificationStatus status,
            Collection<String> unresolvableBlogs, Collection<String> unwritableBlogs, Long noteId,
            Long version, boolean direct,
            com.communote.server.core.vo.uti.UserNotificationResult userNotificationResult) {
        this.status = status;
        this.unresolvableBlogs = unresolvableBlogs;
        this.unwritableBlogs = unwritableBlogs;
        this.noteId = noteId;
        this.version = version;
        this.direct = direct;
        this.userNotificationResult = userNotificationResult;
    }

    /**
     * @return the cause of an internal error if available. There was an internal error if the
     *         status is {@link NoteModificationStatus#SYSTEM_ERROR}
     */
    public Throwable getErrorCause() {
        return errorCause;
    }

    /**
     * @return A message key, which could be used to displayed a message. Might be null.
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * @return the ID of the created note.
     */
    public Long getNoteId() {
        return this.noteId;
    }

    /**
     * @return the status of the creation / modification. In case the status is
     *         {@link NoteModificationStatus#SYSTEM_ERROR} {@link #getErrorCause()} might lead to
     *         more details.
     */
    public NoteModificationStatus getStatus() {
        return this.status;
    }

    /**
     * @return the tagsWithProblems
     */
    public Collection<String> getTagsWithProblems() {
        return tagsWithProblems;
    }

    /**
     * @return collection of blog aliases that could not be resolved to existing blogs
     */
    public Collection<String> getUnresolvableBlogs() {
        return this.unresolvableBlogs;
    }

    /**
     * @return collection of cross post target blog aliases the user has no write access to.
     */
    public Collection<String> getUnwritableBlogs() {
        return this.unwritableBlogs;
    }

    /**
     * @return the userNotificationResult
     */
    public com.communote.server.core.vo.uti.UserNotificationResult getUserNotificationResult() {
        return this.userNotificationResult;
    }

    /**
     * @return the version of the note
     */
    public Long getVersion() {
        return this.version;
    }

    /**
     * @return whether the created note is a direct message
     */
    public boolean isDirect() {
        return this.direct;
    }

    /**
     * Set whether the created note is a direct message.
     * 
     * @param direct
     *            true if the note is a direct message
     */
    public void setDirect(boolean direct) {
        this.direct = direct;
    }

    /**
     * @param cause
     *            set the cause of an internal error. This should be set if status is
     *            {@link NoteModificationStatus#SYSTEM_ERROR}
     */
    public void setErrorCause(Throwable cause) {
        this.errorCause = cause;
    }

    /**
     * @param messageKey
     *            A message key, which could be used to display a message for this result.
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * @param noteId
     *            ID of the created note
     */
    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    /**
     * @param status
     *            the status of the note creation / modification
     */
    public void setStatus(NoteModificationStatus status) {
        this.status = status;
    }

    /**
     * @param tagsWithProblems
     *            the tagsWithProblems to set
     */
    public void setTagsWithProblems(Collection<String> tagsWithProblems) {
        this.tagsWithProblems = tagsWithProblems;
    }

    /**
     * 
     * @param unresolvableBlogs
     *            collection of blog aliases that could not be resolved to existing blogs
     */
    public void setUnresolvableBlogs(Collection<String> unresolvableBlogs) {
        this.unresolvableBlogs = unresolvableBlogs;
    }

    /**
     * 
     * @param unwritableBlogs
     *            collection of cross post target blog aliases the user has no write access to
     */
    public void setUnwritableBlogs(Collection<String> unwritableBlogs) {
        this.unwritableBlogs = unwritableBlogs;
    }

    /**
     * Sets the userNotificationResult
     * 
     * @param userNotificationResult
     *            the notification result
     */
    public void setUserNotificationResult(
            com.communote.server.core.vo.uti.UserNotificationResult userNotificationResult) {
        this.userNotificationResult = userNotificationResult;
    }

    /**
     * @param version
     *            the version of the created note
     */
    public void setVersion(Long version) {
        this.version = version;
    }
}