package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.storing.AttachmentStoringPreProcessor;
import com.communote.server.core.storing.ResourceStoringManagement;

/**
 * This waits for new {@link AttachmentStoringPreProcessor} and registers them within the
 * {@link ResourceStoringManagement}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Instantiate(name = "AttachmentStoringPreProcessorRegistry")
public class AttachmentStoringPreProcessorRegistry {

    /**
     * Adds the given processor to the list of processors.
     * 
     * @param attachmentStoringPreProcessor
     *            The processor.
     */
    @Bind(id = "registerAttachmentStoringPreProcessor", optional = true, aggregate = true)
    public void registerAttachmentStoringPreProcessor(
            AttachmentStoringPreProcessor attachmentStoringPreProcessor) {
        ServiceLocator.instance().getService(ResourceStoringManagement.class)
                .addAttachmentStoringPreProcessor(attachmentStoringPreProcessor);
    }

    /**
     * Removes the given processor from the list of processors.
     * 
     * @param attachmentStoringPreProcessor
     *            The processor.
     */
    @Unbind(id = "registerAttachmentStoringPreProcessor", optional = true, aggregate = true)
    public void removeAttachmentStoringPreProcessor(
            AttachmentStoringPreProcessor attachmentStoringPreProcessor) {
        ServiceLocator.instance().getService(ResourceStoringManagement.class)
                .removeAttachmentStoringPreProcessor(attachmentStoringPreProcessor);
    }
}
