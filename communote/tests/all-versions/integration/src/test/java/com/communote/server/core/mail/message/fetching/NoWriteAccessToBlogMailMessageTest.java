package com.communote.server.core.mail.message.fetching;

import java.util.UUID;

import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.core.mail.messages.fetching.NoWriteAccessToBlogMailMessage;
import com.communote.server.model.user.User;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoWriteAccessToBlogMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) {
        for (User receiver : receivers) {
            mailManagement.sendMail(new NoWriteAccessToBlogMailMessage(receiver,
                    UUID.randomUUID().toString()));
        }
    }
}
