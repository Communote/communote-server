package com.communote.server.api.core.note.processor;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;

/**
 * An extension point that allows transient modification of a note before it is rendered.
 *
 * This extension point supports 2 kinds of preprocessors, one that can modify the content of a note
 * and the other that can modify the metadata of a note.
 *
 * This extension point respects the order value of the registered extensions when calling them.
 * Those with a higher order value will be called first.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface NoteRenderingPreProcessorManager {
    /**
     * Processes a note by calling all the registered preprocessors which support the render mode of
     * the context.
     *
     * @param context
     *            the render context. If the render context has no render mode (i.e. the render mode
     *            is null), no preprocessors will be called.
     * @param item
     *            value object holding the details about the note
     * @return the modified item
     * @throws NoteRenderingPreProcessorException
     *             in case one of the preprocessors failed. When this exception is thrown the
     *             provided note list item can be in an inconsistent state (partially processed or
     *             might contain unexpected data). The caller should therefore ignore the provided
     *             note list item.
     */
    NoteData process(NoteRenderContext context, NoteData item)
            throws NoteRenderingPreProcessorException;

    /**
     * Adds the given processor to the list of processors. The content preprocessors run after the
     * metadata preprocessors.
     *
     * @param processor
     *            The processor to add.
     */
    void addProcessor(NoteContentRenderingPreProcessor processor);

    /**
     * Adds the given processor to the list of processors. The metadata preprocessors run before the
     * content preprocessors.
     *
     * @param processor
     *            The processor to add.
     */
    void addProcessor(NoteMetadataRenderingPreProcessor processor);

    /**
     * Adds the given processor to the list of processors.
     *
     * @param processor
     *            the processor to register
     * @param runAfterContentProcessors
     *            if true the metadata preprocessor is invoked after the content preprocessors
     *            completed
     */
    void addProcessor(NoteMetadataRenderingPreProcessor processor,
            boolean runAfterContentProcessors);

    /**
     * Removes the given processor from the list of processors.
     *
     * @param processor
     *            The processor to remove
     */
    void removeProcessor(NoteContentRenderingPreProcessor processor);

    /**
     * Removes the given processor from the list of processors.
     *
     * @param processor
     *            The processor to remove
     */
    void removeProcessor(NoteMetadataRenderingPreProcessor processor);
}
