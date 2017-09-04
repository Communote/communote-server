package com.communote.server.core.note.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorContext;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.blog.notes.processors.TopicNotificationNoteProcessor;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.messaging.NotificationDefinition;
import com.communote.server.core.messaging.NotificationScheduleTypes;
import com.communote.server.core.messaging.NotificationService;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

public class TopicNotificationNoteProcessorTest extends CommunoteIntegrationTest {

    private static final int TEST_AUTHOR_FETCH_SIZE = 5;
    @Autowired
    private BlogRightsManagement topicRightsManagement;
    @Autowired
    private TransactionManagement transactionManagement;
    @Autowired
    private UserManagement userManagement;
    @Autowired
    private QueryManagement queryManagement;

    private TopicNotificationNoteProcessor topicNotificationNoteProcessor;
    private TestNotificationService notificationService;

    private class TestNotificationService extends NotificationService {

        Set<Long> notifiedUsers = new HashSet<>();;

        @Override
        public void sendMessage(Long noteId, Long userToNotify,
                NotificationDefinition notificationDefinition, Map<String, Object> model) {
            notifiedUsers.add(userToNotify);
        }

        @Override
        public void sendMessage(Note note, Collection<User> usersToNotify,
                NotificationDefinition notificationDefinition) {
            for (User user : usersToNotify) {
                this.notifiedUsers.add(user.getId());
            }
        }

        @Override
        public boolean userHasSchedule(Long userId, NotificationDefinition definition,
                NotificationScheduleTypes schedule) {
            return true;
        }
    }

    @BeforeClass
    public void setup() {
        this.notificationService = new TestNotificationService();
        this.topicNotificationNoteProcessor = new TopicNotificationNoteProcessor(
                topicRightsManagement, userManagement, queryManagement);
        this.topicNotificationNoteProcessor.setAuthorFetchSize(TEST_AUTHOR_FETCH_SIZE);
        this.topicNotificationNoteProcessor.setNotificationService(notificationService);
    }

    private List<User> createUsers(int count) {
        List<User> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(TestUtils.createRandomUser(false));
        }
        return result;
    }

    /**
     * Test that all authors, excluding the author of the note to inform about, are notified when
     * @@authors is used.
     */
    @Test
    public void testAuthorMention() {
        // ensure that more authors than fetch-size exist
        List<User> users = createUsers(TEST_AUTHOR_FETCH_SIZE + 2);
        Blog topic = TestUtils.createRandomBlog(false, false,
                users.toArray(new User[users.size()]));
        User lastAuthor = users.remove(users.size() - 1);
        for (User user : users) {
            TestUtils.createAndStoreCommonNote(topic, user.getId(), UUID.randomUUID().toString());
        }
        Long noteId = TestUtils.createAndStoreCommonNote(topic, lastAuthor.getId(), "some content "
                + NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS + " some more content");
        Set<Long> notifiedUsers = processNote(noteId);
        Assert.assertEquals(notifiedUsers.size(), users.size());
        for (int i = 0; i < users.size(); i++) {
            Assert.assertTrue(notifiedUsers.contains(users.get(i).getId()),
                    "User " + i + " was not notified");
        }
    }

    /**
     * Test that authors of direct messages are notified even if the author of the new note is not
     * allowed to see the direct message.
     */
    @Test
    public void testAuthorMentionDirectMessage() {
        List<User> users = createUsers(3);
        Blog topic = TestUtils.createRandomBlog(false, false,
                users.toArray(new User[users.size()]));
        TestUtils.createAndStoreCommonNote(topic, users.get(0).getId(),
                "d @" + users.get(1).getAlias() + " dm note");

        Long noteId = TestUtils.createAndStoreCommonNote(topic, users.get(2).getId(),
                "some content " + NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS
                        + " some more content");
        Set<Long> notifiedUsers = processNote(noteId);
        Assert.assertEquals(notifiedUsers.size(), 1);
        Assert.assertTrue(notifiedUsers.contains(users.get(0).getId()));
        
    }
    
    /**
     * Test that authors which are now disabled are not notified.
     */
    @Test
    public void testAuthorMentionSkipDisabledUser() throws Exception {
        List<User> users = createUsers(3);
        Blog topic = TestUtils.createRandomBlog(false, false,
                users.toArray(new User[users.size()]));
        User lastAuthor = users.remove(users.size() - 1);
        for (User user : users) {
            TestUtils.createAndStoreCommonNote(topic, user.getId(), UUID.randomUUID().toString());
        }
        AuthenticationTestUtils.setManagerContext();
        userManagement.changeUserStatusByManager(users.get(0).getId(), UserStatus.TEMPORARILY_DISABLED);
        AuthenticationTestUtils.setAuthentication(null);

        Long noteId = TestUtils.createAndStoreCommonNote(topic, lastAuthor.getId(),
                "some content " + NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS
                        + " some more content");
        Set<Long> notifiedUsers = processNote(noteId);
        Assert.assertEquals(notifiedUsers.size(), 1);
        Assert.assertTrue(notifiedUsers.contains(users.get(1).getId()));
    }
    
    /**
     * Test that authors which now don't have read access to the topic anymore are not notified.
     */
    @Test
    public void testAuthorMentionSkipUserWithoutReadAccess() throws Exception {
        List<User> users = createUsers(3);
        Blog topic = TestUtils.createRandomBlog(false, false,
                users.toArray(new User[users.size()]));
        User lastAuthor = users.remove(users.size() - 1);
        for (User user : users) {
            TestUtils.createAndStoreCommonNote(topic, user.getId(), UUID.randomUUID().toString());
        }
        topicRightsManagement.removeMemberByEntityId(topic.getId(), users.get(1).getId());

        Long noteId = TestUtils.createAndStoreCommonNote(topic, lastAuthor.getId(),
                "some content " + NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS
                        + " some more content");
        Set<Long> notifiedUsers = processNote(noteId);
        Assert.assertEquals(notifiedUsers.size(), 1);
        Assert.assertTrue(notifiedUsers.contains(users.get(0).getId()));
    }

    private Set<Long> processNote(final Long noteId) {
        // note processors run async without user in security context
        AuthenticationTestUtils.setAuthentication(null);
        notificationService.notifiedUsers.clear();
        transactionManagement.execute(new RunInTransaction() {
            @Override
            public void execute() throws TransactionException {
                topicNotificationNoteProcessor.processAsynchronously(noteId,
                        new NoteStoringPostProcessorContext(null));
            }
        });
        return notificationService.notifiedUsers;
    }
}
