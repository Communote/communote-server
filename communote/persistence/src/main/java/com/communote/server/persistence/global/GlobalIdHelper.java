package com.communote.server.persistence.global;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;

/**
 * Helper class for the global id
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public final class GlobalIdHelper {

    /**
     * Build the global id String for the given type and id
     *
     * @param type
     *            the type of the id
     * @param id
     *            the id itself
     * @return the string, e.g. /serverId/blog/12
     */
    public static String buildGlobalIdIString(GlobalIdType type, Long id) {
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        sb.append(getUnqiueIdentifier());
        sb.append("/");
        sb.append(type.getGlobalIdPath());
        sb.append("/");
        sb.append(id);
        return sb.toString();
    }

    /**
     * @return the unique identifier of this system
     */
    private static String getUnqiueIdentifier() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.UNIQUE_CLIENT_IDENTIFER);
    }

    /**
     * Helper class
     */
    private GlobalIdHelper() {
        // Nothing to implement here
    }

}
