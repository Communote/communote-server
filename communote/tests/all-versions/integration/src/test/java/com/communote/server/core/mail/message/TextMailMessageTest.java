package com.communote.server.core.mail.message;

import java.util.UUID;

import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.messages.TextMailMessage;
import com.communote.server.model.user.User;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TextMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) {
        for (User receiver : receivers) {
            mailManagement.sendMail(new TextMailMessage("Random subject: " + UUID.randomUUID(),
                    "Random content: " + UUID.randomUUID(), receiver.getEmail()));
        }
    }
}
