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
     * Add a processor. If there is already a processor with the same type, nothing will happen. If
     * the ID of the processor contains space characters they will be replaced by underscores.
     *
     * @param processor
     *            the processor to register
     * @return true if the processor was registered, false if there is already one with the same ID
     * @since 3.5
     */
    boolean addProcessor(NoteStoringPostProcessor processor);

    /**
     * Process the provided notes synchronously by the registered processors. This will also
     * schedule any asynchronous processing if necessary.
     *
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
     * @param context
     *            the context with details to be passed to the processors
     * @throws IllegalArgumentException
     *             in case the context did not contain a note ID
     * @since 3.5
     */
    void processAsynchronously(NoteStoringPostProcessorContext context);

    /**
     * Remove a processor if it is present.
     *
     * @param processor
     *            the processor to unregister
     * @return true if the processor was registered, false otherwise
     * @since 3.5
     */
    boolean removeProcessor(NoteStoringPostProcessor processor);

    /**
     * Remove a processor if it is present.
     *
     * @param processorId
     *            the ID of the processor to unregister
     * @return true if the processor was registered, false otherwise
     * @since 3.5
     */
    boolean removeProcessor(String processorId);
}
