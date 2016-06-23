package com.communote.server.persistence.user.note;

import java.util.List;

import com.communote.server.model.user.note.UserNoteEntity;
import com.communote.server.model.user.note.UserNoteEntityConstants;
import com.communote.server.persistence.user.note.UserNoteEntityDaoBase;

/**
 * @see com.communote.server.model.user.note.UserNoteEntity
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserNoteEntityDaoImpl extends UserNoteEntityDaoBase {

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#findByUserIdNoteId(Long,
     *      Long)
     */
    @Override
    protected UserNoteEntity handleFindByUserIdNoteId(Long userId, Long noteId) {
        List<?> result = getHibernateTemplate().find("select une "
                + "from " + UserNoteEntityConstants.CLASS_NAME + " une "
                + "left join une." + UserNoteEntityConstants.USER + " user "
                + "left join une." + UserNoteEntityConstants.NOTE + " note "
                + "where user.id=? and note.id=? ", userId, noteId);

        if (result == null || result.size() == 0) {
            return null;
        }
        return (UserNoteEntity) result.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int handleRemoveUserNoteEntitiesForNote(Long noteId) {
        // TODO actually we would have to invalidate the UserEntityCache ( cache, which is kind of
        // expensive (get all set properties for the note and invalidate them). Since this method is
        // only used when deleting a note no one accesses the entity after deletion of the note
        // because they are only read when getting the note data.
        return getHibernateTemplate().bulkUpdate(
                "DELETE FROM " + UserNoteEntityConstants.CLASS_NAME + " une WHERE une."
                        + UserNoteEntityConstants.NOTE + ".id = ?", noteId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int handleRemoveUserNoteEntitiesForUser(Long userId) {
        // TODO actually we would have to invalidate the UserNoteProperty cache, which is currently
        // not possible (=too expensive: fetch all entities we are going to delete and invalidate
        // the cache entries). Since this method is only used when a user is deleted (actually it's
        // only used in tests), we currently have no problem as deleted users are removed from the
        // result set. In case we use this method for other stuff we
        // need a solution for easier invalidation.
        return getHibernateTemplate().bulkUpdate(
                "DELETE FROM " + UserNoteEntityConstants.CLASS_NAME + " une WHERE property."
                        + UserNoteEntityConstants.USER + ".id = ?", userId);
    }
}