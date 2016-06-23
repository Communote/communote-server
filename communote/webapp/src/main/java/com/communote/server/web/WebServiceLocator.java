package com.communote.server.web;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.stereotype.Service;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.AuthenticationProviderManagement;
import com.communote.server.core.security.CommunoteAuthenticationProvider;
import com.communote.server.core.security.authentication.BaseCommunoteAuthenticationProvider;
import com.communote.server.web.commons.controller.StartpageRegistry;
import com.communote.server.web.commons.i18n.JsMessagesRegistry;
import com.communote.server.web.fe.widgets.extension.WidgetExtensionManagementRepository;

/**
 * Service locator for beans of the web context
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class WebServiceLocator implements ApplicationContextAware {

    private static WebServiceLocator INSTANCE;

    /**
     * Get a service that has the given type. If there are more services of that type an exception
     * will be thrown.
     *
     * @param requiredType
     *            the type of the service to return
     * @return the service
     * @param <T>
     *            the type of the service
     */
    public static final <T> T findService(Class<T> serviceClass) {
        return instance().getService(serviceClass);
    }

    /**
     * Get a service by name that has the given type. If there is no such service an exception will
     * be thrown.
     *
     * @param name
     *            the name of the service
     * @param requiredType
     *            the type of the service to return
     * @return the service
     * @param <T>
     *            the type of the service
     */
    public static final <T> T findService(String name, Class<T> requiredType) {
        return instance().getService(name, requiredType);
    }

    /**
     * The singleton instance
     *
     * @return the instance
     */
    public static WebServiceLocator instance() {
        if (INSTANCE == null) {
            throw new IllegalStateException(
                    "WebServiceLocator not yet initialized! The web ApplicationContext is probably not prepared");
        }
        return INSTANCE;
    }

    private ApplicationContext webApplicationContext;

    /**
     * No public construction
     */
    private WebServiceLocator() {

    }

    /**
     * @param identifier
     *            Identifier of the provider to use. if <code>null</code> the next possible provider
     *            will be used.
     * @return Get the provider, which is able to handle an invitation.
     */
    public BaseCommunoteAuthenticationProvider getInvitationProvider(String identifier) {
        ProviderManager authenticationManager = getProviderManager();

        // all providers to iterate for
        List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>(
                authenticationManager.getProviders());
        // also add the plugin providers
        // TODO far from perfect, it would be better to have them all in single list, but this means
        // moving the authentication provider stuff into the core
        List<CommunoteAuthenticationProvider> pluginProviders = ServiceLocator.instance()
                .getService(AuthenticationProviderManagement.class).getProviders();
        providers.addAll(pluginProviders);

        for (Object object : providers) {
            if (!(object instanceof BaseCommunoteAuthenticationProvider)) {
                continue;
            }
            BaseCommunoteAuthenticationProvider provider = (BaseCommunoteAuthenticationProvider) object;
            if (provider.supportsUserQuerying()
                    && (identifier == null || provider.getIdentifier().equals(identifier))) {
                return provider;
            }
        }
        throw new IllegalStateException("There is no provider that allows an invitation!");

    }

    /**
     * @return the configured JsMessagesRegistry
     */
    public JsMessagesRegistry getJsMessagesRegistry() {
        JsMessagesRegistry registry = (JsMessagesRegistry) webApplicationContext
                .getBean("jsMessagesRegistry");
        return registry;
    }

    /**
     * @return the configured provider manager
     */
    public ProviderManager getProviderManager() {
        ProviderManager authenticationManager = (ProviderManager) webApplicationContext
                .getBean("authenticationManager");
        return authenticationManager;
    }

    /**
     * Get a service that has the given type. If there are more services with that type an exception
     * will be thrown.
     *
     * @param requiredType
     *            the type of the service to return
     * @return the service
     * @param <T>
     *            the type of the service
     */
    public <T> T getService(Class<T> requiredType) {
        return webApplicationContext.getBean(requiredType);
    }

    /**
     * Get a service by name that has the given type. If there is no such service an exception will
     * be thrown.
     *
     * @param name
     *            the name of the service
     * @param requiredType
     *            the type of the service to return
     * @return the service
     * @param <T>
     *            the type of the service
     */
    public <T> T getService(String name, Class<T> requiredType) {
        return webApplicationContext.getBean(name, requiredType);
    }

    /**
     * @return the configured startpage resolver
     */
    public StartpageRegistry getStartpageRegistry() {
        return (StartpageRegistry) webApplicationContext.getBean("startpageRegistry");
    }

    /**
     * @return the web application context
     */
    public ApplicationContext getWebApplicationContext() {
        return webApplicationContext;
    }

    /**
     * @return the repository for widget extension managements
     */
    public WidgetExtensionManagementRepository getWidgetExtensionManagementRepository() {
        return webApplicationContext.getBean(WidgetExtensionManagementRepository.class);
    }

    @PostConstruct
    private void init() {
        INSTANCE = this;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext == null) {
            throw new IllegalArgumentException("context cannot be null!");
        }
        webApplicationContext = applicationContext;

    }
}
