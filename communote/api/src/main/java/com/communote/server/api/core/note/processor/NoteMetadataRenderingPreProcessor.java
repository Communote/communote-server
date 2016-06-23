package com.communote.server.api.core.note.processor;

import com.communote.common.util.Orderable;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;

/**
 * This pre-processor is called before a note is rendered and thus provides a flexible way for
 * making additional modifications to the metadata of a note. However, the content cannot be
 * modified by this preprocessor.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface NoteMetadataRenderingPreProcessor extends Orderable {

    /**
     * default order value
     */
    int DEFAULT_ORDER = 1000;

    /**
     * @return the order value which is interpreted as the priority of the pre-processor. The higher
     *         the priority, the earlier this processor will be called.
     */
    @Override
    int getOrder();

    /**
     * Processes a note for a specific render context. This method will only be called if the
     * processor supports the mode given by the render context.
     * 
     * @param context
     *            holds details about the render context to allow specific processing in different
     *            situations
     * @param item
     *            the item to be processed
     * @return true if the item was modified, false otherwise
     * @throws NoteRenderingPreProcessorException
     *             in case something unexpected lead to the failure of the processor
     */
    boolean process(NoteRenderContext context, NoteData item)
            throws NoteRenderingPreProcessorException;

    /**
     * Whether the processor supports a specific render mode.
     * 
     * @param mode
     *            the note render mode, never null
     * @return true if the mode can be handled by the processor, false otherwise
     */
    boolean supports(NoteRenderMode mode);
}
