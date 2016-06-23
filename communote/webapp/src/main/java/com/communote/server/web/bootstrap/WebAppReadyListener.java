package com.communote.server.web.bootstrap;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.communote.server.api.core.application.CommunoteRuntime;

/**
 * Listener which fulfills the web application context init-condition as soon as the web application
 * context is ready for use.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component
public class WebAppReadyListener implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * ID of the initialization condition which will be fulfilled as soon as the web application
     * context is ready for use.
     */
    public static String WEB_APPLICATION_CONTEXT_INITIALIZATION_CONDITION = "WebApplicationContextInitialization";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        CommunoteRuntime.getInstance().fulfillInitializationCondition(
                WEB_APPLICATION_CONTEXT_INITIALIZATION_CONDITION);
    }

}
