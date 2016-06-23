package com.communote.server.core.vo.query;

import com.communote.common.util.PageableList;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <I>
 *            The object type of the returning temporary object
 * 
 * @param <O>
 *            The returning list object
 */
public abstract class FilterQueryResultConverter<I, O> extends QueryResultConverter<I, O> {

    private final QueryResultConverter<I, O> queryResultConverter;

    /**
     * Constructor which set the queryResultConverter
     * 
     * @param queryResultConverter
     *            The converter which need a filter
     */
    public FilterQueryResultConverter(QueryResultConverter<I, O> queryResultConverter) {
        this.queryResultConverter = queryResultConverter;
    }

    @Override
    public boolean convert(I queryResult, O finalResult) {
        if (queryResultConverter.convert(queryResult, finalResult)) {
            return postConvert(queryResult, finalResult);
        }
        return false;
    }

    @Override
    public PageableList<O> convert(PageableList<I> queryResult) {
        return postConvert(queryResultConverter.convert(queryResult));
    }

    @Override
    public O create() {
        return queryResultConverter.create();
    }

    /**
     * @param queryResult
     *            The temporary object returned by the executed query
     * @param finalResult
     *            The converted list object
     * @return By default true
     */
    protected boolean postConvert(I queryResult, O finalResult) {
        return true;
    }

    /**
     * 
     * @param finalResult
     *            the temporary object returned by the executed query
     * @return The finalResult
     */
    protected PageableList<O> postConvert(PageableList<O> finalResult) {
        return finalResult;
    }

}
