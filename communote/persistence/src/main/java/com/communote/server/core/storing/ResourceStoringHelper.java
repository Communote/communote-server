package com.communote.server.core.storing;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.config.ClientConfigurationHelper;

/**
 * The Class ResourceStoringHelper contains helper methods for the resource storing management.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class ResourceStoringHelper {

    /**
     * Gets the resource limit.
     *
     * @return the limit
     */
    public static long getCountLimit() {
        return CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.CLIENT_USER_TAGGED_COUNT_LIMIT,
                        ClientConfigurationHelper.DEFAULT_CLIENT_USER_TAGGED_COUNT_LIMIT);
    }

    /**
     * Instantiates a new resource storing helper.
     */
    private ResourceStoringHelper() {

    }
}
