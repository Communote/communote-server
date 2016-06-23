package com.communote.server.core.vo.query.config;

import java.util.Map;

import com.communote.common.util.ParameterHelper;
import com.communote.server.core.filter.ResultSpecification;


/**
 * Configurator for query.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class QueryParametersConfigurator {
    private final QueryParametersParameterNameProvider parameterNameProvider;

    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_CHECK_AT_LEAST_MORE_RESULTS = -1;
    private static final int MAX_CHECK_AT_LEAST_MORE_RESULTS = 100;
    private int defaultMaxCount = 10;

    /**
     * @param parameterNameProvider
     *            Name provider for getting parameters.
     */
    public QueryParametersConfigurator(QueryParametersParameterNameProvider parameterNameProvider) {
        if (parameterNameProvider == null) {
            throw new IllegalArgumentException("parameterNameProvider cannot be null!");
        }
        this.parameterNameProvider = parameterNameProvider;
    }

    /**
     * @param parameterNameProvider
     *            Name provider for getting parameters.
     * @param defaultMaxCount
     *            the default value to use if maxCount parameter is not set
     */
    public QueryParametersConfigurator(QueryParametersParameterNameProvider parameterNameProvider,
            int defaultMaxCount) {
        if (parameterNameProvider == null) {
            throw new IllegalArgumentException("parameterNameProvider cannot be null!");
        }
        this.parameterNameProvider = parameterNameProvider;
        this.defaultMaxCount = defaultMaxCount;
    }

    /**
     * Returns the current name provider.
     * 
     * @return The name provider.
     */
    protected QueryParametersParameterNameProvider getParameterNameProvider() {
        return parameterNameProvider;
    }

    /**
     * Get a result specification using the parameters offset and max count.
     * 
     * @param parameters
     *            Map containing all parameters.
     * @return The result specification initialized by the parameters
     */
    public ResultSpecification getResultSpecification(Map<String, ? extends Object> parameters) {
        return getResultSpecification(parameters, DEFAULT_OFFSET, defaultMaxCount);
    }

    /**
     * Get a result specification using the parameters offset and max count.
     * 
     * @param parameters
     *            Map containing all parameters.
     * @param defaultOffset
     *            value to use if offset is unset
     * @param defaultMaxCount
     *            value to use if maxCount is unset
     * @return The result specification initialized by the parameters
     */
    protected ResultSpecification getResultSpecification(Map<String, ? extends Object> parameters,
            int defaultOffset, int defaultMaxCount) {
        int offset = ParameterHelper.getParameterAsInteger(parameters, getParameterNameProvider()
                .getNameForOffset(), defaultOffset);
        int maxCount = ParameterHelper.getParameterAsInteger(parameters, getParameterNameProvider()
                .getNameForMaxCount(), defaultMaxCount);
        ResultSpecification resultSpecification = new ResultSpecification(offset, maxCount);
        int checkAtLeastMoreResults = ParameterHelper.getParameterAsInteger(parameters,
                getParameterNameProvider().getNameForCheckAtLeastMoreResults(),
                DEFAULT_CHECK_AT_LEAST_MORE_RESULTS);
        if (checkAtLeastMoreResults > MAX_CHECK_AT_LEAST_MORE_RESULTS) {
            checkAtLeastMoreResults = MAX_CHECK_AT_LEAST_MORE_RESULTS;
        }
        resultSpecification.setCheckAtLeastMoreResults(checkAtLeastMoreResults);
        return resultSpecification;
    }
}
