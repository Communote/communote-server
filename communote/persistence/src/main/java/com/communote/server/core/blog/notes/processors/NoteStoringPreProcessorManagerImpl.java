package com.communote.server.core.blog.notes.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.util.DescendingOrderComparator;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringEditableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorManager;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.template.TemplateNoteStoringPreProcessor;
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
// TODO wrap NoteStoringTO in another object or path another object (a context) to process method to
// transfer some additional data like a plain text version of text (for easier blog, user, etc.
// extraction patterns) or add these transient data to the transient properties of the TO

@Service("noteStoringPreProcessorManager")
public class NoteStoringPreProcessorManagerImpl implements NoteStoringPreProcessorManager {

    private final DescendingOrderComparator notePreProcessorComparator = new DescendingOrderComparator();

    private List<NoteStoringImmutableContentPreProcessor> notePreProcessors;
    private List<NoteStoringEditableContentPreProcessor> noteContentPreProcessors;
    // internal programmatically sorted lists of preprocessors that should run after the others of a
    // given type. These lists are not extendible by plugins. It is required since the processor's
    // order can't assert that a plugin does not add a processor running after those that should run
    // last.
    private final List<NoteStoringEditableContentPreProcessor> runAfterContentPreProcessors;
    private final List<NoteStoringImmutableContentPreProcessor> runAfterPreProcessors;

    @Autowired
    private NotePermissionManagement notePermissionManagement;

    @Autowired
    private PropertyManagement propertyManagement;

    @Autowired
    private ResourceStoringManagement resourceStoringManagement;

    /**
     * Default constructor
     */
    public NoteStoringPreProcessorManagerImpl() {
        runAfterContentPreProcessors = new ArrayList<NoteStoringEditableContentPreProcessor>();
        noteContentPreProcessors = new ArrayList<NoteStoringEditableContentPreProcessor>();
        // add default content editing preprocessors to extension point
        noteContentPreProcessors.add(new TemplateNoteStoringPreProcessor());
        noteContentPreProcessors.add(new ExtractBlogsNoteStoringPreProcessor());
        // add the preprocessors which should run after the extensible pre-processors to ensure
        // integrity
        runAfterContentPreProcessors.add(new RemoveUnsupportedMarkupNotePreProcessor());

        // prepare preprocessors that cannot edit the content
        runAfterPreProcessors = new ArrayList<NoteStoringImmutableContentPreProcessor>();
        notePreProcessors = new ArrayList<NoteStoringImmutableContentPreProcessor>();
        // add default preprocessors to extension point
        notePreProcessors.add(new ExtractTagsNotePreProcessor());
        Collections.sort(notePreProcessors, notePreProcessorComparator);
        // add the preprocessors which should run after the extensible preprocessors to ensure
        // integrity
        runAfterPreProcessors.add(new AssertCommentPreProcessor());
        runAfterPreProcessors.add(new AssertNoteContentNotePreProcessor());
    }

    @Override
    public void addProcessor(NoteStoringEditableContentPreProcessor notePreProcessor) {
        // thread-safe add
        synchronized (noteContentPreProcessors) {
            ArrayList<NoteStoringEditableContentPreProcessor> newProcessors = new ArrayList<NoteStoringEditableContentPreProcessor>(
                    noteContentPreProcessors);
            newProcessors.add(notePreProcessor);
            Collections.sort(newProcessors, notePreProcessorComparator);
            noteContentPreProcessors = newProcessors;
        }
    }

    @Override
    public void addProcessor(NoteStoringImmutableContentPreProcessor notePreProcessor) {
        // thread-safe add
        synchronized (notePreProcessors) {
            ArrayList<NoteStoringImmutableContentPreProcessor> newProcessors = new ArrayList<NoteStoringImmutableContentPreProcessor>(
                    notePreProcessors);
            newProcessors.add(notePreProcessor);
            Collections.sort(newProcessors, notePreProcessorComparator);
            notePreProcessors = newProcessors;
        }
    }

