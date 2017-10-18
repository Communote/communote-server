package com.communote.server.core.crc;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.io.MaxLengthReachedException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.common.LimitHelper;
import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.crc.vo.ExtendedContentId;
import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.user.NotifyAboutCRCSizeLimitReachedMailMessage;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.crc.ContentRepositoryException;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO this is flawed, especially the stuff with the repository size limit. See TODOs in this
// class, the FilesystemConnector and the
// ResourceStoringManagementImpl.storeInRepository(AttachmentTO)
public class RepositoryConnectorDelegateImpl extends RepositoryConnectorDelegateBase {

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory
            .getLogger(RepositoryConnectorDelegateImpl.class);

    private final Map<String, RepositoryConnector> repositoryConnectors = new HashMap<>();

    private boolean initialized;

    /**
     * Create the default repository connector out of the file system property
     *
     * @return {@link RepositoryConnector}.
     *
     * @throws RepositoryConnectorDelegateException
     *             when the connector creation failed
     * @throws ContentRepositoryException
     *             Exception.
     */
    private RepositoryConnector createDefaultRepositoryConnector()
            throws RepositoryConnectorDelegateException, ContentRepositoryException {
        RepositoryConnectorConfiguration configuration = new RepositoryConnectorConfiguration(
                FilesystemConnector.DEFAULT_FILESYSTEM_CONNECTOR, true);
        try {
            RepositoryConnector connector = new FilesystemConnector(configuration);
            return connector;
        } catch (IOException e) {
            throw new RepositoryConnectorDelegateException("Creating default connector failed", e);
        }

    }

    @Override
    protected synchronized String handleAddRepositoryConnector(RepositoryConnector connector) {
        initIfNecessary();
        this.repositoryConnectors.put(connector.getConfiguration().getConnectorId(), connector);
        return connector.getConfiguration().getConnectorId();
    }

    @Override
    protected void handleAssertRepositorySizeLimitNotReached(RepositoryConnector connector,
            long sizeOfNewDataToStore) throws ResourceSizeLimitReachedException {

        final float ratio = connector.getRepositorySizeLimitRatio(sizeOfNewDataToStore);
        final long limit = connector.getRepositorySizeLimit();
        final long repoSize = connector.getRepositorySize();
        final long sizeWithNewData = sizeOfNewDataToStore + repoSize;

        // TODO the mail sent flags are intended for the file-system repos but this method is called
        // for all repos!
        if (ratio > 1.0f) {
            // limit exceeded
            String size100mail = CommunoteRuntime.getInstance().getConfigurationManager()
                    .getClientConfigurationProperties()
                    .getProperty(ClientProperty.FILE_SYSTEM_REPOSITORY_SIZE_100_MAIL);
            String today = new java.sql.Date(new java.util.Date().getTime()).toString();
            // if the limit will be reached with the new file, notify the client manager
            // that the limit is nearly reached
            if ((!today.equals(size100mail))) {
                // since the file is not stored, pass current repository size
                // TODO this e-mail is not to useful, we should instead send an email stating that a
                // file couldn't be uploaded because the size limit would be exceeded. This mail
                // should contain the size of the file, current ratio and limit.
                sendLimitReachedMail(repoSize, limit);

                // TODO updating this property does not work because caller is throwing a runtime
                // exception which will rollback the transaction including this update. How to
                // solve: 1. update property in new transaction -> dangerous w.r.t. deadlocks on
                // connection pool. 2. local member that stores that mail was sent today -> not
                // restart or cluster safe. 3. just always send mail, but have an option to disable
                // this e-mail which can be set in DB/admin if someone is complaining. 4. caller
                // should not throw an RTE -> problem with cross posts because in this scenario the
                // transaction should be rolled back
                CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .updateClientConfigurationProperty(
                                ClientProperty.FILE_SYSTEM_REPOSITORY_SIZE_100_MAIL,
                        String.valueOf(today.toString()));
            }

            throw new ResourceSizeLimitReachedException(
                    "Content repository size limit would be exceeded with new attachment. Size of upload: "
                            + sizeOfNewDataToStore + " Current size: " + repoSize + " Limit: "
                            + limit);

        } else if (ratio >= 0.9f) {
            boolean size90mail = CommunoteRuntime.getInstance().getConfigurationManager()
                    .getClientConfigurationProperties()
                    .getProperty(ClientProperty.FILE_SYSTEM_REPOSITORY_SIZE_90_MAIL, false);
            if (!size90mail) {
                // if the limit is reached by 90% with the new file, notify the client
                // manager per e-mail (but only once). Since the file can be stored pass the size of
                // the repository including that of the new data.
                sendLimitReachedMail(sizeWithNewData, limit);
                CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .updateClientConfigurationProperty(
                        ClientProperty.FILE_SYSTEM_REPOSITORY_SIZE_90_MAIL,
                        String.valueOf(true));
            }
        }
    }

