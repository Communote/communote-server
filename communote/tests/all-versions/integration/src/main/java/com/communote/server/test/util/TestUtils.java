package com.communote.server.test.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.impl.SessionFactoryImpl;
import org.springframework.security.core.Authentication;
import org.testng.Assert;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringFailDefinition;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.crc.RepositoryConnector;
import com.communote.server.core.crc.RepositoryConnectorDelegate;
import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.crc.vo.ExtendedContentId;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.core.external.ExternalObjectSourceAlreadyExistsException;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.content.AttachmentStreamTO;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.attachment.AttachmentStatus;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.crc.ContentRepositoryException;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.service.NoteService;
import com.communote.server.service.UserService;
import com.communote.server.test.external.MockExternalObjectSource;
import com.communote.server.test.external.MockExternalUserRepository;
import com.communote.server.test.external.MockExternalUserRepository.MockExternalSystemConfiguration;

/**
 * Utilities, which could be used by tests.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TestUtils {

    /**
     * Inner class for encapsulating exceptions thrown by the TestUtils.
     */
    public static class TestUtilsException extends RuntimeException {

        private static final long serialVersionUID = -6581583466802962071L;

        /**
         * @param cause
         *            The cause.
         */
        public TestUtilsException(Throwable cause) {
            super(cause);
        }
    }

    private static void addAndCheckCrawlLastModificationDate(List<Timestamp> crawlDates,
            final Timestamp latest, final Timestamp current) throws InterruptedException {

        if (latest != null) {
            Assert.assertTrue(current.getTime() > latest.getTime(),
                    "CrawlLastModifcationDate should have changed! latest=" + latest + " current="
                            + current);
        }

        crawlDates.add(current);

        // wait some ms to assure that the next set date is different
        Thread.sleep(10);
    }

    /**
     * Loads the note with the given id and checks that the last timestampt of the list is before
     * the current value of the crawl last modification date of the note
     *
     * @param noteId
     * @param crawlDates
     * @throws InterruptedException
     */
    public static void addAndCheckCrawlLastModificationDateForNote(Long noteId,
            List<Timestamp> crawlDates) throws InterruptedException {
        final Timestamp latest = crawlDates.size() > 0 ? crawlDates.get(crawlDates.size() - 1)
                : null;
        final com.communote.server.model.note.Note note = ServiceLocator
                .findService(NoteService.class).getNote(noteId, new IdentityConverter<Note>());

        addAndCheckCrawlLastModificationDate(crawlDates, latest,
                note.getCrawlLastModificationDate());

    }

    /**
     * Loads the topic with the given id and checks that the last timestampt of the list is before
     * the current value of the crawl last modification date of the topic
     *
     * @param topicId
     * @param crawlDates
     * @throws BlogNotFoundException
     * @throws BlogAccessException
     * @throws InterruptedException
     */
    public static void addAndCheckCrawlLastModificationDateForTopic(Long topicId,
            List<Timestamp> crawlDates)
            throws BlogNotFoundException, BlogAccessException, InterruptedException {
        final Timestamp latest = crawlDates.size() > 0 ? crawlDates.get(crawlDates.size() - 1)
                : null;
        final Blog topic = ServiceLocator.findService(BlogManagement.class).getBlogById(topicId,
                false);

        addAndCheckCrawlLastModificationDate(crawlDates, latest,
                topic.getCrawlLastModificationDate());

    }

    public static Attachment addAttachment(NoteStoringTO storingTO, final String fileName,
            final String content) throws AuthorizationException {
        AttachmentTO attachement = createAttachment(fileName);

        storingTO.setContent(content);
        storingTO.setContentType(NoteContentType.PLAIN_TEXT);

        Attachment attachment = ServiceLocator.findService(ResourceStoringManagement.class)
                .storeAttachment(attachement);

        Assert.assertNotNull(attachment, "Attachement resource should not be null!");
        Assert.assertEquals(attachment.getStatus(), AttachmentStatus.UPLOADED);
        storingTO.setAttachmentIds(new Long[] { attachment.getId() });

        return attachment;
    }

    /**
     * Creates a new note and stores in within the db.
     *
     * @param topic
     *            The topic.
     * @param creatorId
     *            The creator.
     * @param content
     *            The content.
     * @return Id of the created note.
     */
    public static Long createAndStoreCommonNote(Blog topic, Long creatorId, String content) {
        return createAndStoreCommonNote(topic, creatorId, content, new Date());
    }

    /**
     * Creates a new note and stores in within the database.
     *
     * @param topic
     *            The topic.
     * @param creatorId
     *            The creator.
     * @param content
     *            The content.
     * @param creationDate
     *            The creation date of the note.
     * @return Id of the created note.
     */
    public static Long createAndStoreCommonNote(Blog topic, Long creatorId, String content,
            Date creationDate) {
        return createAndStoreCommonNote(topic, creatorId, content, new Long[0], null, creationDate);
    }

    /**
     * Creates a new note and stores in within the database.
     *
     * @param topic
     *            The topic.
     * @param creatorId
     *            The creator.
     * @param content
     *            The content.
     * @param parentNoteId
     *            If not null, this note will be reply to the given parent note.
     * @return Id of the created note.
     */
    public static Long createAndStoreCommonNote(Blog topic, Long creatorId, String content,
            Long parentNoteId) {
        return createAndStoreCommonNote(topic, creatorId, content, new Long[0], parentNoteId,
                new Date());
    }

    /**
     * Creates a new note and stores in within the db.
     *
     * @param topic
     *            The topic.
     * @param creatorId
     *            The creator.
     * @param content
     *            The content.
     * @param parentNoteId
     *            If not null, this note will be reply to the given parent note.
     * @param creationDate
     *            The creation date of the note.
     * @return Id of the created note.
     */
    public static Long createAndStoreCommonNote(Blog topic, Long creatorId, String content,
            Long parentNoteId, Date creationDate) {
        return createAndStoreCommonNote(topic, creatorId, content, new Long[0], parentNoteId,
                creationDate);
    }

    /**
     * Creates a new note and stores in within the db.
     *
     * @param topic
     *            The topic.
     * @param creatorId
     *            The creator.
     * @param content
     *            The content.
     * @param attachmentIds
     *            List of attachments for note.
     * @return Id of the created note.
     * @throws TestUtilsException
     *             Exception.
     */
    public static Long createAndStoreCommonNote(Blog topic, Long creatorId, String content,
            Long[] attachmentIds) throws TestUtilsException {
        return createAndStoreCommonNote(topic, creatorId, content, attachmentIds, null, new Date());
    }

    /**
     * Creates a new note and stores in within the db.
     *
     * @param topic
     *            The topic.
     * @param creatorId
     *            The creator.
     * @param content
     *            The content.
     * @param attachmentIds
     *            List of attachments for note.
     * @param parentNoteId
     *            If not null, this note will be reply to the given parent note.
     * @param creationDate
     *            The creation date of the note.
     * @return Id of the created note.
     * @throws TestUtilsException
     *             Exception.
     */
    public static Long createAndStoreCommonNote(Blog topic, Long creatorId, String content,
            Long[] attachmentIds, Long parentNoteId, Date creationDate) throws TestUtilsException {
        NoteStoringTO note = createCommonNote(topic, creatorId, content, attachmentIds);
        note.setParentNoteId(parentNoteId);
        note.setCreationDate(creationDate == null ? null : new Timestamp(creationDate.getTime()));
        try {
            Long noteId;
            if (parentNoteId == null) {
                noteId = ServiceLocator.findService(NoteService.class)
                        .createNote(note, new HashSet<String>()).getNoteId();
            } else {
                note.setParentNoteId(parentNoteId);
                noteId = ServiceLocator.findService(NoteService.class).createNote(note, null)
                        .getNoteId();
            }
            // TODO why manually evicting the just created note? Well could be useful for tests
            // where the creationDate or modificationDate of the created note is used in further
            // operations that query the database. MySQL has only seconds-precision. When getting
            // the note it is loaded from cache which still has the date with milliseconds and not
            // the value that was stored in database. Maybe we should optimize this with an
            // additional boolean parameter that when true will evict the note.
            ServiceLocator.instance().getService("sessionFactory", SessionFactoryImpl.class)
                    .getCache().evictEntity(Note.class, noteId);
            return noteId;
        } catch (BlogNotFoundException | NoteStoringPreProcessorException
                | NoteManagementAuthorizationException e) {
            throw new TestUtilsException(e);
        }
    }

    public static Long createAndStoreCommonNoteWithAttachments(Blog topic, Long creatorId,
            String content, Long parentNoteId, Date creationDate, int numAttachments)
            throws TestUtilsException, AuthorizationException {

        Long[] attachmentIds = null;

        if (numAttachments > 0) {
            attachmentIds = new Long[numAttachments];
            for (int i = 0; i < numAttachments; i++) {
                attachmentIds[i] = TestUtils.createAttachment().getId();
                Assert.assertNotNull(attachmentIds[i]);
            }
        }
        return TestUtils.createAndStoreCommonNote(topic, creatorId, content, attachmentIds,
                parentNoteId, creationDate);
    }

    /**
     * Creates and stores a random attachment.
     *
     * @return The attachment.
     * @throws AuthorizationException
     *             in case no user is set
     */
    public static Attachment createAttachment() throws AuthorizationException {
        String content = RandomStringUtils.randomAlphanumeric(100);
        AttachmentTO attachment = new AttachmentStreamTO(
                new ByteArrayInputStream(content.getBytes()));
        attachment.setStatus(AttachmentStatus.UPLOADED);
        attachment.setMetadata(new ContentMetadata());
        attachment.getMetadata().setDate(new Date());
        attachment.getMetadata().setFilename(RandomStringUtils.randomAlphanumeric(10));
        return ServiceLocator.instance().getService(ResourceStoringManagement.class)
                .storeAttachment(attachment);
    }

    public static AttachmentTO createAttachment(final String fileName) {
        AttachmentTO attachement = new AttachmentStreamTO(
                new ByteArrayInputStream("Test attachment".getBytes()), AttachmentStatus.UPLOADED);
        attachement.setMetadata(new ContentMetadata());
        attachement.getMetadata().setFilename(fileName);
        attachement.setStatus(AttachmentStatus.UPLOADED);
        return attachement;
    }

    /**
     *
     * @param blog
     *            The blog the message should be posted in
     * @param creatorId
     *            ID of the creator
     * @param content
     *            The content of the note
     * @return A TO for creating an autosave
     */
    public static NoteStoringTO createCommonAutosave(Blog blog, Long creatorId, String content) {
        return createCommonNote(blog, creatorId, content, false, new Long[0]);
    }

    /**
     * @param blog
     *            The blog the message should be posted in
     * @param creatorId
     *            ID of the creator
     * @return NoteStoringTO with random content.
     */
    public static NoteStoringTO createCommonNote(Blog blog, Long creatorId) {
        return createCommonNote(blog, creatorId, new Long[0]);
    }

    /**
     * @param blog
     *            The blog the message should be posted in
     * @param creatorId
     *            ID of the creator
     * @param attachmentIds
     *            List of attachments for note.
     * @return NoteStoringTO with random content.
     */
    public static NoteStoringTO createCommonNote(Blog blog, Long creatorId, Long[] attachmentIds) {
        return createCommonNote(blog, creatorId,
                RandomStringUtils.randomAlphanumeric(400).replace("a", " "), attachmentIds);
    }

    /**
     * @param blog
     *            The blog the message should be posted in
     * @param creatorId
     *            ID of the creator
     * @param content
     *            The content of the note
     * @param publish
     *            whether to publish the note or create an autosave
     * @param attachmentIds
     *            List of attachments for note.
     * @param properties
     *            List of properties as Array. The array must contains key-value pairs, i.e.
     *            ["key1","value1"],["key2","value2"].
     * @return A TO for the note creation
     */
    private static NoteStoringTO createCommonNote(Blog blog, Long creatorId, String content,
            boolean publish, Long[] attachmentIds, String[]... properties) {
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setPublish(publish);
        noteStoringTO.setAttachmentIds(attachmentIds == null ? new Long[0] : attachmentIds);
        noteStoringTO.setCreationSource(NoteCreationSource.WEB);
        noteStoringTO.setLanguage(Locale.ENGLISH.getLanguage());
        noteStoringTO.setSendNotifications(false);
        noteStoringTO.setUnparsedTags(UUID.randomUUID().toString());
        noteStoringTO.setVersion(0L);
        noteStoringTO.setBlogId(blog.getId());
        noteStoringTO.setCreatorId(creatorId);
        noteStoringTO.setContent(content);
        noteStoringTO.setContentType(NoteContentType.PLAIN_TEXT);
        noteStoringTO.setFailDefinition(new NoteStoringFailDefinition(false, false, false, false));
        if (properties != null) {
            noteStoringTO.setProperties(new HashSet<StringPropertyTO>());
            for (String[] property : properties) {
                noteStoringTO.getProperties().add(
                        new StringPropertyTO(property[1], property[0], property[0], new Date()));
            }
        }
        return noteStoringTO;
    }

    /**
     * @param blog
     *            The blog the message should be posted in
     * @param creatorId
     *            ID of the creator
     * @param content
     *            The content of the note
     * @param attachmentIds
     *            List of attachments for note.
     * @return A TO for the note creation
     */
    public static NoteStoringTO createCommonNote(Blog blog, Long creatorId, String content,
            Long[] attachmentIds) {
        return createCommonNote(blog, creatorId, content, true, attachmentIds);
    }

    /**
     * @param blog
     *            The blog the message should be posted in
     * @param creatorId
     *            ID of the creator
     * @param content
     *            The content of the note
     * @param properties
     *            List of properties as Array. The array must contains key-value pairs, i.e.
     *            ["key1","value1"],["key2","value2"].
     * @return A TO for the note creation
     */
    public static NoteStoringTO createCommonNote(Blog blog, Long creatorId, String content,
            String[]... properties) {
        return createCommonNote(blog, creatorId, content, true, new Long[0], properties);
    }

    public static String createEmailFromAlias(String alias) {
        if (alias.length() > 50) {
            alias = alias.substring(0, 50);
        }
        alias = alias.replace("/", "-");
        return alias + "@localhost";
    }

    public static ExternalUserVO createExternalKenmeiUserVO(String externalSystemId, String alias,
            UserRole... roles) {
        ExternalUserVO user = new ExternalUserVO();
        fillKenmeiUserVO(user, alias, roles);
        user.setExternalUserName(alias);
        user.setSystemId(externalSystemId);
        user.setAdditionalProperty(UUID.randomUUID().toString());
        user.setPermanentId(UUID.randomUUID().toString());
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    public static UserVO createKenmeiUserVO(String alias, UserRole... roles) {
        return fillKenmeiUserVO(new UserVO(), alias, roles);
    }

    /**
     * Create an external object source with a random ID.
     *
     * @param register
     *            whether to register it to the ExternalObjectManagement
     * @return the created ExternalObjectSource
     * @throws ExternalObjectSourceAlreadyExistsException
     *             in case register was true and there was already a resource with the same ID
     */
    public static MockExternalObjectSource createNewExternalObjectSource(boolean register)
            throws ExternalObjectSourceAlreadyExistsException {
        String externalSystemId = UUID.randomUUID().toString();
        MockExternalObjectSource source = new MockExternalObjectSource(externalSystemId,
                new MockExternalObjectSource.MockExternalObjectSourceConfiguration());
        if (register) {
            ServiceLocator.findService(ExternalObjectManagement.class)
                    .registerExternalObjectSource(source);
        }
        return source;
    }

    /**
     * Creates a new (mock) external user repo with a random system id and register it at the user
     * service.
     *
     * @return the mock repo. use {@link MockExternalUserRepository#getExternalSystemId()} to get
     *         the external system id
     */
    public static MockExternalUserRepository createNewExternalUserRepo() {

        String externalSystemId = UUID.randomUUID().toString();
        MockExternalUserRepository repo = new MockExternalUserRepository(externalSystemId, null);
        repo.setConfiguration(new MockExternalSystemConfiguration());

        ServiceLocator.findService(UserService.class).registerRepository(externalSystemId, repo);

        return repo;
    }

    /**
     * create a PNG image that is filled with the given color.
     *
     * @param width
     *            the width of the created image
     *
     * @param height
     *            the height of the created image
     * @param color
     *            the color for filling the image
     *
     * @return the byte array of the image
     *
     * @throws IOException
     *             in case of an exception
     */
    public static byte[] createPngImage(int width, int height, Color color) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(color);
            graphics.fillRect(0, 0, width - 1, height - 1);
            image.flush();
            // write as png
            ImageIO.write(image, "png", baos);
            baos.flush();
            byte[] resultImageAsRawBytes = baos.toByteArray();
            return resultImageAsRawBytes;
        } finally {
            baos.close();

        }
    }

    /**
     * Create a new topic with random title and alias.
     *
     * Warning: The topic creator is kept in the security context.
     *
     * @param allCanRead
     *            whether all users should have read access to the blog
     * @param allCanWrite
     *            whether all users should have write access to the blog
     * @param tags
     *            List of tags to assign to the blog
     * @param tagTOs
     *            List of tags as TagTO to assign the the blog
     * @param members
     *            List of members (write and read access). The first member will be the manager of
     *            the blog.
     * @return A new blog.
     */
    public static Blog createRandomBlog(boolean allCanRead, boolean allCanWrite, String[] tags,
            Set<TagTO> tagTOs, User... members) {
        AuthenticationTestUtils.setSecurityContext(members[0]);
        Assert.assertTrue(members.length > 0);
        CreationBlogTO blogTO = createRandomCreationBlogTO(members[0].getId(), allCanRead,
                allCanWrite, tags, tagTOs);
        try {
            Blog blog = ServiceLocator.instance().getService(BlogManagement.class)
                    .createBlog(blogTO);
            for (int i = 1; i < members.length; i++) {
                ServiceLocator.instance().getService(BlogRightsManagement.class)
                        .addEntity(blog.getId(), members[i].getId(), BlogRole.MEMBER);
            }
            return blog;
        } catch (Exception e) {
            throw new TestUtilsException(e);
        }
    }

    /**
     * Create a new topic with random title and alias.
     *
     * Warning: The topic creator is kept in the security context.
     *
     * @param allCanRead
     *            whether all users should have read access to the blog
     * @param allCanWrite
     *            whether all users should have write access to the blog
     * @param tags
     *            List of tags for this blog.
     * @param members
     *            List of members (write and read access). The first member will be the manager of
     *            the blog.
     * @return A new blog.
     */
    public static Blog createRandomBlog(boolean allCanRead, boolean allCanWrite, String[] tags,
            User... members) {
        return createRandomBlog(allCanRead, allCanWrite, tags, null, members);
    }

    /**
     * Create a new topic with random title and alias.
     *
     * Warning: The topic creator is kept in the security context.
     *
     * @param allCanRead
     *            whether all users should have read access to the blog
     * @param allCanWrite
     *            whether all users should have write access to the blog
     * @param members
     *            List of members (write and read access). The first member will be the manager of
     *            the blog.
     * @throws TestUtilsException
     *             Exception.
     * @return A new blog.
     */
    public static Blog createRandomBlog(boolean allCanRead, boolean allCanWrite, User... members)
            throws TestUtilsException {
        return createRandomBlog(allCanRead, allCanWrite, null, members);
    }

    /**
     * Create a TO for creating a blog/topic with a random title and alias.
     *
     * @param creatorUserId
     *            the ID of the user that should be the manager of the topic
     * @param allCanRead
     *            whether all users should have read access to the blog
     * @param allCanWrite
     *            whether all users should have write access to the blog
     * @param tags
     *            List of tags to assign to the blog
     * @param tagTOs
     *            List of tags as TagTO to assign the the blog
     * @return the TO
     */
    public static CreationBlogTO createRandomCreationBlogTO(Long creatorUserId, boolean allCanRead,
            boolean allCanWrite, String[] tags, Set<TagTO> tagTOs) {
        String blogName = UUID.randomUUID().toString();
        CreationBlogTO blogTO = new CreationBlogTO(allCanRead, allCanWrite, blogName, false);
        blogTO.setCreatorUserId(creatorUserId);
        blogTO.setNameIdentifier(blogName.replace("-", ""));
        blogTO.setUnparsedTags(tags);
        blogTO.setTags(tagTOs);
        blogTO.setCreateSystemNotes(false);
        return blogTO;
    }

    /**
     * Create a new random user.
     *
     * @param isManager
     *            whether the created user should be a client manager.
     * @return A new user.
     */
    public static User createRandomUser(boolean isManager) {
        return createRandomUser(isManager, false, null);
    }

    /**
     * Creates a new random user.
     *
     * @param isManager
     *            True, if this user should be a manager.
     * @param managerConfirmationRequired
     *            True, if the manager has to confirm the user creation.
     * @param externalSystemId
     *            Id of the external system.
     * @return A new user.
     * @throws TestUtilsException
     *             Exception.
     * @deprecated Use {@link #createRandomUser(boolean, String, UserRole...)} instead.
     */
    @Deprecated
    public static User createRandomUser(boolean isManager, boolean managerConfirmationRequired,
            String externalSystemId) throws TestUtilsException {
        UserRole[] roles = isManager
                ? new UserRole[] { UserRole.ROLE_KENMEI_USER, UserRole.ROLE_KENMEI_CLIENT_MANAGER }
                : new UserRole[] { UserRole.ROLE_KENMEI_USER };
        return createRandomUser(managerConfirmationRequired, externalSystemId, roles);
    }

    /**
     * Creates a new random user.
     *
     * @param isManager
     *            True, if this user should be a manager.
     * @param externalSystemId
     *            The external system id for this user.
     * @return A new user.
     */
    public static User createRandomUser(boolean isManager, String externalSystemId) {
        return createRandomUser(isManager, false, externalSystemId);
    }

    /**
     * Creates a random user.
     *
     * @param managerActivationRequired
     *            True, if the manager has to activate the user creation.
     * @param externalSystemId
     *            The external system id for this user, can be null.
     * @param setOrginalAuth
     *            if true the current authentication will be kept after creating the user
     * @param roles
     *            The roles of the user.
     * @return A new random user.
     * @throws TestUtilsException
     *             Thrown, when something goes wrong.
     */
    public static User createRandomUser(boolean managerActivationRequired, String externalSystemId,
            boolean setOrginalAuth, UserRole... roles) throws TestUtilsException {
        return createRandomUser(createRandomUserAlias(), managerActivationRequired,
                externalSystemId, setOrginalAuth, roles);
    }

    /**
     * Creates a random user.
     *
     * @param managerActivationRequired
     *            True, if the manager has to activate the user creation.
     * @param externalSystemId
     *            The external system id for this user, can be null.
     * @param roles
     *            The roles of the user.
     * @return A new random user.
     * @throws TestUtilsException
     *             Thrown, when something goes wrong.
     */
    public static User createRandomUser(boolean managerActivationRequired, String externalSystemId,
            UserRole... roles) throws TestUtilsException {
        return createRandomUser(createRandomUserAlias(), managerActivationRequired,
                externalSystemId, false, roles);
    }

    public static User createRandomUser(String alias, boolean managerActivationRequired,
            String externalSystemId, boolean setOrginalAuth, UserRole... roles)
            throws TestUtilsException {

        Authentication orginalAuthentication = AuthenticationTestUtils.setManagerContext();

        User createdUser;
        try {
            if (StringUtils.isNotBlank(externalSystemId)) {
                createdUser = ServiceLocator.instance().getService(UserManagement.class)
                        .createOrUpdateExternalUser(
                                createExternalKenmeiUserVO(externalSystemId, alias, roles));
            } else {
                createdUser = ServiceLocator.instance().getService(UserManagement.class).createUser(
                        createKenmeiUserVO(alias, roles), false, managerActivationRequired);
            }
        } catch (Exception e) {
            throw new TestUtilsException(e);
        }
        if (orginalAuthentication != null && setOrginalAuth) {
            AuthenticationHelper.setAuthentication(orginalAuthentication);
        } else {
            AuthenticationHelper.removeAuthentication();
        }
        return createdUser;
    }

    /**
     * Creates a legal random user alias.
     *
     * @return the alias
     */
    public static String createRandomUserAlias() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Creates a new random user for the given external system id.
     *
     * @param externalSystemId
     *            External system id.
     * @return A new user.
     * @throws TestUtilsException
     *             Exception.
     */
    public static User createRandomUserForExternalSystem(String externalSystemId)
            throws TestUtilsException {
        Authentication oldAuth = AuthenticationTestUtils.setManagerContext();

        User createKenmeiUser;
        try {
            createKenmeiUser = ServiceLocator.instance().getService(UserManagement.class)
                    .createOrUpdateExternalUser(
                            createRandomUserVoForExternalSystem(externalSystemId));
        } catch (Exception e) {
            throw new TestUtilsException(e);
        }
        AuthenticationTestUtils.unsetManagerContext(oldAuth);
        return createKenmeiUser;
    }

    /**
     * Create a user VO with random data for an external system
     *
     * @param externalSystemId
     *            the ID of the external system
     * @return the VO
     */
    public static ExternalUserVO createRandomUserVoForExternalSystem(String externalSystemId) {
        String alias = createRandomUserAlias();
        ExternalUserVO user = createExternalKenmeiUserVO(externalSystemId, alias,
                UserRole.ROLE_KENMEI_USER);
        user.setUpdateEmail(false);
        user.setUpdateFirstName(false);
        user.setUpdateLastName(false);
        user.setUpdateLanguage(false);
        user.setClearPassword(false);
        return user;
    }

    /**
     * Creates a new user with the given alias
     *
     * @param isManager
     *            True, if this user should be a manager.
     * @param setOriginalAuth
     *            true to keep the authentication
     * @return A new user.
     */
    public static User createUser(String alias, boolean isManager, boolean setOriginalAuth) {
        UserRole[] roles = isManager
                ? new UserRole[] { UserRole.ROLE_KENMEI_USER, UserRole.ROLE_KENMEI_CLIENT_MANAGER }
                : new UserRole[] { UserRole.ROLE_KENMEI_USER };

        return createRandomUser(alias, false, null, setOriginalAuth, roles);
    }

    public static UserVO fillKenmeiUserVO(UserVO user, String alias, UserRole... roles) {

        user.setLanguage(Locale.ENGLISH);

        user.setEmail(createEmailFromAlias(alias));
        user.setRoles(roles);
        user.setAlias(alias);
        user.setPassword(UUID.randomUUID().toString());
        user.setFirstName(UUID.randomUUID().toString());
        user.setLastName(UUID.randomUUID().toString());
        user.setTimeZoneId(TimeZone.getDefault().getID());
        return user;
    }

    public static NoteStoringTO generateCommonNoteStoringTO(User user, Blog testBlog) {

        AuthenticationTestUtils.setSecurityContext(user);

        NoteStoringTO storingTO = new NoteStoringTO();

        storingTO.setCreatorId(user.getId());
        storingTO.setCreationSource(NoteCreationSource.WEB);
        storingTO.setBlogId(testBlog.getId());

        storingTO.setUnparsedTags("");

        storingTO.setContentType(NoteContentType.UNKNOWN);
        NoteStoringFailDefinition failDef = new NoteStoringFailDefinition();
        failDef.setFailOnMissingBlogWriteAccess(false);
        failDef.setFailOnUninformableUser(false);
        failDef.setFailOnUnresolvableBlogs(false);
        failDef.setFailOnUnresolvableUsers(false);
        storingTO.setFailDefinition(failDef);
        storingTO.setSendNotifications(true);
        storingTO.setPublish(true);
        storingTO.setVersion(0L);

        return storingTO;

    }

    public static AttachmentTO getAttachmentFromDefaultFilesystemConnectorByContentId(
            String contentIdStr) throws ContentRepositoryException {

        RepositoryConnector filesystemConnector = ServiceLocator
                .findService(RepositoryConnectorDelegate.class).getDefaultRepositoryConnector();
        Assert.assertNotNull(filesystemConnector);
        final ContentId contentId = new ContentId(contentIdStr,
                filesystemConnector.getConfiguration().getConnectorId());

        AttachmentTO attachmentTO = filesystemConnector
                .getContent(new ExtendedContentId(contentId));
        return attachmentTO;
    }

    /**
     * Method to invalidate all caches.
     */
    public static void invalidateCaches() {
        CacheManager cacheManager = ServiceLocator.findService(CacheManager.class);
        cacheManager.invalidateMainCache();
        for (String cache : cacheManager.getAdditionalCaches()) {
            cacheManager.invalidateAdditionalCache(cache);
        }
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private TestUtils() {
        // Do nothing
    }
}
