package com.communote.server.core.mail.message.user.manager;

import java.util.ArrayList;
import java.util.Collection;

import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.core.mail.messages.user.manager.NewUserConfirmedForManagerMessage;
import com.communote.server.model.user.User;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NewUserConfirmedForManagerMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) throws Exception {
        for (User receiver : receivers) {
            Collection<User> receiversAsCollection = new ArrayList<User>();
            receiversAsCollection.add(receiver);
            mailManagement.sendMail(new NewUserConfirmedForManagerMessage(receiver,
                    receiversAsCollection, receiver.getLanguageLocale()));
        }
    }
}