    @Override
    protected void handleDeleteContent(ContentId contentId) throws ContentRepositoryException {
        RepositoryConnector connector = handleGetRepositoryConnector(contentId.getConnectorId());
        connector.deleteContent(contentId);
        ConfigurationManager configurationManager = CommunoteRuntime.getInstance()
                .getConfigurationManager();
        // TODO the mail sent flags are intended for the file-system repos but this method is called
        // for all repos!
        boolean size90mail = configurationManager.getClientConfigurationProperties().getProperty(
                ClientProperty.FILE_SYSTEM_REPOSITORY_SIZE_90_MAIL, false);
        if (size90mail && connector.getRepositorySizeLimitRatio() < 0.9f) {
            configurationManager.updateClientConfigurationProperty(
                    ClientProperty.FILE_SYSTEM_REPOSITORY_SIZE_90_MAIL, String.valueOf(false));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AttachmentTO handleGetContent(ContentId contentId) throws ContentRepositoryException {
        if (contentId instanceof ExtendedContentId) {
            return this.handleGetContent((ExtendedContentId) contentId);
        }
        return this.handleGetContent(new ExtendedContentId(contentId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AttachmentTO handleGetContent(ExtendedContentId contentId)
            throws ContentRepositoryException {
        // forward the request to the target connector if the content
        // is not cached
        try {
            AttachmentTO contentTo = getRepositoryConnector(contentId.getConnectorId()).getContent(
                    contentId);
            return contentTo;
        } catch (ContentRepositoryException e) {
            LOGGER.error("Error getting content for " + contentId + " " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error getting content for " + contentId + " " + e.getMessage(), e);
            throw new ContentRepositoryException("Error getting content: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RepositoryConnector handleGetDefaultRepositoryConnector()
            throws RepositoryConnectorNotFoundException {
        RepositoryConnector repositoryConnector = handleGetRepositoryConnector(FilesystemConnector.DEFAULT_FILESYSTEM_CONNECTOR);
        return repositoryConnector;
    }

    /**
     * {@inheritDoc}
     *
     * @throws RepositoryConnectorNotFoundException
     */
    @Override
    protected ContentMetadata handleGetMetadata(ContentId contentId)
            throws RepositoryConnectorNotFoundException {
        return internalGetRepositoryConnector(contentId.getConnectorId(),
                MetadataRepositoryConnector.class).getMetadata(contentId);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected RepositoryConnector handleGetRepositoryConnector(String connectorId)
            throws RepositoryConnectorNotFoundException {
        return internalGetRepositoryConnector(connectorId, RepositoryConnector.class);
    }

    @Override
    protected Collection<RepositoryConnector> handleGetRepositoryConnectors() {
        return Collections.unmodifiableCollection(new HashSet<>(repositoryConnectors.values()));
    }

    @Override
    protected synchronized void handleRemoveRepositoryConnector(RepositoryConnector connector) {
        this.handleRemoveRepositoryConnector(connector.getConfiguration().getConnectorId());
    }

    @Override
    protected synchronized void handleRemoveRepositoryConnector(String connectorId) {
        initIfNecessary();
        // TODO call remove callback on connector?
        // TODO should it be possible to remove the default connector?
        this.repositoryConnectors.remove(connectorId);
    }

    @Override
    protected ContentId handleStoreContent(AttachmentTO contentTo)
            throws ContentRepositoryException, ResourceSizeLimitReachedException {

        ContentMetadata contentMetadata = contentTo.getMetadata();
        if (contentMetadata == null) {
            throw new IllegalArgumentException("ContentMetaData cannot be null!");
        }
        if (contentMetadata.getContentId() == null) {
            throw new IllegalArgumentException("ContentId cannot be null!");
        }
        if (contentMetadata.getContentId().getConnectorId() == null) {
            throw new IllegalArgumentException("ContentId.connectorId cannot be null!");
        }

        final RepositoryConnector connector = handleGetRepositoryConnector(contentMetadata
                .getContentId().getConnectorId());

        // TODO assumes that the length is set which might not be the case for streams. In that case
        // the size is only known after storing.
        assertRepositorySizeLimitNotReached(connector, contentTo.getContentLength());

        // otherwise store it directly in the target connector
        ContentId contentId;
        try {
            contentId = connector.storeContent(contentTo);
        } catch (ContentRepositoryException e) {
            if (e.getCause() instanceof MaxLengthReachedException) {
                // TODO handles the case mentioned above but does not send e-mails
                throw new ResourceSizeLimitReachedException(
                        "Content repository size limit reached.", e.getCause());
            }
            throw e;
        }

        return contentId;
    }

    private synchronized void init() {
        if (!initialized) {

            RepositoryConnector defaultConnecor = loadDefaultConnector();
            this.repositoryConnectors.put(defaultConnecor.getConfiguration().getConnectorId(),
                    defaultConnecor);
            initialized = true;
        }
    }

    public void initIfNecessary() {
        if (!initialized) {
            init();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends RepositoryConnector> T internalGetRepositoryConnector(String connectorId,
            Class<T> targetClazz) throws RepositoryConnectorNotFoundException {
        initIfNecessary();
        RepositoryConnector repoConnector = this.repositoryConnectors.get(connectorId);
        if (repoConnector == null) {
            throw new RepositoryConnectorNotFoundException(connectorId,
                    "No repository connector found for connectorId=" + connectorId);
        }
        if (targetClazz != null && !targetClazz.isInstance(repoConnector)) {
            throw new RepositoryConnectorNotFoundException(connectorId, targetClazz,
                    "Repository connector found but not of target clazz= " + targetClazz.getName()
                    + " for connectorId=" + connectorId);
        }

        return ((T) repoConnector);
    }

    private RepositoryConnector loadDefaultConnector() {
        RepositoryConnector connector = null;
        // create default connector, if no other is available, but default
        // configuration exists
        try {
            connector = createDefaultRepositoryConnector();
        } catch (RepositoryConnectorDelegateException | ContentRepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return connector;
    }

    /**
     * Send "Limit reached" email to all client manager, where in the email the current size and
     * limit size is included.
     *
     * @param size
     *            the size
     * @param limit
     *            the limit
     */
    private void sendLimitReachedMail(long size, long limit) {
        List<User> clientManager = ServiceLocator.instance().getService(UserManagement.class)
                .findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER, UserStatus.ACTIVE);
        Map<Locale, Collection<User>> localizedUsers = UserManagementHelper
                .getUserByLocale(clientManager);
        for (Locale locale : localizedUsers.keySet()) {
            NotifyAboutCRCSizeLimitReachedMailMessage message = new NotifyAboutCRCSizeLimitReachedMailMessage(
                    localizedUsers.get(locale), locale, ClientHelper.getCurrentClientId(),
                    ContentRepositoryManagementHelper.getSizeAsString(size),
                    LimitHelper.getCountPercentAsString(size, limit),
                    ContentRepositoryManagementHelper.getSizeLimitAsString(limit));
            ServiceLocator.findService(MailSender.class).send(message);
        }
    }

}
