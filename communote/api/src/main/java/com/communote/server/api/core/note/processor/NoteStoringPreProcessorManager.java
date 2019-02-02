package com.communote.server.api.core.note.processor;

import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.model.note.Note;

/**
 * Extension point for registering processors to manipulate notes before they are persisted. Typical
 * use cases cover the extraction of meta data from the text and cleaning the DOM of content so that
 * only supported tags and attributes are kept.
 *
 * This extension point respects the order value of the registered processors when calling them.
 * Those with a higher order value will be called first.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface NoteStoringPreProcessorManager {

    /**
     * Add the given preprocessor to the list of processors.
     *
     * @param notePreProcessor
     *            the preprocessor to add
     */
    void addProcessor(NoteStoringEditableContentPreProcessor notePreProcessor);

    /**
     * Add the given preprocessor to the list of processors.
     *
     * @param notePreProcessor
     *            the preprocessor to add
     */
    void addProcessor(NoteStoringImmutableContentPreProcessor notePreProcessor);

    /**
     * Run the registered preprocessors before creating a new note. This will at first invoke the
     * {@link NoteStoringEditableContentPreProcessor}s and afterwards the
     * {@link NoteStoringImmutableContentPreProcessor}s.
     *
     * @param noteStoringTO
     *            the TO to pass to the preprocessors
     * @throws NoteStoringPreProcessorException
     *             in case any of the preprocessors failed
     * @throws NoteManagementAuthorizationException
     *             thrown to indicate that the note cannot be created because of access restrictions
     */
    void process(NoteStoringTO noteStoringTO) throws NoteStoringPreProcessorException,
    NoteManagementAuthorizationException;

    /**
     * Run the registered preprocessors before updating an existing note. This will at first invoke
     * the {@link NoteStoringEditableContentPreProcessor}s and afterwards the
     * {@link NoteStoringImmutableContentPreProcessor}s.
     *
     * @param noteToEdit
     *            the note to edit
     * @param noteStoringTO
     *            the TO to pass to the preprocessors
     * @throws NoteStoringPreProcessorException
     *             in case any of the preprocessors failed
     * @throws NoteManagementAuthorizationException
     *             thrown to indicate that the note cannot be created because of access restrictions
     * @since 3.5
     */
    void processEdit(Note noteToEdit, NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException;

    /**
     * Remove the given preprocessor from the list of processors.
     *
     * @param notePreProcessor
     *            The processor.
     */
    void removeProcessor(NoteStoringEditableContentPreProcessor notePreProcessor);

    /**
     * Remove the given preprocessor from the list of processors.
     *
     * @param notePreProcessor
     *            The processor.
     */
    void removeProcessor(NoteStoringImmutableContentPreProcessor notePreProcessor);

}