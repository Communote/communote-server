package com.communote.server.core.mail.message.user;

import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.user.InviteUserToClientMailMessage;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.InviteUserToClientSecurityCode;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToClientMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailSender mailSender, User... recipients) throws Exception {
        SecurityCode code = InviteUserToClientSecurityCode.Factory.newInstance();
        code.generateNewCode();
        for (User recipient : recipients) {
            code.setUser(recipient);
            mailSender.send(new InviteUserToClientMailMessage(recipient, recipient
                    .getLanguageLocale(), recipient.getEmail(), code));
        }
    }
}
