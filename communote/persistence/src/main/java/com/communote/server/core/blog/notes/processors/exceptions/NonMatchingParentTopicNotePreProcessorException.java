package com.communote.server.core.blog.notes.processors.exceptions;

import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * This message can be thrown, when the parent note has another topic than the expected topic.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NonMatchingParentTopicNotePreProcessorException extends NoteStoringPreProcessorException {

    private static final long serialVersionUID = 2613626009699840587L;

    /**
     * Constructs a new instance of ParentDoesNotExistsNotePreProcessorException
     *
     * @param message
     *            A message describing the error.
     */
    public NonMatchingParentTopicNotePreProcessorException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of ParentDoesNotExistsNotePreProcessorException
     *
     * @param message
     *            A message describing the error.
     *
     * @param throwable
     *            A root cause.
     */
    public NonMatchingParentTopicNotePreProcessorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
