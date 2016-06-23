package com.communote.server.core.vo.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.MatchMode;

import com.communote.common.paging.PageInformation;
import com.communote.common.util.PageableList;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.api.core.config.database.DatabaseConfiguration;
import com.communote.server.core.filter.ResultSpecification;

/**
 * <p>
 * Abstract implementation for query parameters. Can be used to configure Communote queries.
 * </p>
 *
 * <b>Note:</b> QueryParameters should not be reused for multiple queries, as they can create query
 * parameters on the fly. This could result in wrong queries.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public abstract class QueryParameters {

    /**
     * Enum for possible orderings in "ORDER BY" clauses.
     */
    public static enum OrderDirection {
        /** Ascending order. */
        ASCENDING(" asc"),
        /** Descending order. */
        DESCENDING(" desc");

        /**
         * Method to append the given orderBy clause with the new ordering.
         *
         * @param orderBy
         *            The current orderBy clause
         * @param field
         *            The field to order.
         * @param orderDirection
         *            The direction to order in. If this is null, the method will return without
         *            doing anything.
         * @return The new orderBy clause.
         */
        public static String appendOrderBy(String orderBy, String field,
                OrderDirection orderDirection) {
            if (orderDirection == null) {
                return orderBy;
            }
            orderBy = orderBy == null ? "" : orderBy + ",";
            return orderBy + field + orderDirection;
        }

        private final String value;

        /**
         * Constructor.
         *
         * @param value
         *            To be rendered into the order by clause.
         */
        private OrderDirection(String value) {
            this.value = value;
        }

        /**
         * @return A value, which can directly be rendered into the order by clause.
         */
        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * sort constant for descending
     */
    public static final String SORT_DESCENDING = " desc ";

    /**
     * sort constant for ascending
     */
    public static final String SORT_ASCENDING = " asc ";

    /**
     * Get the page information about the current page in relation to the overall count
     *
     * @param resultList
     *            the result list with the counts
     * @param resultSpecification
     *            the result specification
     * @param pagingInterval
     *            the interval of how many pages to show at one time
     * @return the page information
     */
    public static PageInformation getPageInformation(PageableList<?> resultList,
            ResultSpecification resultSpecification, int pagingInterval) {
        PageInformation pageInformation;
        if (resultSpecification == null) {
            pageInformation = new PageInformation(0, resultList.size(),
                    resultList.getMinNumberOfElements(), pagingInterval);
        } else {
            pageInformation = new PageInformation(resultSpecification.getOffset(),
                    resultSpecification.getNumberOfElements(), resultList.getMinNumberOfElements(),
                    pagingInterval);
        }
        return pageInformation;

    }

    private String languageCode = Locale.ENGLISH.getLanguage();

    private ResultSpecification resultSpecification;

    /**
     * the sortstring
     */
    private StringBuilder sortString;

    private final Map<String, Object> queryParameters = new HashMap<String, Object>();

    private boolean limitResultSet = true;

    /**
     * If true the computation of the limitation based on the note list will avoid duplicates by
     * using distinct. If false distinct will not be used, being somewhat faster. Default is true.
     */
    private boolean limitResultSetAvodingDuplicates = false;

    /**
     * Adds a named query parameter that can be referenced when rendering the query.
     *
     * @param value
     *            The value of the parameter.
     * @return The name for this parameter to be used within the query including a leading ":".
     */
    public String addParameter(String value) {
        String name = ("queryParameter" + value.hashCode()).replace("-", "A");
        queryParameters.put(name, value);
        return ":" + name;
    }

    /**
     * Adds a named query parameter that can be referenced when rendering the query.
     *
     * @param name
     *            The name of the parameter.
     * @param value
     *            The value of the parameter.
     */
    public void addParameter(String name, Object value) {
        queryParameters.put(name, value);
    }

    /**
     * add a sort field
     *
     * @param resultObjectPrefix
     *            the result prefix
     * @param fieldName
     *            the name of the field to sort
     * @param sortDirection
     *            the sort direction
     * @deprecated Use a sort field in the parameters and do the rendering of the sortfield in the
     *             associated query
     */
    @Deprecated
    protected void addSortField(String resultObjectPrefix, String fieldName, String sortDirection) {
        addSortField(resultObjectPrefix, fieldName, sortDirection, null);
    }

    /**
     * add a sort field
     *
     * @param resultObjectPrefix
     *            the result prefix
     * @param fieldName
     *            the name of the field to sort
     * @param sortDirection
     *            the sort direction
     * @param aggregateFunction
     *            the aggregate function to be performed on the sort field
     * @deprecated Use a sort field in the parameters and do the rendering of the sortfield in the
     *             associated query
     */
    @Deprecated
    protected void addSortField(String resultObjectPrefix, String fieldName, String sortDirection,
            String aggregateFunction) {
        if (sortString != null) {
            sortString.append(", ");
        } else {
            sortString = new StringBuilder();
        }
        if (aggregateFunction != null) {
            sortString.append(aggregateFunction);
            sortString.append("(");
        }
        sortString.append(resultObjectPrefix);
        if (!StringUtils.isBlank(resultObjectPrefix) && !resultObjectPrefix.endsWith(".")) {
            sortString.append(".");
        }
        sortString.append(fieldName);
        if (aggregateFunction != null) {
            sortString.append(") ");
        }
        sortString.append(sortDirection);
    }

    /**
     * Creates an array of parameter names to be used in a query that searches for the provided
     * searchValues.
     *
     * @param paramPrefix
     *            a prefix to be used for building the parameter names. Callers should assure that
     *            the prefix is unique within the query.
     * @param searchValues
     *            the search values for which the parameter names should be created. Blank values
     *            will result in adding null to the parameter names at the index position of that
     *            search value
     * @return an array with parameter names. The returned array will contain null at indexes at
     *         which the searchValue is blank. The result will be null if searchValues is null.
     */
    protected String[] createParameterNamesForSearch(String paramPrefix, String[] searchValues) {
        if (searchValues == null) {
            return null;
        }
        List<String> params = new ArrayList<String>();
        for (int i = 0; i < searchValues.length; i++) {
            if (StringUtils.isNotBlank(searchValues[i])) {
                params.add(paramPrefix + i);
            } else {
                params.add(null);
            }
        }
        return params.toArray(new String[params.size()]);
    }

    /**
     *
     * @return the database configuration
     */
    public DatabaseConfiguration getDatabaseConfiguration() {
        DatabaseConfiguration databaseConfiguration = CommunoteRuntime.getInstance()
                .getConfigurationManager().getDatabaseConfiguration();
        return databaseConfiguration;
    }

    /**
     * @return the languageCode
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * Get the page information about the current page in relation to the overall count
     *
     * @param resultList
     *            the result list with the counts
     * @param pagingInterval
     *            the interval of how many pages to show at one time
     * @return the page information
     */
    public PageInformation getPageInformation(PageableList<?> resultList, int pagingInterval) {
        return QueryParameters.getPageInformation(resultList, resultSpecification, pagingInterval);
    }

    /**
     * Get the parameters of this instances, the names are the keys
     *
     * @return The parameters
     */
    public Map<String, Object> getParameters() {
        return new HashMap<String, Object>(this.queryParameters);
    }

    /**
     * Get the result specification to be used
     *
     * @return the {@link ResultSpecification}
     */
    public ResultSpecification getResultSpecification() {
        return resultSpecification;
    }

    /**
     * get the sort string for the order by clause
     *
     * @return the sort string
     */
    public String getSortString() {
        return sortString == null ? null : sortString.toString();
    }

    /**
     * @return the limitResultSet
     */
    public boolean isLimitResultSet() {
        return limitResultSet;
    }

    /**
     *
     * @return If true the computation of the limitation based on the note list will avoid
     *         duplicates by using distinct. If false distinct will not be used, being somewhat
     *         faster. Default is true.
     */
    public boolean isLimitResultSetAvodingDuplicates() {
        return limitResultSetAvodingDuplicates;
    }

    /**
     * @return true if the transform list item function should be used after retrieving the objects
     * @deprecated Use {@link com.communote.common.converter.Converter} instead.
     */
    @Deprecated
    public boolean needTransformListItem() {
        return false;
    }

    /**
     * Put a parameter, if set, in the map
     *
     * @param params
     *            the map with the parameters
     * @param paramName
     *            the parameter name
     * @param paramValue
     *            the parameter value
     */
    protected void putParameter(Map<String, Object> params, String paramName, Object paramValue) {
        if (paramValue != null) {
            params.put(paramName, paramValue);
        }
    }

    /**
     * Put the parameter names and values for a search into the parameter map.
     *
     * @param paramMap
     *            the parameter map to extend
     * @param paramNames
     *            the names of the parameters to be added to the map. Blank names will be ignored.
     *            It is expected that the names and the values array are consistent with respect to
     *            size and blank values. So if for instance a value is blank the name at the same
     *            array position must also be blank or null. To achieve this the
     *            {@link #createParameterNamesForSearch(String, String[])} can be invoked on the
     *            values array.
     * @param searchValues
     *            an array of values to be passed the parameterized query
     * @param matchMode
     *            the mode for matching against the values
     */
    protected void putParametersForSearch(Map<String, Object> paramMap, String[] paramNames,
            String[] searchValues, MatchMode matchMode) {
        this.putParametersForSearch(paramMap, paramNames, searchValues, matchMode, false);
    }

    /**
     * Put the parameter names and values for a search into the parameter map.
     *
     * @param paramMap
     *            the parameter map to extend
     * @param paramNames
     *            the names of the parameters to be added to the map. Blank names will be ignored.
     *            It is expected that the names and the values array are consistent with respect to
     *            size and blank values. So if for instance a value is blank the name at the same
     *            array position must also be blank or null. To achieve this the
     *            {@link #createParameterNamesForSearch(String, String[])} can be invoked on the
     *            values array.
     * @param searchValues
     *            an array of values to be passed the parameterized query
     * @param matchMode
     *            the mode for matching against the values
     * @param doNotUseFulltext
     *            true if like wildcard syntax should be used even if fulltext would be available
     */
    protected void putParametersForSearch(Map<String, Object> paramMap, String[] paramNames,
            String[] searchValues, MatchMode matchMode, boolean doNotUseFulltext) {

        DatabaseConfiguration databaseConfiguration = getDatabaseConfiguration();

        if (paramNames == null) {
            return;
        }
        for (int i = 0; i < paramNames.length; i++) {
            String paramName = paramNames[i];
            if (StringUtils.isNotBlank(paramName)) {
                paramMap.put(paramName, databaseConfiguration.getFulltextParameterValue(
                        searchValues[i], matchMode, doNotUseFulltext));
            }
        }
    }

    /**
     * @param languageCode
     *            the languageCode to set
     */
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    /**
     * @param limitResultSet
     *            the limitResultSet to set
     */
    public void setLimitResultSet(boolean limitResultSet) {
        this.limitResultSet = limitResultSet;
    }

    /**
     * @param limitResultSetAvodingDuplicates
     *            If true the computation of the limitation based on the note list will avoid
     *            duplicates by using distinct. If false distinct will not be used, being somewhat
     *            faster. Default is true.
     */
    public void setLimitResultSetAvodingDuplicates(boolean limitResultSetAvodingDuplicates) {
        this.limitResultSetAvodingDuplicates = limitResultSetAvodingDuplicates;
    }

    /**
     * Set the result specification
     *
     * @param resultSpecification
     *            the result specification
     */
    public void setResultSpecification(ResultSpecification resultSpecification) {
        this.resultSpecification = resultSpecification;
    }

    /**
     * transform the list item after it has been loaded and filled by the query definition <br>
     * <br>
     *
     * @param resultItem
     *            the item to transform
     * @return the transformed list item
     * @deprecated Use {@link com.communote.common.converter.Converter} instead.
     */
    @Deprecated
    public IdentifiableEntityData transformResultItem(Object resultItem) {
        throw new UnsupportedOperationException(
                "Operation not supported. Implement or let needTransformListItem() return false");
    }

    /**
     * Validates the given String, if this is not null or 0.
     *
     * @param value
     *            Value to check.
     * @return True, if the given String is not null or 0, otherwise false.
     */
    public boolean validateLong(String value) {
        return value != null && !value.equals("0");
    }

}
