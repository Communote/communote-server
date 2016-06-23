package com.communote.server.core.vo.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.communote.common.util.PageableList;

/**
 * Query Result Converter generates from temporary object by the query definition the list object
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <I>
 *            The object type of the returning temporary object
 * 
 * @param <O>
 *            The returning list object
 */

public abstract class QueryResultConverter<I, O> {

    /**
     * Converts the temporary object of the query to a list object
     * 
     * @param source
     *            The temporary object returned by the executed query
     * @param target
     *            The converted list object
     * @return A boolean, which decide to add or remove the object from the list.
     */
    public abstract boolean convert(I source, O target);

    /**
     * Converts the temporary object of the query to a list object
     * 
     * @param queryResult
     *            the temporary object returned by the executed query
     * @return the pageable list object
     */
    public PageableList<O> convert(PageableList<I> queryResult) {
        PageableList<O> finalResult = new PageableList<O>(new ArrayList<O>(queryResult.size()));
        finalResult.setMinNumberOfElements(queryResult.getMinNumberOfElements());
        finalResult.setOffset(queryResult.getOffset());
        for (I item : queryResult) {
            O listObject = create();
            // TODO should we remove those that failed from the minNumberOfElements?
            if (convert(item, listObject)) {
                finalResult.add(listObject);
            }
        }
        return finalResult;
    }

    /**
     * Converts the temporary object to a list object
     * 
     * @param results
     *            the temporary object returned by the executed query
     * @return the converted list
     */
    public List<O> convertCollection(Collection<I> results) {
        return convert(new PageableList<>(results));
    }

    /**
     * Creates a new pageable list object to fill
     * 
     * @return pageable list object
     */

    public abstract O create();
}
