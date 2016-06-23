package com.communote.server.core.tasks;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Job to trigger the {@link TaskExecutionCleaner}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TaskExecutionCleanupJob extends QuartzJobBean {

    private final static Logger LOG = Logger.getLogger(TaskExecutionCleanupJob.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        LOG.debug("Starting clean up of incomplete task executions");
        try {
            TaskExecutionCleaner executionCleaner = (TaskExecutionCleaner) context.getScheduler()
                    .getContext().get(TaskExecutionCleaner.KEY_TASK_EXECUTION_CLEANER);
            executionCleaner.processTasksToFail();
            executionCleaner.processTasksToStop();
        } catch (SchedulerException e) {
            LOG.error("Couldn't retrieve the task execution cleaner from the scheduling context", e);
        }
        LOG.debug("Finished clean up of incomplete task executions");
    }

}
