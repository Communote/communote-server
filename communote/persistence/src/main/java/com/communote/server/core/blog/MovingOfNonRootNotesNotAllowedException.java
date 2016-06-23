package com.communote.server.core.blog;

/**
 * Exception, which is thrown, when the user tried to move a non root note.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MovingOfNonRootNotesNotAllowedException extends NoteManagementException {

    private static final long serialVersionUID = 1690269324184253047L;
    private final Long noteId;

    /**
     * Constructor.
     *
     * @param noteId
     *            If of the note.
     */
    public MovingOfNonRootNotesNotAllowedException(Long noteId) {
        super("Note " + noteId + " is not a root note and thus cannot be moved");
        this.noteId = noteId;
    }

    /**
     * @return the noteId
     */
    public Long getNoteId() {
        return noteId;
    }
}
