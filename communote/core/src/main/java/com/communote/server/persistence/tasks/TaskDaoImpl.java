package com.communote.server.persistence.tasks;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.communote.server.model.task.Task;
import com.communote.server.model.task.TaskConstants;
import com.communote.server.model.task.TaskStatus;

/**
 * @see com.communote.server.model.task.Task
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TaskDaoImpl
        extends com.communote.server.persistence.tasks.TaskDaoBase {

    private final static String QUERY_FIND_NEXT_SCHEDULED_TASK = "FROM " + TaskConstants.CLASS_NAME
            + " WHERE " + TaskConstants.TASKSTATUS + " = '" + TaskStatus.PENDING + "' AND  "
            + TaskConstants.ACTIVE + " = TRUE ORDER BY " + TaskConstants.NEXTEXECUTION + " ASC";

    private final static String QUERY_FIND_BY_UNIQUE_NAME = "FROM " + TaskConstants.CLASS_NAME
            + " WHERE " + TaskConstants.UNIQUENAME + " = ?";

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.tasks.TaskDao#findNextScheduledTask()
     */
    @Override
    protected Task handleFindNextScheduledTask() {
        Query query = getSession().createQuery(QUERY_FIND_NEXT_SCHEDULED_TASK);
        query.setMaxResults(1);
        return (Task) query.uniqueResult();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.tasks.TaskDao#findNextScheduledTasks()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Task> handleFindNextScheduledTasks(Date upperBound, int maxTasks,
            Collection<Long> taskIdsToExclude) {
        Criteria criteria = getSession().createCriteria(Task.class);
        criteria.setFetchMode(TaskConstants.PROPERTIES, FetchMode.JOIN);
        criteria.add(Restrictions.eq(TaskConstants.TASKSTATUS, TaskStatus.PENDING));
        criteria.add(Restrictions.eq(TaskConstants.ACTIVE, true));
        criteria.addOrder(Order.asc(TaskConstants.NEXTEXECUTION));
        if (taskIdsToExclude != null && taskIdsToExclude.size() > 0) {
            criteria.add(Restrictions.not(Restrictions.in(TaskConstants.ID, taskIdsToExclude)));
        }
        if (upperBound != null) {
            criteria.add(Restrictions.lt(TaskConstants.NEXTEXECUTION, upperBound));
        }
        criteria.setMaxResults(maxTasks);
        return criteria.list();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.tasks.TaskDao#findTaskByUniqueName()
     */
    @Override
    protected Task handleFindTaskByUniqueName(String uniqueName) {
        List<?> result = getHibernateTemplate().find(QUERY_FIND_BY_UNIQUE_NAME, uniqueName);
        if (result.size() > 1) {
            throw new NonUniqueResultException(result.size());
        }
        return result.size() == 1 ? (Task) result.get(0) : null;
    }
}