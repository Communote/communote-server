package com.communote.server.core.vo.blog;

/**
 * Exception thrown sorting and comparing the note discussion
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteDiscussionComparatorException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param msg
     *            the message
     */
    public NoteDiscussionComparatorException(String msg) {
        super(msg);
    }

}
