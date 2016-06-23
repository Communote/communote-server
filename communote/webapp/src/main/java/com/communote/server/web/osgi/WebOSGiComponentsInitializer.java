package com.communote.server.web.osgi;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.bootstrap.CustomInitializer;
import com.communote.server.core.osgi.CommunoteBundleListener;
import com.communote.server.core.osgi.OSGiManagement;

/**
 * Adds all {@link CommunoteBundleListener}s found in the web-application context to the
 * {@link OSGiManagement}. Also registers the location of the web specific osgi.properties.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Service
public class WebOSGiComponentsInitializer implements CustomInitializer {

    @Autowired(required = false)
    private Collection<CommunoteBundleListener> listeners = new ArrayList<CommunoteBundleListener>();

    /**
     * Starts the Framework.
     */
    @PostConstruct
    public void beanInitialized() {
        // register bundle listeners directly or as soon as the core is initialized with the help of
        // a custom initializer
        if (CommunoteRuntime.getInstance().isCoreInitialized()) {
            registerBundleListeners();
        } else {
            CommunoteRuntime.getInstance().addCustomInitializer(this);
        }
    }

    /**
     * @return The listeners.
     */
    public Collection<CommunoteBundleListener> getListeners() {
        return listeners;
    }

    @Override
    public void initialize() {
        registerBundleListeners();
    }

    private void registerBundleListeners() {
        OSGiManagement osgiManagement = ServiceLocator.instance().getService(OSGiManagement.class);
        if (listeners != null && !listeners.isEmpty()) {
            osgiManagement.addListeners(listeners);
        }
        osgiManagement
        .addFrameworkPropertiesLocation("classpath:com/communote/server/web/osgi/web_osgi.properties");
    }

    /**
     * @param listener
     *            List of listeners.
     */
    public void setListeners(Collection<CommunoteBundleListener> listener) {
        this.listeners = listener;
    }
}
