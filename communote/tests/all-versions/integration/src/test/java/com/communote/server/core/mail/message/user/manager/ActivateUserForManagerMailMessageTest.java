package com.communote.server.core.mail.message.user.manager;

import java.util.ArrayList;
import java.util.Collection;

import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.user.manager.ActivateUserForManagerMailMessage;
import com.communote.server.model.user.User;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ActivateUserForManagerMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailSender mailSender, User... recipients) throws Exception {
        for (User recipient : recipients) {
            Collection<User> recipientsAsCollection = new ArrayList<User>();
            recipientsAsCollection.add(recipient);
            mailSender.send(new ActivateUserForManagerMailMessage(recipient, recipient
                    .getLanguageLocale(), true, recipientsAsCollection));
            mailSender.send(new ActivateUserForManagerMailMessage(recipient, recipient
                    .getLanguageLocale(), false, recipientsAsCollection));
        }
    }
}
