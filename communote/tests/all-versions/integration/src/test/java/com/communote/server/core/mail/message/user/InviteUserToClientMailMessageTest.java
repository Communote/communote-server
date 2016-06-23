package com.communote.server.core.mail.message.user;

import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageCommunoteIntegrationTest;
import com.communote.server.core.mail.messages.user.InviteUserToClientMailMessage;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.InviteUserToClientSecurityCode;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToClientMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) throws Exception {
        SecurityCode code = InviteUserToClientSecurityCode.Factory.newInstance();
        code.generateNewCode();
        for (User receiver : receivers) {
            code.setUser(receiver);
            mailManagement.sendMail(new InviteUserToClientMailMessage(receiver, receiver
                    .getLanguageLocale(), receiver.getEmail(), code));
        }
    }
}
