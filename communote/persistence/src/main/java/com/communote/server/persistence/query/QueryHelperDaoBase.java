package com.communote.server.persistence.query;

import com.communote.server.core.common.exceptions.UnexpectedAuthorizationException;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.query.QueryHelper</code>.
 * </p>
 * 
 * @see com.communote.server.persistence.query.QueryHelper
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class QueryHelperDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.query.QueryHelperDao {

    /**
     * @see com.communote.server.persistence.query.QueryHelperDao#executeQuery(com.communote.server.core.vo.query.Query,
     *      com.communote.server.core.vo.query.QueryParameters)
     */
    @Override
    public com.communote.common.util.PageableList executeQuery(
            final com.communote.server.core.vo.query.Query query,
            final com.communote.server.core.vo.query.QueryParameters queryParameters)
    /* throws UnexpectedAuthorizationException */{
        if (query == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.query.QueryHelperDao.executeQuery(Query query, QueryParameters queryParameters) - 'query' can not be null");
        }
        if (queryParameters == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.query.QueryHelperDao.executeQuery(Query query, QueryParameters queryParameters) - 'queryParameters' can not be null");
        }
        try {
            return this.handleExecuteQuery(query, queryParameters);
        } catch (UnexpectedAuthorizationException uae) {
            throw uae;
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.query.QueryHelperDao.executeQuery(Query query, QueryParameters queryParameters)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.query.QueryHelperDao#executeQuery(com.communote.server.core.vo.query.Query,
     *      com.communote.server.core.vo.query.QueryParameters,
     *      com.communote.server.core.vo.query.QueryResultConverter,
     *      com.communote.server.core.filter.ResultSpecification)
     */
    @Override
    public com.communote.common.util.PageableList executeQuery(
            final com.communote.server.core.vo.query.Query query,
            final com.communote.server.core.vo.query.QueryParameters queryParameters,
            final com.communote.server.core.vo.query.QueryResultConverter resultConverter,
            final com.communote.server.core.filter.ResultSpecification resultSpecification)
    /* throws UnexpectedAuthorizationException */{
        if (query == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.query.QueryHelperDao.executeQuery(Query query, QueryParameters queryParameters, QueryResultConverter resultConverter, ResultSpecification resultSpecification) - 'query' can not be null");
        }
        if (queryParameters == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.query.QueryHelperDao.executeQuery(Query query, QueryParameters queryParameters, QueryResultConverter resultConverter, ResultSpecification resultSpecification) - 'queryParameters' can not be null");
        }
        if (resultSpecification == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.query.QueryHelperDao.executeQuery(Query query, QueryParameters queryParameters, QueryResultConverter resultConverter, ResultSpecification resultSpecification) - 'resultSpecification' can not be null");
        }
        try {
            return this.handleExecuteQuery(query, queryParameters, resultConverter,
                    resultSpecification);
        } catch (UnexpectedAuthorizationException uae) {
            throw uae;
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.query.QueryHelperDao.executeQuery(Query query, QueryParameters queryParameters, QueryResultConverter resultConverter, ResultSpecification resultSpecification)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.query.QueryHelperDao#executeQueryComplete(com.communote.server.core.vo.query.Query,
     *      com.communote.server.core.vo.query.QueryParameters)
     */
    @Override
    public java.util.List executeQueryComplete(
            final com.communote.server.core.vo.query.Query query,
            final com.communote.server.core.vo.query.QueryParameters queryParameters) {
        if (query == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.query.QueryHelperDao.executeQueryComplete(Query query, QueryParameters queryParameters) - 'query' can not be null");
        }
        if (queryParameters == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.query.QueryHelperDao.executeQueryComplete(Query query, QueryParameters queryParameters) - 'queryParameters' can not be null");
        }
        try {
            return this.handleExecuteQueryComplete(query, queryParameters);
        } catch (UnexpectedAuthorizationException uae) {
            throw uae;
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.query.QueryHelperDao.executeQueryComplete(Query query, QueryParameters queryParameters)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #executeQuery(com.communote.server.core.vo.query.Query, com.communote.server.core.vo.query.QueryParameters)}
     */
    protected abstract com.communote.common.util.PageableList handleExecuteQuery(
            com.communote.server.core.vo.query.Query query,
            com.communote.server.core.vo.query.QueryParameters queryParameters) /*
                                                                                 * throws
                                                                                 * UnexpectedAuthorizationException
                                                                                 */;

    /**
     * Performs the core logic for
     * {@link #executeQuery(com.communote.server.core.vo.query.Query, com.communote.server.core.vo.query.QueryParameters, com.communote.server.core.vo.query.QueryResultConverter, com.communote.server.core.filter.ResultSpecification)}
     */
    protected abstract com.communote.common.util.PageableList handleExecuteQuery(
            com.communote.server.core.vo.query.Query query,
            com.communote.server.core.vo.query.QueryParameters queryParameters,
            com.communote.server.core.vo.query.QueryResultConverter resultConverter,
            com.communote.server.core.filter.ResultSpecification resultSpecification)/*
                                                                                      * throws
                                                                                      * UnexpectedAuthorizationException
                                                                                      */;

    /**
     * Performs the core logic for
     * {@link #executeQueryComplete(com.communote.server.core.vo.query.Query, com.communote.server.core.vo.query.QueryParameters)}
     */
    protected abstract java.util.List handleExecuteQueryComplete(
            com.communote.server.core.vo.query.Query query,
            com.communote.server.core.vo.query.QueryParameters queryParameters)/*
                                                                                * throws
                                                                                * UnexpectedAuthorizationException
                                                                                */;

}