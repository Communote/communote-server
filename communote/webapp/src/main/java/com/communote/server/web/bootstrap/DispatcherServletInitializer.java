package com.communote.server.web.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.core.bootstrap.ApplicationPreparedCallback;
import com.communote.server.core.bootstrap.InstallationPreparedCallback;

/**
 * Component which initializes the main Spring DispatcherServlet which handles all requests. This
 * component ensures that the web application context of the DispatcherServlet is configured
 * correctly:
 * <ul>
 * <li>if Communote is not yet installed, the context will contain only beans required by the
 * installer</li>
 * <li>if Communote is already installed, the context will contain all beans needed for normal
 * operation and will have the core application context (backend beans) as a parent context so that
 * these beans can be autowired and don't have to be fetched via the ServiceLocator</li>
 * <li>if Communote is not yet installed but the installation process is completed by the user, the
 * web application context of the DispatcherServlet is refreshed to contain the beans and have the
 * parent context as described above</li>
 * </ul>
 * For Spring security to work correctly after the refresh a special delegating filter proxy
 * (com.communote.server.web.commons.filter.RefreshAwareDelegatingFilterProxy) is used.
 * 
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 */
public class DispatcherServletInitializer implements ApplicationPreparedCallback,
        InstallationPreparedCallback {

    private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            // just delegate the refresh event to the DispatcherServlet
            mainDispatcherServlet.onApplicationEvent(event);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServletInitializer.class);
    // param holding location of web-app context configuration which should be used after installation (see web.xml)
    private static final String PARAM_WEB_CONTEXT_CONFIG_LOCATION = "communoteWebContextConfigLocation";
    // param holding location of web-app context configuration which should be used during installation
    private static final String PARAM_INSTALLER_WEB_CONTEXT_CONFIG_LOCATION = "communoteInstallerWebContextConfigLocation";

    private final ServletContext servletContext;
    private DispatcherServlet mainDispatcherServlet;

    private XmlWebApplicationContext mainWebApplicationContext;

    public DispatcherServletInitializer(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void applicationPrepared(ApplicationContext applicationContext) {
        if (this.mainDispatcherServlet == null) {
            // delay complete application initialization until the web application context was
            // completely initialized
            CommunoteRuntime.getInstance().addInitializationCondition(
                    WebAppReadyListener.WEB_APPLICATION_CONTEXT_INITIALIZATION_CONDITION);
            // set the core application context as parent context of the web app context
            LOGGER.debug("Creating main DispatcherServlet with web application context and parent core context");
            createMainDispatcherServlet(PARAM_WEB_CONTEXT_CONFIG_LOCATION, applicationContext);
        } else {
            // the main DispatcherServlet already exists which means when Communote was started it
            // was not yet installed. Now the installation process got that far that the core app
            // context is available. Thus, we refresh the web app context.

            // First we need to add a refresh listener otherwise the DispatcherServlet won't be
            // informed about the refresh of the context. URL handler mappings and other stuff
            // wouldn't be updated then. The listener must also be added to the static listeners
            // otherwise it will be lost when the context is refreshed.
            mainWebApplicationContext.getApplicationListeners().add(new SourceFilteringListener(
                    mainWebApplicationContext, new ContextRefreshListener()));
            // set core app context as parent
            mainWebApplicationContext.setParent(applicationContext);
            // set the new config location containing the beans to be used after installation
            mainWebApplicationContext
                    .setConfigLocation(getRequiredInitParameter(PARAM_WEB_CONTEXT_CONFIG_LOCATION));
            LOGGER.info("Refreshing web application context");
            mainDispatcherServlet.refresh();
        }
    }

    private void createMainDispatcherServlet(String contextConfigLocationParameter,
            ApplicationContext rootContext) {
        mainWebApplicationContext = new XmlWebApplicationContext();
        mainWebApplicationContext
                .setConfigLocation(getRequiredInitParameter(contextConfigLocationParameter));
        mainWebApplicationContext.setParent(rootContext);
        // add ContextLoaderListener with web-ApplicationContext which publishes it under a
        // ServletContext attribute and closes it on shutdown. The former is required for
        // WebApplicationContextUtils which are used by DelegatingFilterProxy (spring security).
        // Closing is also done by dispatcher servlet's implementation of destroy method.
        this.servletContext.addListener(new ContextLoaderListener(mainWebApplicationContext));
        DispatcherServlet dispatcherServlet = new DispatcherServlet(mainWebApplicationContext);
        ServletRegistration.Dynamic addedServlet = this.servletContext.addServlet(
                getInitParameter("communoteServletName", "communote"), dispatcherServlet);
        addedServlet.setLoadOnStartup(1);
        addedServlet.addMapping(getRequiredInitParameter("communoteServletUrlPattern"));
        this.mainDispatcherServlet = dispatcherServlet;
    }

    private String getInitParameter(String parameterName, String fallbackValue) {
        String value = servletContext.getInitParameter(parameterName);
        if (value == null) {
            return fallbackValue;
        }
        return value;
    }

    private String getRequiredInitParameter(String parameterName) {
        String value = servletContext.getInitParameter(parameterName);
        if (value == null) {
            throw new BootstrapException("Required servlet context initialization parameter "
                    + parameterName + " is not defined");
        }
        return value;
    }

    @Override
    public void installationPrepared() {
        // When programmatically creating servlets all servlets have to be created before the
        // servlet context is initialized. But since the (root) application context cannot be
        // initialized until the installation is done we don't set a parent context. This will be
        // done when applicationPrepared is called. The null check should avoid re-creating the
        // dispatcher servlet if the Runtime is restarted by the installer.
        if (this.mainDispatcherServlet == null) {
            LOGGER.debug("Creating main DispatcherServlet with installer web application context");
            createMainDispatcherServlet(PARAM_INSTALLER_WEB_CONTEXT_CONFIG_LOCATION, null);
        }
    }

}
