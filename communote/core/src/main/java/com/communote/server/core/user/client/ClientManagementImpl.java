package com.communote.server.core.user.client;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.ApplicationInformation;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.client.InvalidClientIdException;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.common.exceptions.PasswordLengthException;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.AliasAlreadyExistsException;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.client.converters.ClientToClientTOConverter;
import com.communote.server.model.client.Client;
import com.communote.server.model.client.ClientStatus;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.client.ClientDao;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.persistence.user.client.ClientStatisticDao;
import com.communote.server.persistence.user.client.ClientValidator;
import com.communote.server.service.NavigationItemService;

/**
 * @see com.communote.server.core.user.client.ClientManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("clientManagement")
public class ClientManagementImpl extends ClientManagementBase {

    private final ClientToClientTOConverter clientConverter = new ClientToClientTOConverter();

    @Autowired
    private ClientDao clientDao;
    @Autowired
    private UserManagement userManagement;
    @Autowired
    private UserDao kenmeiUserDao;
    @Autowired
    private ClientStatisticDao clientStatisticDao;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private ConfigurationManagement configurationManagement;
    @Autowired
    private BlogManagement blogManagement;

    @Override
    @Transactional(readOnly = true)
    public ClientTO findClient(String clientId) throws ClientNotFoundException {
        if (clientId == null) {
            throw new IllegalArgumentException("ClientId must not be null");
        }
        Client client = clientDao.findByClientId(clientId);
        if (client == null) {
            throw new ClientNotFoundException(clientId);
        }
        return clientConverter.convert(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ClientTO> getAllClients() {
        Collection<Client> clients = clientDao.loadAll();
        return clientConverter.convert(clients);
    }

    @Override
    protected void handleCreateGlobalClient(String clientName, String timeZoneId)
            throws InvalidClientIdException {
        String clientId = ClientHelper.getGlobalClientId();
        Client client = clientDao.findByClientId(clientId);
        if (client == null) {
            ClientValidator.assertValidClientId(clientId);
            client = Client.Factory.newInstance();
            client.setClientId(clientId);
            client.setName(clientName);
            client.setClientStatus(ClientStatus.ACTIVE);
            ApplicationInformation applicationInfo = CommunoteRuntime.getInstance()
                    .getApplicationInformation();
            client.setCreationVersion(applicationInfo.getBuildNumber());
            client.setCreationRevision(applicationInfo.getRevision());
            Timestamp creationTime = new Timestamp(new Date().getTime());
            creationTime.setNanos(0);
            client.setCreationTime(creationTime);

            client = clientDao.create(client);

            // add default config settings
            Map<ClientConfigurationPropertyConstant, String> initialClientConfig = ClientConfigurationHelper
                    .createStandardClientSettings();
            // create and save the unique client ID
            initialClientConfig.put(ClientProperty.UNIQUE_CLIENT_IDENTIFER,
                    ClientHelper.createUniqueClientId());
            try {
                initialClientConfig.put(ClientProperty.CREATION_DATE, EncryptionUtils.encrypt(
                        Long.toString(creationTime.getTime()),
                        ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue()));
            } catch (EncryptionException e) {
                throw new ClientManagementException("Was not able to encrypt a property.", e);
            }
            configurationManagement.updateClientSettings(initialClientConfig);
            clientStatisticDao.initialise();
        } else {
            if (!StringUtils.equals(client.getName(), clientName)) {
                client.setName(clientName);
                clientDao.update(client);
            }
        }
        CommunoteRuntime.getInstance().getConfigurationManager().updateClientTimeZone(timeZoneId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleInitializeGlobalClient(UserVO userVO) throws EmailValidationException {

        try {
            User existingUser = null;
            List<User> managerList = userManagement.findUsersByRole(
                    UserRole.ROLE_KENMEI_CLIENT_MANAGER, UserStatus.ACTIVE);
            if (!managerList.isEmpty()) {
                existingUser = managerList.get(0);
            }
            if (existingUser != null) {
                kenmeiUserDao.userVOToEntity(userVO, existingUser, false);
            } else {
                User admin = userManagement.createUser(userVO, false, false);

                postProcessFirstClientManager(admin);

            }
        } catch (PasswordLengthException e) {
            throw new ClientManagementException("Unexpected exception", e);
        } catch (EmailAlreadyExistsException e) {
            // should not occur when creating the first user
            throw new ClientManagementException("Unexpected exception", e);
        } catch (AliasAlreadyExistsException e) {
            // should not occur when creating the first user
            throw new ClientManagementException("Unexpected exception", e);
        } catch (AuthorizationException e) {
            // should not occur when creating the first user
            throw new ClientManagementException("Unexpected exception", e);
        }

        // create the default topic with the name of the client
        // note: cannot use ClientHelper#getCurrentClient because the application is not yet
        // initialized
        try {
            ClientTO client = findClient(ClientHelper.getGlobalClientId());
            blogManagement.createDefaultBlog(client.getName());
        } catch (NonUniqueBlogIdentifierException e) {
            throw new ClientManagementException("Creation of the default blog failed", e);
        } catch (BlogIdentifierValidationException e) {
            throw new ClientManagementException("Creation of the default blog failed", e);
        } catch (ClientNotFoundException e) {
            throw new ClientManagementException(
                    "Unexpected exception while creating the default topic", e);
        }
    }

    /**
     * Do the things for the first client manager that are not done if the application is fully
     * initialized, e.g. by event listeners
     *
     * @param admin
     * @throws AuthorizationException
     */
    private void postProcessFirstClientManager(User admin) throws AuthorizationException {
        SecurityContext currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            ServiceLocator.findService(NavigationItemService.class).createBuiltInNavigationItems(
                    admin.getId());
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
    }

    @Override
    public ClientTO updateClientName(String clientId, String clientName)
            throws ClientNotFoundException {
        if (clientId == null || clientId.trim().length() == 0) {
            throw new IllegalArgumentException("'clientId' can not be null or empty");
        }
        if (clientName == null || clientName.trim().length() == 0) {
            throw new IllegalArgumentException("'clientName' can not be null or empty");
        }
        Client client = clientDao.findByClientId(clientId);
        if (client == null) {
            throw new ClientNotFoundException(clientId);
        }
        client.setName(clientName);
        return clientConverter.convert(client);
    }

}
