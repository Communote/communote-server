package com.communote.server.persistence.helper.dao;

import java.util.ArrayList;

import org.apache.commons.lang.time.StopWatch;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.communote.common.util.PageableList;
import com.communote.server.core.common.util.LogHelper;
import com.communote.server.core.filter.ResultSpecification;


/**
 * Query helper class
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class QueryHelper {

    /**
     * Number of pages that are shown (if available) after the current pages
     */
    public final static int NUMBER_MORE_PAGES = 10;

    /**
     * Get a query
     * 
     * @param session
     *            the session
     * @param mainQuery
     *            the main query string
     * @param filterSpecification
     *            the filter specification
     * @param params
     *            the parameters
     * @return the configured query
     */
    public static Query getQuery(Session session, String mainQuery,
            ResultSpecification filterSpecification, Object... params) {

        Query query = session.createQuery(mainQuery);

        ResultSpecificationHelper.configureQuery(query, filterSpecification);
        ResultSpecificationHelper.setParameters(query, params);

        return query;
    }

    /**
     * Execute the query and also retrieve the overall count to successfully build the pageable list
     * 
     * @param <I>
     *            the type of the item (anyway no check will be done)
     * @param query
     *            the configured query to execute
     * @param resultSpecification
     *            the {@link ResultSpecification} containing offset and overall count
     * @param logPerformance
     *            true if the performance should be logged
     * @return the pageable list with the result values
     */
    public static <I extends Object> PageableList<I> queryAsPageableList(Query query,
            ResultSpecification resultSpecification, boolean logPerformance) {

        StopWatch stopWatch = new StopWatch();
        if (logPerformance) {
            stopWatch.start();
        }
        if (logPerformance) {
            stopWatch.stop();
            LogHelper.logPerformance("QueryHelper#query.scroll", stopWatch);
            stopWatch.reset();
        }

        int numberOfElements = Integer.MAX_VALUE;
        int offset = 0;
        int initSize = 10;
        if (resultSpecification != null) {
            offset = resultSpecification.getOffset();
            if (resultSpecification.getNumberOfElements() > 0) {
                numberOfElements = resultSpecification.getNumberOfElements();
                initSize = numberOfElements;
                int maxResults;
                if (resultSpecification.getCheckAtLeastMoreResults() < 0) {
                    // legacy behavior: fetch as many results as are required to fill
                    // NUMBER_MORE_PAGES pages with numberOfElements. Add 1 to find out whether
                    // would be another page. Useful for creating page navigations.
                    maxResults = numberOfElements * NUMBER_MORE_PAGES + 1;
                } else {
                    // fetch only the defined number of additional results
                    maxResults = numberOfElements
                            + resultSpecification.getCheckAtLeastMoreResults();
                }
                // respect the offset
                query.setMaxResults(maxResults + offset);
            }
        }

        if (logPerformance) {
            stopWatch.start();
        }
        ScrollableResults scrollableResults = query.scroll(ScrollMode.SCROLL_INSENSITIVE);
        PageableList<I> pageableList = new PageableList<I>(new ArrayList<I>(initSize));
        // iterate through the result to get the elements requested
        if (scrollableResults.setRowNumber(offset)) {
            if (logPerformance) {
                stopWatch.stop();
                LogHelper.logPerformance("QueryHelper#scrollableResults.setRowNumber"
                        + numberOfElements, stopWatch);
                stopWatch.reset();
                stopWatch.start();
            }

            for (int i = 0; i < numberOfElements; i++) {
                Object item = scrollableResults.get(0);
                pageableList.add((I) item);

                if (!scrollableResults.next()) {
                    break;
                }
            }

            if (logPerformance) {
                stopWatch.stop();
                LogHelper.logPerformance("QueryHelper#scrollableResults.iterate_"
                        + numberOfElements, stopWatch);
                stopWatch.reset();
            }
        }

        if (logPerformance) {
            stopWatch.reset();
            stopWatch.start();
        }

        // get the overall count; count starts with 0
        scrollableResults.last();
        int minNumberOfElements = scrollableResults.getRowNumber() + 1;
        if (logPerformance) {
            stopWatch.stop();
            LogHelper.logPerformance("QueryHelper#scrollableResults.last", stopWatch);
            stopWatch.reset();
        }
        pageableList.setOffset(offset);
        pageableList.setMinNumberOfElements(minNumberOfElements);
        return pageableList;
    }

    /**
     * Helper class
     */
    private QueryHelper() {

    }
}
