package com.communote.server.core.mail.message.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.communote.server.core.mail.MailManagement;
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
    public void sendMail(MailManagement mailManagement, User... receivers) throws Exception {
        Collection<User> receiversAsCollection = new ArrayList<User>();
        for (User receiver : receivers) {
            receiversAsCollection.clear();
            receiversAsCollection.add(receiver);
            mailManagement.sendMail(new NotifyAboutCRCSizeLimitReachedMailMessage(
                    receiversAsCollection, receiver.getLanguageLocale(), UUID.randomUUID()
                            .toString(), "550", "49", "456"));
        }
    }

}
