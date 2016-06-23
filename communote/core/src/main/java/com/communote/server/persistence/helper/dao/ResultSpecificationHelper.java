package com.communote.server.persistence.helper.dao;

import java.util.List;

import org.hibernate.Query;

import com.communote.server.core.filter.ResultSpecification;


/**
 * Helper for <code>FilterResultSpecification</code> stuff
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class ResultSpecificationHelper {

    /**
     * Configure the query by setting the first and max result properties
     * 
     * @param query
     *            The query to configure
     * @param params
     *            The parameters to set at the query. Can be null.
     * @param filterSpecification
     *            The filter specification to use, if null nothing happens here
     */
    public static void configureQuery(Query query, List params,
            ResultSpecification filterSpecification) {
        if (filterSpecification != null) {
            query.setFirstResult(filterSpecification.getOffset());
            if (filterSpecification.getNumberOfElements() > 0) {
                query.setMaxResults(filterSpecification.getNumberOfElements());
            }
        }
        setParameters(query, params);

        query.setReadOnly(true);
    }

    /**
     * Configure the query by setting the first and max result properties
     * 
     * @param query
     *            The query to configure
     * @param filterSpecification
     *            The filter specification to use, if null nothing happens here
     */
    public static void configureQuery(Query query, ResultSpecification filterSpecification) {
        configureQuery(query, null, filterSpecification);
    }

    /**
     * Set the parameters in the query
     * 
     * @param query
     *            The query object with the parameters
     * @param params
     *            The parameters to set
     */
    public static void setParameters(Query query, List params) {
        if (params != null) {
            int i = 0;
            for (Object param : params) {
                query.setParameter(i++, param);
            }
        }
    }

    /**
     * Set the parameters in the query
     * 
     * @param query
     *            The query object with the parameters
     * @param params
     *            The parameters to set
     */
    public static void setParameters(Query query, Object... params) {
        if (params != null) {
            int i = 0;
            for (Object param : params) {
                query.setParameter(i++, param);
            }
        }
    }

    /**
     * Helper class
     */
    private ResultSpecificationHelper() {
    }
}
