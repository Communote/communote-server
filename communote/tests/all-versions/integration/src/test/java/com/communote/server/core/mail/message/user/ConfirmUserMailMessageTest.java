package com.communote.server.core.mail.message.user;

import org.springframework.security.core.Authentication;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.user.ConfirmUserMailMessage;
import com.communote.server.core.user.UserManagement.RegistrationType;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.security.UserSecurityCodeDao;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfirmUserMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailSender mailSender, User... recipients) throws Exception {
        for (User recipient : recipients) {
            mailSender.send(new ConfirmUserMailMessage(recipient, ServiceLocator
                    .findService(UserSecurityCodeDao.class).createCode(recipient),
                    RegistrationType.SELF));
        }
        // admin user must be set for invitation
        Authentication oldAuth = AuthenticationTestUtils.setManagerContext();
        try {
            for (User recipient : recipients) {
                mailSender.send(new ConfirmUserMailMessage(recipient, ServiceLocator
                        .findService(UserSecurityCodeDao.class).createCode(recipient),
                        RegistrationType.INVITED));
            }
        } finally {
            AuthenticationTestUtils.setAuthentication(oldAuth);
        }

    }
}
