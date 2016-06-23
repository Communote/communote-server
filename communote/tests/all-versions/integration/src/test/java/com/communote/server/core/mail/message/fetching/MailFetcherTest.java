package com.communote.server.core.mail.message.fetching;

import java.util.HashMap;
import java.util.Map;

import javax.mail.Message;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ApplicationPropertyMailfetching;
import com.communote.server.core.mail.fetching.MailFetcher;
import com.communote.server.core.mail.fetching.MailFetchingService;
import com.communote.server.core.mail.fetching.MailMessageWorker;
import com.communote.server.core.mail.fetching.MailboxConnectionException;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.service.BuiltInServiceNames;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * Test the mail fetcher service logic and restart capabilities
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MailFetcherTest extends CommunoteIntegrationTest {

    /**
     * Mock the mail fetcher. It holds a count for the number of startFetching calls.
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     *
     */
    public class MockMailFetcher extends MailFetcher {

        private MailboxConnectionException throwMe;

        private int count;

        @Override
        public boolean deleteMessage(Message message) {
            return true;
        }

        @Override
        public boolean expungeMessages() {
            return true;
        }

        public int getCount() {
            return count;
        }

        public MailboxConnectionException getThrowMe() {
            return throwMe;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void setThrowMe(MailboxConnectionException throwMe) {
            this.throwMe = throwMe;
        }

        @Override
        public void shutdown() {

        }

        @Override
        public void startFetching(MailMessageWorker worker) throws MailboxConnectionException {

            while (throwMe == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                count++;
            }

            MailboxConnectionException e = throwMe;
            throwMe = null;
            throw e;

        }

    }

    /**
     * Mock the mail fetching service.
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     *
     */
    public class MockMailFetchingService extends MailFetchingService {

        private final MockMailFetcher mailFetcher = new MockMailFetcher();

        public MockMailFetchingService(String name) {
            super(name);
        }

        @Override
        protected MailFetcher getMailFetcher() {
            return mailFetcher;
        }

        public MockMailFetcher getMockMailFetcher() {
            return mailFetcher;
        }

    }

    /**
     * Delegates to #sendMail
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testMailFetcher() throws Exception {

        AuthenticationHelper.setAsAuthenticatedUser(TestUtils.createRandomUser(true));

        Map<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        // set reconnect to zero for faster testing
        settings.put(ApplicationPropertyMailfetching.RECONNECT_TIMEOUT, "0");
        CommunoteRuntime.getInstance().getConfigurationManager()
        .updateApplicationConfigurationProperties(settings);

        MockMailFetchingService mailFetchingService = new MockMailFetchingService(
                BuiltInServiceNames.MAIL_FETCHING);

        // run
        mailFetchingService.start(true);

        Assert.assertTrue(mailFetchingService.isRunning());

        // wait for the count to be set
        int count1 = 0;
        int max = 10;
        while (count1 == 0 && max > 0) {
            Thread.sleep(500);
            count1 = mailFetchingService.getMockMailFetcher().getCount();
            max--;
        }
        if (max <= 0) {
            Assert.fail("MailFetcher was not called.");
        }
        // now throw an exception when fetcher runs again
        mailFetchingService.getMockMailFetcher().setThrowMe(
                new MailboxConnectionException("test", true));

        Assert.assertTrue(mailFetchingService.isRunning());
        Thread.sleep(500);

        // wait for the second count to be set
        int count2 = count1;
        max = 10;
        while (count2 == count1 && max > 0) {
            Thread.sleep(500);
            count2 = mailFetchingService.getMockMailFetcher().getCount();
            max--;
        }
        if (max <= 0) {
            Assert.fail("MailFetcher was not called again.");
        }

        // where should be more counts as before since the fetcher should continue directly after
        // the thrown exception
        Assert.assertTrue(count1 < count2, "count1: " + count1 + " count2: " + count2);

        // now do the same with an exception that will stop the mail fetcher
        mailFetchingService.getMockMailFetcher().setThrowMe(
                new MailboxConnectionException("test", false));

        max = 10;
        while (mailFetchingService.isRunning() && max > 0) {
            Thread.sleep(500);
            max--;
        }
        Assert.assertFalse(mailFetchingService.isRunning());
    }
}
