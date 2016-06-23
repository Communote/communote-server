package com.communote.server.core.user.note;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.user.note.UserNoteEntityTO;


/**
 * This service provides access to cached {@link UserNoteEntityTO}s.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Service("userNoteEntityService")
public class UserNoteEntityService {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserNoteEntityManagement userNoteEntityManagement;

    private UserNoteTOElementProvider userNoteEntityTOElementProvider;

    /**
     * Gets the {@link UserNoteEntityTO} for the current user and the note. It will use the
     * application cache.
     * 
     * @param noteId
     *            the note id
     * @return the user note entity for the current user and the given id. Null if it does not
     *         exist.
     */
    public UserNoteEntityTO getUserNoteEntity(Long noteId) {

        return getUserNoteEntity(SecurityHelper.assertCurrentUserId(), noteId);
    }

    /**
     * Gets the {@link UserNoteEntityTO} for the given user and the given note. It will use the
     * application cache.
     * 
     * @param userId
     *            the id of the user
     * @param noteId
     *            the id of the note
     * @return the user note entity for the user and the given id. Null if it does not exist.
     */
    public UserNoteEntityTO getUserNoteEntity(Long userId, Long noteId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (noteId == null) {
            throw new IllegalArgumentException("noteId cannot be null");
        }

        return cacheManager.getCache().get(new UserNoteTOCacheKey(userId, noteId),
                getUserNoteTOElementProvider());
    }

    /**
     * 
     * @return get the element provider initialized
     */
    private UserNoteTOElementProvider getUserNoteTOElementProvider() {
        if (userNoteEntityTOElementProvider == null) {
            synchronized (this) {
                if (userNoteEntityTOElementProvider == null) {
                    userNoteEntityTOElementProvider = new UserNoteTOElementProvider(
                            this.userNoteEntityManagement);
                }
            }
        }
        return userNoteEntityTOElementProvider;
    }

    /**
     * Update the {@link UserNoteEntityTO}. It will also invalidate the cache
     * 
     * @param userNoteEntityTO
     *            the entity to update
     * @throws NotFoundException
     *             in case the note or user could not be found
     */
    public void updateUserNoteEntity(UserNoteEntityTO userNoteEntityTO) throws NotFoundException {
        this.userNoteEntityManagement.updateUserNoteEntity(userNoteEntityTO);
        cacheManager.getCache().invalidate(new UserNoteTOCacheKey(userNoteEntityTO),
                getUserNoteTOElementProvider());
    }
}
