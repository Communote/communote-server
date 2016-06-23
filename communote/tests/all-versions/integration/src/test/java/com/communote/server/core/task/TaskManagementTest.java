package com.communote.server.core.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;
import org.quartz.Scheduler;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.task.InvalidInstanceException;
import com.communote.server.api.core.task.TaskAlreadyRunningException;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.api.core.task.TaskNotActiveException;
import com.communote.server.api.core.task.TaskStatusException;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.model.task.Task;
import com.communote.server.model.task.TaskExecution;
import com.communote.server.model.task.TaskProperty;
import com.communote.server.model.task.TaskStatus;
import com.communote.server.persistence.tasks.TaskDao;
import com.communote.server.persistence.tasks.TaskExecutionDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.event.DeactivatableEventDispatcher;

/**
 * Tests for {@link TaskManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Test(singleThreaded = true)
public class TaskManagementTest extends CommunoteIntegrationTest {

    private TaskManagement taskManagement;
    private TaskDao taskDao;
    private TaskExecutionDao taskExecutionDao;
    private String instanceName;

    /**
     * Cleans up the db before every run.
     */
    @BeforeMethod
    public void clear() {
        taskExecutionDao.remove(taskExecutionDao.loadAll());
        taskDao.remove(taskDao.loadAll());
    }

    /**
     * Setup.
     *
     * @throws Exception
     *             in case setup failed
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        taskManagement = ServiceLocator.findService(TaskManagement.class);
        taskDao = ServiceLocator.findService(TaskDao.class);
        taskExecutionDao = ServiceLocator.findService(TaskExecutionDao.class);
        // pause the scheduler so it does not interfere with our tests. This also requires to
        // deactivate the firing of events because the TaskSchedulingContext would resume the
        // trigger of the task scheduler as soon it is informed about new tasks.
        ServiceLocator.instance().getService("eventDispatcher", DeactivatableEventDispatcher.class)
                .deactivate();
        ServiceLocator.findService(Scheduler.class).pauseAll();
        instanceName = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties().getInstanceName();
        taskExecutionDao.remove(taskExecutionDao.loadAll());
        taskDao.remove(taskDao.loadAll());
    }

    /**
     * Tests for
     * {@link TaskManagement#addTask(String, boolean, Long, java.util.Date, java.util.Map, Class)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testAddTask() throws Exception {
        final Map<String, String> properties = new HashMap<String, String>();
        for (int i = 0; i < 10 + RandomUtils.nextInt(20); i++) {
            properties.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }
        final String uniqueTaskName = UUID.randomUUID().toString();
        final Long interval = RandomUtils.nextLong();
        final Long taskId = taskManagement.addTask(uniqueTaskName, false, interval, new Date(),
                properties, TestTaskHandler.class);
        ServiceLocator.findService(TransactionManagement.class).execute(new RunInTransaction() {
            @Override
            public void execute() throws TransactionException {
                Task task = taskDao.load(taskId);
                Assert.assertNotNull(task);
                Assert.assertEquals(task.getUniqueName(), uniqueTaskName);
                Assert.assertEquals(task.getTaskInterval(), interval);
                for (TaskProperty property : task.getProperties()) {
                    Assert.assertEquals(property.getPropertyValue(),
                            properties.get(property.getPropertyKey()));
                }

            }
        });

    }

    /**
     * Tests {@link TaskManagement#failTaskExecution(String)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFailTaskExecution() throws Exception {
        String uniqueTaskName = UUID.randomUUID().toString();
        taskManagement.addTask(uniqueTaskName, true, 0L, new Date(), TestTaskHandler.class);
        taskManagement.startTaskExecution(uniqueTaskName);
        taskManagement.failTaskExecution(uniqueTaskName);
        Assert.assertNotNull(taskExecutionDao.findTaskExecution(uniqueTaskName));
        Assert.assertEquals(taskDao.findTaskByUniqueName(uniqueTaskName).getTaskStatus(),
                TaskStatus.FAILED);
        // test robustness with not existing task names
        taskManagement.failTaskExecution(UUID.randomUUID().toString());

        // test robustness with not running tasks
        uniqueTaskName = UUID.randomUUID().toString();
        taskManagement.addTask(uniqueTaskName, true, 0L, new Date(), TestTaskHandler.class);
        taskManagement.failTaskExecution(uniqueTaskName);

        // test that tasks of other instances cannot be failed
        Long executionId = taskManagement.startTaskExecution(uniqueTaskName);
        TaskExecution taskExecution = taskExecutionDao.load(executionId);
        taskExecution.setInstanceName(UUID.randomUUID().toString());
        taskExecutionDao.update(taskExecution);
        try {
            taskManagement.failTaskExecution(uniqueTaskName);
            Assert.fail("It must be possible to fail a task of other instances.");
        } catch (InvalidInstanceException e) {
            // All right.
        }
    }

    /**
     * Tests for {@link TaskManagement#getNextScheduledTask()}.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testGetNextScheduledTask() throws Exception {
        TaskTO task = taskManagement.getNextScheduledTask();
        Assert.assertNull(task);

        taskManagement.addTask(UUID.randomUUID().toString(), true, 0L, new Date(5000000000L),
                new HashMap<String, String>(), TestTaskHandler.class);
        Assert.assertNotNull(taskManagement.getNextScheduledTask());
        Assert.assertNull(taskManagement.getNextScheduledTask(new Date(5000000000L)));
        for (int i = 10 + RandomUtils.nextInt(32); i > 0; i--) {
            String uniqueTaskName = UUID.randomUUID().toString();
            taskManagement.addTask(uniqueTaskName, true, 0L, new Date(10000000 * i),
                    new HashMap<String, String>(), TestTaskHandler.class);
            task = taskManagement.getNextScheduledTask();
            Assert.assertNotNull(task);
            Assert.assertEquals(task.getUniqueName(), uniqueTaskName);
        }
    }

    /**
     * Tests {@link TaskManagement#killTaskExecution(Long)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testKillTaskExecution() throws Exception {
        // test killing running task
        String uniqueTaskName = UUID.randomUUID().toString();
        taskManagement.addTask(uniqueTaskName, true, 0L, new Date(), new HashMap<String, String>(),
                TestTaskHandler.class);
        Long executionId = taskManagement.startTaskExecution(uniqueTaskName);
        String taskName = taskManagement.killTaskExecution(executionId);
        Assert.assertEquals(taskName, uniqueTaskName);
        Task task = taskDao.findTaskByUniqueName(uniqueTaskName);
        Assert.assertEquals(task.getTaskStatus(), TaskStatus.PENDING);
        Assert.assertNull(taskExecutionDao.load(executionId), "Execution was not removed");

        // test killing a failed task
        executionId = taskManagement.startTaskExecution(uniqueTaskName);
        taskManagement.failTaskExecution(uniqueTaskName);
        task = taskDao.findTaskByUniqueName(uniqueTaskName);
        Assert.assertEquals(task.getTaskStatus(), TaskStatus.FAILED);
        taskName = taskManagement.killTaskExecution(executionId);
        Assert.assertEquals(taskName, uniqueTaskName);
        task = taskDao.findTaskByUniqueName(uniqueTaskName);
        Assert.assertEquals(task.getTaskStatus(), TaskStatus.PENDING);
        Assert.assertNull(taskExecutionDao.load(executionId), "Execution was not removed");

        // test killing a task of another instance
        executionId = taskManagement.startTaskExecution(uniqueTaskName);
        TaskExecution taskExecution = taskExecutionDao.load(executionId);
        taskExecution.setInstanceName(UUID.randomUUID().toString());
        taskExecutionDao.update(taskExecution);
        taskName = taskManagement.killTaskExecution(executionId);
        Assert.assertEquals(taskName, uniqueTaskName);
        task = taskDao.findTaskByUniqueName(uniqueTaskName);
        Assert.assertEquals(task.getTaskStatus(), TaskStatus.PENDING);
    }

    /**
     * Test for rescheduling tasks
     *
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testRescheduleTask() throws Exception {
        // standard reschedule test
        long millis = (System.currentTimeMillis() / 1000L) * 1000L;
        Date oldStart = new Date(millis + 120000);
        Date newStart = new Date(oldStart.getTime() - 2000);
        String uniqueTaskName = UUID.randomUUID().toString();
        taskManagement.addTask(uniqueTaskName, true, 0L, oldStart, TestTaskHandler.class);
        taskManagement.rescheduleTask(uniqueTaskName, newStart);
        Task task = taskDao.findTaskByUniqueName(uniqueTaskName);
        // Clean the milliseconds.
        Assert.assertEquals(task.getNextExecution().getTime() / 1000, newStart.getTime() / 1000);

        // test that running tasks cannot be rescheduled
        taskManagement.rescheduleTask(uniqueTaskName, new Date());
        taskManagement.startTaskExecution(uniqueTaskName);
        try {
            taskManagement.rescheduleTask(uniqueTaskName, newStart);
            Assert.fail("Rescheduling a running task must not be possible");
        } catch (TaskStatusException e) {
            Assert.assertEquals(e.getExpectedStatus(), TaskStatus.PENDING);
            Assert.assertEquals(e.getActualStatus(), TaskStatus.RUNNING);
        }
        // test that failed tasks cannot be rescheduled
        taskManagement.failTaskExecution(uniqueTaskName);
        try {
            taskManagement.rescheduleTask(uniqueTaskName, newStart);
            Assert.fail("Rescheduling a failed task must not be possible");
        } catch (TaskStatusException e) {
            Assert.assertEquals(e.getExpectedStatus(), TaskStatus.PENDING);
            Assert.assertEquals(e.getActualStatus(), TaskStatus.FAILED);
        }
    }

    /**
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testStartTaskExecution() throws Exception {
        // test that inactive tasks cannot be started
        String uniqueTaskName = UUID.randomUUID().toString();
        taskManagement.addTask(uniqueTaskName, false, 0L, new Date(),
                new HashMap<String, String>(), TestTaskHandler.class);
        try {
            taskManagement.startTaskExecution(uniqueTaskName);
            Assert.fail("Inactive tasks shouldn't be able to be started");
        } catch (TaskNotActiveException e) {
            // All right.
        }

        // test that tasks can be started
        uniqueTaskName = UUID.randomUUID().toString();
        Long taskId = taskManagement.addTask(uniqueTaskName, true, 0L, new Date(),
                new HashMap<String, String>(), TestTaskHandler.class);
        Long executionId = taskManagement.startTaskExecution(uniqueTaskName);
        Assert.assertNotNull(executionId, "The task was not started");
        TaskExecution foundTaskExecution = taskExecutionDao.findTaskExecution(uniqueTaskName);
        Assert.assertNotNull(foundTaskExecution);

        // test that running tasks cannot be started twice
        try {
            taskManagement.startTaskExecution(uniqueTaskName);
            Assert.fail("Running tasks shouldn't be able to be started twice");
        } catch (TaskAlreadyRunningException e) {
            // All right.
        }

        // test that failed tasks cannot be started
        taskManagement.failTaskExecution(uniqueTaskName);
        Task task = taskDao.load(taskId);
        Assert.assertNotNull(task);
        Assert.assertEquals(task.getTaskStatus(), TaskStatus.FAILED);
        try {
            taskManagement.startTaskExecution(uniqueTaskName);
            Assert.fail("Failed tasks must not be restarted without killing first");
        } catch (TaskAlreadyRunningException e) {
            // All right.
        }

        // test that tasks with future nextExecution timestamp cannot be started
        uniqueTaskName = UUID.randomUUID().toString();
        Date executionTime = new Date(new Date().getTime() + 300000);
        taskManagement.addTask(uniqueTaskName, true, 0L, executionTime, TestTaskHandler.class);
        executionId = taskManagement.startTaskExecution(uniqueTaskName);
        Assert.assertNull(executionId,
                "Tasks with future next execution timestamp must not be started");
    }

    /**
     * Tests {@link TaskManagement#stopAllTaskExecutions(boolean, Date)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testStopAllTaskExecutions() throws Exception {
        List<String> taskNames = new ArrayList<String>();
        for (int i = 0; i < 10 + RandomUtils.nextInt(32); i++) {
            String uniqueTaskName = UUID.randomUUID().toString();
            taskManagement.addTask(uniqueTaskName, true, 0L, new Date(), TestTaskHandler.class);
            taskManagement.startTaskExecution(uniqueTaskName);
            taskNames.add(uniqueTaskName);
        }
        List<String> failedTaskNames = new ArrayList<String>();
        for (int e = 0; e < 10 + RandomUtils.nextInt(32); e++) {
            String uniqueTaskName = UUID.randomUUID().toString();
            taskManagement.addTask(uniqueTaskName, true, 0L, new Date(), TestTaskHandler.class);
            taskManagement.startTaskExecution(uniqueTaskName);
            taskManagement.failTaskExecution(uniqueTaskName);
            failedTaskNames.add(uniqueTaskName);
        }
        taskManagement.stopAllTaskExecutions(false, null);
        Assert.assertEquals(taskExecutionDao.findTaskExecutions(instanceName).size(),
                failedTaskNames.size());
        for (String taskName : taskNames) {
            Assert.assertNull(taskDao.findTaskByUniqueName(taskName));
        }
        for (String failedTaskName : failedTaskNames) {
            Task taskByUniqueName = taskDao.findTaskByUniqueName(failedTaskName);
            Assert.assertNotNull(taskByUniqueName);
            Assert.assertEquals(taskByUniqueName.getTaskStatus(), TaskStatus.FAILED);
        }

        // test reset of failed tasks with reschedule date
        taskManagement.stopAllTaskExecutions(true, new Date());
        for (String failedTaskName : failedTaskNames) {
            Task taskByUniqueName = taskDao.findTaskByUniqueName(failedTaskName);
            Assert.assertNotNull(taskByUniqueName);
            Assert.assertEquals(taskByUniqueName.getTaskStatus(), TaskStatus.PENDING);
        }
        // test reset of failed tasks without reschedule date
        for (String failedTaskName : failedTaskNames) {
            taskManagement.startTaskExecution(failedTaskName);
            taskManagement.failTaskExecution(failedTaskName);
        }
        taskManagement.stopAllTaskExecutions(true, null);
        for (String failedTaskName : failedTaskNames) {
            Task taskByUniqueName = taskDao.findTaskByUniqueName(failedTaskName);
            Assert.assertNull(taskByUniqueName);
        }
    }

    /**
     * Test {@link TaskManagement#stopTaskExecution(String)} and
     * {@link TaskManagement#stopTaskExecution(String, Date)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testStopTaskExecution() throws Exception {
        String uniqueTaskName = UUID.randomUUID().toString();
        Long taskId = taskManagement.addTask(uniqueTaskName, true, 0L, new Date(),
                TestTaskHandler.class);
        taskManagement.startTaskExecution(uniqueTaskName);
        taskManagement.stopTaskExecution(uniqueTaskName);
        Assert.assertNull(taskDao.load(taskId));
        Assert.assertNull(taskExecutionDao.findTaskExecution(uniqueTaskName));

        taskId = taskManagement.addTask(uniqueTaskName, true, 10000L, new Date(),
                TestTaskHandler.class);
        Date now = new Date();
        taskManagement.startTaskExecution(uniqueTaskName);
        taskManagement.stopTaskExecution(uniqueTaskName);
        Assert.assertNotNull(taskDao.load(taskId));
        Assert.assertNull(taskExecutionDao.findTaskExecution(uniqueTaskName));
        Assert.assertFalse(now.after(taskDao.load(taskId).getNextExecution()));

        uniqueTaskName = UUID.randomUUID().toString();
        taskId = taskManagement
                .addTask(uniqueTaskName, true, 0L, new Date(), TestTaskHandler.class);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        Date rescheduleDate = new Date(calendar.getTimeInMillis() + 100000000);
        taskManagement.startTaskExecution(uniqueTaskName);
        taskManagement.stopTaskExecution(uniqueTaskName, rescheduleDate);
        Assert.assertNotNull(taskDao.load(taskId));
        Assert.assertNull(taskExecutionDao.findTaskExecution(uniqueTaskName));
        Assert.assertEquals(taskDao.load(taskId).getNextExecution(), rescheduleDate);

        uniqueTaskName = UUID.randomUUID().toString();
        taskId = taskManagement
                .addTask(uniqueTaskName, true, 0L, new Date(), TestTaskHandler.class);
        TaskExecution taskExecution = TaskExecution.Factory.newInstance(UUID.randomUUID()
                .toString(), taskDao.load(taskId));
        taskExecutionDao.create(taskExecution);
        try {
            taskManagement.stopTaskExecution(uniqueTaskName);
            Assert.fail("This method is not allowed to stop tasks for other instances.");
        } catch (InvalidInstanceException e) {
            // expected
        }

        // Test that failed tasks cannot be stopped
        uniqueTaskName = UUID.randomUUID().toString();
        taskId = taskManagement
                .addTask(uniqueTaskName, true, 0L, new Date(), TestTaskHandler.class);
        Long execId = taskManagement.startTaskExecution(uniqueTaskName);
        Assert.assertNotNull(execId, "Task was not started");
        taskManagement.failTaskExecution(uniqueTaskName);
        Assert.assertEquals(taskDao.load(taskId).getTaskStatus(), TaskStatus.FAILED);
        taskManagement.stopTaskExecution(uniqueTaskName);
        Task task = taskDao.load(taskId);
        Assert.assertNotNull(task);
        Assert.assertEquals(task.getTaskStatus(), TaskStatus.FAILED);
    }

    @AfterClass
    public void unpauseScheduler() throws Exception {
        ServiceLocator.findService(Scheduler.class).resumeAll();
        ServiceLocator.instance().getService("eventDispatcher", DeactivatableEventDispatcher.class)
        .activate();
    }
}
