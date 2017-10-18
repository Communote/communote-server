package com.communote.server.core.mail.message.user;

import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.user.ActivateUserMailMessage;
import com.communote.server.model.user.User;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ActivateUserMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailSender mailSender, User... recipients) throws Exception {
        for (User recipient : recipients) {
            mailSender.send(new ActivateUserMailMessage(recipient, true));
            mailSender.send(new ActivateUserMailMessage(recipient, false));
        }
    }
}
