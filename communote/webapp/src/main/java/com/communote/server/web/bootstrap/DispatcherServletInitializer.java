package com.communote.server.web.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.core.bootstrap.ApplicationPreparedCallback;
import com.communote.server.core.bootstrap.InstallationPreparedCallback;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DispatcherServletInitializer implements ApplicationPreparedCallback,
        InstallationPreparedCallback {

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
            // there is currently no benefit in setting the applicationContext as parent context
            // because of the installer use-case. As long as wee do not find a way to have a
            // separate web app context for the installer components the web-beans cannot have
            // autowired core beans because these are not available when not yet installed
            // (BeanCreationExeptions).
            createMainDispatcherServlet(null);
        } else {
            // if we find a way to solve the installer-use-case as mentioned above we could use this
            // else branch to add the core context as parent and refresh the web app context like
            // so:

            // First we need to add a refresh listener otherwise the dispatcherServlet won't be
            // informed about the refresh of the context. URL handler mappings and other stuff
            // wouldn't be updated then. The listener must also be added to the static listeners
            // otherwise it will be lost when the context is refreshed
            // mainWebApplicationContext.getApplicationListeners().add( new
            // SourceFilteringListener(mainWebApplicationContext, new ContextRefreshListener()));
            // mainWebApplicationContext.setParent(applicationContext);
            // mainDispatcherServlet.refresh();
            // The ContextRefreshListener is just an implementation of ApplicationListener which
            // delegates the applicationEvent to the mainDispatcherServlet.
        }
    }

    private void createMainDispatcherServlet(ApplicationContext rootContext) {
        mainWebApplicationContext = new XmlWebApplicationContext();
        mainWebApplicationContext
                .setConfigLocation(getRequiredInitParameter("communoteWebContextConfigLocation"));
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
        // TODO could add a separate dispatcher servlet which only handles the installer. With a
        // special filter that forwards to internal/installer the installer could than be removed
        // from spring security. Moreover the installer servlet wouldn't be needed when starting
        // Communote after the installation is done.

        // another idea would be to have one dispatcher servlet and a special 'installer' bean
        // profile which includes only the installer beans. If not installed we activate the
        // installer profile otherwise the 'default' one. In applicationPrepared the installer
        // profile is deactivated and the context is refreshed. But at this point the installation
        // is not complete and thus some (all?) installer beans also have to be in the default
        // profile. It is also unsure what happens if a request is sent while the context is
        // being refreshed...

        // When programmatically creating servlets all servlets have to be created before the
        // servlet context is initialized. But since the (root) application context cannot be
        // initialized until the installation is done we do not set aparent context. This will be
        // done when applicationPrepared is called. The null check should avoid re-creating the
        // dispatcher servlet if the Runtime is restarted (by the installer).
        if (this.mainDispatcherServlet == null) {
            createMainDispatcherServlet(null);
        }
    }

}
