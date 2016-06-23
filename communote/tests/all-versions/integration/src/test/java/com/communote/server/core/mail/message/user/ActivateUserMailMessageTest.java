package com.communote.server.core.mail.message.user;

import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.core.mail.messages.user.ActivateUserMailMessage;
import com.communote.server.model.user.User;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ActivateUserMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) throws Exception {
        for (User receiver : receivers) {
            mailManagement.sendMail(new ActivateUserMailMessage(receiver, true));
            mailManagement.sendMail(new ActivateUserMailMessage(receiver, false));
        }
    }
}
