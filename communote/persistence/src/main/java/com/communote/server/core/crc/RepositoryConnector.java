package com.communote.server.core.crc;

import java.io.Serializable;

import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ExtendedContentId;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.persistence.crc.ContentRepositoryException;

/**
 * Connector to a repository which can store attachments
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface RepositoryConnector extends Serializable {

    /**
     * Remove all content from the repository of this connector.
     *
     * @throws ContentRepositoryException
     *             in case deleting the content failed
     */
    public void clearContent() throws ContentRepositoryException;

    /**
     * Delete the content with the given ContentId.
     *
     * @param contentId
     *            The ContentId for the content to delete.
     * @throws ContentRepositoryException
     *             in case deleting the content failed
     */
    public void deleteContent(ContentId contentId) throws ContentRepositoryException;

    /**
     * @return the configuration of the connector
     */
    public RepositoryConnectorConfiguration getConfiguration();

    /**
     * Returns a ContentTO that is associated with the given ContentId.
     *
     * @param contentId
     *            The ContentId for the ContentTO to be read.
     * @return The read ContentTO.
     * @throws ContentRepositoryException
     *             in case reading the content failed
     */
    public AttachmentTO getContent(ExtendedContentId contentId) throws ContentRepositoryException;

    /**
     * @return the current repository size in bytes
     */
    public long getRepositorySize();

    /**
     * @return the repository size limit in bytes. 0 if there is no limit.
     */
    public long getRepositorySizeLimit();

    /**
     * Gets the ratio of the current repository size to the repository size limit. If there is no
     * size limit 0 is returned.
     *
     * @return the ratio of current size to the repository size limit. 0 if there are no limits.
     */
    public float getRepositorySizeLimitRatio();

    /**
     * Gets the ratio of the current repository size plus sizeOfNewDataToStore to the repository
     * size limit. If there is no size limit 0 is returned.
     *
     * @param sizeOfNewDataToStore
     *            size of data in bytes
     * @return the ratio of current size to the repository size limit. 0 if there are no limits.
     */
    public float getRepositorySizeLimitRatio(long sizeOfNewDataToStore);

    /**
     * Callback that is invoked if a connector is removed. Can be used for cleanup operations.
     */
    public void removeConnector() throws ContentRepositoryException;

    /**
     * Store the given ContentTO and return a ContentId for it.
     *
     * @param content
     *            The ContentTO to store.
     * @return A ContentId object containing the ID for the stored ContentTO.
     * @throws ContentRepositoryException
     *             in case storing the content failed
     */
    public ContentId storeContent(AttachmentTO contentTo) throws ContentRepositoryException;

}