package com.communote.server.core.mail.message.fetching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.fetching.BlogsNotFoundMailMessage;
import com.communote.server.model.user.User;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogsNotFoundMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailSender mailSender, User... recipients) {
        Collection<String> blogAlias = new ArrayList<String>();
        blogAlias.add(UUID.randomUUID().toString());
        Collection<String> blogAliases = new ArrayList<String>();
        blogAliases.add(UUID.randomUUID().toString());
        blogAliases.add(UUID.randomUUID().toString());
        blogAliases.add(UUID.randomUUID().toString());
        for (User recipient : recipients) {
            mailSender.send(new BlogsNotFoundMailMessage(recipient, blogAliases, true));
            mailSender.send(new BlogsNotFoundMailMessage(recipient, blogAliases, false));
            mailSender.send(new BlogsNotFoundMailMessage(recipient, blogAlias, false));
        }
    }
}
