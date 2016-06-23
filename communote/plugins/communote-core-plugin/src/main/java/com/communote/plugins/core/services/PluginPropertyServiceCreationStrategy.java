package com.communote.plugins.core.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.handlers.providedservice.CreationStrategy;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import com.communote.plugins.core.services.impl.PluginPropertyServiceImpl;

/**
 * Creation strategy for our custom PluginPropertyService.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PluginPropertyServiceCreationStrategy extends CreationStrategy {

    private final Map<String, PluginPropertyService> services = new HashMap<String, PluginPropertyService>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration) {
        String symbolicName = bundle.getSymbolicName();
        PluginPropertyService pluginPropertyService = services.get(symbolicName);
        if (pluginPropertyService == null) {
            pluginPropertyService = new PluginPropertyServiceImpl(symbolicName);
            services.put(symbolicName, pluginPropertyService);
        }
        return pluginPropertyService;
    }

    /**
     * Does nothing.
     * 
     * {@inheritDoc}
     */
    @Override
    public void onPublication(InstanceManager instance, String[] interfaces, Properties props) {
        // Do nothing.
    }

    /**
     * Does nothing.
     * 
     * {@inheritDoc}
     */
    @Override
    public void onUnpublication() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
        services.remove(bundle.getSymbolicName());
    }
}
