package com.communote.server.core.blog.notes.processors.exceptions;

import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * Thrown if a recipient of a direct message which is an answer to another direct message is not
 * among the recipients of the parent note or is not the author of the parent note.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class DirectMessageWrongRecipientForAnswerException extends NoteStoringPreProcessorException {

    private static final long serialVersionUID = 4605677561501065994L;

    /**
     * Constructor.
     *
     * @param message
     *            The message.
     */
    public DirectMessageWrongRecipientForAnswerException(String message) {
        super(message);
    }
}
