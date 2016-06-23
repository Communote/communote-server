package com.communote.server.core.task;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.core.tasks.RunScheduledTasksJob;
import com.communote.server.test.CommunoteIntegrationTest;

/**
 * Tests for {@link RunScheduledTasksJob}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class RunScheduledTasksJobTest extends CommunoteIntegrationTest {

    @Autowired
    private TaskManagement taskManagement;

    /**
     * Test.
     *
     * @throws Exception
     *             Exception.
     */
    @Test(timeOut = 60000)
    @Parameters("RunScheduledTasksJobTest.sleepTimeout")
    public void testRunScheduledTasksJob() throws Exception {
        Date now = new Date();
        long repeatInterval = 1000L;
        taskManagement.addTask(random(), true, 0L, new Date(now.getTime() + repeatInterval - 1),
                TestTaskHandler.class);
        taskManagement.addTask(random(), true, 0L, null, TestTaskHandler.class);
        taskManagement.addTask(random(), true, 0L, now, TestTaskHandler.class);
        taskManagement.addTask(random(), true, 0L, new Date(now.getTime() + 1000000 + 100
                * repeatInterval), TestTaskHandler.class);
        Scheduler scheduler = ServiceLocator.findService(Scheduler.class);
        scheduler.start();
        JobDetail jobDetail = new JobDetail(random(), random(), RunScheduledTasksJob.class);
        jobDetail.setVolatility(true);
        jobDetail.setDurability(true);
        scheduler.addJob(jobDetail, true);
        scheduler.triggerJob(jobDetail.getName(), jobDetail.getGroup());
        // As this is not really predictable, we just have to assume that at least one task was
        // executed and at a max 3 tasks were executed.
        while (TestTaskHandler.getCounter().get() == 0) {
            sleep(1000);
            scheduler.triggerJob(jobDetail.getName(), jobDetail.getGroup());
        }
        Assert.assertTrue(TestTaskHandler.getCounter().get() > 0);
        Assert.assertTrue(TestTaskHandler.getCounter().get() <= 3);
    }
}
