package com.communote.server.core.mail.message.fetching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.communote.server.core.mail.MailManagement;
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
    public void sendMail(MailManagement mailManagement, User... receivers) {
        Collection<String> blogAlias = new ArrayList<String>();
        blogAlias.add(UUID.randomUUID().toString());
        Collection<String> blogAliases = new ArrayList<String>();
        blogAliases.add(UUID.randomUUID().toString());
        blogAliases.add(UUID.randomUUID().toString());
        blogAliases.add(UUID.randomUUID().toString());
        for (User receiver : receivers) {
            mailManagement.sendMail(new BlogsNotFoundMailMessage(receiver, blogAliases, true));
            mailManagement.sendMail(new BlogsNotFoundMailMessage(receiver, blogAliases, false));
            mailManagement.sendMail(new BlogsNotFoundMailMessage(receiver, blogAlias, false));
        }
    }
}
