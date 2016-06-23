package com.communote.server.core.mail.fetching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationPropertyMailfetching;
import com.communote.server.core.blog.NoteMultipleAddressesMailMessageWorker;
import com.communote.server.core.blog.NoteSingleAddressMailMessageWorker;
import com.communote.server.core.blog.helper.MailBasedPostingHelper;
import com.communote.server.core.service.BuiltInServiceNames;
import com.communote.server.core.service.CommunoteSingletonService;

/**
 * Service that fetches e-mails from an external server and creates notes.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MailFetchingService implements CommunoteSingletonService {
    private static final long DEFAULT_RECONNECT_TIMEOUT = 120000;

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MailFetchingService.class);

    /**
     * Factory method to create the default mail fetching service.
     *
     * @return the mail fetching service
     */
    public static MailFetchingService createDefaultMailFetchingService() {
        return new MailFetchingService(BuiltInServiceNames.MAIL_FETCHING);
    }

    private Thread fetchThread = null;

    private boolean running = false;

    private final String name;

    /**
     * Creates a new MailFetching service with the given name.
     *
     * @param name
     *            the name of the service
     */
    public MailFetchingService(String name) {
        this.name = name;
    }

    protected MailFetcher getMailFetcher() {
        return MailFetcher.instance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the mail message worker to be used.
     *
     * @return the worker
     */
    private MailMessageWorker getWorker() {

        MailMessageWorker worker = null;
        if (MailBasedPostingHelper.isRunningInSingleAddressMode()) {
            worker = new NoteSingleAddressMailMessageWorker();
            LOGGER.debug("Using subject evaluating mail message worker");
        } else {
            worker = new NoteMultipleAddressesMailMessageWorker();
            LOGGER.debug("Using email address evaluating mail message worker");
        }
        return worker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        // always force cache reload to avoid stale settings in clustered environment
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties(true);
        return props.getProperty(ApplicationPropertyMailfetching.ENABLED, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(boolean triggeredLocally) {
        startThreaded(triggeredLocally);
    }

    /**
     * Starts the mailfetching within a thread and returns. The thread will try to start fetching
     *
     * @param triggeredLocally
     *            true if the service was triggered on this Communote instance, false if it was
     *            triggered by an event from another Communote instance when running a clustered
     *            setup
     */
    private void startThreaded(boolean triggeredLocally) {
        // do not use MailBasedPostingHelper to handle triggeredLocally correctly
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties(!triggeredLocally);

        final MailFetcher fetcher = getMailFetcher();
        if (fetcher != null) {
            final long retryWaitTime = props.getProperty(
                    ApplicationPropertyMailfetching.RECONNECT_TIMEOUT, DEFAULT_RECONNECT_TIMEOUT);
            final MailMessageWorker worker = getWorker();

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    boolean retry = true;
                    while (retry) {
                        LOGGER.info("Starting the mail fetcher");
                        try {
                            fetcher.startFetching(worker);
                            // stopped regularly, do not retry
                            retry = false;
                        } catch (MailboxConnectionException e) {
                            // retry if it may succeed
                            retry = e.getReconnectMaySucceed();

                            LOGGER.warn(
                                    "Error in mail fetcher. Retry: " + retry + " Message: "
                                            + e.getMessage(), e);
                        }
                        LOGGER.info("Mail fetcher stopped");
                        long wakeUpTime = System.currentTimeMillis() + retryWaitTime;
                        boolean stopIdling = !retry;
                        while (!stopIdling) {
                            try {
                                Thread.sleep(2000);
                                stopIdling = wakeUpTime > System.currentTimeMillis() ? false : true;
                            } catch (InterruptedException e) {
                                LOGGER.debug("Interrupted while waiting for next restart attempt",
                                        e);
                                retry = false;
                                stopIdling = true;
                            }
                        }
                    }
                    running = false;
                }
            };

            fetchThread = new Thread(r, "MailFetcherThread");
            running = true;
            fetchThread.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        MailFetcher.instance().shutdown();
        if (fetchThread != null) {
            fetchThread.interrupt();
            fetchThread = null;
            // wait until other thread has finished
            while (running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // nothing
                }
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsRestart() {
        return true;
    }

}
