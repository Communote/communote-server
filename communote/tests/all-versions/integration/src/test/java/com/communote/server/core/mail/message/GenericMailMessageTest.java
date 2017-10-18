package com.communote.server.core.mail.message;

import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.GenericMailMessage;
import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.model.user.User;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;

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
    public void sendMail(MailSender mailSender, User... recipients) {
        for (User recipient : recipients) {
            mailSender.send(new GenericMailMessage(
                    "mail.message.fetching.parent-note-not-found", recipient.getLanguageLocale(),
                    recipient));
            mailSender.send(new GenericMailMessage(
                    "mail.message.fetching.post-limit-reached", recipient.getLanguageLocale(),
                    recipient));
            mailSender.send(new GenericMailMessage(
                    "mail.message.fetching.reply-is-no-directmessage",
                    recipient.getLanguageLocale(), recipient));
            mailSender.send(new GenericMailMessage(
                    "mail.message.fetching.user-temporarily-disabled",
                    recipient.getLanguageLocale(), recipient));
        }
    }
}
