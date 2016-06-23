package com.communote.server.core.tasks;

import java.util.Date;

import org.apache.log4j.Logger;

import com.communote.server.api.core.task.TaskHandler;
import com.communote.server.api.core.task.TaskHandlerException;
import com.communote.server.api.core.task.TaskTO;

/**
 * Empty task handler for services.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ServiceTaskHandler implements TaskHandler {

    /** Logger. */
    private final static Logger LOG = Logger.getLogger(ServiceTaskHandler.class);

    /**
     * {@inheritDoc}
     * 
     * @return <code>null</code>
     */
    public Date getRescheduleDate(Date now) {
        return null;
    }

    /**
     * This method should never be called.
     * 
     * {@inheritDoc}
     */
    public void run(TaskTO task) throws TaskHandlerException {
        LOG.error("This method should never be called: " + task.getUniqueName());
    }
}
