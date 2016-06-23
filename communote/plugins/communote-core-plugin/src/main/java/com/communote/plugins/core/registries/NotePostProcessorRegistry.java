package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorManager;

/**
 * This waits for new {@link NoteStoringPostProcessor} and registers them within the
 * {@link NotePostProcessorRegistryExtensionPoint}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Instantiate(name = "NotePostProcessorRegistryRegistry")
public class NotePostProcessorRegistry {

    /**
     * Adds the given processor to the list of processors.
     * 
     * @param notePostProcessor
     *            The processor.
     */
    @Bind(id = "registerProcessor", optional = true, aggregate = true)
    public void registerNotePreProcessor(NoteStoringPostProcessor notePostProcessor) {
        ServiceLocator.instance().getService(NoteStoringPostProcessorManager.class)
                .addProcessor(notePostProcessor);
    }

    /**
     * Removes the given processor from the list of processors.
     * 
     * @param notePostProcessor
     *            The processor.
     */
    @Unbind(id = "registerProcessor", optional = true, aggregate = true)
    public void removeNotePreProcessor(NoteStoringPostProcessor notePostProcessor) {
        ServiceLocator.instance().getService(NoteStoringPostProcessorManager.class)
                .removeProcessor(notePostProcessor.getClass());
    }
}
