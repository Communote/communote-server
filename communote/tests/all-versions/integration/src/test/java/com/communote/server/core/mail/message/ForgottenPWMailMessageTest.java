package com.communote.server.core.mail.message;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.ForgottenPWMailMessage;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.ForgottenPasswordSecurityCode;
import com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ForgottenPWMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailSender mailSender, User... recipients) {
        for (User recipient : recipients) {
            ForgottenPasswordSecurityCode securityCode = ServiceLocator.findService(
                    ForgottenPasswordSecurityCodeDao.class).createCode(recipient);
            mailSender.send(new ForgottenPWMailMessage(recipient, securityCode));
        }
    }
}
