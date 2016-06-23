package com.communote.server.core.crc;

/**
 * <p>
 * This is the Interface to implement by the classes that needs information about the stored
 * content.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ContentObserver {

    /**
     * <p>
     * Sends a message about an error on storing content.
     * </p>
     */
    public void errorOnStoreContent(com.communote.server.core.vo.content.AttachmentTO contentTo);

    /**
     * <p>
     * Sends the new and the old ContentId, when a ContentId has changed.
     * </p>
     */
    public void updateContentId(com.communote.server.core.crc.vo.ContentId oldContentId,
            com.communote.server.core.crc.vo.ContentId newContentId);

}