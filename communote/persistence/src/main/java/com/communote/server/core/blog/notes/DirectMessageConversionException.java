package com.communote.server.core.blog.notes;

import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * Exception to be raised when someone update a note and tries to convert it into a direct message.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class DirectMessageConversionException extends NoteStoringPreProcessorException {

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
    public DirectMessageConversionException(String message) {
        super(message);
    }

}
