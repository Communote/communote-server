package com.communote.server.core.messaging.vo;

import java.util.HashMap;
import java.util.Map;

import com.communote.server.model.messaging.MessagerConnectorType;

/**
 * Transfer object for the
 * {@link com.communote.server.model.messaging.mc.config.MessagerConnectorConfig} entity.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MessagerConnectorConfigTO {
    private final Map<String, String> properties;
    private final MessagerConnectorType type;
    private final int priority;
    private final boolean onlyIfAvailable;

    /**
     * @param type
     *            the type of the messager connector
     * @param priority
     *            the priority of the configuration
     */
    public MessagerConnectorConfigTO(MessagerConnectorType type, int priority) {
        this(type, priority, null, false);
    }

    /**
     * @param type
     *            the type of the messager connector
     * @param priority
     *            the priority of the configuration
     * @param properties
     *            additional properties of the configuration
     * @param onlyIfAvailable
     *            the messager is only active if available
     */
    public MessagerConnectorConfigTO(MessagerConnectorType type, int priority,
            Map<String, String> properties, boolean onlyIfAvailable) {
        if (properties == null) {
            this.properties = new HashMap<String, String>();
        } else {
            this.properties = properties;
        }
        this.type = type;
        this.priority = priority;
        this.onlyIfAvailable = onlyIfAvailable;
    }

    /**
     * @return the priority of the configuration
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * @return additional properties of the configuration
     */
    public Map<String, String> getProperties() {
        return this.properties;
    }

    /**
     * @return the type of the messager connector
     */
    public MessagerConnectorType getType() {
        return this.type;
    }

    /**
     * @return whether the messager is only active if available
     */
    public boolean isOnlyIfAvailable() {
        return this.onlyIfAvailable;
    }

}
