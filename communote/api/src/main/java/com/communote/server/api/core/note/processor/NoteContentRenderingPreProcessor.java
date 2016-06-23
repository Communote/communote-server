package com.communote.server.api.core.note.processor;

import com.communote.common.util.Orderable;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;

/**
 * This pre-processor is called before a note is rendered and thus provides a flexible way for
 * making additional modifications to the content of a note.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface NoteContentRenderingPreProcessor extends Orderable {

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
     * @return whether the result can be cached. This method has to return false if this
     *         PreProcessor modifies the content in a way that depends on other NoteRenderContext
     *         attributes than the NoteRenderMode (e.g. locale, request attributes or modeOptions).
     *         This PreProcessor should not modify the note metadata (NoteData object) while
     *         processing the content if this method returns true because the PreProcessor will not
     *         be invoked if the processed content was cached.
     */
    boolean isCachable();

    /**
     * Processes the note content for a specific render context. This method will only be called if
     * the processor supports the mode given by the render context.
     * 
     * @param context
     *            holds details about the render context to allow specific processing in different
     *            situations
     * @param item
     *            the note item to process
     * @return true if the item was modified, false otherwise
     * @throws NoteRenderingPreProcessorException
     *             in case something unexpected lead to the failure of the processor
     */
    boolean processNoteContent(NoteRenderContext context, NoteData item)
            throws NoteRenderingPreProcessorException;

    /**
     * Denotes whether the pre-processor replaces the note content completely.
     * 
     * @return true if the processor replaces the content and the shortened content completely. If
     *         the processor only modifies parts of the content or other members of the item, false
     *         should be returned. When replacing content the new content must match the render
     *         mode.
     */
    boolean replacesContent();

    /**
     * Whether the processor supports a specific render mode. When returning true and the processor
     * modifies or replaces the note content it must ensure that it only inserts text which matches
     * the render mode, specifically the processor shouldn't insert HTML markup when called in PLAIN
     * mode.
     * 
     * @param mode
     *            the note render mode, never null
     * @param note
     *            the note to render. A pre-processor can use this parameter to decide based on note
     *            attributes whether the note content should be processed
     * @return true if the mode can be handled by the processor, false otherwise
     */
    boolean supports(NoteRenderMode mode, NoteData note);
}
