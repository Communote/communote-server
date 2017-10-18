package com.communote.server.core.mail.message.user;

import java.sql.Timestamp;
import java.util.UUID;

import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.user.RemindUserRegistrationMailMessage;
import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.UserSecurityCode;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RemindUserRegistrationMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailSender mailSender, User... recipients) throws Exception {
        for (User recipient : recipients) {
            UserSecurityCode securityCode = UserSecurityCode.Factory.newInstance(UUID.randomUUID()
                    .toString(), SecurityCodeAction.CONFIRM_USER,
                    new Timestamp(System.currentTimeMillis()), recipient);
            mailSender.send(new RemindUserRegistrationMailMessage(recipient, securityCode));
        }
    }

}
