package com.communote.server.core.blog.notes.processors.exceptions;

import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * Note preprocessor exception containing a message key which can be used for rendering the error
 * message.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MessageKeyNoteContentException extends NoteStoringPreProcessorException {

    private static final long serialVersionUID = 1408855325517593555L;

    private String messageKey = "No message key defined.";

    private Object[] parameters;

    /**
     * @param messageKey
     *            Key of the message.
     */
    public MessageKeyNoteContentException(String messageKey) {
        this(messageKey, new Object[0]);
    }

    /**
     * @param messageKey
     *            Key of the message.
     * @param parameters
     *            Parameters of the message.
     */
    public MessageKeyNoteContentException(String messageKey, Object[] parameters) {
        super("Pre-processing failed");
        this.messageKey = messageKey;
        this.parameters = parameters;
    }

    /**
     * @param message
     *            Readable message.
     * @param messageKey
     *            Key of the message.
     */
    public MessageKeyNoteContentException(String message, String messageKey) {
        this(message, messageKey, new Object[0]);
    }

    /**
     * @param message
     *            Readable message.
     *
     * @param messageKey
     *            Key of the message.
     * @param parameters
     *            Parameters of the message.
     */
    public MessageKeyNoteContentException(String message, String messageKey, Object[] parameters) {
        super(message);
        this.messageKey = messageKey;
        this.parameters = parameters;
    }

    /**
     * @param message
     *            Readable message.
     * @param cause
     *            A cause for this Exception.
     * @param messageKey
     *            Key of the message.
     */
    public MessageKeyNoteContentException(String message, Throwable cause, String messageKey) {
        this(message, cause, messageKey, new Object[0]);
    }

    /**
     * @param message
     *            Readable message.
     *
     * @param cause
     *            A cause for this Exception.
     *
     * @param messageKey
     *            Key of the message.
     * @param parameters
     *            Parameters of the message.
     */
    public MessageKeyNoteContentException(String message, Throwable cause, String messageKey,
            Object[] parameters) {
        super(message, cause);
        this.messageKey = messageKey;
        this.parameters = parameters;
    }

    /**
     * @return the messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * @return the parameters
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * @param messageKey
     *            the messageKey to set
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * @param parameters
     *            the parameters to set
     */
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
