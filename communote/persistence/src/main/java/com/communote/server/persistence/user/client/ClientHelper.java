package com.communote.server.persistence.user.client;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.service.ClientRetrievalService;

/**
 * The Class ClientHelper contains helper methods for the client id management.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class ClientHelper {

    /** The Constant DEFAULT_GLOBAL_CLIENT_ID defines the default id for the global client. */
    public final static String DEFAULT_GLOBAL_CLIENT_ID = "global";

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientHelper.class);

    private static String GLOBAL_CLIENT_ID;

    /**
     * @throws AuthorizationException
     *             Thrown, when the current client is not the global client.
     */
    public static void assertIsCurrentClientGlobal() throws AuthorizationException {
        if (!isCurrentClientGlobal()) {
            throw new AuthorizationException("The current client is not the global client.");
        }
    }

    /**
     * Creates a (worldwide) unique ID to be used as unique identifier for a client.
     *
     * @return the unique client ID
     */
    public static String createUniqueClientId() {
        return UUID.randomUUID().toString();
    }

    private static ApplicationConfigurationProperties getAppConfigurationProperties() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties();
    }

    /**
     * Get the details of a client that has the given ID.
     *
     * @param clientId
     *            the ID of the client
     * @return the client details or null if the application is not initialized, the clientId was
     *         null or the client does not exist
     */
    public static ClientTO getClient(String clientId) {
        if (clientId != null && CommunoteRuntime.getInstance().isCoreInitialized()) {
            try {
                return ServiceLocator.findService(ClientRetrievalService.class)
                        .findClient(clientId);
            } catch (ClientNotFoundException e) {
                LOGGER.debug("The client with ID {} does not exist", clientId);
            }
        }
        return null;
    }

    private static ClientConfigurationProperties getClientConfigurationProperties() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties();
    }

    /**
     * @return {@link Timestamp} of the creation date of the current client.
     */
    public static Timestamp getCreationDate() {
        String encryptedCreationDate = getClientConfigurationProperties().getProperty(
                ClientProperty.CREATION_DATE);
        String creationDate = null;
        if (encryptedCreationDate != null) {
            try {
                creationDate = EncryptionUtils.decrypt(encryptedCreationDate,
                        ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
            } catch (EncryptionException e) {
                LOGGER.error("There was an error decrypting a property: {}",
                        ClientProperty.CREATION_DATE);
            }
        }
        return new Timestamp(NumberUtils.toLong(creationDate, 0));
    }

    /**
     * Get the client that is associated with the current thread. If there is no client bound to the
     * thread and the application is initialized, that is {@link CommunoteRuntime#isInitialized()}
     * returns true, the global client will be returned. Otherwise null is returned.
     *
     * @return the current client, or null if no client is bound to this thread and the application
     *         is not initialized
     */
    public static ClientTO getCurrentClient() {
        ClientTO client = ClientAndChannelContextHolder.getClient();
        // fallback to global client as soon as the application is initialized
        // TODO should we check if the installation is done (StartupPropertiesImpl) because the
        // global client should theoretically be available than?
        if (client == null && CommunoteRuntime.getInstance().isCoreInitialized()) {
            try {
                return ServiceLocator.findService(ClientRetrievalService.class).findClient(
                        getGlobalClientId());
            } catch (ClientNotFoundException e) { // Should never happen
                LOGGER.error("The global client does not exist: " + e.getMessage());
            }
        }
        return client;
    }

    /**
     * Get the ID of the client which is associated with the current thread. If no client is
     * associated with the current thread the ID of the global client is returned. The latter will
     * lead to an exception if called before the start of the Communote runtime.
     *
     *
     * @return the ID of the current client
     */
    // TODO shouldn't we return null as long as the application is not initialized so that this
    // method behaves like getCurrentClient?
    public static String getCurrentClientId() {

        String clientId;
        ClientTO client = ClientAndChannelContextHolder.getClient();

        // if its null use the global client id
        if (client == null) {
            clientId = getGlobalClientId();
        } else {
            clientId = client.getClientId();
        }

        return clientId;
    }

    /**
     * Return the default language defined in the client configuration properties
     *
     * @return default locale
     */
    public static Locale getDefaultLanguage() {
        return getClientConfigurationProperties().getDefaultLanguage();
    }

    /**
     * Returns the file system directory path of the file repository of the current client.
     *
     * @return the file repository directory
     */
    public static String getFileRepositoryDirectory() {
        return getFileRepositoryDirectory(getCurrentClient());
    }

    /**
     * Returns the file system directory path of the file repository of a client.
     *
     * @param client
     *            the client for which the path is to be returned
     * @return the file repository directory
     */
    public static String getFileRepositoryDirectory(ClientTO client) {
        return getFileRepositoryDirectory(client.getClientId());
    }

    /**
     * Returns the file system directory path of the file repository of a client.
     *
     * @param clientId
     *            the client for which the path is to be returned
     * @return the file repository directory
     */
    public static String getFileRepositoryDirectory(String clientId) {
        String base = getAppConfigurationProperties().getProperty(
                ApplicationProperty.FILE_SYSTEM_REPOSITORY_STORAGE_DIR_ROOT);
        return base + "/" + clientId;
    }

    /**
     * Return the ID of the global client. An exception will be thrown if called before the start of
     * the Communote runtime.
     *
     * @return the ID of the global client
     */
    public static String getGlobalClientId() {
        if (GLOBAL_CLIENT_ID == null) {
            synchronized (ClientHelper.class) {
                GLOBAL_CLIENT_ID = getGlobalClientIdFromConfig(getAppConfigurationProperties());
            }
        }
        return GLOBAL_CLIENT_ID;
    }

    /**
     * Get the global id from the configuration or if not defined correctly use the
     * {@link #DEFAULT_GLOBAL_CLIENT_ID}
     *
     * @param properties
     *            the properties to use
     * @return the global id from the config, or default
     */
    private static String getGlobalClientIdFromConfig(ApplicationConfigurationProperties properties) {
        String globalId = null;
        String globalIdFromConfig = properties.getProperty(ApplicationProperty.GLOBAL_CLIENT_ALIAS);
        if (StringUtils.isBlank(globalIdFromConfig)) {
            LOGGER.debug("Found no client ID for global client in configuration. "
                    + "Falling back to default global client ID.");
        } else {
            globalIdFromConfig = globalIdFromConfig.toLowerCase(Locale.ENGLISH).trim();
            if (!ClientValidator.validateClientIdLength(globalIdFromConfig)) {
                LOGGER.error("The predefined client ID '{}'"
                        + " does not fulfill the length requirement. "
                        + "Falling back to default client ID.", globalIdFromConfig);
            } else if (!ClientValidator.validateClientId(globalIdFromConfig)) {
                LOGGER.error("The predefined client ID '{}'"
                        + "' does not fulfill the formatting requirements. "
                        + "Falling back to default client ID.", globalIdFromConfig);
            } else {
                globalId = globalIdFromConfig;
            }
        }
        if (globalId == null) {
            globalId = DEFAULT_GLOBAL_CLIENT_ID;
        }
        return globalId;
    }

    /**
     * Checks if the given client is the global client.
     *
     * @param client
     *            the client. Can be null.
     * @return true if the client is the global client or the argument was null
     */
    public static boolean isClientGlobal(ClientTO client) {
        return client == null || client.getClientId().equals(getGlobalClientId());
    }

    /**
     * Checks if the given ID is the ID of the global client
     *
     * @param clientId
     *            the ID of the client to test. Can be null.
     * @return true if the ID belongs to the global client or the argument was null
     */
    public static boolean isClientGlobal(String clientId) {
        return clientId == null || clientId.equals(getGlobalClientId());
    }

    /**
     * Checks if the current client is global.
     *
     * @return true, if the current client is global
     */
    public static boolean isCurrentClientGlobal() {
        return isClientGlobal(getCurrentClient());
    }

    /**
     * Instantiates a new client helper.
     */
    private ClientHelper() {
        // Do nothing.
    }
}
