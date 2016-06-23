package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.processor.NoteContentRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteMetadataRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorManager;

/**
 * Registry that waits for the arrival and departure of {@link NoteMetadataRenderingPreProcessor}s
 * and {@link NoteContentRenderingPreProcessor}s and adds them to or removes them from the
 * {@link NoteRenderingPreProcessorManager}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Instantiate(name = "NoteRenderingPreProcessorRegistry")
public class NoteRenderingPreProcessorRegistry {

    /**
     * Adds the given processor to the list of processors.
     * 
     * @param noteRenderingPreProcessor
     *            The processor.
     */
    @Bind(id = "registerContentProcessor", optional = true, aggregate = true)
    public void registerNoteContentPreProcessor(
            NoteContentRenderingPreProcessor noteRenderingPreProcessor) {
        ServiceLocator.findService(NoteRenderingPreProcessorManager.class).addProcessor(
                noteRenderingPreProcessor);
    }

    /**
     * Adds the given processor to the list of processors.
     * 
     * @param noteRenderingPreProcessor
     *            The processor.
     */
    @Bind(id = "registerProcessor", optional = true, aggregate = true)
    public void registerNoteMetadataPreProcessor(
            NoteMetadataRenderingPreProcessor noteRenderingPreProcessor) {
        ServiceLocator.findService(NoteRenderingPreProcessorManager.class).addProcessor(
                noteRenderingPreProcessor);
    }

    /**
     * Removes the given processor from the list of processors.
     * 
     * @param noteRenderingPreProcessor
     *            The processor.
     */
    @Unbind(id = "registerContentProcessor", optional = true, aggregate = true)
    public void removeNoteContentPreProcessor(
            NoteContentRenderingPreProcessor noteRenderingPreProcessor) {
        ServiceLocator.findService(NoteRenderingPreProcessorManager.class).removeProcessor(
                noteRenderingPreProcessor);
    }

    /**
     * Removes the given processor from the list of processors.
     * 
     * @param noteRenderingPreProcessor
     *            The processor.
     */
    @Unbind(id = "registerProcessor", optional = true, aggregate = true)
    public void removeNoteMetadataPreProcessor(
            NoteMetadataRenderingPreProcessor noteRenderingPreProcessor) {
        ServiceLocator.findService(NoteRenderingPreProcessorManager.class).removeProcessor(
                noteRenderingPreProcessor);
    }
}
