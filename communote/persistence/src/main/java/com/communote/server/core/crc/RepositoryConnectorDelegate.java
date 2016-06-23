package com.communote.server.core.crc;

import java.util.Collection;

import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.crc.vo.ExtendedContentId;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.persistence.crc.ContentRepositoryException;

/**
 * <p>
 * This is the main class of the Content Repository Connector. It provides all necessary functions
 * needed for the storing and managing of contents and managing the RepositoryConnectors.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface RepositoryConnectorDelegate {

    /**
     * Adds the connector, if a connector with the same connector id already exists it will be
     * replaced
     * 
     * @param connector
     *            the connector
     * @return the connector id
     */
    public String addRepositoryConnector(RepositoryConnector connector);

    /**
     * Check the size of the content repository if the new data can be added, e.g. adding the new
     * data size will exceed the size limit. Send email if this limit is reached or 90% is reached.
     * 
     * If the new size is to large a {@link ResourceSizeLimitReachedException} is thrown
     * 
     * @param connector
     * 
     * @param sizeOfNewDataToStore
     *            size of new data file
     * @return the file with the transmitted size can be added, otherwise false
     * @throws ResourceSizeLimitReachedException
     */
    public void assertRepositorySizeLimitNotReached(RepositoryConnector connector,
            long sizeOfNewDataToStore) throws ResourceSizeLimitReachedException;

    /**
     * <p>
     * Deletes the content with the given ContentId.
     * </p>
     */
    public void deleteContent(ContentId contentId) throws ContentRepositoryException;

    /**
     * <p>
     * Returns a ContentTO that is associated to the given ContentId.
     * </p>
     */
    public AttachmentTO getContent(ContentId contentId) throws ContentRepositoryException;

    /**
     * <p>
     * Returns a ContentTO that is associated to the given ContentId.
     * </p>
     */
    public AttachmentTO getContent(ExtendedContentId contentId) throws ContentRepositoryException;

    /**
     * <p>
     * Returns the default repository
     * </p>
     * <p>
     * 
     * @return the default repo
     *         </p>
     */
    public RepositoryConnector getDefaultRepositoryConnector()
            throws RepositoryConnectorNotFoundException;

    /**
     * <p>
     * Returns the ContentMetadata that is associated to the given ContentId.
     * </p>
     */
    public ContentMetadata getMetadata(ContentId contentId)
            throws RepositoryConnectorNotFoundException;

    /**
     * <p>
     * Returns the RepositoryConnector that belongs to the given connectorId.
     * </p>
     * 
     * @throws RepositoryConnectorNotFoundException
     */
    public RepositoryConnector getRepositoryConnector(String connectorId)
            throws RepositoryConnectorNotFoundException;

    public Collection<RepositoryConnector> getRepositoryConnectors()
            throws RepositoryConnectorNotFoundException;

    public void removeRepositoryConnector(RepositoryConnector connector);

    public void removeRepositoryConnector(String connectorId);

    /**
     * <p>
     * Stores the given ContentTO and returns a ContentId for it.
     * </p>
     */
    public ContentId storeContent(AttachmentTO contentTo) throws ContentRepositoryException,
            ResourceSizeLimitReachedException;

}
