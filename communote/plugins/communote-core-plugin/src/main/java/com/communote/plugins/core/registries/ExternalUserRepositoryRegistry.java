package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.external.ExternalUserRepository;
import com.communote.server.service.UserService;

/**
 * Registry for ExternalUserRepositoryRegistry
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate
public class ExternalUserRepositoryRegistry {

    private final UserService userService = ServiceLocator.instance().getService(UserService.class);

    /**
     * @param externalUserRepository
     *            Adds the externalUserRepository to the {@link UserService}
     */
    @Bind(id = "registerExternalUserRepository", optional = true, aggregate = true)
    public void registerExternalUserRepository(ExternalUserRepository externalUserRepository) {
        userService.registerRepository(externalUserRepository.getExternalSystemId(),
                externalUserRepository);
    }

    /**
     * Removes the given ExternalUserRepository from the known User Service
     * 
     * @param externalUserRepository
     *            Removes the externalUserRepository in the {@link UserService}
     */
    @Unbind(id = "registerExternalUserRepository", optional = true, aggregate = true)
    public void removeExternalUserRepository(ExternalUserRepository externalUserRepository) {
        userService.unregisterRepository(externalUserRepository.getExternalSystemId());
    }
}
