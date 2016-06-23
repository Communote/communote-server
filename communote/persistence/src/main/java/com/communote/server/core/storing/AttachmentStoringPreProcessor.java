package com.communote.server.core.storing;

import com.communote.common.util.Orderable;
import com.communote.server.core.vo.content.AttachmentTO;


/**
 * Processor to process attachments before they will finally be stored.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface AttachmentStoringPreProcessor extends Orderable {
    /**
     * Processes the given attachment.
     * 
     * @param attachment
     *            The attachment to process.
     * @throws LocalizedResourceStoringManagementException
     *             Thrown, when something went's wrong.
     */
    void process(AttachmentTO attachment) throws LocalizedResourceStoringManagementException;

}
