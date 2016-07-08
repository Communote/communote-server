package com.communote.server.core.mail.message.fetching;

import java.util.UUID;

import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.messages.fetching.DirectMessageMissingRecipientMailMessage;
import com.communote.server.model.user.User;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DirectMessageMissingRecipientMailMessageTest extends
        MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) {
        for (User receiver : receivers) {
            mailManagement.sendMail(new DirectMessageMissingRecipientMailMessage(receiver,
                    new String[] { UUID.randomUUID().toString(), UUID.randomUUID().toString() },
                    new String[] { UUID.randomUUID().toString(), UUID.randomUUID().toString() }));
        }
    }
}
