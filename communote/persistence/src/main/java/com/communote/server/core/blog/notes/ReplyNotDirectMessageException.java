package com.communote.server.core.blog.notes;

import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * Exception to be raised when someone tries to create a reply on a direct message that is not a
 * direct message.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ReplyNotDirectMessageException extends NoteStoringPreProcessorException {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an instance of the exception with a details message.
     * 
     * @param message
     *            message holding details
     */
    public ReplyNotDirectMessageException(String message) {
        super(message);
    }

}