    /**
     * Invoke the provided preprocessors and take care of the autosave status of the note.
     *
     * @param preProcessors
     *            the preprocessors to call
     * @param noteToEdit
     *            the note to edit or null if creating a new note
     * @param noteStoringTO
     *            the current note TO
     * @return the modified note TO
     * @throws NoteManagementAuthorizationException
     *             thrown to indicate that the note cannot be created because of access restrictions
     * @throws NoteStoringPreProcessorException
     *             thrown to indicate that the pre-processing failed and the note cannot be created
     */
    private NoteStoringTO invokeEditableContentProcessors(
            List<NoteStoringEditableContentPreProcessor> preProcessors, Note noteToEdit,
            NoteStoringTO noteStoringTO) throws NoteManagementAuthorizationException,
            NoteStoringPreProcessorException {
        for (NoteStoringEditableContentPreProcessor notePreProcessor : preProcessors) {
            if (noteStoringTO.isPublish() || notePreProcessor.isProcessAutosave()) {
                if (noteToEdit == null) {
                    noteStoringTO = notePreProcessor.process(noteStoringTO);
                } else {
                    noteStoringTO = notePreProcessor.processEdit(noteToEdit, noteStoringTO);
                }
            }
        }
        return noteStoringTO;
    }

    /**
     * Invoke the provided preprocessors and take care of the autosave status of the note.
     *
     * @param preProcessors
     *            the preprocessors to call
     * @param noteToEdit
     *            the note to edit or null if creating a new note
     * @param noteStoringTO
     *            the current note TO
     * @return the modified note TO
     * @throws NoteManagementAuthorizationException
     *             thrown to indicate that the note cannot be created because of access restrictions
     * @throws NoteStoringPreProcessorException
     *             thrown to indicate that the pre-processing failed and the note cannot be created
     */
    private NoteStoringTO invokeImmutableContentProcessors(
            List<NoteStoringImmutableContentPreProcessor> preProcessors, Note noteToEdit,
            NoteStoringTO noteStoringTO) throws NoteManagementAuthorizationException,
            NoteStoringPreProcessorException {
        for (NoteStoringImmutableContentPreProcessor notePreProcessor : preProcessors) {
            if (noteStoringTO.isPublish() || notePreProcessor.isProcessAutosave()) {
                if (noteToEdit == null) {
                    noteStoringTO = notePreProcessor.process(noteStoringTO);
                } else {
                    noteStoringTO = notePreProcessor.processEdit(noteToEdit, noteStoringTO);
                }
            }
        }
        return noteStoringTO;
    }

    /**
     * Initializer.
     */
    @PostConstruct
    public void postConstruct() {
        addProcessor(new ExtractUsersNotePreProcessor());
        addProcessor(new RepostNoteStoringPreProcessor(notePermissionManagement,
                propertyManagement, resourceStoringManagement));
        addProcessor(new EditNotificationNoteStoringPreProcessor());
    }

    @Override
    public void process(NoteStoringTO noteStoringTO) throws NoteStoringPreProcessorException,
    NoteManagementAuthorizationException {
        processEdit(null, noteStoringTO);
    }

    @Override
    public void processEdit(Note noteToEdit, NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        // do some normalizations
        // pre-processors usually assume that the content is not null, also it can be (e.g. template
        // notes). Hint: that the content is not blank will be checked by one of the
        // runAfterPreProcessors.
        if (noteStoringTO.getContent() == null) {
            noteStoringTO.setContent("");
        }
        // first invoke the preprocessors that can modify the content
        noteStoringTO = invokeEditableContentProcessors(noteContentPreProcessors, noteToEdit,
                noteStoringTO);
        noteStoringTO = invokeEditableContentProcessors(runAfterContentPreProcessors, noteToEdit,
                noteStoringTO);
        // save current content and run preprocessors that do not modify the content
        String savedContent = noteStoringTO.getContent();
        noteStoringTO = invokeImmutableContentProcessors(notePreProcessors, noteToEdit,
                noteStoringTO);
        noteStoringTO = invokeImmutableContentProcessors(runAfterPreProcessors, noteToEdit,
                noteStoringTO);
        noteStoringTO.setContent(savedContent);

    }

    @Override
    public void removeProcessor(NoteStoringEditableContentPreProcessor notePreProcessor) {
        synchronized (noteContentPreProcessors) {
            ArrayList<NoteStoringEditableContentPreProcessor> newProcessors = new ArrayList<NoteStoringEditableContentPreProcessor>(
                    noteContentPreProcessors);
            newProcessors.remove(notePreProcessor);
            noteContentPreProcessors = newProcessors;
        }
    }

    @Override
    public void removeProcessor(NoteStoringImmutableContentPreProcessor notePreProcessor) {
        synchronized (notePreProcessors) {
            ArrayList<NoteStoringImmutableContentPreProcessor> newProcessors = new ArrayList<NoteStoringImmutableContentPreProcessor>(
                    notePreProcessors);
            newProcessors.remove(notePreProcessor);
            notePreProcessors = newProcessors;
        }
    }

}
