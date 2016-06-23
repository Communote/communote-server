package com.communote.server.plugins.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessor;


/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
// TODO Is this still used anywhere? If not, remove.
// TODO Is this deprecated? Is the new functionality UserServer#getExternalUserGroupAccessor?
public class ExternalUserGroupAccessorExtensionPoint {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ExternalUserGroupAccessorExtensionPoint.class);

    private final Map<String, ExternalUserGroupAccessor> accessors;
    private static ExternalUserGroupAccessorExtensionPoint INSTANCE = null;

    /**
     * @return Instance of this RemoteUSerGroup.
     */
    public static synchronized ExternalUserGroupAccessorExtensionPoint instance() {
        if (INSTANCE == null) {
            INSTANCE = new ExternalUserGroupAccessorExtensionPoint();
        }
        return INSTANCE;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private ExternalUserGroupAccessorExtensionPoint() {
        accessors = new HashMap<String, ExternalUserGroupAccessor>();
    }

    /**
     * Returns the accessor for the specific system.
     * 
     * @param systemId
     *            Id.
     * @return {@link ExternalUserGroupAccessor} or null, if there is no one.
     */
    public ExternalUserGroupAccessor getAccessor(String systemId) {
        return accessors.get(systemId);
    }

    /**
     * @return the accessors
     */
    public Map<String, ExternalUserGroupAccessor> getAccessors() {
        return accessors;
    }

    /**
     * This method uses {@link ExternalUserGroupAccessor#supports(Object)} for checking.
     * 
     * @param externalConfiguration
     *            external configuration.
     * @return Returns an accessor for this configuration or null, if there is no one.
     */
    public ExternalUserGroupAccessor getExternalUserGroupAccessor(
            ExternalSystemConfiguration externalConfiguration) {
        ExternalUserGroupAccessor accessor = accessors.get(externalConfiguration.getSystemId());
        if (accessor != null && accessor.supports(externalConfiguration)) {
            return accessor;
        }
        return null;
    }

    /**
     * @param accessors
     *            List of accessors.
     */
    public void register(Map<String, ExternalUserGroupAccessor> accessors) {
        this.accessors.putAll(accessors);
    }

    /**
     * Register an accesser at this extension point. Replaces the accessor if it already exists.
     * 
     * @param systemId
     *            Id.
     * @param accessor
     *            The accessor.
     */
    public void register(String systemId, ExternalUserGroupAccessor accessor) {
        accessors.put(systemId, accessor);
    }

    /**
     * @param accessors
     *            List of accessors, with the systemId as key and the Class name as value.
     */
    public void setAccessorsByClassName(Map<String, String> accessors) {
        for (Entry<String, String> entry : accessors.entrySet()) {
            try {
                Class<ExternalUserGroupAccessor> accessor = (Class<ExternalUserGroupAccessor>) Class
                        .forName(entry.getValue());
                this.accessors.put(entry.getKey(), accessor.newInstance());
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Adding ExternalUserGroupAccessor " + entry.getValue()
                        + " failed because the class was not found.");
            } catch (InstantiationException e) {
                LOGGER.warn("Adding ExternalUserGroupAccessor " + entry.getValue()
                        + " failed because of an InstantiationException.");
            } catch (IllegalAccessException e) {
                LOGGER.warn("Adding ExternalUserGroupAccessor " + entry.getValue()
                        + " failed because of an IllegalAccessException.");
            }
        }
    }
}
