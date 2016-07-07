package com.communote.server.test.note;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.processor.NoteContentRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorException;

/**
 * Content pre-processor with which supports caching bot does not modify the content. Can be used
 * for tests that want to ensure that the cache of NoteRenderingPreProcessorManager implementation
 * is correctly invalidated.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class DummyCachingNoteRenderingProcessor implements NoteContentRenderingPreProcessor {

    @Override
    public int getOrder() {
        return DEFAULT_ORDER + 200;
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    @Override
    public boolean processNoteContent(NoteRenderContext context, NoteData item)
            throws NoteRenderingPreProcessorException {
        // doesn't actually do anything
        return false;
    }

    @Override
    public boolean replacesContent() {
        return false;
    }

    @Override
    public boolean supports(NoteRenderMode mode, NoteData note) {
        return true;
    }

}
