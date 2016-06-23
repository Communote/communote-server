package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.processor.NoteStoringEditableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorManager;

/**
 * This waits for new {@link NoteStoringImmutableContentPreProcessor} and registers them within the
 * {@link NoteStoringPreProcessorManager}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Instantiate(name = "NoteStoringEditableContentPreProcessorRegistry")
public class NoteStoringEditableContentPreProcessorRegistry {

    /**
     * Adds the given processor to the list of processors.
     * 
     * @param notePreProcessor
     *            The processor.
     */
    @Bind(id = "noteStoringEditableContentPreProcessorHook", optional = true, aggregate = true)
    public void registerEditablePreProcessor(NoteStoringEditableContentPreProcessor notePreProcessor) {
        ServiceLocator.instance().getService(NoteStoringPreProcessorManager.class)
                .addProcessor(notePreProcessor);
    }

    /**
     * Adds the given processor to the list of processors.
     * 
     * @param notePreProcessor
     *            The processor.
     */
    @Bind(id = "noteStoringImmutableContentPreProcessorHook", optional = true, aggregate = true)
    public void registerImmutablePreProcessor(
            NoteStoringImmutableContentPreProcessor notePreProcessor) {
        ServiceLocator.instance().getService(NoteStoringPreProcessorManager.class)
                .addProcessor(notePreProcessor);
    }

    /**
     * Removes the given processor from the list of processors.
     * 
     * @param notePreProcessor
     *            The processor.
     */
    @Unbind(id = "noteStoringEditableContentPreProcessorHook", optional = true, aggregate = true)
    public void removeEditablePreProcessor(NoteStoringEditableContentPreProcessor notePreProcessor) {
        ServiceLocator.instance().getService(NoteStoringPreProcessorManager.class)
                .removeProcessor(notePreProcessor);
    }

    /**
     * Removes the given processor from the list of processors.
     * 
     * @param notePreProcessor
     *            The processor.
     */
    @Unbind(id = "noteStoringImmutableContentPreProcessorHook", optional = true, aggregate = true)
    public void removeImmutablePreProcessor(NoteStoringImmutableContentPreProcessor notePreProcessor) {
        ServiceLocator.instance().getService(NoteStoringPreProcessorManager.class)
                .removeProcessor(notePreProcessor);
    }
}
