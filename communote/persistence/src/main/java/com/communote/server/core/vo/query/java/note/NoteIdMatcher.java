package com.communote.server.core.vo.query.java.note;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;


/**
 * Matcher for {@link com.communote.server.core.vo.query.TimelineQueryParameters#getNoteId()}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteIdMatcher extends Matcher<NoteData> {

    private final Long noteId;

    /**
     * Constructor.
     * 
     * @param noteId
     *            Id of the note to match. Can be null (all notes will match).
     */
    public NoteIdMatcher(Long noteId) {
        this.noteId = noteId;
    }

    /**
     * {@inheritDoc}
     * 
     * @return True, if the set note id is null or matches the id of the entity.
     * 
     */
    @Override
    public boolean matches(NoteData entity) {
        return noteId == null || entity.getId().equals(noteId);
    }
}
