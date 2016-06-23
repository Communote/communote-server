package com.communote.server.web.fe.widgets;

import java.util.Map;

import com.communote.common.paging.PageInformation;
import com.communote.common.util.PageableList;
import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.SortedResultSpecification;
import com.communote.server.core.vo.query.QueryParameters;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersConfigurator;
import com.communote.server.widgets.AbstractMultipleResultWidget;

/**
 * Abstract widget to be used by Widgets which only return a single result
 * 
 * @param <I>
 *            the type of {@link IdentifiableEntityData} the widget will return
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractPagedListWidget<I> extends AbstractMultipleResultWidget<I> implements
        PagedWidget<I> {
    private final static FilterWidgetParameterNameProvider NAME_PROVIDER = FilterWidgetParameterNameProvider.INSTANCE;
    /**
     * Key to be used in the response metadata to denote whether there are more elements available
     * that can be fetched with an additional query.
     */
    protected static final String METADATA_KEY_MORE_AVAILABLE = "moreElementsAvailable";
    /**
     * Key to be used in the response metadata to denote the number of elements contained in the
     * response.
     */
    protected static final String METADATA_KEY_ELEMENTS_CONTAINED = "numberOfElementsContained";
    private PageInformation pageInformation;

    /**
     * Parameter for the paging interval
     */
    public static final String PARAM_PAGING_INTERVAL = "pagingInterval";

    /**
     * Returns 'default'
     * 
     * @return the message key suffix
     * @see PagedWidget#getDefaultPagingMessageKeySuffix()
     */
    @Override
    public String getDefaultPagingMessageKeySuffix() {
        return "default";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInformation getPageInformation() {
        return pageInformation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getParameterDescriptions() {
        return null;
    }

    /**
     * Get a result specification using the parameters offset and maxcount
     * 
     * @return The result specification initialized by the parameters
     */
    protected ResultSpecification getResultSpecification() {
        QueryParametersConfigurator configurer = new QueryParametersConfigurator(NAME_PROVIDER);
        ResultSpecification resultSpecification = configurer
                .getResultSpecification(getParameters());
        return resultSpecification;
    }

    /**
     * Get a sorted specification using the parameters offset and max count
     * 
     * @return The sorted result specification initialized by the parameters
     */
    protected SortedResultSpecification getSortedResultSpecification() {
        int offset = getIntParameter(NAME_PROVIDER.getNameForOffset(), 0);
        int maxCount = getIntParameter(NAME_PROVIDER.getNameForMaxCount(), 0);

        SortedResultSpecification resultSpecification = new SortedResultSpecification(offset,
                maxCount);
        return resultSpecification;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected abstract PageableList<I> handleQueryList();

    @Override
    public Object handleRequest() {
        PageableList<I> result = handleQueryList();
        this.setCommonResponseMetadata(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        setParameter(NAME_PROVIDER.getNameForOffset(), "0");
        setParameter(NAME_PROVIDER.getNameForMaxCount(), "10");
        setParameter(PARAM_PAGING_INTERVAL, "5");

    }

    /**
     * Set common meta data extracted from the query result. This includes
     * {@link #METADATA_KEY_MORE_AVAILABLE} and {@link #METADATA_KEY_ELEMENTS_CONTAINED}
     * 
     * @param result
     *            the query result
     */
    protected void setCommonResponseMetadata(PageableList<?> result) {
        if (result != null) {
            setResponseMetadata(METADATA_KEY_MORE_AVAILABLE,
                    result.getMinNumberOfAdditionalElements() > 0);
            setResponseMetadata(METADATA_KEY_ELEMENTS_CONTAINED,
                    result.size());
            if (result.getOffset() == 0 && result.size() == 0) {
                setResponseMetadata(METADATA_KEY_NO_CONTENT, true);
            }
        } else {
            setResponseMetadata(METADATA_KEY_ELEMENTS_CONTAINED, 0);
        }
    }

    /**
     * Set the Page Information
     * 
     * @param result
     *            the list
     * @param resultSpecification
     *            the result specification
     */
    protected void setPageInformation(PageableList<?> result,
            ResultSpecification resultSpecification) {
        int pagingInterval = getIntParameter(PARAM_PAGING_INTERVAL, 5);
        pageInformation = QueryParameters.getPageInformation(result, resultSpecification,
                pagingInterval);

    }

    /**
     * @param pageInformation
     *            the pageInformation to set
     */
    protected void setPageInformation(PageInformation pageInformation) {
        this.pageInformation = pageInformation;
    }

    /**
     * Set the Page Information
     * 
     * @param queryParameters
     *            the instance
     * @param result
     *            the list
     */
    protected void setPageInformation(QueryParameters queryParameters,
            PageableList<? extends IdentifiableEntityData> result) {
        int pagingInterval = getIntParameter(PARAM_PAGING_INTERVAL, 5);
        pageInformation = queryParameters.getPageInformation(result, pagingInterval);
    }
}
