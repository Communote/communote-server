/**
 *
 */
package de.communardo.kenmei.database.update.v1_2;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.model.note.Note;
import com.communote.server.persistence.blog.NoteDao;

/**
 * Adds the followableItems to the existing notes.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AddFollowableItemsToNotes implements CustomTaskChange {

    /**
     * Helper class for updating the followable items of a note within a transaction
     *
     */
    private class UpdateFollowablesInTransactionTask implements RunInTransaction {
        private final NoteDao noteDao;
        private Long noteId;

        /**
         * instantiates the helper class
         *
         * @param dao
         *            the note DAO
         */
        UpdateFollowablesInTransactionTask(NoteDao dao) {
            this.noteDao = dao;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void execute() throws TransactionException {
            Note note = noteDao.load(noteId);
            if (note != null) {
                noteDao.updateFollowableItems(note, false);
            }
        }

        /**
         * @param noteId
         *            ID of note to process
         */
        public void setNoteId(Long noteId) {
            this.noteId = noteId;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database arg0) throws CustomChangeException, UnsupportedChangeException {
        NoteDao dao = ServiceLocator.findService(NoteDao.class);
        TransactionManagement tm = ServiceLocator.findService(TransactionManagement.class);
        Note latestNote = dao.findLatestNote();
        if (latestNote != null) {
            UpdateFollowablesInTransactionTask task = new UpdateFollowablesInTransactionTask(dao);
            long maxId = latestNote.getId();
            for (long i = 0; i <= maxId; i++) {
                task.setNoteId(i);
                tm.execute(task);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileOpener(FileOpener arg0) {
        // nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
        // nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // nothing

    }

}
