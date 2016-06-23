package com.communote.plugins.core.services.impl;

import java.io.Serializable;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.plugins.core.services.PluginPropertyServiceException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.plugin.PluginPropertyManagement;
import com.communote.server.core.plugin.PluginPropertyManagementException;

/**
 * Implementation of {@link PluginPropertyService}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides(strategy = "com.communote.plugins.core.services.PluginPropertyServiceCreationStrategy")
@Instantiate
public class PluginPropertyServiceImpl implements PluginPropertyService {

    private final String symbolicName;
    private final PluginPropertyManagement pluginPropertyManagement;

    /**
     * Constructor.
     *
     * @param symbolicName
     *            Symbolic name for the concrete service.
     */
    public PluginPropertyServiceImpl(String symbolicName) {
        if (symbolicName == null || symbolicName.trim().length() == 0) {
            throw new IllegalArgumentException("symbolicName cannot be null. symbolicName='"
                    + String.valueOf(symbolicName) + "'");
        }
        this.symbolicName = symbolicName;
        pluginPropertyManagement = ServiceLocator.instance().getService(
                PluginPropertyManagement.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAllApplicationProperties() {
        return getAllApplicationProperties(symbolicName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAllApplicationProperties(String symbolicName) {
        return pluginPropertyManagement.getAllApplicationProperties(symbolicName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAllClientProperties() {
        return getAllClientProperties(symbolicName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAllClientProperties(String symbolicName) {
        return pluginPropertyManagement.getAllClientProperties(symbolicName);
    }

    @Override
    public String getApplicationProperty(String key) throws PluginPropertyServiceException {
        return getApplicationProperty(symbolicName, key);
    }

    /**
     * {@inheritDoc}
     *
     * @throws PluginPropertyServiceException
     */
    @Override
    public String getApplicationProperty(String symbolicName, String key)
            throws PluginPropertyServiceException {
        return getApplicationPropertyWithDefault(symbolicName, key, null);
    }

    /**
     * {@inheritDoc}
     *
     * @throws PluginPropertyServiceException
     */
    @Override
    public String getApplicationPropertyWithDefault(String key, String defaultValue)
            throws PluginPropertyServiceException {
        return getApplicationPropertyWithDefault(symbolicName, key, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getApplicationPropertyWithDefault(String symbolicName, String key,
            String defaultValue) throws PluginPropertyServiceException {
        try {
            return pluginPropertyManagement.getApplicationProperty(symbolicName, key, defaultValue);
        } catch (PluginPropertyManagementException e) {
            throw new PluginPropertyServiceException(e.getCause());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientProperty(String key) {
        return getClientProperty(symbolicName, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientProperty(String symbolicName, String key) {
        return getClientPropertyWithDefault(symbolicName, key, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Serializable> T getClientPropertyAsObject(String key, Class<T> clazz)
            throws PluginPropertyServiceException {
        return getClientPropertyAsObject(symbolicName, key, clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Serializable> T getClientPropertyAsObject(String symbolicName, String key,
            Class<T> clazz) throws PluginPropertyServiceException {
        try {
            return pluginPropertyManagement.getClientPropertyAsObject(symbolicName, key, clazz);
        } catch (PluginPropertyManagementException e) {
            throw new PluginPropertyServiceException(e.getCause());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientPropertyWithDefault(String key, String defaultValue) {
        return getClientPropertyWithDefault(symbolicName, key, defaultValue);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientPropertyWithDefault(String symbolicName, String key, String defaultValue) {
        return pluginPropertyManagement.getClientProperty(symbolicName, key, defaultValue);
    }

    @Override
    public void setApplicationProperty(String key, String value) throws AuthorizationException {
        pluginPropertyManagement.setApplicationProperty(symbolicName, key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientProperty(String key, String value) {
        pluginPropertyManagement.setClientProperty(symbolicName, key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientPropertyAsObject(String key, Serializable value)
            throws PluginPropertyServiceException {
        try {
            pluginPropertyManagement.setClientPropertyAsObject(symbolicName, key, value);
        } catch (PluginPropertyManagementException e) {
            throw new PluginPropertyServiceException(e.getCause());
        }
    }
}
