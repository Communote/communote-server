package com.communote.server.core.crc;

import java.util.Collection;

import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.crc.vo.ExtendedContentId;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.persistence.crc.ContentRepositoryException;
import com.communote.server.persistence.user.client.ClientStatisticDao;

/**
 * <p>
 * Spring Service base class for
 * <code>com.communote.server.service.crc.RepositoryConnectorDelegate</code>, provides access to all
 * services and entities referenced by this service.
 * </p>
 *
 * @see com.communote.server.core.crc.RepositoryConnectorDelegate
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class RepositoryConnectorDelegateBase implements RepositoryConnectorDelegate {

    private ClientStatisticDao clientStatisticDao;

    /**
     *
     */
    @Override
    public String addRepositoryConnector(RepositoryConnector connector) {
        if (connector == null) {
            throw new IllegalArgumentException("connector can not be null or empty");
        }
        if (connector.getConfiguration() == null) {
            throw new IllegalArgumentException("connectorconfiguration can not be null or empty");
        }
        if (connector.getConfiguration().getConnectorId() == null) {
            throw new IllegalArgumentException(
                    "connector.configuration.connectorId can not be null or empty");
        }
        try {
            return this.handleAddRepositoryConnector(connector);
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing addRepositoryConnector --> " + rt, rt);
        }
    }

    @Override
    public void assertRepositorySizeLimitNotReached(RepositoryConnector connector,
            long sizeOfNewDataToStore) throws ResourceSizeLimitReachedException {
        if (connector == null) {
            throw new IllegalArgumentException("connector can not be null or empty");
        }
        try {
            this.handleAssertRepositorySizeLimitNotReached(connector, sizeOfNewDataToStore);
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing assertRepositorySizeLimitNotReached --> " + rt, rt);
        }
    }

    /**
     * @see com.communote.server.core.crc.RepositoryConnectorDelegate#deleteContent(com.communote.server.core.crc.vo.ContentId)
     */
    @Override
    public void deleteContent(ContentId contentId) throws ContentRepositoryException {
        if (contentId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.deleteContent(ContentId contentId) - 'contentId' can not be null");
        }
        if (contentId.getContentId() == null || contentId.getContentId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.deleteContent(ContentId contentId) - 'contentId.contentId' can not be null or empty");
        }
        if (contentId.getConnectorId() == null || contentId.getConnectorId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.deleteContent(ContentId contentId) - 'contentId.connectorId' can not be null or empty");
        }
        try {
            this.handleDeleteContent(contentId);
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing 'com.communote.server.service.crc.RepositoryConnectorDelegate.deleteContent(ContentId contentId)' --> "
                            + rt,
                            rt);
        }
    }

    /**
     * Gets the reference to <code>clientStatistic</code>'s DAO.
     */
    protected ClientStatisticDao getClientStatisticDao() {
        return this.clientStatisticDao;
    }

    /**
     * @see com.communote.server.core.crc.RepositoryConnectorDelegate#getContent(com.communote.server.core.crc.vo.ContentId)
     */
    @Override
    public AttachmentTO getContent(ContentId contentId) throws ContentRepositoryException {
        if (contentId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.getContent(ContentId contentId) - 'contentId' can not be null");
        }
        if (contentId.getContentId() == null || contentId.getContentId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.getContent(ContentId contentId) - 'contentId.contentId' can not be null or empty");
        }
        if (contentId.getConnectorId() == null || contentId.getConnectorId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.getContent(ContentId contentId) - 'contentId.connectorId' can not be null or empty");
        }
        try {
            return this.handleGetContent(contentId);
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing 'com.communote.server.service.crc.RepositoryConnectorDelegate.getContent(ContentId contentId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.core.crc.RepositoryConnectorDelegate#getContent(ExtendedContentId)
     */
    @Override
    public AttachmentTO getContent(ExtendedContentId contentId) throws ContentRepositoryException {
        if (contentId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.getContent(ContentId contentId) - 'contentId' can not be null");
        }
        if (contentId.getContentId() == null || contentId.getContentId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.getContent(ContentId contentId) - 'contentId.contentId' can not be null or empty");
        }
        if (contentId.getConnectorId() == null || contentId.getConnectorId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.getContent(ContentId contentId) - 'contentId.connectorId' can not be null or empty");
        }
        try {
            return this.handleGetContent(contentId);
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing 'com.communote.server.service.crc.RepositoryConnectorDelegate.getContent(ContentId contentId)' --> "
                            + rt, rt);
        }
    }

    @Override
    public RepositoryConnector getDefaultRepositoryConnector()
            throws RepositoryConnectorNotFoundException {
        try {
            return this.handleGetDefaultRepositoryConnector();
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing 'com.communote.server.service.crc.RepositoryConnectorDelegate.getDefaultRepositoryConnectorConfigurations()' --> "
                            + rt,
                            rt);
        }
    }

    /**
     * @see com.communote.server.core.crc.RepositoryConnectorDelegate#getMetadata(com.communote.server.core.crc.vo.ContentId)
     */
    @Override
    public ContentMetadata getMetadata(ContentId contentId)
            throws RepositoryConnectorNotFoundException {
        if (contentId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.getMetadata(ContentId contentId) - 'contentId' can not be null");
        }
        if (contentId.getContentId() == null || contentId.getContentId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.getMetadata(ContentId contentId) - 'contentId.contentId' can not be null or empty");
        }
        if (contentId.getConnectorId() == null || contentId.getConnectorId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.getMetadata(ContentId contentId) - 'contentId.connectorId' can not be null or empty");
        }
        try {
            return this.handleGetMetadata(contentId);
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing 'com.communote.server.service.crc.RepositoryConnectorDelegate.getMetadata(ContentId contentId)' --> "
                            + rt,
                            rt);
        }
    }

    /**
     * @see com.communote.server.core.crc.RepositoryConnectorDelegate#getRepositoryConnector(String)
     */
    @Override
    public RepositoryConnector getRepositoryConnector(
            String connectorId) throws RepositoryConnectorNotFoundException {
        if (connectorId == null || connectorId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.getRepositoryConnector(String connectorId) - 'connectorId' can not be null or empty");
        }
        try {
            return this.handleGetRepositoryConnector(connectorId);
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing 'com.communote.server.service.crc.RepositoryConnectorDelegate.getRepositoryConnector(String connectorId)' --> "
                            + rt,
                            rt);
        }
    }

    /**
     * @see com.communote.server.core.crc.RepositoryConnectorDelegate#getRepositoryConnectors()
     */
    @Override
    public Collection<RepositoryConnector> getRepositoryConnectors()
            throws RepositoryConnectorNotFoundException {
        try {
            return this.handleGetRepositoryConnectors();
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing 'com.communote.server.service.crc.RepositoryConnectorDelegate.getRepositoryConnectors()' --> "
                            + rt,
                            rt);
        }
    }

    protected abstract String handleAddRepositoryConnector(RepositoryConnector connector);

    protected abstract void handleAssertRepositorySizeLimitNotReached(
            RepositoryConnector connector,
            long sizeOfNewDataToStore) throws ResourceSizeLimitReachedException;

    /**
     * Performs the core logic for
     * {@link #deleteContent(com.communote.server.core.crc.vo.ContentId)}
     */
    protected abstract void handleDeleteContent(ContentId contentId)
            throws ContentRepositoryException;

    /**
     * Performs the core logic for {@link #getContent(ContentId)}
     */
    protected abstract AttachmentTO handleGetContent(ContentId contentId)
            throws ContentRepositoryException;

    /**
     * Performs the core logic for {@link #getContent(ExtendedContentId)}
     */
    protected abstract AttachmentTO handleGetContent(ExtendedContentId contentId)
            throws ContentRepositoryException;

    /**
     * Performs the core logic for {@link #getDefaultRepository()}
     */
    protected abstract RepositoryConnector handleGetDefaultRepositoryConnector()
            throws RepositoryConnectorNotFoundException;

    /**
     * Performs the core logic for {@link #getMetadata(com.communote.server.core.crc.vo.ContentId)}
     */
    protected abstract ContentMetadata handleGetMetadata(ContentId contentId)
            throws RepositoryConnectorNotFoundException;

    /**
     * Performs the core logic for {@link #getRepositoryConnector(String)}
     *
     * @throws RepositoryConnectorNotFoundException
     */
    protected abstract RepositoryConnector handleGetRepositoryConnector(String connectorId)
            throws RepositoryConnectorNotFoundException;

    protected abstract Collection<RepositoryConnector> handleGetRepositoryConnectors();

    protected abstract void handleRemoveRepositoryConnector(RepositoryConnector connector);

    protected abstract void handleRemoveRepositoryConnector(String connectorId);

    /**
     * Performs the core logic for
     * {@link #storeContent(com.communote.server.core.vo.content.AttachmentTO)}
     */
    protected abstract ContentId handleStoreContent(AttachmentTO contentTo)
            throws ContentRepositoryException, ResourceSizeLimitReachedException;

    /**
     *
     */
    @Override
    public void removeRepositoryConnector(RepositoryConnector connector) {
        if (connector == null) {
            throw new IllegalArgumentException("connector can not be null or empty");
        }
        if (connector.getConfiguration() == null) {
            throw new IllegalArgumentException("connectorConfiguration can not be null or empty");
        }
        if (connector.getConfiguration().getConnectorId() == null) {
            throw new IllegalArgumentException(
                    "connector.configuration.connectorId can not be null or empty");
        }
        try {
            this.handleRemoveRepositoryConnector(connector);
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing removeRepositoryConnector --> " + rt, rt);
        }
    }

    /**
     *
     */
    @Override
    public void removeRepositoryConnector(String connectorId) {
        if (connectorId == null) {
            throw new IllegalArgumentException("connectorId can not be null or empty");
        }
        try {
            this.handleRemoveRepositoryConnector(connectorId);
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing removeRepositoryConnector --> " + rt, rt);
        }
    }

    /**
     * Sets the reference to <code>clientStatistic</code>'s DAO.
     */
    public void setClientStatisticDao(ClientStatisticDao clientStatisticDao) {
        this.clientStatisticDao = clientStatisticDao;
    }

    /**
     * @see com.communote.server.core.crc.RepositoryConnectorDelegate#storeContent(com.communote.server.core.vo.content.AttachmentTO)
     */
    @Override
    public ContentId storeContent(
            com.communote.server.core.vo.content.AttachmentTO contentTo) throws
            ContentRepositoryException,
            ResourceSizeLimitReachedException {
        if (contentTo == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.crc.RepositoryConnectorDelegate.storeContent(AttachmentTO contentTo) - 'contentTo' can not be null");
        }
        try {
            return this.handleStoreContent(contentTo);
        } catch (RuntimeException rt) {
            throw new RepositoryConnectorDelegateException(
                    "Error performing 'com.communote.server.service.crc.RepositoryConnectorDelegate.storeContent(AttachmentTO contentTo)' --> "
                            + rt,
                            rt);
        }
    }
}