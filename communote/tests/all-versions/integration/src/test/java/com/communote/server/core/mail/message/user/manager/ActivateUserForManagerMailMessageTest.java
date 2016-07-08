package com.communote.server.core.mail.message.user.manager;

import java.util.ArrayList;
import java.util.Collection;

import com.communote.server.core.mail.MailManagement;
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
    public void sendMail(MailManagement mailManagement, User... receivers) throws Exception {
        for (User receiver : receivers) {
            Collection<User> receiversAsCollection = new ArrayList<User>();
            receiversAsCollection.add(receiver);
            mailManagement.sendMail(new ActivateUserForManagerMailMessage(receiver, receiver
                    .getLanguageLocale(), true, receiversAsCollection));
            mailManagement.sendMail(new ActivateUserForManagerMailMessage(receiver, receiver
                    .getLanguageLocale(), false, receiversAsCollection));
        }
    }
}
