package com.communote.server.core.blog.notes.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.string.StringHelper;
import com.communote.common.util.DescendingOrderComparator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorContext;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorManager;
import com.communote.server.api.core.property.PropertyManagement;
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

    private static final char PROCESSOR_IDS_SEPARATOR_CHAR = ' ';
    private static final int PROPERTY_VALUE_LENGTH = 1024;
    private static final String PROPERTY_KEY_PREFIX_PROCESSOR_IDS = PropertyManagement.KEY_GROUP
            + ".processorIds";
    // backwards compatibility: old property key for saving the note id in the task properties
    private static final String LEGACY_PROPERTY_KEY_NOTE_ID = "noteId";
    private static final String PROPERTY_KEY_NOTE_ID = PropertyManagement.KEY_GROUP + "."
            + LEGACY_PROPERTY_KEY_NOTE_ID;

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

    private final HashMap<String, NoteStoringPostProcessor> registeredProcessors;
    private ArrayList<NoteStoringPostProcessor> sortedProcessors;

    private final DescendingOrderComparator extensionComparator;

    /**
     * Default constructor.
     */
    public NoteStoringPostProcessorManagerImpl() {
        registeredProcessors = new HashMap<String, NoteStoringPostProcessor>();
        sortedProcessors = new ArrayList<NoteStoringPostProcessor>();
        extensionComparator = new DescendingOrderComparator();
    }

    private void addAsynchronousProcessorIds(List<String> processorIds,
            Map<String, String> properties) {
        String allIds = StringUtils.join(processorIds, PROCESSOR_IDS_SEPARATOR_CHAR);
        // values of task properties are limited in length -> use additional properties if necessary
        int count = 0;
        while (allIds.length() > PROPERTY_VALUE_LENGTH) {
            String firstChunk = allIds.substring(0, PROPERTY_VALUE_LENGTH);
            properties.put(PROPERTY_KEY_PREFIX_PROCESSOR_IDS + count, firstChunk);
            allIds = allIds.substring(PROPERTY_VALUE_LENGTH);
            count++;
        }
        properties.put(PROPERTY_KEY_PREFIX_PROCESSOR_IDS + count, allIds);
    }

    @Override
    public boolean addProcessor(NoteStoringPostProcessor processor) {
        String processorId = getNormalizedProcessorId(processor.getId());
        if (processorId == null) {
            throw new IllegalArgumentException("Processor ID cannot be null, empty or blank");
        }
        synchronized (this) {
            if (!registeredProcessors.containsKey(processorId)) {
                registeredProcessors.put(processorId, processor);
                ArrayList<NoteStoringPostProcessor> newProcessors = new ArrayList<>(
                        sortedProcessors);
                newProcessors.add(processor);
                if (newProcessors.size() > 1) {
                    Collections.sort(newProcessors, extensionComparator);
                }
                sortedProcessors = newProcessors;
                return true;
            }
        }
        return false;
    }

    private Set<String> extractProcessorIds(Map<String, String> properties) {
        HashSet<String> ids = new HashSet<>();
        int count = 0;
        StringBuilder allIds = new StringBuilder();
        String chunk = properties.get(PROPERTY_KEY_PREFIX_PROCESSOR_IDS + count);
        while (chunk != null) {
            allIds.append(chunk);
            count++;
            chunk = properties.get(PROPERTY_KEY_PREFIX_PROCESSOR_IDS + count);
        }
        if (allIds.length() > 0) {
            Collections.addAll(ids,
                    StringUtils.split(allIds.toString(), PROCESSOR_IDS_SEPARATOR_CHAR));
        }
        return ids;
    }

    private String getNormalizedProcessorId(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return id.replace(' ', '_');
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
    public void processAsynchronously(NoteStoringPostProcessorContext context) {
        Long noteId = StringHelper.getStringAsLong(context.getProperties()
                .get(PROPERTY_KEY_NOTE_ID), null);
        boolean callAllProcessors = false;
        if (noteId == null) {
            // for backwards compatibility check old property. If this property is set, invoke all
            // processors to conform to the old buggy implementation.
            noteId = StringHelper.getStringAsLong(
                    context.getProperties().get(LEGACY_PROPERTY_KEY_NOTE_ID), null);
            if (noteId == null) {
                throw new IllegalArgumentException("Note ID is not contained in context properties");
            }
            callAllProcessors = true;
        }
        Set<String> ids = extractProcessorIds(context.getProperties());
        for (NoteStoringPostProcessor processor : sortedProcessors) {
            if (callAllProcessors || ids.contains(processor.getId())) {
                invokeProcessor(processor, noteId, context);
            }
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

        if (properties == null) {
            properties = new HashMap<>();
        }
        List<String> idsForAsynchronousProcessing = new ArrayList<>();
        // process synchronously and check if asynchronous processing is required
        for (NoteStoringPostProcessor processor : processors) {
            if (processor.process(note, orginalNoteStoringTO, properties)) {
                idsForAsynchronousProcessing.add(processor.getId());
            }
        }
        if (!idsForAsynchronousProcessing.isEmpty()) {
            Map<String, String> mergedProperties = new HashMap<String, String>();
            mergedProperties.putAll(properties);

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
            mergedProperties.put(PROPERTY_KEY_NOTE_ID, note.getId().toString());
            addAsynchronousProcessorIds(idsForAsynchronousProcessing, mergedProperties);
            taskManagement.addTask(taskId, true, 0L, null, mergedProperties,
                    NotePostProcessTaskHandler.class);
        }
    }

    @Override
    public boolean removeProcessor(NoteStoringPostProcessor processor) {
        return removeProcessor(processor.getId());
    }

    @Override
    public boolean removeProcessor(String processorId) {
        processorId = getNormalizedProcessorId(processorId);
        synchronized (this) {
            NoteStoringPostProcessor processor = registeredProcessors.remove(processorId);
            if (processor != null) {
                ArrayList<NoteStoringPostProcessor> newProcessors = new ArrayList<>();
                newProcessors.remove(processor);
                sortedProcessors = newProcessors;
                return true;
            }
        }
        return false;
    }
}
