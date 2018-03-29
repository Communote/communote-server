package com.communote.server.service.blog.notes.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorContext;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.core.blog.notes.processors.NoteNotificationDetails;
import com.communote.server.core.blog.notes.processors.NotificationNoteProcessor;
import com.communote.server.core.messaging.NotificationDefinition;
import com.communote.server.core.messaging.NotificationScheduleTypes;
import com.communote.server.core.messaging.NotificationService;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.blog.NoteDao;

/**
 * Test for {@link NotificationNoteProcessor}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class NotificationNoteProcessorTest {

    /**
     * Test implementation.
     */
    private class TestNotificationNoteProcessor extends NotificationNoteProcessor {

        @Override
        public String getId() {
            return "test";
        }

        @Override
        protected NotificationService getNotificationService() {
            return new NotificationService(null, null, null, null, null) {
                @Override
                public void sendMessage(Note note, Collection<User> usersToNotify,
                        NotificationDefinition notificationDefinition) {
                    // store notified users
                    for (User user : usersToNotify) {
                        notifiedUsers.add(user.getId());
                    }
                }

                @Override
                public boolean userHasSchedule(Long userId, NotificationDefinition definition,
                        NotificationScheduleTypes schedule) {
                    return true;
                }
            };
        }

        /**
         * @return 0
         */
        @Override
        public int getOrder() {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Collection<User> getUsersToNotify(Note note,
                NoteStoringPostProcessorContext context, Set<Long> userIdsToSkip) {
            return note.getUsersToBeNotified();
        }

        @Override
        protected boolean isSendNotifications(Note note, NoteStoringTO noteStoringTO,
                Map<String, String> properties, NoteNotificationDetails resendDetails) {
            return !note.getUsersToBeNotified().isEmpty();
        }
    }

    private final Set<Long> notifiedUsers = new HashSet<Long>();

    private NoteDao noteDao;
    private NotificationNoteProcessor notificationNoteProcessor;
    private Note[] notes;
    private List<User> users;
    private Set<Long> activeUsers;

    /**
     * Setup.
     */
    @BeforeClass
    public void setup() {
        User author = User.Factory.newInstance();
        author.setId(-1L);
        noteDao = EasyMock.createMock(NoteDao.class);
        EasyMock.expect(noteDao.load(-1l)).andReturn(null);
        notificationNoteProcessor = new TestNotificationNoteProcessor();
        users = new ArrayList<User>();
        activeUsers = new HashSet<Long>();
        for (long i = 0; i < 10; i++) {
            User user = User.Factory.newInstance();
            user.setId(i);
            if (i % 2 == 0) {
                user.setStatus(UserStatus.ACTIVE);
                activeUsers.add(i);
            } else {
                user.setStatus(UserStatus.TEMPORARILY_DISABLED);
            }
            users.add(user);
        }
        notes = new Note[10];
        for (long i = 0; i < 10; i++) {
            Note note = Note.Factory.newInstance();
            note.setId(i);
            note.setUsersToBeNotified(new HashSet<User>(users));
            note.setUser(author);
            EasyMock.expect(noteDao.load(i)).andReturn(note);
            notes[(int) i] = note;
        }
        notificationNoteProcessor.setNoteDao(noteDao);
        EasyMock.replay(noteDao);
    }

    /**
     * Test for
     * {@link NotificationNoteProcessor#processAsynchronously(Long, NoteStoringPostProcessorContext)}
     */
    @Test
    public void testProcess() {
        notificationNoteProcessor.processAsynchronously(-1l, null);
        for (long i = 0; i < notes.length; i++) {
            User registeredUser = users.get((int) i);
            registeredUser.setStatus(UserStatus.REGISTERED);
            Long userIdToSkip = (i + 1) % 10;
            Map<String, String> properties = new HashMap<>();
            // TODO this is ugly
            properties.put(PropertyManagement.KEY_GROUP + ".notification.idsToSkip",
                    userIdToSkip.toString());
            notifiedUsers.clear();
            notificationNoteProcessor.processAsynchronously(i,
                    new NoteStoringPostProcessorContext(properties));
            HashSet<Long> usersToNotify = new HashSet<>(activeUsers);
            usersToNotify.remove(userIdToSkip);
            usersToNotify.remove(registeredUser.getId());
            Assert.assertEquals(notifiedUsers, usersToNotify);
            registeredUser.setStatus(UserStatus.ACTIVE);
            // add to active users if user was created as temp. disabled
            activeUsers.add(registeredUser.getId());
        }
    }
}
