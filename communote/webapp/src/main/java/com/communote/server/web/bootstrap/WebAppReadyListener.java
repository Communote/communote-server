package com.communote.server.web.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.communote.server.api.core.application.CommunoteRuntime;

/**
 * Listener which fulfills the web application context init-condition as soon as the web application
 * context is ready for use.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @see com.communote.server.web.bootstrap.DispatcherServletInitializer
 */
@Component
public class WebAppReadyListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebAppReadyListener.class);
    
    /**
     * ID of the initialization condition which will be fulfilled as soon as the web application
     * context is ready for use.
     */
    public static String WEB_APPLICATION_CONTEXT_INITIALIZATION_CONDITION = "WebApplicationContextInitialization";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOGGER.debug("Web application context initialization completed");
        CommunoteRuntime.getInstance().fulfillInitializationCondition(
                WEB_APPLICATION_CONTEXT_INITIALIZATION_CONDITION);
    }

}
