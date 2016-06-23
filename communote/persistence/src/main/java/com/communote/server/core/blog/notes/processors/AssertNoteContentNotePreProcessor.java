package com.communote.server.core.blog.notes.processors;

import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.core.blog.NoteManagementException;

/**
 * Processor, which checks the content syntactically.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AssertNoteContentNotePreProcessor implements NoteStoringImmutableContentPreProcessor {

    @Override
    public int getOrder() {
        // note: not that important what we return here because this pre-processor will be added to
        // a list that is not modifiable
        return 0;
    }

    @Override
    public boolean isProcessAutosave() {
        return false;
    }

    @Override
    public NoteStoringTO process(NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        if (noteStoringTO.getContent() == null || noteStoringTO.getContent().length() == 0) {
            throw new NoteManagementException("Content of note must not be empty.");
        }
        return noteStoringTO;
    }
}
