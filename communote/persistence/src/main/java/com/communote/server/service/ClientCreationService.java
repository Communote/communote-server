package com.communote.server.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;

import com.communote.common.encryption.EncryptionUtils;
import com.communote.common.io.FileHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.client.GlobalClientDelegateCallback;
import com.communote.server.api.core.client.InvalidClientIdException;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertySecurity;
import com.communote.server.api.core.config.type.ApplicationPropertyVirusScanning;
import com.communote.server.api.core.config.type.CoreProperty;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.client.ClientInitializationException;
import com.communote.server.core.client.ClientInitializer;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.client.ClientManagement;
import com.communote.server.core.user.client.ClientManagementException;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Service for creating and updating a client.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class ClientCreationService {

    private static final String DEFAULT_SUBDIR_NAME_FILE_REPOSITORY = "filerepository";

    @Autowired
    private ClientRetrievalService clientService;

    @Autowired
    private EventDispatcher eventDispatcher;

    /**
     * Create the global client.
     *
     * @param clientName
     *            The name of the global client.
     * @param timeZoneId
     *            The ID of the time zone, used for the global client.
     * @throws InvalidClientIdException
     *             in case of an invalid client id
     */
    public void createGlobalClient(final String clientName, final String timeZoneId)
            throws InvalidClientIdException {
        try {
            new ClientDelegate().execute(new GlobalClientDelegateCallback<Object>() {

                @Override
                public Object doOnGlobalClient() throws InvalidClientIdException {
                    ServiceLocator.findService(ClientManagement.class).createGlobalClient(
                            clientName, timeZoneId);
                    return null;
                }

            });
        } catch (InvalidClientIdException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientManagementException("Unknown error on calling createGlobalClient", e);
        }
        return;
    }

    /**
     * Initialize the global client on startup.
     *
     * @throws ClientInitializationException
     *             in case the initialization failed
     */
    public void initGlobalClient() throws ClientInitializationException {
        ClientTO globalClient;
        try {
            globalClient = clientService.findClient(ClientHelper.getGlobalClientId());
            ClientInitializer initializer = new ClientInitializer();
            initializer.initialize(globalClient);
        } catch (ClientNotFoundException e) {
            throw new ClientInitializationException("Global client does not exist", e);
        }
    }

    /**
     * Initializes the global client
     *
     * @param clientName
     *            Name of the global client.
     * @param adminAccount
     *            the first administrator of the global client
     * @param params
     *            list of application parameters to set
     * @throws Exception
     *             in case of an error
     */
    public void initializeGlobalClient(final UserVO adminAccount,
            final Map<ApplicationProperty, String> params) throws Exception {

        SecurityContext securityContext = null;
        try {
            securityContext = AuthenticationHelper.setInternalSystemToSecurityContext();

            new ClientDelegate().execute(new GlobalClientDelegateCallback<Object>() {
                @Override
                public Object doOnGlobalClient() throws Exception {
                    ServiceLocator.findService(ClientManagement.class).initializeGlobalClient(
                            adminAccount);
                    return null;
                }
            });

            ConfigurationManager confManager = CommunoteRuntime.getInstance()
                    .getConfigurationManager();

            Map<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();

            // add stuff like WEB_SERVER_HOST_NAME, WEB_HTTP_PORT, ...)
            settings.putAll(params);

            // set truststore password
            String uuid = UUID.randomUUID().toString();
            settings.put(ApplicationPropertySecurity.TRUSTED_CA_TRUSTSTORE_PASSWORD, uuid);

            // set keystore password
            uuid = UUID.randomUUID().toString();
            settings.put(ApplicationPropertySecurity.KEYSTORE_PASSWORD, uuid);

            // set default value for attachment upload limit
            settings.put(ApplicationProperty.ATTACHMENT_MAX_UPLOAD_SIZE,
                    String.valueOf(ApplicationProperty.DEFAULT_ATTACHMENT_MAX_UPLOAD_SIZE));

            // set default value for image/logo upload limit
            settings.put(ApplicationProperty.IMAGE_MAX_UPLOAD_SIZE,
                    String.valueOf(ApplicationProperty.DEFAULT_IMAGE_MAX_UPLOAD_SIZE));

            // set default dir for uploaded files
            File dataDir = confManager.getStartupProperties().getDataDirectory();
            File fileDir = new File(dataDir, DEFAULT_SUBDIR_NAME_FILE_REPOSITORY);
            FileHelper.validateDir(fileDir);
            settings.put(ApplicationProperty.FILE_SYSTEM_REPOSITORY_STORAGE_DIR_ROOT,
                    fileDir.getAbsolutePath());

            settings.put(ApplicationPropertyVirusScanning.ENABLED, String.valueOf(false));
            // disable captcha because it's not working correctly
            settings.put(ApplicationProperty.CAPTCHA_DISABLED, String.valueOf(true));

            // set installation date
            settings.put(ApplicationProperty.INSTALLATION_DATE, EncryptionUtils.encrypt(
                    String.valueOf(System.currentTimeMillis()),
                    ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue()));

            confManager.updateApplicationConfigurationProperties(settings);

            // finalization of the installation process
            confManager.updateStartupProperty(CoreProperty.INSTALLATION_DONE, String.valueOf(true));

        } finally {
            AuthenticationHelper.setSecurityContext(securityContext);
        }
    }

    /**
     * Update the name of the current client.
     *
     * @param clientName
     *            the new name to set
     * @throws AuthorizationException
     *             in case the current user is not client manager
     * @throws ClientNotFoundException
     *             in case the current client got removed
     */
    public void updateClientName(final String clientName) throws AuthorizationException,
            ClientNotFoundException {
        if (!SecurityHelper.isClientManager()) {
            throw new AuthorizationException(
                    "Only a client manager is allowed to update the client name");
        }
        final String clientId = ClientHelper.getCurrentClientId();
        try {
            new ClientDelegate().execute(new GlobalClientDelegateCallback<ClientTO>() {
                @Override
                public ClientTO doOnGlobalClient() throws Exception {
                    return ServiceLocator.findService(ClientManagement.class).updateClientName(
                            clientId, clientName);
                }

            });
            clientService.clientChanged(clientId);
            // update thread local
            ClientAndChannelContextHolder.setClient(clientService.findClient(clientId));
        } catch (ClientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception updating name of client " + clientId,
                    e);
        }
    }

}
