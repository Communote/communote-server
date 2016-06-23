package com.communote.server.core.mail.message.user;

import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.core.mail.messages.user.InviteUserToBlogMailMessage;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.InviteUserToBlogSecurityCode;
import com.communote.server.test.util.TestUtils;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToBlogMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) throws Exception {
        Blog blog = TestUtils.createRandomBlog(true, true, receivers);
        for (User receiver : receivers) {
            InviteUserToBlogSecurityCode code = InviteUserToBlogSecurityCode.Factory.newInstance();
            code.setUser(receiver);
            code.generateNewCode();
            mailManagement
                    .sendMail(new InviteUserToBlogMailMessage(receiver, receiver, blog, code));
        }
    }
}
