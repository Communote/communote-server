package com.communote.server.core.mail.message.user;

import java.sql.Timestamp;
import java.util.UUID;

import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.core.mail.messages.user.RemindUserRegistrationMailMessage;
import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.UserSecurityCode;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RemindUserRegistrationMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) throws Exception {
        for (User receiver : receivers) {
            UserSecurityCode securityCode = UserSecurityCode.Factory.newInstance(UUID.randomUUID()
                    .toString(), SecurityCodeAction.CONFIRM_USER,
                    new Timestamp(System.currentTimeMillis()), receiver);
            mailManagement.sendMail(new RemindUserRegistrationMailMessage(receiver, securityCode));
        }
    }

}
