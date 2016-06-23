package com.communote.server.core.blog;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteWriterException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6016728973407507310L;

    /**
     * Constructs a new instance of NoteWriterException
     *
     */
    public NoteWriterException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of NoteWriterException
     *
     */
    public NoteWriterException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
