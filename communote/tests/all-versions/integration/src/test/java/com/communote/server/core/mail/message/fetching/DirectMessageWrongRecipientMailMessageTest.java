package com.communote.server.core.mail.message.fetching;

import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.messages.GenericMailMessage;
import com.communote.server.model.user.User;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DirectMessageWrongRecipientMailMessageTest extends
        MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) {
        for (User receiver : receivers) {
            mailManagement.sendMail(new GenericMailMessage(
                    "mail.message.fetching.directmessage-wrong-recipient-message", receiver
                            .getLanguageLocale(), receivers));
        }
    }
}
