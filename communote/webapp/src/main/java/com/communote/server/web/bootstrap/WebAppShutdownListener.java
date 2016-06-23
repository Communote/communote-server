package com.communote.server.web.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.communote.server.api.core.application.CommunoteRuntime;

/**
 * Listener which stops the Communote runtime when the web application context is destroyed.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component
public class WebAppShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebAppShutdownListener.class);

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            LOGGER.info("Stopping Communote");
            com.communote.server.api.core.application.Runtime runtime = CommunoteRuntime
                    .getInstance();
            if (runtime != null) {
                runtime.stop();
            }
        } catch (Exception e) {
            LOGGER.error("Error stopping Communote runtime", e);
        }
        LOGGER.info("Communote runtime stopped and web application context closed");
    }

}
