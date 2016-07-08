package com.communote.server.core.mail.message.user;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.messages.user.NotifyAboutNoteMailMessage;
import com.communote.server.core.messaging.NotificationDefinition.NotificationTypes;
import com.communote.server.core.messaging.definitions.MentionNotificationDefinition;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NotifyAboutNoteMailMessageTest extends MailMessageCommunoteIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(final MailManagement mailManagement, final User... receivers)
            throws Exception {
        final Blog topic = TestUtils.createRandomBlog(true, true, receivers);
        final Collection<User> receiversAsCollection = new ArrayList<User>();
        final Map<String, String> definitionKeys = new HashMap<String, String>();
        definitionKeys.put("content", MentionNotificationDefinition.INSTANCE
                .getMessageKeyForMessage(NotificationTypes.PLAIN));
        definitionKeys.put("subject", MentionNotificationDefinition.INSTANCE
                .getMessageKeyForSubject(NotificationTypes.PLAIN));
        ServiceLocator.instance().getService(TransactionManagement.class)
                .execute(new RunInTransaction() {
                    @Override
                    public void execute() throws TransactionException {
                        for (User receiver : receivers) {
                            receiversAsCollection.clear();
                            receiversAsCollection.add(receiver);
                            Long noteId = TestUtils.createAndStoreCommonNote(topic,
                                    receiver.getId(), UUID.randomUUID().toString());
                            Note note = ServiceLocator.findService(NoteDao.class).load(noteId);
                            mailManagement.sendMail(new NotifyAboutNoteMailMessage(
                                    receiversAsCollection, receiver, receiver.getLanguageLocale(),
                                    note, topic, definitionKeys, null));
                            note.setLastModificationDate(new Timestamp(
                                    System.currentTimeMillis() + 1000));
                            mailManagement.sendMail(new NotifyAboutNoteMailMessage(
                                    receiversAsCollection, receiver, receiver.getLanguageLocale(),
                                    note, topic, definitionKeys, null));
                        }
                    }
                });
    }
}
