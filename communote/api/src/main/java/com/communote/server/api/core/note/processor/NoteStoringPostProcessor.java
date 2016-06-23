package com.communote.server.api.core.note.processor;

import java.util.Map;

import com.communote.common.util.Orderable;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.model.note.Note;

/**
 * Component for processing a note after it was created or updated. This processor supports
 * synchronous and asynchronous post-processing. The former runs within the same transaction in
 * which the note was created or updated. The latter is invoked after the transaction has been
 * committed.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface NoteStoringPostProcessor extends Orderable {

    /**
     * @return the order value which is interpreted as the priority of the extension. The higher the
     *         priority, the earlier this extension will be called.
     */
    @Override
    int getOrder();

    /**
     * Allows synchronous processing of a note after it was created or updated. Synchronous means
     * that this method is run within the transaction in which the note was created or updated.
     * Moreover, this method returns whether an asynchronous post-processing of the note by this
     * processor is required. An implementation should be fast and must not modify the note in a way
     * which leads to a semantically invalid note or note hierarchy.
     *
     * @param note
     *            the note to process
     * @param orginalNoteStoringTO
     *            the noteStoringTO that holds the original data which was passed to the note
     *            creation/update process
     * @param properties
     *            Properties that can be used to pass some data to the asynchronous post-processing.
     *            They will be available in the {@link NoteStoringPostProcessorContext}.
     *
     * @return true if this processor also wants to process this note asynchronously
     */
    boolean process(Note note, NoteStoringTO orginalNoteStoringTO, Map<String, String> properties);

    /**
     * Allows asynchronous processing of a note after it was stored. This method is only called if
     * the process method returned <code>true</code> for this note. Since the method is called
     * asynchronously there will be no current user. Each post-processor implementation is run
     * within its own transaction.
     *
     * @param noteId
     *            the ID of the note to process. If the ID cannot be resolved to a note, which might
     *            be the case if the note was deleted, implementations should just return.
     * @param context
     *            the context which holds some additional data and allows sharing information with
     *            subsequent processors
     */
    void processAsynchronously(Long noteId, NoteStoringPostProcessorContext context);
}
