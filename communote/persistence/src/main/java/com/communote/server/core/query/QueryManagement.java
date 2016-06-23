package com.communote.server.core.query;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.util.PageableList;
import com.communote.server.PrincipalStore;
import com.communote.server.core.vo.query.Query;
import com.communote.server.core.vo.query.QueryParameters;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.persistence.query.QueryHelperDao;

/**
 * @see com.communote.server.core.query.QueryManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("queryManagement")
@Transactional(readOnly = true)
public class QueryManagement {

    @Autowired
    private QueryHelperDao queryHelperDao;

    /**
     * @see com.communote.server.core.query.QueryManagement#executeQuery(com.communote.server.core.vo.query.QueryParameters)
     * 
     * @param query
     *            The query to execute.
     * @param queryParameters
     *            The query instance
     * @param <L>
     *            Type of the results for the query.
     * @param <T>
     *            Type of the query parameters.
     * @return PageableList
     * @deprecated Use {@link #query(Query, QueryParameters, QueryResultConverter)} instead.
     */
    @Deprecated
    public <L, T extends QueryParameters> PageableList<L> executeQuery(Query<L, T> query,
            T queryParameters) {
        return query(query, queryParameters, null);
    }

    /**
     * @see com.communote.server.core.query.QueryManagement#executeQueryComplete(com.communote.server.core.vo.query.QueryParameters)
     * 
     * @param query
     *            The query to execute.
     * @param queryParameters
     *            The query instance
     * @param <T>
     *            Type of the query parameters.
     * @param <L>
     *            Type of the results for the query.
     * @return list
     */
    public <L, T extends QueryParameters> List<L> executeQueryComplete(
            Query<L, T> query, T queryParameters) {
        if (queryParameters == null) {
            throw new IllegalArgumentException(
                    "QueryManagement.executeQueryComplete(QueryInstance queryInstance)"
                            + " - 'queryInstance' can not be null");
        }
        try {
            return getQueryHelperDao().executeQueryComplete(query, queryParameters);
        } catch (RuntimeException rt) {
            throw new QueryManagementException(
                    "Error performing 'QueryManagement.executeQueryComplete(QueryInstance queryInstance)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     * 
     * @return the current principal
     */
    protected Principal getPrincipal() {
        return PrincipalStore.get();
    }

    /**
     * Gets the reference to <code>queryHelper</code>'s DAO.
     * 
     * @return the QueryHelperDao
     */
    protected QueryHelperDao getQueryHelperDao() {
        return this.queryHelperDao;
    }

    /**
     * Returns the result without converting them in any way.
     * 
     * @param query
     *            The query.
     * @param queryParameters
     *            The query instance to use
     * @param <L>
     *            Type of the results items.
     * 
     * @return Pageable list of results.
     */
    public <L> PageableList<L> query(Query<L, ?> query,
            QueryParameters queryParameters) /* throws UnexpectedAuthorizationException */{
        return getQueryHelperDao().executeQuery(query, queryParameters);
    }

    /**
     * Executes the given query and converts the result using the given converter.
     * 
     * @param query
     *            The query to execute.
     * @param queryParameters
     *            The queries parameters.
     * @param converter
     *            The converter.
     * @param <O>
     *            Type of the final entities.
     * @param <L>
     *            Type of the ListItems of the queries result.
     * @param <T>
     *            Type of the query parameters.
     * 
     * @return A pageable list containing the results.
     */
    public <O, L, T extends QueryParameters> PageableList<O> query(
            Query<L, T> query, T queryParameters, QueryResultConverter<L, O> converter)
    /* throws UnexpectedAuthorizationException */{
        return getQueryHelperDao().executeQuery(query, queryParameters, converter,
                queryParameters.getResultSpecification());
    }

    /**
     * Sets the reference to <code>queryHelper</code>'s DAO.
     * 
     * @param queryHelperDao
     *            The queryHelperDao
     */
    public void setQueryHelperDao(
            QueryHelperDao queryHelperDao) {
        this.queryHelperDao = queryHelperDao;
    }

}
