package com.communote.server.persistence.tasks;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.model.task.Task;
import com.communote.server.model.task.TaskStatus;
import com.communote.server.test.CommunoteIntegrationTest;

/**
 * Tests for TaskDao.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TaskDaoTest extends CommunoteIntegrationTest {

    private TaskDao taskDao;
    private TaskExecutionDao taskExecutionDao;

    /**
     * Clean.
     */
    @AfterClass(dependsOnGroups = "integration-test-setup")
    public void cleanUp() {
        taskExecutionDao.remove(taskExecutionDao.loadAll());
        taskDao.remove(taskDao.loadAll());
    }

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() {
        taskExecutionDao = ServiceLocator.findService(TaskExecutionDao.class);
        taskExecutionDao.remove(taskExecutionDao.loadAll());
        taskDao = ServiceLocator.findService(TaskDao.class);
        taskDao.remove(taskDao.loadAll());
    }

    /**
     * Tests for {@link TaskDao#findNextScheduledTask()}.
     */
    @Test
    public void testFindNextScheduledTask() {
        taskDao.create(Task.Factory.newInstance(UUID.randomUUID().toString(), true,
                TaskStatus.FAILED, new Date(System.currentTimeMillis() - 100000), "1"));
        taskDao.create(Task.Factory.newInstance(UUID.randomUUID().toString(), true,
                TaskStatus.RUNNING, new Date(System.currentTimeMillis() - 100000), "1"));
        taskDao.create(Task.Factory.newInstance(UUID.randomUUID().toString(), false,
                TaskStatus.PENDING, new Date(System.currentTimeMillis() - 100000), "1"));
        Assert.assertEquals(taskDao.findNextScheduledTask(), null);
        int e = 1000000;
        for (int i = e * (10 + RandomUtils.nextInt(32)); i > 0; i = i - e) {
            Long taskId = taskDao.create(
                    Task.Factory.newInstance(UUID.randomUUID().toString(), true,
                            TaskStatus.PENDING, new Date(i), "test")).getId();
            Assert.assertEquals(taskDao.findNextScheduledTask().getId(), taskId);
        }
    }
}
