package com.communote.server.core.mail.message.user;

import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.messages.user.ManagerSecurityWarnMailMessage;
import com.communote.server.model.user.User;
import com.communote.server.test.mail.MailMessageCommunoteIntegrationTest;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ManagerSecurityWarnMailMessageTest extends MailMessageCommunoteIntegrationTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MailManagement mailManagement, User... receivers) throws Exception {
        for (User receiver : receivers) {
            mailManagement.sendMail(new ManagerSecurityWarnMailMessage(receiver,
                    ManagerSecurityWarnMailMessage.RISK_LEVEL_HIGH,
                    ManagerSecurityWarnMailMessage.WARN_REASON_POSSIBLE_HACK_ATTEMPT, 42L));
        }
    }

}
