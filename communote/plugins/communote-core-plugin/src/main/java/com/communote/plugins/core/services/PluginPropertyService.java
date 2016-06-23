package com.communote.plugins.core.services;

import java.io.Serializable;
import java.util.Map;

import com.communote.server.api.core.security.AuthorizationException;

/**
 * Interface for the plugin property service.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 *         TODO exception handling: throwing only generic checked exceptions will not allow to react
 *         in a specific way.
 */
public interface PluginPropertyService {

    /**
     * Returns all application properties for this bundle.
     *
     * @return A map of all properties for the bundle.
     */
    Map<String, String> getAllApplicationProperties();

    /**
     * Returns all application properties for the given bundle.
     *
     * @param symbolicName
     *            Symbolic name of the bundle.
     * @return A map of all properties for the bundle.
     */
    Map<String, String> getAllApplicationProperties(String symbolicName);

    /**
     * Returns all client properties for this bundle.
     *
     * @return A map of all properties for the bundle.
     */
    Map<String, String> getAllClientProperties();

    /**
     * Returns all client properties for the given bundle.
     *
     * @param symbolicName
     *            Symbolic name of the bundle.
     * @return A map of all properties for the bundle.
     */
    Map<String, String> getAllClientProperties(String symbolicName);

    /**
     * Returns an application property for this bundle.
     *
     * @param key
     *            The key of the property.
     * @return The properties value or null, if not set.
     * @throws PluginPropertyServiceException
     *             Exception.
     */
    String getApplicationProperty(String key) throws PluginPropertyServiceException;

    /**
     * Returns an application property for a specific bundle.
     *
     * @param symbolicName
     *            Symbolic name of the bundle, the property should be read from.
     * @param key
     *            The key of the property.
     * @return The properties value or null, if not set.
     * @throws PluginPropertyServiceException
     *             Exception.
     */
    String getApplicationProperty(String symbolicName, String key)
            throws PluginPropertyServiceException;

    /**
     * Returns an application property for this bundle.
     *
     * @param key
     *            The key of the property.
     * @param defaultValue
     *            The default value.
     * @return The properties value or the default value if no value is set.
     * @throws PluginPropertyServiceException
     *             Exception.
     */
    String getApplicationPropertyWithDefault(String key, String defaultValue)
            throws PluginPropertyServiceException;

    /**
     * Returns an application property for a specific bundle with default value.
     *
     * @param symbolicName
     *            Symbolic name of the bundle.
     * @param key
     *            The key of the property.
     * @param defaultValue
     *            The default value.
     * @return The value or the default value if no value is set.
     * @throws PluginPropertyServiceException
     *             Exception.
     */
    String getApplicationPropertyWithDefault(String symbolicName, String key, String defaultValue)
            throws PluginPropertyServiceException;

    /**
     * Returns a property within the current clients scope.
     *
     * @param key
     *            The key of the property.
     * @return The value or the null, if no value is set.
     */
    String getClientProperty(String key);

    /**
     * Returns a property within the current clients scope for the given bundle.
     *
     * @param symbolicName
     *            Symbolic name of the bundle to use.
     * @param key
     *            The key of the property.
     * @return The value or null, if no value is set.
     */
    String getClientProperty(String symbolicName, String key);

    /**
     * Returns an instance of the given object.
     *
     * @param key
     *            The key of the property.
     * @param <T>
     *            Type of the value.
     * @param clazz
     *            Class containing the result type.
     * @return The value as object or null, if no value is set.
     * @throws PluginPropertyServiceException
     *             Exception.
     */
    <T extends Serializable> T getClientPropertyAsObject(String key, Class<T> clazz)
            throws PluginPropertyServiceException;

    /**
     * Returns a property within the current clients scope for the given bundle.
     *
     * @param symbolicName
     *            Symbolic name of the bundle to use.
     * @param key
     *            The key of the property.
     * @param clazz
     *            Class containing the result type.
     * @param <T>
     *            Type of the value.
     * @return The value as object or null, if no value is set.
     * @throws PluginPropertyServiceException
     *             Exception..
     */
    <T extends Serializable> T getClientPropertyAsObject(String symbolicName, String key,
            Class<T> clazz) throws PluginPropertyServiceException;

    /**
     * * Returns a property within the current clients scope.
     *
     * @param key
     *            The key of the property.
     * @param defaultValue
     *            The default value.
     * @return The value or the default value, if no value is set.
     */
    String getClientPropertyWithDefault(String key, String defaultValue);

    /**
     * Returns a property within the current clients scope for the given bundle.
     *
     * @param symbolicName
     *            Symbolic name of the bundle to use.
     * @param key
     *            The key of the property.
     * @param defaultValue
     *            The default value.
     * @return The value or the default value, if no value is set.
     */
    String getClientPropertyWithDefault(String symbolicName, String key, String defaultValue);

    /**
     * Sets an application property.
     *
     * @param key
     *            The key of the property.
     * @param value
     *            The value of the property.
     * @throws AuthorizationException
     *             in case the current client is not the global client
     */
    void setApplicationProperty(String key, String value) throws AuthorizationException;

    /**
     * Sets a client property.
     *
     * @param key
     *            The key of the property.
     * @param value
     *            The value. Set this to "null" to delete the property.
     */
    void setClientProperty(String key, String value);

    /**
     * Sets a client property from an object.
     *
     * @param key
     *            The key of the property.
     * @param value
     *            The value. Set this to "null" to delete the property.
     * @throws PluginPropertyServiceException
     *             Exception.
     */
    void setClientPropertyAsObject(String key, Serializable value)
            throws PluginPropertyServiceException;

}