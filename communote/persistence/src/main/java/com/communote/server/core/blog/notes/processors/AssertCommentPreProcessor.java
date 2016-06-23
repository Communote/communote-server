package com.communote.server.core.blog.notes.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.blog.notes.ReplyNotDirectMessageException;
import com.communote.server.core.blog.notes.processors.exceptions.NonMatchingParentTopicNotePreProcessorException;
import com.communote.server.core.blog.notes.processors.exceptions.ParentDoesNotExistsNotePreProcessorException;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;

/**
 * This processor checks for a valid reply.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AssertCommentPreProcessor implements NoteStoringImmutableContentPreProcessor {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AssertCommentPreProcessor.class);

    /**
     * Method to assert a correct direct message reply.
     *
     * @param noteStoringTO
     *            The note as TO.
     * @param parentNote
     *            The parent note.
     * @throws ReplyNotDirectMessageException
     *             Thrown, when the reply is not a direct message.
     * @throws NoteManagementAuthorizationException
     *             Thrown, when current user is not allowed to read the parent note.
     */
    private void assertCommentOnDirectMessage(NoteStoringTO noteStoringTO, Note parentNote)
            throws ReplyNotDirectMessageException, NoteManagementAuthorizationException {
        if (!parentNote.isDirect()) {
            return;
        }
        if (noteStoringTO.isPublish() && !noteStoringTO.isIsDirectMessage()) {
            throw new ReplyNotDirectMessageException("It is not possible to create a reply"
                    + " on a direct message that is not a direct message");
        }
        Long creatorId = noteStoringTO.getCreatorId();
        userCheck: if (!parentNote.getUser().getId().equals(creatorId)) {
            for (User user : parentNote.getUsersToBeNotified()) {
                if (user.getId().equals(creatorId)) {
                    break userCheck;
                }
            }
            throw new NoteManagementAuthorizationException("The user " + creatorId
                    + "is not allowed to read the parent note " + parentNote.getId(), null);
        }

    }

    @Override
    public int getOrder() {
        // note: not that important what we return here because this pre-processor will be added to
        // a list that is not modifiable
        return 0;
    }

    @Override
    public boolean isProcessAutosave() {
        // it's enough to validate when publishing since autosaves are usually done in the
        // background
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteStoringTO process(NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        if (noteStoringTO.getParentNoteId() == null) {
            return noteStoringTO; // No reply -> no check needed.
        }
        NoteDao noteDao = ServiceLocator.findService(NoteDao.class);
        Note parentNote = noteDao.load(noteStoringTO.getParentNoteId());
        if (parentNote == null) {
            LOGGER.error("Comment creation failed because the parent post with id "
                    + noteStoringTO.getParentNoteId()
                    + " was not found.");

            throw new ParentDoesNotExistsNotePreProcessorException("", new NoteNotFoundException(
                    "Cannot create a comment because the parent post does not exist"));
        }
        Note discussionRootNote = parentNote;
        if (!parentNote.getDiscussionId().equals(parentNote.getId())) {
            discussionRootNote = noteDao.load(parentNote.getDiscussionId());
        }
        if (!discussionRootNote.getBlog().getId().equals(noteStoringTO.getBlogId())) {
            throw new NonMatchingParentTopicNotePreProcessorException(
                    "Cannot create comment for a topic not equal to topic of discussion");
        }
        // stop a reply to a system generated post
        if (NoteCreationSource.SYSTEM.equals(parentNote.getCreationSource())) {
            throw new NoteManagementAuthorizationException("The creation source of this post is '"
                    + parentNote.getCreationSource() + "'. A reply to this post is not allowed.",
                    parentNote.getBlog().getTitle());
        }
        assertCommentOnDirectMessage(noteStoringTO, parentNote);
        return noteStoringTO;
    }
}
