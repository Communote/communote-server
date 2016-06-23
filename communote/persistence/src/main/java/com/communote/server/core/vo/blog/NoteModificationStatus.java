package com.communote.server.core.vo.blog;

/**
 * Enumeration for possible modification result status.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum NoteModificationStatus {

    /**
     * Note creation / modification was successful but there might be some warnings.
     */
    SUCCESS,

    /**
     * Note creation failed because the note limit was reached.
     */
    LIMIT_REACHED,

    /**
     * Note creation / modification failed because some users to be notified do not exist or have no
     * read access to the blog.
     */
    NOTIFICATION_ERROR,

    /**
     * Note creation / modification failed because some of the cross posting blogs do not exist or
     * are not writable.
     */
    CROSSPOST_ERROR,

    /**
     * Note creation / modification failed because of some internal error.
     */
    SYSTEM_ERROR,

    /** Denotes, that one of the attachments isn't available anymore. */
    MISSING_ATTACHMENT;
}