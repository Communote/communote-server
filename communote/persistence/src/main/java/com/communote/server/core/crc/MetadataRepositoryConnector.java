package com.communote.server.core.crc;

import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.persistence.crc.ContentRepositoryException;

/**
 * A repository connector that supports storing additional metadata
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface MetadataRepositoryConnector extends RepositoryConnector {

    /**
     * Return the ContentMetadata that is associated with the given ContentId.
     *
     * @param contentId
     *            The ContentId for the ContentTO to be read.
     * @return The read ContentMetadata.
     * @throws ContentRepositoryException
     *             in case reading the metadata failed
     */
    public ContentMetadata getMetadata(ContentId contentId);

    /**
     * Update the metadata of existing content with the provided metadata. The ContentId of the
     * content to update is contained in the according metadata field.
     *
     * @param metadata
     *            The ContentMetadata to update.
     * @throws ContentRepositoryException
     *             in case the metadata cannot be updated.
     */
    public void updateMetadata(ContentMetadata metadata) throws ContentRepositoryException;
}