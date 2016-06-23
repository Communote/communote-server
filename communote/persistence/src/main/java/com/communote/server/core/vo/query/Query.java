package com.communote.server.core.vo.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.communote.common.util.PageableList;

/**
 * Abstract implementation for Communote queries.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <I>
 *            The Query Instance which is valid for this definition
 * @param <R>
 *            Type of the resulting items.
 */
public abstract class Query<R, I extends QueryParameters> {

    protected static final String AND = " and ";
    protected static final String OR = " or ";

    /**
     * The constructor doing some initialization
     */
    public Query() {
        setupQueries();
    }

    /**
     * Build the query
     * 
     * @param queryInstance
     *            The query instance containing the necessary parameters and configuration
     * @return the query string with named parameters
     */
    public abstract String buildQuery(I queryInstance)
    /* throws UnexpectedAuthorizationException */;

    /**
     * Create an empty instance for this definition
     * 
     * @return the instance created
     */
    public abstract I createInstance();

    /**
     * This function is called after executing the query for further augmentations. By default this
     * function just returns the result given. However the result object can be changed to different
     * instances.
     * 
     * @param queryParameters
     *            the query instance
     * @param result
     *            the result containing the items returned by executed query
     * @return the maybe new list
     * @deprecated Use {@link com.communote.common.converter.Converter} instead.
     */
    @Deprecated
    public PageableList postQueryExecution(I queryParameters, PageableList result) {
        if (queryParameters.needTransformListItem()) {
            List tempList = new ArrayList(result.size());
            tempList.addAll(result);

            result.clear();
            for (Iterator iterator = tempList.iterator(); iterator.hasNext();) {
                Object item = iterator.next();
                result.add(queryParameters.transformResultItem(item));
            }
        }
        return result;
    }

    /**
     * Render the order by clause
     * 
     * @param mainQuery
     *            the main query
     * @param queryInstance
     *            the query instance
     */
    protected void renderOrderbyClause(StringBuilder mainQuery, I queryInstance) {
        String order = queryInstance.getSortString();
        if (StringUtils.isNotBlank(order)) {
            mainQuery.append(" order by ");
            mainQuery.append(order);
        }
    }

    /**
     * Render a search condition that will search in one or several fields for one ore more values.
     * The fields are OR and the values are AND connected. Thus, the resulting query will search for
     * items which have for each value at least one field that matches it.
     * 
     * @param whereQuery
     *            the where part of the query to extend
     * @param fields
     *            the fields that should be searched
     * @param paramNames
     *            the parameter names that substitute the search values. Blank values will be
     *            skipped.
     * @param encloseInParenthesis
     *            whether to enclose the rendered search condition in parenthesis
     * @param useFulltext
     *            true if the fulltext search should be used (if the database is supporting it)
     * 
     */
    protected void renderSearch(StringBuilder whereQuery, String[] fields, String[] paramNames,
            boolean encloseInParenthesis, boolean useFulltext) {
        String myPrefix = StringUtils.EMPTY;
        StringBuilder renderSearchQuery = new StringBuilder();
        for (String paramName : paramNames) {
            // ignore blank params
            if (StringUtils.isBlank(paramName)) {
                continue;
            }
            renderSearchQuery.append(myPrefix);

            renderSearchQuery.append("(");

            String fieldPrefix = StringUtils.EMPTY;
            for (String field : fields) {
                if (useFulltext) {
                    renderSearchQuery.append(fieldPrefix);
                    renderSearchQuery.append("fulltext(");
                    renderSearchQuery.append(field);
                    renderSearchQuery.append(", :" + paramName);
                    renderSearchQuery.append(") = true");
                } else {
                    renderSearchQuery.append(fieldPrefix
                            + " lower(" + field + ") like lower(:" + paramName + ") ");
                }
                fieldPrefix = OR;
            }

            renderSearchQuery.append(")");
            myPrefix = AND;
        }
        if (renderSearchQuery.length() > 0) {
            if (encloseInParenthesis) {
                whereQuery.append("(");
                whereQuery.append(renderSearchQuery);
                whereQuery.append(")");
            } else {
                whereQuery.append(renderSearchQuery);
            }
        }
    }

    /**
     * Setup the query constants. Do not forget to call super.setupQueries
     */
    protected abstract void setupQueries();

}
