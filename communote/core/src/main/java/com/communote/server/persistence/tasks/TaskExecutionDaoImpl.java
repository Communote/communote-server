package com.communote.server.persistence.tasks;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.communote.server.model.task.TaskConstants;
import com.communote.server.model.task.TaskExecution;
import com.communote.server.model.task.TaskExecutionConstants;

/**
 * @see com.communote.server.model.task.TaskExecution
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TaskExecutionDaoImpl
        extends com.communote.server.persistence.tasks.TaskExecutionDaoBase {

    private static final String QUERY_FIND_TASKS_FOR_INSTANCE_NAME = "FROM "
            + TaskExecutionConstants.CLASS_NAME + " WHERE " + TaskExecutionConstants.INSTANCENAME
            + "=?";

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#findTaskExecution(String)
     */
    @Override
    protected TaskExecution handleFindTaskExecution(String uniqueTaskName) {
        Criteria criteria = getSession().createCriteria(TaskExecutionConstants.CLASS_NAME);
        criteria.createAlias(TaskExecutionConstants.TASK, "task");
        criteria.add(Restrictions.eq("task." + TaskConstants.UNIQUENAME, uniqueTaskName));
        Object result = criteria.uniqueResult();
        if (result != null) {
            return (TaskExecution) result;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @return This will always return a List, even if it empty.
     * 
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#findTaskExecutions(String)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Collection<TaskExecution> handleFindTaskExecutions(String instanceName) {
        return getHibernateTemplate().find(
                QUERY_FIND_TASKS_FOR_INSTANCE_NAME, instanceName);
    }
}