package com.communote.server.persistence.tasks;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.model.task.Task;
import com.communote.server.model.task.TaskExecution;
import com.communote.server.model.task.TaskStatus;
import com.communote.server.test.CommunoteIntegrationTest;

/**
 * Test for {@link TaskExecutionDao}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TaskExecutionDaoTest extends CommunoteIntegrationTest {

    private TaskExecutionDao taskExecutionDao;
    private TaskDao taskDao;

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() {
        taskDao = ServiceLocator.findService(TaskDao.class);
        taskExecutionDao = ServiceLocator.findService(TaskExecutionDao.class);
    }

    /**
     * Tests for finding a task execution for a unique task name.
     */
    @Test
    public void testFindTaskExecutionByUniqueName() {
        String uniqueName = UUID.randomUUID().toString();
        String instanceName = UUID.randomUUID().toString();
        Task task = Task.Factory.newInstance(uniqueName, true, TaskStatus.PENDING, new Date(),
                "com.");
        task = taskDao.create(task);
        TaskExecution taskExecution = taskExecutionDao.findTaskExecution(uniqueName);
        Assert.assertNull(taskExecution);
        taskExecution = TaskExecution.Factory.newInstance(instanceName, task);
        taskExecution = taskExecutionDao.create(taskExecution);
        TaskExecution foundExecution = taskExecutionDao.findTaskExecution(uniqueName);
        Assert.assertNotNull(foundExecution);
        Assert.assertEquals(foundExecution.getId(), taskExecution.getId());
        Assert.assertEquals(foundExecution.getTask().getUniqueName(), uniqueName);
    }

    /**
     * Tests, that it is possible to find all TaskExecutions for a given instance name.
     */
    @Test
    public void testFindTaskExecutionsByInstanceName() {
        String instanceName;
        Task task;
        TaskExecution taskExecution;
        for (int e = 1; e <= 5 + RandomUtils.nextInt(20); e++) {
            instanceName = UUID.randomUUID().toString();
            Assert.assertEquals(taskExecutionDao.findTaskExecutions(instanceName).size(), 0);
            for (int i = 1; i <= 10 + RandomUtils.nextInt(20); i++) {
                task = Task.Factory.newInstance(UUID.randomUUID().toString(), true,
                        TaskStatus.PENDING, new Date(), "com.");
                taskExecution = TaskExecution.Factory.newInstance(instanceName, task);
                taskDao.create(task);
                taskExecutionDao.create(taskExecution);
                Assert.assertEquals(taskExecutionDao.findTaskExecutions(instanceName).size(), i);
            }
        }
    }

    /**
     * Tests, that it is not possible to add more than one TaskExecution for one task.
     */
    @Test
    public void testInsertOnlyOneExecutionForATask() {
        String uniqueName = UUID.randomUUID().toString();
        String instanceName = UUID.randomUUID().toString();
        Task task = Task.Factory.newInstance(uniqueName, true, TaskStatus.PENDING, new Date(),
                "com.");
        task = ServiceLocator.findService(TaskDao.class).create(task);
        TaskExecution taskExecution = TaskExecution.Factory.newInstance(instanceName, task);
        taskExecutionDao.create(taskExecution);
        taskExecution.setInstanceName(instanceName + instanceName);
        try {
            taskExecutionDao.create(taskExecution);
        } catch (DataIntegrityViolationException e) {
            // All fine.
            return;
        }
        Assert.fail("This block should never be reached.");
    }
}
