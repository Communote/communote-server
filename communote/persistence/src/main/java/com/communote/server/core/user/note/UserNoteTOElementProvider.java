package com.communote.server.core.user.note;

import com.communote.server.core.common.caching.AbstractCacheElementProvider;
import com.communote.server.core.vo.user.note.UserNoteEntityTO;

/**
 * Element provider for getting {@link UserNoteEntityTO}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserNoteTOElementProvider extends
        AbstractCacheElementProvider<UserNoteTOCacheKey, UserNoteEntityTO> {

    private UserNoteEntityManagement userNoteEntityManagement;

    /**
     * 
     * @param userNoteEntityManagement
     *            the management is used for loading the entity.
     */
    public UserNoteTOElementProvider(UserNoteEntityManagement userNoteEntityManagement) {
        super("Object", 3600);
        if (userNoteEntityManagement == null) {
            throw new IllegalArgumentException("userNoteEntityDao cannot be null.");
        }
        this.userNoteEntityManagement = userNoteEntityManagement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserNoteEntityTO load(UserNoteTOCacheKey key) {
        return this.userNoteEntityManagement.findByUserIdNoteId(key.getUserId(),
                key.getNoteId());
    }
}