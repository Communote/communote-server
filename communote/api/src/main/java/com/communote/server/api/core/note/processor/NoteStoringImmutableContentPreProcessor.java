package com.communote.server.api.core.note.processor;

import com.communote.common.util.Orderable;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.model.note.Note;

/**
 * A preprocessor which is invoked before storing a note. This kind of preprocessor is not allowed
 * to edit the content of the note, but all the other members of the TO can be modified. These
 * preprocessors are called before those that can edit the content.
 *
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface NoteStoringImmutableContentPreProcessor extends Orderable {
    /**
     * the default value for the order. This value should be used if the preprocessor has no
     * specific requirements to the invocation order.
     */
    public static final int DEFAULT_ORDER = 1000;

    /**
     *
     * @return the order value which is interpreted as the priority of the processor. The higher the
     *         priority, the earlier this extension will be called.
     */
    @Override
    public int getOrder();

    /**
     * @return whether the PreProcessor should also be called when doing an autosave. This is
     *         normally not required.
     */
    public boolean isProcessAutosave();

    /**
     * Processing function which is invoked <b>before</b> a new note is created.
     *
     * @param noteStoringTO
     *            transfer object holding the data for creating the note
     * @return The altered NoteStoringTO.
     * @throws NoteStoringPreProcessorException
     *             thrown to indicate that the pre-processing failed and the note cannot be created
     * @throws NoteManagementAuthorizationException
     *             thrown to indicate that the note cannot be created because of access restrictions
     */
    // TODO extend signature and pass the plaintext version of the content
    public NoteStoringTO process(NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException;

    /**
     * Processing function which is invoked <b>before</b> an existing note is updated.
     *
     * @param noteToEdit
     *            the note which should be edited. Implementors should not modify the note.
     * @param noteStoringTO
     *            transfer object holding the data for updating the note
     * @return The altered NoteStoringTO.
     * @throws NoteStoringPreProcessorException
     *             thrown to indicate that the pre-processing failed and the note cannot be created
     * @throws NoteManagementAuthorizationException
     *             thrown to indicate that the note cannot be created because of access restrictions
     */
    public NoteStoringTO processEdit(Note noteToEdit, NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException;

}
