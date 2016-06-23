package com.communote.server.core.common.velocity.tools;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Tool for clients.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientTool {
    private ClientConfigurationProperties getClientConfigurationProperties() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties();
    }

    /**
     * Get the name of a client.
     *
     * @param clientId
     *            the ID of the client whose name should be returned. Can be null get the name of
     *            the current client.
     * @return the name of the client or null if the client does not exist
     */
    public String getClientName(String clientId) {
        ClientTO client;
        if (clientId != null) {
            client = ClientHelper.getClient(clientId);
        } else {
            client = ClientHelper.getCurrentClient();
        }
        if (client != null) {
            return client.getName();
        }
        return null;
    }

    /**
     * @param property
     *            The property.
     * @return The property or null if not set.
     */
    public String getProperty(String property) {
        return getClientConfigurationProperties().getProperty(ClientProperty.valueOf(property));
    }

    /**
     * @param property
     *            The property.
     * @param fallback
     *            The fallback.
     * @return The property as boolean, with the given fallback.
     */
    public boolean getProperty(String property, Boolean fallback) {
        return getClientConfigurationProperties().getProperty(ClientProperty.valueOf(property),
                fallback);
    }

    /**
     * @param property
     *            The property.
     * @param fallback
     *            The fallback.
     * @return The property as string, with the given fallback.
     */
    public String getProperty(String property, String fallback) {
        return getClientConfigurationProperties().getProperty(ClientProperty.valueOf(property),
                fallback);
    }

    /**
     * @return The current server id.
     */
    public String getServerId() {
        return CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties()
                .getServerId();
    }

    /**
     * @return True, if the current client is the global client.
     */
    public boolean isGlobalClient() {
        return ClientHelper.isCurrentClientGlobal();
    }
}
