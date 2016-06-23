package com.communote.server.persistence.query;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.time.StopWatch;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.PageableList;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.common.util.LogHelper;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.vo.query.Query;
import com.communote.server.core.vo.query.QueryParameters;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.core.vo.query.TimelineQueryParameters;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.persistence.helper.dao.QueryHelper;

/**
 * The query helper executes queries instances
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class QueryHelperDaoImpl extends QueryHelperDaoBase {

    private static final ResultSpecification ALL_DATA_RESULT_SPECIFICATION = new ResultSpecification(
            0, 0, 0);

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryHelperDaoImpl.class);

    /**
     * Set the parameters to the query, and remove these which are not contained in the query. Set
     * the query as readonly
     *
     * @param myQuery
     *            the query to configure
     * @param params
     *            the parameters to use; delete the ones not needed in the query
     */
    private void configureAndFilterParameters(org.hibernate.Query myQuery,
            Map<String, Object> params) {
        if (params != null) {
            for (Iterator<Entry<String, Object>> iterator = params.entrySet().iterator(); iterator
                    .hasNext();) {
                Entry<String, Object> param = iterator.next();
                if (myQuery.getQueryString().contains(param.getKey())) {
                    String key = param.getKey();
                    Object value = param.getValue();
                    if (value.getClass().isArray()) {
                        myQuery.setParameterList(key, (Object[]) value);
                    } else {
                        myQuery.setParameter(key, value);
                    }
                } else {
                    iterator.remove();
                }
            }
        }
        myQuery.setReadOnly(true);
    }

    /**
     * Create a Hibernate query from the given instance
     *
     * @param query
     *            The query to use.
     * @param queryParameters
     *            the query instance
     * @param <T>
     *            Type of the query parameters.
     * @return the hibernate query
     */
    private <T extends QueryParameters> org.hibernate.Query createHibernateQuery(Query<?, T> query,
            T queryParameters) /* throws UnexpectedAuthorizationException */{

        // get the queries and parameters
        String queryAsString = query.buildQuery(queryParameters);
        // String countQuery = queryDefinition.buildCountQuery(queryInstance);
        Map<String, Object> params = queryParameters.getParameters();

        // configure the query
        Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
        org.hibernate.Query myQuery = session.createQuery(queryAsString);
        // Query myCountQuery = session.createQuery(query);

        configureAndFilterParameters(myQuery, params);
        return myQuery;
    }

    /**
     * @param query
     *            The query.
     * @param n
     *            The n'th element.
     * @param logPerformance
     *            True, if the performance should be logged.
     * @param class1
     * @param <T>
     *            Return type of this method.
     * @return The n'th element or null.
     */
    private <T> T getNthElement(org.hibernate.Query query, int n, boolean logPerformance) {
        StopWatch stopWatch = new StopWatch();
        if (logPerformance) {
            stopWatch.start();
        }
        query.setMaxResults(n);
        ScrollableResults scrollableResults = query.scroll(ScrollMode.SCROLL_INSENSITIVE);

        stopStopWatches(logPerformance, stopWatch, "QueryHelper#query.scroll");
        resetAndStartStopWatch(logPerformance, stopWatch);
        if (scrollableResults.last()) {
            stopStopWatches(logPerformance, stopWatch, "QueryHelper#scrollableResults.setRowNumber"
                    + n);
            resetAndStartStopWatch(logPerformance, stopWatch);
            return (T) scrollableResults.get(0);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PageableList handleExecuteQuery(Query query, QueryParameters queryParameters)
    /* throws UnexpectedAuthorizationException */{
        return handleExecuteQuery(query, queryParameters, null,
                queryParameters.getResultSpecification());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PageableList handleExecuteQuery(Query query, QueryParameters queryParameters,
            QueryResultConverter resultConverter, ResultSpecification resultSpecification)
    /* throws UnexpectedAuthorizationException */{
        StopWatch stopWatch = new StopWatch();
        StopWatch overallStopWatch = new StopWatch();
        boolean logPerformance = LogHelper.logPerformance(query);
        resetAndStartStopWatch(logPerformance, stopWatch);
        resetAndStartStopWatch(logPerformance, overallStopWatch);

        if (queryParameters.isLimitResultSet()
                && queryParameters instanceof TimelineQueryParameters) {
            limitQueryInstance((TimelineQueryParameters) queryParameters, logPerformance);
        }

        org.hibernate.Query hibernateQuery = createHibernateQuery(query, queryParameters);
        stopStopWatches(logPerformance, stopWatch, query.getClass().getSimpleName()
                + "#createQuery");
        resetAndStartStopWatch(logPerformance, stopWatch);

        PageableList pageableList = QueryHelper.queryAsPageableList(hibernateQuery,
                resultSpecification, logPerformance);

        stopStopWatches(logPerformance, stopWatch, query.getClass().getSimpleName() + "#runQuery");

        resetAndStartStopWatch(logPerformance, stopWatch);

        if (resultConverter == null) {
            pageableList = query.postQueryExecution(queryParameters, pageableList);
        } else {
            pageableList = resultConverter.convert(pageableList);
        }

        stopStopWatches(logPerformance, stopWatch, query.getClass().getSimpleName()
                + "#postQueryExecution");
        stopStopWatches(logPerformance, overallStopWatch, query.getClass().getSimpleName()
                + "#executeQuery");
        return pageableList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List handleExecuteQueryComplete(Query query, QueryParameters queryInstance)
    /* throws UnexpectedAuthorizationException */{
        return handleExecuteQuery(query, queryInstance, null, ALL_DATA_RESULT_SPECIFICATION);
    }

    /**
     * Limit the query instance, by retrieving the X-th elements of the associated NoteQuery and use
     * the creation date for filtering
     *
     * @param queryParameters
     *            the instance to limit
     * @param logPerformance
     *            true if some performance measure should be logged
     */
    private void limitQueryInstance(TimelineQueryParameters queryParameters, boolean logPerformance)
    /* throws UnexpectedAuthorizationException */{
        NoteQueryParameters noteQueryInstance = NoteQueryParameters.clone(queryParameters);
        if (!queryParameters.isLimitResultSetAvodingDuplicates()) {
            noteQueryInstance.setAllowDuplicateResults(true);
        }
        org.hibernate.Query noteQuery = createHibernateQuery(new NoteQuery(), noteQueryInstance);

        int nThElement;
        if (queryParameters.isRetrieveOnlyFollowedItems()) {
            nThElement = ClientProperty.NOTES_TO_USE_FOR_TRENDS_IN_FOLLOWING.getValue(200);
        } else {
            nThElement = ClientProperty.NOTES_TO_USE_FOR_TRENDS.getValue(1000);
        }
        SimpleNoteListItem listItem = getNthElement(noteQuery, nThElement, logPerformance);
        if (listItem != null) {
            Date creationDate = listItem.getCreationDate();
            if (queryParameters.getLowerTagDate() == null
                    || queryParameters.getLowerTagDate().after(creationDate)) {
                queryParameters.setLowerTagDate(creationDate);
                LOGGER.trace("Using limit creationDate = {}", creationDate);
            }
        }
    }

    /**
     * Resets and starts the given stop watch.
     *
     * @param logPerformance
     *            Only if true.
     * @param stopWatch
     *            The stop watch.
     */
    private void resetAndStartStopWatch(boolean logPerformance, StopWatch stopWatch) {
        if (logPerformance) {
            stopWatch.reset();
            stopWatch.start();
        }
    }

    /**
     * Stops the given watches.
     *
     * @param logPerformance
     *            Only if true.
     * @param stopWatch
     *            The watch.
     * @param message
     *            A message to display. Might be null.
     */
    private void stopStopWatches(boolean logPerformance, StopWatch stopWatch, String message) {
        if (!logPerformance) {
            return;
        }
        stopWatch.stop();
        if (message != null) {
            LogHelper.logPerformance(message, stopWatch);
        }
    }
}
