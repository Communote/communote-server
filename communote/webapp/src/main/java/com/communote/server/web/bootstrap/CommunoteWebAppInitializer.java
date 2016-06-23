package com.communote.server.web.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.bootstrap.ApplicationInitializationException;
import com.communote.server.core.application.DefaultRuntimeBuilder;

/**
 * WebApplicationInitializer that starts Communote.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteWebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        DispatcherServletInitializer servletInitializer = new DispatcherServletInitializer(
                servletContext);
        DefaultRuntimeBuilder.getInstance().setInstallationPreparedCallback(servletInitializer);
        DefaultRuntimeBuilder.getInstance().setApplicationPreparedCallback(servletInitializer);
        DefaultRuntimeBuilder.getInstance()
                .setApplicationDirectory(servletContext.getRealPath("/"));
        try {
            CommunoteRuntime.init(DefaultRuntimeBuilder.getInstance());
            CommunoteRuntime.getInstance().start();
        } catch (ApplicationInitializationException e) {
            // ignore because the starting controller will show a nice error page
        }

    }

}
