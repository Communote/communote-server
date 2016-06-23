package com.communote.server.core.blog;

import java.util.ArrayList;
import java.util.List;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.common.caching.IdBasedCacheKey;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DiscussionDetailsRetriever implements EventListener<DiscussionChangedEvent> {
    private class DiscussionNotesCacheElementProvider implements
    CacheElementProvider<IdBasedCacheKey, ArrayList<Long>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getContentType() {
            return "discussionNotes";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getTimeToLive() {
            return 3600;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ArrayList<Long> load(IdBasedCacheKey key) {
            List<Long> ids = noteDao.getNoteIdsOfDiscussion(key.getId());
            if (ids instanceof ArrayList<?>) {
                return (ArrayList<Long>) ids;
            }
            return new ArrayList<Long>(ids);
        }

    }

    private final NoteDao noteDao;

    private final DiscussionNotesCacheElementProvider elementProvider;

    /**
     * Creates a new instance of the retriever
     *
     * @param noteDao
     *            the note DAO
     */
    public DiscussionDetailsRetriever(NoteDao noteDao) {
        this.noteDao = noteDao;
        this.elementProvider = new DiscussionNotesCacheElementProvider();
    }

    /**
     * Returns the notes of the discussion which does not include the root note of the discussion.
     *
     * @param discussionId
     *            the ID of the discussion
     * @return the note IDs of the notes participating in the discussion
     */
    private List<Long> getAllNotesOfDiscussion(Long discussionId) {
        IdBasedCacheKey key = new IdBasedCacheKey(discussionId);
        return ServiceLocator.findService(CacheManager.class).getCache()
                .get(key, this.elementProvider);
    }

    /**
     * Returns the notes in a discussion. Notes the provided user is not allowed to read, i.e.
     * direct messages that were not sent to that user, are not included. The root note of the
     * discussion is ignored. It is assumed that the provided user has read access to the blog.
     *
     * @param discussionId
     *            the ID of the discussion for which the notes should be returned
     * @param user
     *            the user to test for access right, can be null for instance in anonymous access
     *            use-case
     * @return the notes in the discussion
     */
    public List<SimpleNoteListItem> getCommentsInDiscussion(Long discussionId, User user) {
        List<SimpleNoteListItem> result = new ArrayList<SimpleNoteListItem>();
        List<Long> discussionNoteIds = getAllNotesOfDiscussion(discussionId);
        for (Long noteId : discussionNoteIds) {
            Note note = getReadableNote(noteId, user);
            if (note != null) {
                result.add(new SimpleNoteListItem(note.getId(), note.getCreationDate()));
            }
        }
        return result;
    }

    /**
     * Returns the number of notes in a discussion. Notes the provided user is not allowed to read,
     * i.e. direct messages that were not sent to that user, are not included. It is assumed that
     * the provided user has read access to the blog.
     *
     * @param discussionId
     *            the ID of the discussion for which the notes should be returned
     * @param user
     *            the user to test for access right, can be null for instance in anonymous access
     *            use-case
     * @return the number of notes in the discussion
     */
    public int getDiscussionNoteCount(Long discussionId, User user) {
        int count = 0;
        List<Long> discussionNoteIds = getAllNotesOfDiscussion(discussionId);
        for (Long noteId : discussionNoteIds) {
            if (getReadableNote(noteId, user) != null) {
                count++;
            }
        }
        // increment by one for the root note
        return count + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<DiscussionChangedEvent> getObservedEvent() {
        return DiscussionChangedEvent.class;
    }

    /**
     * Returns the note object if the user is allowed to read it.
     *
     * @param noteId
     *            the ID of the note
     * @param user
     *            the user, can be null, which will lead to returning false for direct messages
     * @return the note object or null
     */
    private Note getReadableNote(Long noteId, User user) {
        Note discussionNote = noteDao.load(noteId);
        if (discussionNote != null && discussionNote.isDirect()) {
            // TODO this assumes users to be notified + note creator are all the users that are
            // contained in note.getDirectUsers which currently is true. When changing this, direct
            // users must be cached too
            if (user != null && (discussionNote.getUser().getId().equals(user.getId())
                    || discussionNote.getUsersToBeNotified().contains(user))) {
                return discussionNote;
            }
        } else {
            return discussionNote;
        }
        return null;
    }

    /**
     * Returns the number of replies to a note. This includes replies on replies and also replies
     * that are direct messages which the user might not be able to read.
     *
     * @param note
     *            the note for which the number of replies should be returned
     * @return the number of replies
     */
    public int getReplyCount(Note note) {
        List<Long> notesOfDiscussion = getAllNotesOfDiscussion(note.getDiscussionId());
        int replyCount = 0;
        for (Long noteId : notesOfDiscussion) {
            if (!noteId.equals(note.getId())) {
                Note discussionNote = noteDao.load(noteId);
                // discussionNote can be null in case the note was deleted and the cache was not yet
                // invalidated (e.g. in cluster), just ignore nulls
                if (discussionNote != null
                        && discussionNote.getDiscussionPath().startsWith(note.getDiscussionPath())) {
                    replyCount++;
                }
            }
        }
        return replyCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(DiscussionChangedEvent event) {
        IdBasedCacheKey key = new IdBasedCacheKey(event.getDiscussionId());
        ServiceLocator.findService(CacheManager.class).getCache()
        .invalidate(key, this.elementProvider);
    }

}
