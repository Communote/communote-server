package com.communote.server.core.storing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.virusscan.VirusScanner;
import com.communote.common.virusscan.exception.InitializeException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.vo.content.AttachmentTO;

/**
 * Internal processor to scan for viruses.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class VirusScannerAttachmentStoringPreProcessor implements AttachmentStoringPreProcessor {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(VirusScannerAttachmentStoringPreProcessor.class);

    /**
     * @return Integer.MAX_VALUE
     */
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(AttachmentTO attachment) throws LocalizedResourceStoringManagementException {
        try {
            VirusScanner scanner = ServiceLocator.instance().getVirusScanner();
            if (scanner != null) {
                attachment.setVirusScanner(scanner);
            } else {
                LOGGER.debug("No virus scan will be executed, because the scanner is disabled");
            }
        } catch (InitializeException e) {
            throw new ResourceStoringManagementException("Virus scanner not initialized", e);
        }
    }

}
