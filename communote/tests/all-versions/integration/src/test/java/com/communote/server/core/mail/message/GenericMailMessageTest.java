package com.communote.server.core.mail.message;

import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.core.mail.messages.GenericMailMessage;
import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.model.user.User;

/**
 * This tests all message keys without own {@link MailMessage} implementation.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GenericMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) {
        for (User receiver : receivers) {
            mailManagement.sendMail(new GenericMailMessage(
                    "mail.message.fetching.parent-note-not-found", receiver.getLanguageLocale(),
                    receiver));
            mailManagement.sendMail(new GenericMailMessage(
                    "mail.message.fetching.post-limit-reached", receiver.getLanguageLocale(),
                    receiver));
            mailManagement.sendMail(new GenericMailMessage(
                    "mail.message.fetching.reply-is-no-directmessage",
                    receiver.getLanguageLocale(), receiver));
            mailManagement.sendMail(new GenericMailMessage(
                    "mail.message.fetching.user-temporarily-disabled",
                    receiver.getLanguageLocale(), receiver));
        }
    }
}
