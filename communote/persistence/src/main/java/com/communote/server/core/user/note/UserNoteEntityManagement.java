package com.communote.server.core.user.note;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.vo.user.note.UserNoteEntityTO;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.model.user.note.UserNoteEntity;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.note.UserNoteEntityDao;


/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Service("userNoteEntityManagement")
public class UserNoteEntityManagement {

    @Autowired
    private UserNoteEntityDao userNoteEntityDao;

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private UserDao userDao;

    private final static Logger LOGGER = LoggerFactory.getLogger(UserNoteEntityManagement.class);

    /**
     * Get the {@link UserNoteEntityTO} by the id of the user and note
     * 
     * @param userId
     *            the id for the user. cannot be null.
     * @param noteId
     *            the id of the note. cannot be null.
     * @return the entity for the given ids. can be null.
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public UserNoteEntityTO findByUserIdNoteId(Long userId, Long noteId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (noteId == null) {
            throw new IllegalArgumentException("noteId cannot be null");
        }
        UserNoteEntity entity = this.userNoteEntityDao.findByUserIdNoteId(userId, noteId);
        UserNoteEntityTO to = null;

        if (entity != null) {
            to = new UserNoteEntityTO(userId, noteId);
            to.setNormalizedRank(entity.getRankNormalized());
        }
        return to;
    }

    /**
     * Update the backend with the given {@link UserNoteEntityTO}. What exactly is updated is
     * defined by the given TO.
     * 
     * @param userNoteEntityTO
     *            used for updating
     * @throws NotFoundException
     *             in case the note or user associated with the userNoteEntityTO is not found (e.g.
     *             deleted)
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUserNoteEntity(UserNoteEntityTO userNoteEntityTO) throws NotFoundException {
        if (userNoteEntityTO == null) {
            throw new IllegalArgumentException("userNoteEntityTO cannot be null");
        }
        UserNoteEntity userNoteEntity = userNoteEntityDao.findByUserIdNoteId(
                userNoteEntityTO.getUserId(), userNoteEntityTO.getNoteId());

        if (userNoteEntity == null) {
            Note note = noteDao.load(userNoteEntityTO.getNoteId());
            User user = userDao.load(userNoteEntityTO.getUserId());
            if (note == null) {
                throw new NoteNotFoundException("note not found userNoteEntityTO="
                        + userNoteEntityTO);
            }
            if (user == null) {
                throw new UserNotFoundException("user not found userNoteEntityTO="
                        + userNoteEntityTO);
            }
            userNoteEntity = UserNoteEntity.Factory.newInstance();
            userNoteEntity.setNote(note);
            userNoteEntity.setUser(user);
            userNoteEntityDao.create(userNoteEntity);
        }

        if (userNoteEntityTO.isUpdateRank()) {
            userNoteEntity.setRankNormalized(userNoteEntityTO.getNormalizedRank());
        } else {
            LOGGER.debug("Warning: No field has been updated. " + userNoteEntityTO.getNoteId()
                    + " " + userNoteEntity.getUser());
        }
    }
}
