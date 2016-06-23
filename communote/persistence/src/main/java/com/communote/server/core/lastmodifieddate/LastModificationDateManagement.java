package com.communote.server.core.lastmodifieddate;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.AttachmentNotFoundException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.persistence.lastmodifieddate.LastModificationDateDao;
import com.communote.server.persistence.lastmodifieddate.LastModificationDateFactory;

/**
 * Service to retrieve the crawl-last-modification dates of certain entities.
 * <p>
 * The crawl-last-modification dates are similar to the last-modification dates of the entities but
 * usually are also updated by other operations. For example the last-modification date of a topic
 * is updated when the title, alias, description, the create system notes flag and/or the tag
 * assignment is modified. The crawl-last-modification date of the topic is also updated if the
 * topic access rights are changed.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
@Transactional
public class LastModificationDateManagement {

    @Autowired
    private LastModificationDateDao lastModificationDateDao;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private NotePermissionManagement notePermissionManagement;
    @Autowired
    private TopicPermissionManagement topicPermissionManagement;
    @Autowired
    private ResourceStoringManagement resourceStoringManagement;

    private void assertAuthorization() throws AuthorizationException {
        if (!SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException(
                    "Only the interal system is allowed to execute this action. Current user is: "
                            + SecurityHelper.getCurrentUserId());
        }
    }

    /**
     * Get the crawl-last-modification date of the given attachment. The date will have the value of
     * the crawl-Last-modification date of the note of the attachment or the topic, whichever is
     * newer.
     *
     * @param attachmentId
     *            the ID of the attachment
     * @return the crawl-last-modification
     * @throws AuthorizationException
     *             in case the current user is not allowed to read the note of the attachment
     * @throws AttachmentNotFoundException
     *             in case the attachment does not exist or is not yet published
     */
    public Date getAttachmentCrawlLastModificationDate(Long attachmentId)
            throws AuthorizationException, AttachmentNotFoundException {
        Long noteId = resourceStoringManagement.getNoteOfAttachment(attachmentId);
        if (noteId == null) {
            throw new AttachmentNotFoundException(attachmentId);
        }
        return getNoteCrawlLastModificationDate(noteDao.load(noteId));
    }

    /**
     * Get the crawl-last-modification dates of all attachments which are assigned to a note. The
     * date will have the value of the crawl-Last-modification date of the note of the attachment or
     * the topic, whichever is newer.
     *
     * @param <T>
     *            the type of object to be returned that holds the information
     * @param lastModificationDateFactory
     *            factory that creates objects that holds the last modification date information and
     *            will return it as list
     * @return a list of all attachment ids with their last modification date (which is the same of
     *         the note)
     * @throws AuthorizationException
     *             in case the current user is not the internal system user
     */
    public <T> List<T> getAttachmentCrawlLastModificationDates(
            LastModificationDateFactory<T> lastModificationDateFactory)
            throws AuthorizationException {
        assertAuthorization();

        return lastModificationDateDao
                .getAttachmentLastModificationDates(lastModificationDateFactory);
    }

    /**
     * Get the crawl-last-modification date of the given note. The date will have the value of the
     * crawl-Last-modification date of the note or the assigned topic, whichever is newer.
     *
     * @param noteId
     *            the ID of the note
     * @return the crawl-last-modification
     * @throws AuthorizationException
     *             in case the current user is not allowed to read the note
     * @throws NoteNotFoundException
     *             in case the note does not exist
     */
    public Date getNoteCrawlLastModificationDate(Long noteId) throws AuthorizationException,
            NoteNotFoundException {
        Note note;
        try {
            note = notePermissionManagement.hasAndGetWithPermission(noteId,
                    NotePermissionManagement.PERMISSION_READ, new IdentityConverter<Note>());
        } catch (NotFoundException e) {
            throw new NoteNotFoundException(e.getMessage());
        }
        return getNoteCrawlLastModificationDate(note);
    }

    private Date getNoteCrawlLastModificationDate(Note note) {
        long noteCrawlLastModDate = note.getCrawlLastModificationDate().getTime();
        long topicCrawlLastModDate = note.getBlog().getCrawlLastModificationDate().getTime();
        if (noteCrawlLastModDate > topicCrawlLastModDate) {
            return new Date(noteCrawlLastModDate);
        }
        return new Date(topicCrawlLastModDate);
    }

    /**
     * Get the crawl-last-modification dates of all published notes. The date will have the value of
     * the crawl-Last-modification date of the note or the assigned topic, whichever is newer.
     *
     * @param <T>
     *            the type of object to be returned that holds the information
     * @param lastModificationDateFactory
     *            factory that creates objects that holds the last modification date information and
     *            will return it as list
     * @return a list of all note ids with their last modification date (which is the young of the
     *         note and topic)
     * @throws AuthorizationException
     *             in case the current user is not the internal system user
     */
    public <T> List<T> getNoteCrawlLastModificationDates(
            LastModificationDateFactory<T> lastModificationDateFactory)
            throws AuthorizationException {
        assertAuthorization();

        return lastModificationDateDao.getNoteLastModificationDates(lastModificationDateFactory);
    }

    /**
     * Get the crawl-last-modification date of the given topic.
     *
     * @param topicId
     *            the ID of the topic
     * @return the crawl-last-modification
     * @throws AuthorizationException
     *             in case the current user has no read access to the topic
     * @throws BlogNotFoundException
     *             in case the topic does not exist
     */
    public Date getTopicCrawlLastModificationDate(Long topicId) throws AuthorizationException,
    BlogNotFoundException {
        Blog topic;
        try {
            topic = topicPermissionManagement.hasAndGetWithPermission(topicId,
                    TopicPermissionManagement.PERMISSION_VIEW_TOPIC_DETAILS,
                    new IdentityConverter<Blog>());
        } catch (NotFoundException e) {
            throw new BlogNotFoundException(e.getMessage(), null, topicId, null);
        }
        return topic.getCrawlLastModificationDate();
    }

    /**
     * Get the crawl-last-modification dates of all topics.
     *
     * @param <T>
     *            the type of object to be returned that holds the information
     * @param lastModificationDateFactory
     *            factory that creates objects that holds the last modification date information and
     *            will return it as list
     * @return a list of all topic ids with their last modification date
     * @throws AuthorizationException
     *             in case the current user is not the internal system user
     */
    public <T> List<T> getTopicCrawlLastModificationDates(
            LastModificationDateFactory<T> lastModificationDateFactory)
            throws AuthorizationException {
        assertAuthorization();

        return lastModificationDateDao.getTopicLastModificationDates(lastModificationDateFactory);
    }

}