package com.communote.server.core.mail.message.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.user.NotifyAboutCRCSizeLimitReachedMailMessage;
import com.communote.server.model.user.User;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NotifyAboutCRCSizeLimitReachedMailMessageTest extends
        MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailSender mailSender, User... recipients) throws Exception {
        Collection<User> recipientsAsCollection = new ArrayList<User>();
        for (User recipient : recipients) {
            recipientsAsCollection.clear();
            recipientsAsCollection.add(recipient);
            mailSender.send(new NotifyAboutCRCSizeLimitReachedMailMessage(
                    recipientsAsCollection, recipient.getLanguageLocale(), UUID.randomUUID()
                            .toString(), "550", "49", "456"));
        }
    }

}
