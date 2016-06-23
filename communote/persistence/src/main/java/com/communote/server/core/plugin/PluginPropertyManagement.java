package com.communote.server.core.plugin;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.GlobalClientDelegateCallback;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.general.RunInTransactionWithResult;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.model.property.PluginProperty;
import com.communote.server.persistence.property.PluginPropertyDao;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Service for accessing plugin properties.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class PluginPropertyManagement {

    @Autowired
    private PluginPropertyDao pluginPropertyDao;
    @Autowired
    private CacheManager cacheManager;

    private PluginPropertyCacheElementProvider pluginPropertyCacheElementProvider;

    /**
     * Method to assert if a property is either an application property or client property. This
     * method throws an {@link IllegalArgumentException}.
     *
     * @param pluginProperty
     *            The property to check.
     * @param isApplicationProperty
     *            If true, the property must be an application property, else a client property.
     */
    private void assertIsApplicationProperty(PluginProperty pluginProperty,
            boolean isApplicationProperty) {
        if (pluginProperty.isApplicationProperty() != isApplicationProperty) {
            throw new IllegalArgumentException("You tried to access a "
                    + (isApplicationProperty ? "application" : "client") + " property which is an "
                    + (isApplicationProperty ? "client" : "application") + " property (id: "
                    + pluginProperty.getId() + ")");
        }
    }

    /**
     * Returns all application properties for the given bundle.
     *
     * @param symbolicName
     *            Symbolic name of the bundle.
     * @return A map of all properties for the bundle.
     */
    public Map<String, String> getAllApplicationProperties(String symbolicName) {
        Serializable value = cacheManager.getCache().get(
                new PluginPropertyCacheKey<String>(symbolicName, true, true),
                pluginPropertyCacheElementProvider);
        return (Map<String, String>) value;
    }

    /**
     * Returns all client properties for the given bundle.
     *
     * @param symbolicName
     *            Symbolic name of the bundle.
     * @return A map of all properties for the bundle.
     */
    public Map<String, String> getAllClientProperties(String symbolicName) {
        Serializable value = cacheManager.getCache().get(
                new PluginPropertyCacheKey<String>(symbolicName, false, true),
                pluginPropertyCacheElementProvider);
        return (Map<String, String>) value;
    }

    /**
     * Returns an application property for a specific bundle.
     *
     * @param symbolicName
     *            Symbolic name of the bundle, the property should be read from.
     * @param propertyKey
     *            The key of the property.
     * @return The properties value or null, if not set.
     * @throws PluginPropertyManagementException
     *             Exception.
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public String getApplicationProperty(String symbolicName, String propertyKey)
            throws PluginPropertyManagementException {
        return internalGetApplicationProperty(symbolicName, propertyKey, cacheManager.getCache());
    }

    /**
     * Returns an application property for a specific bundle with default value.
     *
     * @param symbolicName
     *            Symbolic name of the bundle.
     * @param propertyKey
     *            The key of the property.
     * @param defaultValue
     *            The default value.
     * @return The value or the default value if no value is set.
     * @throws PluginPropertyManagementException
     *             Exception.
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public String getApplicationProperty(String symbolicName, String propertyKey,
            String defaultValue) throws PluginPropertyManagementException {
        String value = internalGetApplicationProperty(symbolicName, propertyKey,
                cacheManager.getCache());
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Returns an application property for a specific bundle bypassing the cache. This is especially
     * useful for large properties that do not need to be cached, because they are seldom used, are
     * cached by other means (e.g. on client or as a file on the server).
     *
     * @param symbolicName
     *            Symbolic name of the bundle, the property should be read from.
     * @param propertyKey
     *            The key of the property.
     * @return The properties value or null, if not set.
     * @throws PluginPropertyManagementException
     *             Exception.
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public String getApplicationPropertyUncached(String symbolicName, String propertyKey)
            throws PluginPropertyManagementException {
        return internalGetApplicationProperty(symbolicName, propertyKey, null);
    }

    /**
     * Returns a property within the current clients scope for the given bundle.
     *
     * @param symbolicName
     *            Symbolic name of the bundle to use.
     * @param propertyKey
     *            The key of the property.
     * @return The value or null, if no value is set.
     */
    @Transactional(readOnly = true)
    public String getClientProperty(String symbolicName, String propertyKey) {
        return getClientProperty(symbolicName, propertyKey, null);
    }

    /**
     * Returns a property within the current clients scope for the given bundle.
     *
     * @param symbolicName
     *            Symbolic name of the bundle to use.
     * @param propertyKey
     *            The key of the property.
     * @param defaultValue
     *            The default value.
     * @return The value or the default value, if no value is set.
     */
    @Transactional(readOnly = true)
    public String getClientProperty(String symbolicName, String propertyKey, String defaultValue) {
        Cache cache = cacheManager.getCache();
        PluginPropertyCacheKey<String> cacheKey = new PluginPropertyCacheKey<String>(symbolicName,
                propertyKey, false);
        Serializable value = cache.get(cacheKey, pluginPropertyCacheElementProvider);
        if (value == null) {
            return defaultValue;
        }
        return (String) value;
    }

    /**
     * Returns a property within the current clients scope for the given bundle. This de-serializes
     * the property value stored as JSON.
     *
     * @param symbolicName
     *            Symbolic name of the bundle to use.
     * @param propertyKey
     *            The key of the property.
     * @param clazz
     *            containing the type.
     * @param <T>
     *            Type of the value. If the type implements
     *            com.communote.server.core.common.time.LastModificationAware the last modification
     *            date of the result will be set from the last modification date of the property.
     *            The getters and setters must be ignored from JSON serialization.
     * @return The value as object or null, if no value is set.
     * @throws PluginPropertyManagementException
     *             Exception.
     */
    @Transactional(readOnly = true)
    public <T extends Serializable> T getClientPropertyAsObject(String symbolicName,
            String propertyKey, Class<T> clazz) throws PluginPropertyManagementException {
        Cache cache = cacheManager.getCache();
        PluginPropertyCacheKey<T> cacheKey = new PluginPropertyCacheKey<T>(symbolicName,
                propertyKey, false, clazz);
        Serializable value = cache.get(cacheKey, pluginPropertyCacheElementProvider);
        if (value != null && !value.getClass().isAssignableFrom(clazz)) {
            cache.invalidate(cacheKey, pluginPropertyCacheElementProvider);
            value = cache.get(cacheKey, pluginPropertyCacheElementProvider);
        }
        return (T) value;

    }

    /**
     * Lazy initialization.
     */
    @PostConstruct
    public void init() {
        pluginPropertyCacheElementProvider = new PluginPropertyCacheElementProvider(
                pluginPropertyDao, JsonHelper.getSharedObjectMapper());
    }

    private String internalGetApplicationProperty(final String symbolicName,
            final String propertyKey, final Cache cache) throws PluginPropertyManagementException {
        try {
            return (String) new ClientDelegate()
            .execute(new GlobalClientDelegateCallback<Serializable>() {
                @Override
                public Serializable doOnGlobalClient() throws Exception {
                    final PluginPropertyCacheKey<String> cacheKey = new PluginPropertyCacheKey<String>(
                            symbolicName, propertyKey, true);
                    TransactionManagement txManagement = ServiceLocator
                            .findService(TransactionManagement.class);
                    RunInTransactionWithResult<Serializable> runInTransaction;
                    runInTransaction = new RunInTransactionWithResult<Serializable>() {

                        @Override
                        public Serializable execute() throws TransactionException {
                            if (cache != null) {
                                return cache.get(cacheKey,
                                        pluginPropertyCacheElementProvider);
                            } else {
                                return pluginPropertyCacheElementProvider.load(cacheKey);
                            }
                        }
                    };
                    Serializable value;
                    if (CommunoteRuntime.getInstance().getApplicationInformation()
                                    .isStandalone()) {
                        value = txManagement.execute(runInTransaction);
                    } else {
                        // force new Tx to get new connection and run on correct DB/Schema
                        value = txManagement.executeInNew(runInTransaction);
                    }
                    return value;
                }
            });
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new PluginPropertyManagementException(e);
        }
    }

    /**
     * Sets an application property.
     *
     * @param symbolicName
     *            Symbolic name of the bundle to use.
     * @param propertyKey
     *            The key of the property.
     * @param propertyValue
     *            The value of the property.
     * @throws AuthorizationException
     *             in case the current client is not the global client
     */
    // TODO authorization should be checked
    public void setApplicationProperty(String symbolicName, String propertyKey, String propertyValue)
            throws AuthorizationException {
        ClientHelper.assertIsCurrentClientGlobal();
        setProperty(symbolicName, propertyKey, propertyValue, true);
    }

    /**
     * Sets a client property.
     *
     * @param symbolicName
     *            Symbolic name of the bundle to use.
     * @param propertyKey
     *            The key of the property.
     * @param propertyValue
     *            The value. Set this to "null" to delete the property.
     */
    // TODO authorization should be checked
    public void setClientProperty(String symbolicName, String propertyKey, String propertyValue) {
        setProperty(symbolicName, propertyKey, propertyValue, false);
    }

    /**
     * Sets a client property from an object. <br />
     * <br />
     * Internally the property value will be stored as the serialized object in JSON format in
     * combination with the class name, for instance:
     * <code>com.example.Property={"count":"42"}</code>
     *
     * @param symbolicName
     *            Symbolic name of the bundle to use.
     * @param propertyKey
     *            The key of the property.
     * @param propertyValue
     *            The value. Set this to "null" to delete the property.
     * @throws PluginPropertyManagementException
     *             Exception.
     */
    // TODO authorization should be checked
    public void setClientPropertyAsObject(String symbolicName, String propertyKey,
            Object propertyValue) throws PluginPropertyManagementException {
        String valueAsString;
        try {
            valueAsString = propertyValue.getClass().getName() + "="
                    + JsonHelper.getSharedObjectMapper().writeValueAsString(propertyValue);
        } catch (Exception e) {
            throw new PluginPropertyManagementException(e);
        }
        setProperty(symbolicName, propertyKey, valueAsString, false);
    }

    /**
     * Sets a property.
     *
     * @param symbolicName
     *            Symbolic name of the bundle to use.
     * @param propertyKey
     *            The key of the property.
     * @param propertyValue
     *            The value. Set this to "null" to delete the property.
     * @param isApplicationProperty
     *            True, if this is an application property.
     */
    private void setProperty(String symbolicName, String propertyKey, String propertyValue,
            boolean isApplicationProperty) {
        PluginProperty pluginProperty = pluginPropertyDao.find(symbolicName, propertyKey);
        Cache cache = cacheManager.getCache();
        if (pluginProperty == null) {
            if (propertyValue != null) {
                pluginProperty = PluginProperty.Factory.newInstance(isApplicationProperty,
                        propertyValue, symbolicName, propertyKey, new Date());
                pluginPropertyDao.create(pluginProperty);
                cache.invalidate(new PluginPropertyCacheKey<Serializable>(symbolicName,
                        propertyKey, isApplicationProperty), pluginPropertyCacheElementProvider);
                cache.invalidate(new PluginPropertyCacheKey<Serializable>(symbolicName,
                        isApplicationProperty, true), pluginPropertyCacheElementProvider);
            }
            return;
        }
        assertIsApplicationProperty(pluginProperty, isApplicationProperty);
        if (propertyValue == null) {
            pluginPropertyDao.remove(pluginProperty);
        } else {
            pluginProperty.setPropertyValue(propertyValue);
            pluginProperty.setLastModificationDate(new Date());
        }
        cache.invalidate(new PluginPropertyCacheKey<Serializable>(symbolicName, propertyKey,
                isApplicationProperty), pluginPropertyCacheElementProvider);
        cache.invalidate(new PluginPropertyCacheKey<Serializable>(symbolicName,
                isApplicationProperty, true), pluginPropertyCacheElementProvider);
    }

}
