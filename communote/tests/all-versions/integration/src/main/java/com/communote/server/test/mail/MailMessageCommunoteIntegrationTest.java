package com.communote.server.test.mail;

import java.util.Locale;

import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.mail.MailManagement;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class MailMessageCommunoteIntegrationTest extends CommunoteIntegrationTest {
    /**
     * This message is called while the test is running.
     * 
     * @param mailManagement
     *            The management.
     * @param receivers
     *            List of receivers.
     * @throws Exception
     *             Exception.
     */
    public abstract void sendMail(MailManagement mailManagement, User... receivers)
            throws Exception;

    /**
     * Delegates to #sendMail
     * 
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testSendMail() throws Exception {
        User englishUser = TestUtils.createRandomUser(false);
        User germanUser = TestUtils.createRandomUser(false);
        germanUser.setLanguageCode(Locale.GERMAN.getLanguage());
        germanUser.setLanguageLocale(Locale.GERMAN);
        sendMail(ServiceLocator.instance().getService(MailManagement.class), englishUser,
                germanUser);
    }
}
