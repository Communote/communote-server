package com.communote.server.core.user.note;

import com.communote.server.core.common.caching.CacheKey;
import com.communote.server.core.vo.user.note.UserNoteEntityTO;

/**
 * The cache key for getting a {@link UserNoteEntityTO}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserNoteTOCacheKey implements CacheKey {

    private final Long userId;
    private final Long noteId;

    /**
     * 
     * @param userId
     *            the id of the user for entity to get
     * @param noteId
     *            the id of the note for entity to get
     */
    public UserNoteTOCacheKey(Long userId, Long noteId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null.");
        }
        if (noteId == null) {
            throw new IllegalArgumentException("noteId cannot be null.");
        }
        this.noteId = noteId;
        this.userId = userId;
    }

    /**
     * 
     * @param userNoteEntityTO
     *            use the user and note id to get it from the cache
     */
    public UserNoteTOCacheKey(UserNoteEntityTO userNoteEntityTO) {
        this(userNoteEntityTO.getUserId(), userNoteEntityTO.getNoteId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCacheKeyString() {
        return userId + "_" + noteId;
    }

    /**
     * 
     * @return the note id of the entity to get
     */
    public Long getNoteId() {
        return noteId;
    }

    /**
     * 
     * @return the user id of the entity to get
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * {@inheritDoc}
     * 
     * @return true because it is different per client
     */
    @Override
    public boolean isUniquePerClient() {
        return true;
    }

}