package com.communote.server.core.blog;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.util.NumberHelper;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.common.caching.IdBasedRangeCacheKey;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.persistence.user.UserDao;

/**
 * @see com.communote.server.core.blog.FavoriteManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("favoriteManagement")
public class FavoriteManagementImpl extends FavoriteManagementBase {

    @Autowired
    private UserDao kenmeiUserDao;

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private NotePermissionManagement notePermissionManagement;

    private final CacheElementProvider<IdBasedRangeCacheKey, HashSet<Long>> elementProvider =
            new CacheElementProvider<IdBasedRangeCacheKey, HashSet<Long>>() {
                /**
                 * @return "user-favorite"
                 */
                @Override
                public String getContentType() {
                    return "user-favorite";
                }

                /**
                 * {@inheritDoc}
                 * 
                 * @return 3600
                 */
                @Override
                public int getTimeToLive() {
                    return 3600;
                }

                /**
                 * @param key
                 *            The key, containing the note.
                 * @return A list of all favorite notes within the given range.
                 */
                @Override
                public HashSet<Long> load(IdBasedRangeCacheKey key) {
                    Collection<Long> result = noteDao.getFavoriteNoteIds(key.getId(),
                            key.getRangeStart(),
                            key.getRangeEnd());
                    return new HashSet<Long>(result);
                }
            };

    /**
     * {@inheritDoc}
     */
    @Override
    protected int handleGetNumberOfFavorites(Long noteId) throws AuthorizationException {
        if (!SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException("Not authorized to get numbet of favorites for note.");
        }

        int num = noteDao.getNumberOfFavorites(noteId);
        return num;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleMarkNoteAsFavorite(Long noteId) throws NoteNotFoundException {
        if (!notePermissionManagement.hasPermission(noteId,
                NotePermissionManagement.PERMISSION_FAVOR)) {
            return;
        }
        Note note = noteDao.load(noteId);
        if (note == null) {
            throw new NoteNotFoundException("Note not found! id=" + noteId);
        }

        Long userId = SecurityHelper.assertCurrentUserId();
        User user = kenmeiUserDao.load(userId);
        Set<User> favoriteUsers = note.getFavoriteUsers();

        if (favoriteUsers == null) {
            favoriteUsers = new HashSet<User>();
        }

        favoriteUsers.add(user);

        long[] rangeOfNumber = NumberHelper.getRangeOfNumber(noteId, 200L);
        IdBasedRangeCacheKey cacheKey = new IdBasedRangeCacheKey(userId, rangeOfNumber[0],
                rangeOfNumber[1]);
        cacheManager.getCache().invalidate(cacheKey, elementProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUnmarkNoteAsFavorite(Long noteId) throws NoteNotFoundException {
        if (!notePermissionManagement.hasPermission(noteId,
                NotePermissionManagement.PERMISSION_FAVOR)) {
            return;
        }
        Note note = noteDao.load(noteId);
        if (note == null) {
            throw new NoteNotFoundException("Note not found! id=" + noteId);
        }

        Long userId = SecurityHelper.assertCurrentUserId();
        User user = kenmeiUserDao.load(userId);
        Set<User> favoriteUsers = note.getFavoriteUsers();

        if (favoriteUsers != null) {
            favoriteUsers.remove(user);
        }

        long[] rangeOfNumber = NumberHelper.getRangeOfNumber(noteId, 200L);
        IdBasedRangeCacheKey cacheKey = new IdBasedRangeCacheKey(userId, rangeOfNumber[0],
                rangeOfNumber[1]);
        cacheManager.getCache().invalidate(cacheKey, elementProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long noteId) {
        if (SecurityHelper.isPublicUser() || SecurityHelper.isInternalSystem()) {
            return false;
        }
        Long currentUserId = SecurityHelper.assertCurrentUserId();
        long[] rangeOfNumber = NumberHelper.getRangeOfNumber(noteId, 200L);
        IdBasedRangeCacheKey cacheKey = new IdBasedRangeCacheKey(currentUserId, rangeOfNumber[0],
                rangeOfNumber[1]);
        HashSet<Long> favorites = cacheManager.getCache().get(cacheKey, elementProvider);
        return favorites.contains(noteId);
    }

    /**
     * @param kenmeiUserDao
     *            the kenmeiUserDao to set
     */
    public void setKenmeiUserDao(UserDao kenmeiUserDao) {
        this.kenmeiUserDao = kenmeiUserDao;
    }

    /**
     * @param noteDao
     *            the noteDao to set
     */
    public void setNoteDao(NoteDao noteDao) {
        this.noteDao = noteDao;
    }
}