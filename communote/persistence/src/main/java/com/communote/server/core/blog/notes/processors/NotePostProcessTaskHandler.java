package com.communote.server.core.blog.notes.processors;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContext;

import com.communote.common.string.StringHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorManager;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorContext;
import com.communote.server.api.core.task.TaskHandler;
import com.communote.server.api.core.task.TaskHandlerException;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.core.security.AuthenticationHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class NotePostProcessTaskHandler implements TaskHandler {
    private final static Logger LOG = Logger.getLogger(NotePostProcessTaskHandler.class);
    /**
     * Property key for saving the note id in the task properties
     */
    public static final String PROPERTY_KEY_NOTE_ID = "noteId";
    /**
     * Property key for saving the IDs of the users that should not be notified in the task
     * properties
     */
    public static final String PROPERTY_KEY_USER_IDS_NO_NOTIFY = "idsToSkip";

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getRescheduleDate(Date now) {
        // never reschedule
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(TaskTO task) throws TaskHandlerException {
        SecurityContext currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            Long noteId = StringHelper
                    .getStringAsLong(task.getProperty(PROPERTY_KEY_NOTE_ID), null);
            if (noteId == null) {
                // log and skip
                LOG.error("Cannot handle NotePostProcessTask because the note ID property is missing. taskId="
                        + task.getId()
                        + " taskUniqueName="
                        + task.getUniqueName()
                        + " PROPERTY_KEY_NOTE_ID="
                        + task.getProperty(PROPERTY_KEY_NOTE_ID));
            } else {
                Long[] userIdsToSkip = StringHelper.getStringAsLongArray(task
                        .getProperty(PROPERTY_KEY_USER_IDS_NO_NOTIFY));
                NoteStoringPostProcessorContext context = new NoteStoringPostProcessorContext(userIdsToSkip);
                NoteStoringPostProcessorManager extensionPoint = ServiceLocator
                        .instance().getService(NoteStoringPostProcessorManager.class);
                extensionPoint.processAsynchronously(noteId, context);
            }
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
    }

}
