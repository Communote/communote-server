package com.communote.plugins.activity.base.task;

/**
 * Occurs in case a note id is detected for deletion that already has been handled in the past. Then
 * this exception is thrown to avoid infinite loops.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class DuplicateNoteIdDetectedForDeletionException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public DuplicateNoteIdDetectedForDeletionException(String message) {
        super(message);
    }

}