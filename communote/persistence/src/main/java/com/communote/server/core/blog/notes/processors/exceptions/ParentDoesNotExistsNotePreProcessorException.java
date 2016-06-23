package com.communote.server.core.blog.notes.processors.exceptions;

import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * This message can be thrown, when a parent message should exists, but doesn't.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ParentDoesNotExistsNotePreProcessorException extends NoteStoringPreProcessorException {

    private static final long serialVersionUID = 2613626009699840587L;

    /**
     * Constructs a new instance of ParentDoesNotExistsNotePreProcessorException
     *
     * @param message
     *            A message describing the error.
     */
    public ParentDoesNotExistsNotePreProcessorException(String message) {
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
    public ParentDoesNotExistsNotePreProcessorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
