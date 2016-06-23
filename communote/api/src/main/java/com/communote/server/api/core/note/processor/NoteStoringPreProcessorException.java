package com.communote.server.api.core.note.processor;


/**
 * Thrown by NoteStoringPreProcessors to indicate that the pre-processing failed.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteStoringPreProcessorException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3948974814340443494L;

    /**
     * Constructs a new instance of NoteStoringPreProcessorException
     *
     */
    public NoteStoringPreProcessorException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of NoteStoringPreProcessorException
     *
     */
    public NoteStoringPreProcessorException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
