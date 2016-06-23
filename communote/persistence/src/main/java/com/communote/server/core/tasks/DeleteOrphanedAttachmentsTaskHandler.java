package com.communote.server.core.tasks;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DeleteOrphanedAttachmentsTaskHandler extends ClientTaskHandler {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(DeleteOrphanedAttachmentsTaskHandler.class);

    /**
     * @return Now + 1 week
     */
    @Override
    public Date getRescheduleDate(Date now) {
        return new Date(System.currentTimeMillis() + 604800000); // Now + 1 Week
    }

    /**
     * Is called for each client
     * 
     * @param task
     *            the task
     * @throws Exception
     *             in case something goes wrong
     */
    @Override
    protected void runOnClient(TaskTO task) throws Exception {
        long upperUploadDate = System.currentTimeMillis()
                - (Long.getLong("com.communote.tasks.clean-attachments.min-age-in-days", 7) * 86400000);
        SecurityContext currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            int numberOfRemovedAttachments = ServiceLocator
                    .findService(ResourceStoringManagement.class).deleteOrphanedAttachments(
                            new Date(upperUploadDate));
            ClientTO currentClient = ClientHelper.getCurrentClient();
            if (ClientHelper.isClientGlobal(currentClient)) {
                LOGGER.info("Cleaned {} orphaned attachments", numberOfRemovedAttachments);
            } else {
                LOGGER.info("Cleaned {} orphaned attachments on client {}",
                        numberOfRemovedAttachments, currentClient.getClientId());
            }
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
    }
}
