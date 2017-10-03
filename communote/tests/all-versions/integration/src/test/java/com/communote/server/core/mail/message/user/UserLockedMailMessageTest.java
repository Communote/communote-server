package com.communote.server.core.mail.message.user;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.user.UserLockedMailMessage;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.UnlockUserSecurityCode;
import com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserLockedMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailSender mailSender, User... recipients) throws Exception {
        for (User recipient : recipients) {
            UnlockUserSecurityCode securityCode = ServiceLocator.findService(
                    UnlockUserSecurityCodeDao.class).createCode(recipient, ChannelType.WEB);
            mailSender.send(new UserLockedMailMessage(recipient, ChannelType.WEB,
                    securityCode));
        }
    }
}
