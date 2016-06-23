package com.communote.server.core.plugin;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.time.LastModificationAware;
import com.communote.server.model.property.PluginProperty;
import com.communote.server.persistence.property.PluginPropertyDao;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PluginPropertyCacheElementProvider implements
        CacheElementProvider<PluginPropertyCacheKey<? extends Serializable>, Serializable> {

    private final PluginPropertyDao pluginPropertyDao;

    private final ObjectMapper objectMapper;

    /**
     * Constructor.
     * 
     * @param pluginPropertyDao
     *            The dao to use for access.
     * @param objectMapper
     *            The object mapper to use.
     */
    public PluginPropertyCacheElementProvider(PluginPropertyDao pluginPropertyDao,
            ObjectMapper objectMapper) {
        this.pluginPropertyDao = pluginPropertyDao;
        this.objectMapper = objectMapper;
    }

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
            throw new IllegalArgumentException(
                    "You tried to access a " + (isApplicationProperty ? "application" : "client")
                            + " property which is an "
                            + (isApplicationProperty ? "client" : "application")
                            + " property (id: " + pluginProperty.getId() + ")");
        }
    }

    /**
     * @return PluginProperty.
     */
    @Override
    public String getContentType() {
        return "pluginProperty";
    }

    /**
     * @param symbolicName
     *            Symbolic name of the bundle.
     * @param propertyKey
     *            The properties key.
     * @param isApplicationProperty
     *            True, if an application property, false for client property.
     * @return The property or null.
     */
    private PluginProperty getProperty(String symbolicName, String propertyKey,
            boolean isApplicationProperty) {
        PluginProperty pluginProperty = pluginPropertyDao.find(symbolicName, propertyKey);
        if (pluginProperty == null) {
            return null;
        }
        assertIsApplicationProperty(pluginProperty, isApplicationProperty);
        return pluginProperty;
    }

    /**
     * @param symbolicName
     *            Symbolic name of the bundle.
     * @param propertyKey
     *            The properties key.
     * @param isApplicationProperty
     *            True, if an application property, false for client property.
     * @return The property as object or null.
     * @param clazz
     *            Class holding the properties type.
     * @throws JsonParseException
     *             Exception.
     * @throws JsonMappingException
     *             Exception.
     * @throws IOException
     *             Exception.
     */
    private Serializable getPropertyAsObject(String symbolicName, String propertyKey,
            boolean isApplicationProperty, Class<? extends Serializable> clazz)
            throws JsonParseException, JsonMappingException, IOException {
        PluginProperty property = getProperty(symbolicName, propertyKey, isApplicationProperty);
        if (property == null || property.getPropertyValue() == null) {
            return null;
        }
        String valueAsString = StringUtils.substringAfter(property.getPropertyValue(), "=");
        Serializable result = objectMapper.readValue(valueAsString, clazz);
        if (result instanceof LastModificationAware) {
            ((LastModificationAware) result).setLastModificationDate(property
                    .getLastModificationDate());
        }
        return result;
    }

    private String getPropertyValue(String symbolicName, String propertyKey,
            boolean isApplicationProperty) {
        PluginProperty property = getProperty(symbolicName, propertyKey, isApplicationProperty);
        if (property != null) {
            return property.getPropertyValue();
        }
        return null;
    }

    /**
     * @return 7200.
     */
    @Override
    public int getTimeToLive() {
        return 7200;
    }

    @Override
    public Serializable load(PluginPropertyCacheKey<? extends Serializable> key) {
        if (key.isLoadAllProperties()) {
            return new HashMap<String, String>(pluginPropertyDao.getAllProperties(
                    key.getSymbolicName(), key.isApplicationProperty()));
        }
        if (key.getType() == null) {
            return getPropertyValue(key.getSymbolicName(), key.getPropertyKey(),
                    key.isApplicationProperty());
        }
        try {
            return getPropertyAsObject(key.getSymbolicName(), key.getPropertyKey(),
                    key.isApplicationProperty(), key.getType());
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

}
