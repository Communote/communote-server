package com.communote.server.api.core.note.processor;

import java.util.Collection;
import java.util.Map;

import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.task.TaskAlreadyExistsException;
import com.communote.server.model.note.Note;

/**
 * Extension point for adding components which can process a note after it was stored.
 * <p>
 * This extension point respects the order value of the registered extensions when calling them.
 * Those with a higher order value will be called first.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface NoteStoringPostProcessorManager {

    /**
     * Add a processor. If there is already a processor of the same type, nothing will happen.
     *
     * @param processor
     *            the processor to register
     */
    void addProcessor(NoteStoringPostProcessor processor);

    /**
     * Process the provided notes synchronously by the registered processors. This will also
     * schedule any asynchronous processing if necessary.
     * @param notes
     *            the notes to add
     * @param orginalNoteStoringTO
     *            the original note storing TO from which the notes were created
     * @param properties
     *            some properties to be stored for all notes
     *
     * @throws TaskAlreadyExistsException
     *             if there is already a task for processing that note
     */
    void process(Collection<Note> notes, NoteStoringTO orginalNoteStoringTO,
            Map<String, String> properties) throws TaskAlreadyExistsException;

    /**
     * Process a note asynchronously by invoking all registered processors.
     *
     * @param noteId
     *            the id of the note
     * @param context
     *            the context with details to be passed to the extensions
     */
    void processAsynchronously(Long noteId, NoteStoringPostProcessorContext context);

    /**
     * Remove a processor if it is present.
     *
     * @param processorType
     *            the processor to unregister
     */
    void removeProcessor(Class<? extends NoteStoringPostProcessor> processorType);
}
