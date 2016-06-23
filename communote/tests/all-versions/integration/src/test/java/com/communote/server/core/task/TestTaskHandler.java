package com.communote.server.core.task;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.task.TaskHandler;
import com.communote.server.api.core.task.TaskHandlerException;
import com.communote.server.api.core.task.TaskTO;

/**
 * Dummy for tests.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TestTaskHandler implements TaskHandler {

    private static AtomicInteger COUNTER = new AtomicInteger(0);

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestTaskHandler.class);

    /**
     * @return the counter.
     */
    public static AtomicInteger getCounter() {
        return COUNTER;
    }

    /**
     * @param counter
     *            the cOUNTER to set
     */
    public static void setCounter(AtomicInteger counter) {
        COUNTER = counter;
    }

    /**
     * {@inheritDoc}
     * 
     * @return null.
     */
    public Date getRescheduleDate(Date now) {
        return null;
    }

    /**
     * Does nothing.
     * 
     * {@inheritDoc}
     */
    public void run(TaskTO task) throws TaskHandlerException {
        LOGGER.info("Running for task: " + task.getUniqueName());
        COUNTER.incrementAndGet();
    }
}
