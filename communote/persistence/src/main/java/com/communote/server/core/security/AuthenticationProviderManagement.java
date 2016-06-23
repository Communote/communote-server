package com.communote.server.core.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.communote.common.util.DescendingOrderComparator;

/**
 * Management for handling with {@link CommunoteAuthenticationProvider}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class AuthenticationProviderManagement {

    private List<CommunoteAuthenticationProvider> providers = new ArrayList<CommunoteAuthenticationProvider>();

    private ProviderManager manager;

    /**
     * Adds a provider to the chain.
     * 
     * @param provider
     *            The provider filter to add.
     */
    public synchronized void addAuthenticationProvider(CommunoteAuthenticationProvider provider) {
        List<CommunoteAuthenticationProvider> newProviders = new ArrayList<CommunoteAuthenticationProvider>(
                providers);
        newProviders.add(provider);
        Collections.sort(newProviders, new DescendingOrderComparator());
        manager = null;
        providers = newProviders;
    }

    /**
     * Iterates over the internal list of authentication providers an tries to authenticate.
     * 
     * @param authentication
     *            The authentication.
     * @return The resulting authentication.
     */
    public Authentication authenticate(Authentication authentication) {
        ProviderManager providerManager = getManager();
        if (providerManager != null) {
            try {
                return providerManager.authenticate(authentication);
            } catch (ProviderNotFoundException e) {
                // will be thrown if there is no supporting provider, ignore it since we are not
                // calling supports methods of the registered providers
            }
        }
        return null;
    }

    /**
     * @return the lazily initialized manager or null if no providers are registered
     */
    private ProviderManager getManager() {
        if (manager == null && !providers.isEmpty()) {
            initProviderManager();
        }
        return manager;
    }

    /**
     * 
     * @return an unmodifiable list of all registered providers
     */
    public List<CommunoteAuthenticationProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    /**
     * Initialize the provider manager
     */
    private synchronized void initProviderManager() {
        if (manager == null && !providers.isEmpty()) {
            // TODO why not adding to existing providers? Ordering is not required. Well,
            // thread-safety might be an issue!
            manager = new ProviderManager(new ArrayList<AuthenticationProvider>(providers));
        }
    }

    /**
     * Removes a provider from the chain.
     * 
     * @param provider
     *            The provider to remove.
     */
    public synchronized void removeCommunoteAuthenticationProvider(
            CommunoteAuthenticationProvider provider) {
        List<CommunoteAuthenticationProvider> newProviders = new ArrayList<CommunoteAuthenticationProvider>(
                providers);
        newProviders.remove(provider);
        manager = null;
        providers = newProviders;
    }

}
