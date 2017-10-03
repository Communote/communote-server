package com.communote.server.core.mail.message.user;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.user.ConfirmEmailAddressMessage;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.EmailSecurityCode;
import com.communote.server.persistence.user.security.EmailSecurityCodeDao;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfirmEmailAddressMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailSender mailSender, User... recipients) throws Exception {
        for (User recipient : recipients) {
            EmailSecurityCode code = ServiceLocator.findService(EmailSecurityCodeDao.class)
                    .createCode(recipient, recipient.getEmail());
            mailSender.send(new ConfirmEmailAddressMessage(recipient, code));
        }
    }
}
