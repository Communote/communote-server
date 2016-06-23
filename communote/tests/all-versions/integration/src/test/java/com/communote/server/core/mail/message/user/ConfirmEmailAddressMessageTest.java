package com.communote.server.core.mail.message.user;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.core.mail.messages.user.ConfirmEmailAddressMessage;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.EmailSecurityCode;
import com.communote.server.persistence.user.security.EmailSecurityCodeDao;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfirmEmailAddressMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) throws Exception {
        for (User receiver : receivers) {
            EmailSecurityCode code = ServiceLocator.findService(EmailSecurityCodeDao.class)
                    .createCode(receiver, receiver.getEmail());
            mailManagement.sendMail(new ConfirmEmailAddressMessage(receiver, code));
        }
    }
}
