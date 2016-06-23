package com.communote.server.core.note.processor;

import java.io.Serializable;

/**
 * Helper to hold the short and full content of a note in an object.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class NoteContentHolder implements Serializable {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final String fullContent;
    private final String shortContent;
    private final long lastModificationTimestamp;

    /**
     * Create a new content wrapper.
     * 
     * @param shortContent
     *            the shortened content of the null. Can be null if the content was short enough and
     *            thus needn't to be shortened.
     * @param fullContent
     *            fullContent the content
     * @param lastModificationTimestamp
     *            the timestamp of the last modification of the note the content belongs to
     */
    public NoteContentHolder(String shortContent, String fullContent, long lastModificationTimestamp) {
        this.shortContent = shortContent;
        this.fullContent = fullContent;
        this.lastModificationTimestamp = lastModificationTimestamp;
    }

    /**
     * @return fullContent the content
     */
    public String getFullContent() {
        return fullContent;
    }

    /**
     * @return the timestamp of the last modification of the note the content belongs to
     */
    public long getLastModificationTimestamp() {
        return lastModificationTimestamp;
    }

    /**
     * @return the shortened content or null if the content was short enough and thus needn't to be
     *         shortened
     */
    public String getShortContent() {
        return shortContent;
    }

}
