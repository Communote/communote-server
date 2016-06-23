package com.communote.server.core.blog;

import com.communote.server.api.core.common.NotFoundException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteNotFoundException extends NotFoundException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6151922644770525610L;

    /**
     * Constructs a new instance of NoteNotFoundException
     *
     */
    public NoteNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of NoteNotFoundException
     *
     */
    public NoteNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
