package com.communote.server.core.mail.message.user;

import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.user.InviteUserToBlogWithExternalAuthMailMessage;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToBlogWithExternalAuthMailMessageTest extends
        MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailSender mailSender, User... recipients) throws Exception {
        Blog blog = TestUtils.createRandomBlog(true, true, recipients);
        for (User recipient : recipients) {
            mailSender.send(new InviteUserToBlogWithExternalAuthMailMessage(recipient,
                    recipient,
                    blog));
        }
    }
}
