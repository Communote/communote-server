package com.communote.server.core.mail.message.user;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.core.mail.messages.user.ConfirmUserMailMessage;
import com.communote.server.core.user.UserManagement.RegistrationType;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.security.UserSecurityCodeDao;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfirmUserMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) throws Exception {
        for (User receiver : receivers) {
            mailManagement.sendMail(new ConfirmUserMailMessage(receiver, ServiceLocator
                    .findService(UserSecurityCodeDao.class).createCode(receiver),
                    RegistrationType.SELF));
            mailManagement.sendMail(new ConfirmUserMailMessage(receiver, ServiceLocator
                    .findService(UserSecurityCodeDao.class).createCode(receiver),
                    RegistrationType.INVITED));
        }
    }
}
