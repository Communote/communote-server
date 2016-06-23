package com.communote.server.core.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.storing.ResourceStoringManagement;

/**
 * Initialize a client so it can be used. The initializer should be called every time on startup
 * after the database was updated or after creation of a new client.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientInitializer.class);

    /**
     * Initialize the client so it can be used.
     * 
     * @param client
     *            the client to initialize
     * @throws ClientInitializationException
     *             in case the initialization failed
     */
    public void initialize(ClientTO client) throws ClientInitializationException {
        SecurityContext securityContext = null;
        try {
            securityContext = AuthenticationHelper.setInternalSystemToSecurityContext();
            new ClientDelegate(client).execute(new ClientDelegateCallback<Object>() {
                @Override
                public Object doOnClient(ClientTO client) throws Exception {
                    ServiceLocator.findService(ResourceStoringManagement.class)
                    .migrateContentTypeEmptyAttachments();
                    ServiceLocator.findService(TaskManagement.class).stopAllTaskExecutions();
                    return null;
                }
            });
        } catch (Exception e) {
            throw new ClientInitializationException("Error initializing client "
                    + client.getClientId(), e);
        } finally {
            AuthenticationHelper.setSecurityContext(securityContext);
        }
        LOGGER.info("Initialized client '{}' successfully",
                (client == null ? "global" : client.getClientId()));
    }
}
