package com.communote.server.core.blog.notes.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.util.DescendingOrderComparator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorManager;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorContext;
import com.communote.server.api.core.task.TaskAlreadyExistsException;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.messaging.NotificationService;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.note.Note;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("noteStoringPostProcessorManager")
public class NoteStoringPostProcessorManagerImpl implements NoteStoringPostProcessorManager {

    @Autowired
    private TransactionManagement transactionManagement;
    @Autowired
    private TaskManagement taskManagement;
    @Autowired
    private BlogRightsManagement topicRightsManagement;
    @Autowired
    private UserManagement userManagement;
    @Autowired
    private NotificationService notificationDefinitionService;

    @Autowired
    private QueryManagement queryManagement;

    private final HashMap<Class<? extends NoteStoringPostProcessor>, NoteStoringPostProcessor> registeredProcessors;
    private ArrayList<NoteStoringPostProcessor> sortedProcessors;

    private final DescendingOrderComparator extensionComparator;

    /**
     * Default constructor.
     */
    public NoteStoringPostProcessorManagerImpl() {
        registeredProcessors = new HashMap<Class<? extends NoteStoringPostProcessor>, NoteStoringPostProcessor>();
        sortedProcessors = new ArrayList<NoteStoringPostProcessor>();
        extensionComparator = new DescendingOrderComparator();
    }

    /**
     * Add a processor. If there is already a processor of the same type, nothing will happen.
     *
     * @param processor
     *            the processor to register
     */
    @Override
    public synchronized void addProcessor(NoteStoringPostProcessor processor) {
        if (!registeredProcessors.containsKey(processor.getClass())) {
            registeredProcessors.put(processor.getClass(), processor);
            ArrayList<NoteStoringPostProcessor> newProcessors = new ArrayList<>(sortedProcessors);
            newProcessors.add(processor);
            if (newProcessors.size() > 1) {
                Collections.sort(newProcessors, extensionComparator);
            }
            sortedProcessors = newProcessors;
        }
    }

    /**
     * Calls the process method of the processor within a transaction.
     *
     * @param processor
     *            the processor to invoke
     * @param noteId
     *            the ID of the note to process
     * @param context
     *            the context to pass to the processor
     */
    private void invokeProcessor(final NoteStoringPostProcessor processor, final Long noteId,
            final NoteStoringPostProcessorContext context) {
        transactionManagement.execute(new RunInTransaction() {
            @Override
            public void execute() throws TransactionException {
                processor.processAsynchronously(noteId, context);
            }
        });
    }

    /**
     * Called after the service itself was instantiated.
     */
    @PostConstruct
    public void postConstruct() {
        // TODO optimize to sort only once on startup
        addProcessor(new UserNotificationNoteProcessor());
        boolean parentTreeOnly = Boolean
                .getBoolean("com.communote.mention.discussion.parent-tree-only");
        addProcessor(new DiscussionNotificationNoteProcessor(parentTreeOnly, topicRightsManagement));
        addProcessor(new DiscussionParticipationNotificationNoteProcessor(parentTreeOnly,
                topicRightsManagement, notificationDefinitionService));
        addProcessor(new TopicNotificationNoteProcessor(topicRightsManagement, userManagement,
                queryManagement));
    }

    @Override
    public void process(Collection<Note> notes, NoteStoringTO orginalNoteStoringTO,
            Map<String, String> properties) throws TaskAlreadyExistsException {
        if (notes == null || notes.isEmpty()) {
            return;
        }

        for (Note note : notes) {
            processSynchronously(orginalNoteStoringTO, note, properties);
        }
    }

    @Override
    public void processAsynchronously(Long noteId, NoteStoringPostProcessorContext context) {
        for (NoteStoringPostProcessor processor : sortedProcessors) {
            invokeProcessor(processor, noteId, context);
        }
    }

    /**
     * Process note synchronously and schedule asynchronous processing
     *
     * @param orginalNoteStoringTO
     *            the noteStoringTO
     * @param note
     *            the note that just been created
     * @param properties
     *            the properties that will be stored for the latter execution
     * @throws TaskAlreadyExistsException
     *             in case of duplicate task creation (indicating some parallel execution)
     */
    private void processSynchronously(NoteStoringTO orginalNoteStoringTO, Note note,
            Map<String, String> properties) throws TaskAlreadyExistsException {
        List<NoteStoringPostProcessor> processors = sortedProcessors;

        Map<String, String> mergedProperties = new HashMap<String, String>();
        if (properties != null) {
            mergedProperties.putAll(properties);
        }

        // process synchronously and check if asynchronous processing is required
        boolean interested = false;
        for (NoteStoringPostProcessor processor : processors) {
            if (processor.process(note, orginalNoteStoringTO, properties)) {
                interested = true;
                break;
            }
        }
        if (interested) {
            String taskId = "notePostProcessTask_";
            boolean editOperation = !note.getCreationDate().equals(note.getLastModificationDate());
            taskId += note.getId();
            // add last mod timestamp for edit case to allow several edits
            // before task is
            // processed
            // TODO would be better to ignore exception so that only one
            // notification is sent for the n edits but this could result in not sending the newest
            // version of the note for instance if the task handler is currently executing and
            // finishes before this transaction is committed
            if (editOperation) {
                taskId += "_" + note.getLastModificationDate().getTime();
            }
            mergedProperties.put(NotePostProcessTaskHandler.PROPERTY_KEY_NOTE_ID, note.getId()
                    .toString());
            taskManagement.addTask(taskId, true, 0L, null, mergedProperties,
                    NotePostProcessTaskHandler.class);
        }
    }

    @Override
    public synchronized void removeProcessor(Class<? extends NoteStoringPostProcessor> processorType) {
        NoteStoringPostProcessor processor = registeredProcessors.remove(processorType);
        if (processor != null) {
            ArrayList<NoteStoringPostProcessor> newProcessors = new ArrayList<>();
            newProcessors.remove(processor);
            sortedProcessors = newProcessors;
        }
    }
}
