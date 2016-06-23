package com.communote.server.api.core.note.processor;

/**
 * Thrown to indicate that a rendering pre-processor failed to process a note. This exception should
 * only be thrown if something unexpected happened.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteRenderingPreProcessorException extends Exception {

    /**
     * serial version UID
     */
    private static final long serialVersionUID = 6435496534214145305L;

    /**
     * Creates a new exception with detail message and cause
     * 
     * @param message
     *            a detail message that describes the exception
     * @param cause
     *            the cause of the exception or null
     */
    public NoteRenderingPreProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
}
