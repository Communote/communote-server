package com.communote.server.core.blog;

import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class OnlyCrosspostMarkupException extends NoteStoringPreProcessorException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 7909925950885188038L;

    /**
     * Constructs a new instance of OnlyCrosspostMarkupException
     *
     */
    public OnlyCrosspostMarkupException(String message) {
        super(message);
    }

}
