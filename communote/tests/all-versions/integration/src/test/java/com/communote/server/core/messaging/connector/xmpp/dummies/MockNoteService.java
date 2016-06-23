package com.communote.server.core.messaging.connector.xmpp.dummies;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.vo.blog.AutosaveNoteData;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.persistence.blog.FilterNoteProperty;

/**
 * Dummy implementation.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MockNoteService extends com.communote.server.service.NoteService {

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteModificationResult createNote(NoteStoringTO noteStoringTO,
            Set<String> additionalBlogNameIds, FilterNoteProperty[] autosaveFilterProperties)
            throws BlogNotFoundException, NoteManagementAuthorizationException,
            NoteStoringPreProcessorException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAutosave(Long postId) throws NoteManagementAuthorizationException {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNote(Long postId, boolean deleteSystemPosts, boolean clientManagerCanDelete) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNotesOfUser(Long userId) throws AuthorizationException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    public Long getArbitraryNoteId(Long blogId, boolean comment) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Long getArbitraryPostId(Long blogId, boolean comment) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AutosaveNoteData getAutosave(Long noteId, Long parentNoteId,
            FilterNoteProperty[] filterNoteProperty, Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SimpleNoteListItem> getCommentsOfDiscussion(Long noteId)
            throws NoteNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getDiscussionId(Long noteId) throws NoteNotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteData getNote(long noteId, NoteRenderContext renderContext)
            throws NoteNotFoundException, AuthorizationException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNoteCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscussionNoteData getNoteWithComments(Long noteId,
            QueryResultConverter<SimpleNoteListItem, DiscussionNoteData> converter)
            throws NoteNotFoundException, AuthorizationException {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfNotesInDiscussion(Long noteId) throws NoteNotFoundException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfReplies(Long postId) throws NoteNotFoundException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean noteExists(Long noteId) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteModificationResult updateNote(NoteStoringTO postStroingTO, Long noteId,
            Set<String> additionalBlogNameIds, boolean resendNotifications)
            throws NoteNotFoundException, NoteManagementAuthorizationException,
            NoteStoringPreProcessorException, BlogNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

}
