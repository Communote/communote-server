package com.communote.server.core.vo.query.note;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.model.note.Note;
import com.communote.server.persistence.blog.NoteDao;


/**
 * Giving access to note object by requesting DB
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <I>
 *            The object type of the returning temporary object
 * @param <O>
 *            The returning list object
 */
public abstract class DataAccessNoteConverter<I, O> extends
        QueryResultConverter<I, O> {

    private NoteDao noteDao;

    /**
     * Getting Note object by id
     * 
     * @param noteId
     *            the note id
     * @return note object
     */

    protected Note getNote(Long noteId) {
        return getNoteDao().load(noteId);
    }

    /**
     * Getting Instance of NoteDao
     * 
     * @return NoteDao Instance
     */

    protected NoteDao getNoteDao() {
        if (noteDao == null) {
            noteDao = ServiceLocator.findService(NoteDao.class);
        }
        return noteDao;
    }

}
