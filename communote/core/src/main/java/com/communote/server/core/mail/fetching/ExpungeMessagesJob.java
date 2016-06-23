/**
 *
 */
package com.communote.server.core.mail.fetching;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.service.BuiltInServiceNames;
import com.communote.server.core.service.CommunoteServiceManager;

/**
 * A job that expunges deleted messages in the mail folder used by the {@link MailFetcher} instance.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ExpungeMessagesJob extends QuartzJobBean {

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(ExpungeMessagesJob.class);

    /**
     * (non-Javadoc)
     *
     * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // check if the service is running
        if (ServiceLocator.findService(CommunoteServiceManager.class).isRunning(
                BuiltInServiceNames.MAIL_FETCHING)) {
            if (MailFetcher.instance() != null) {
                LOGGER.info("Starting expunge messages job.");
                MailFetcher.instance().expungeMessages();
                LOGGER.info("Expunge messages job finished.");
            }
        }
    }

}
