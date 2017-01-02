package com.communote.server.core.blog.notes.processors;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorContext;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorManager;
import com.communote.server.api.core.task.TaskHandler;
import com.communote.server.api.core.task.TaskHandlerException;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.core.security.AuthenticationHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class NotePostProcessTaskHandler implements TaskHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(NotePostProcessTaskHandler.class);

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
            NoteStoringPostProcessorContext context = new NoteStoringPostProcessorContext(
                    task.getProperties());
            NoteStoringPostProcessorManager manager = ServiceLocator
                    .findService(NoteStoringPostProcessorManager.class);
            manager.processAsynchronously(context);
        } catch (IllegalArgumentException e) {
            // log and skip
            LOGGER.error("Cannot handle NotePostProcessTask with ID {} and name {}: {}", task.getId(),
                    task.getUniqueName(), e.getMessage());
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
    }

}
