package com.communote.server.web.api.service.filter;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.communote.common.util.PageableList;
import com.communote.common.util.Pair;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.core.vo.query.TaggingCoreItemQueryDefinition;
import com.communote.server.core.vo.query.TimelineQueryParameters;
import com.communote.server.core.vo.query.config.FilterApiParameterNameProvider;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.web.api.service.ApiResultApiController;
import com.communote.server.web.api.service.IllegalRequestParameterException;
import com.communote.server.web.api.to.ApiResult;


/**
 * Base class for all filter api controllers to support query instance configuration
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <I>
 *            The query parameters type
 * @param <D>
 *            The query type
 * @param <L>
 *            Type of the returning list item.
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public abstract class BaseFilterApiController<L extends IdentifiableEntityData, I extends TimelineQueryParameters, D extends TaggingCoreItemQueryDefinition<L, I>>
        extends ApiResultApiController {
    private final TimelineQueryParametersConfigurator queryInstanceConfigurator;

    /**
     * Default constructor
     */
    public BaseFilterApiController() {
        queryInstanceConfigurator = new TimelineQueryParametersConfigurator(
                new FilterApiParameterNameProvider());
    }

    /**
     * Get the right converter for the current request
     * 
     * @param request
     *            The request
     * @return The converter to be used
     */
    protected QueryResultConverter<L, IdentifiableEntityData> createQueryConverter(HttpServletRequest request) {
        return null;
    }

    /**
     * Get the query instance for the current request to be configured
     * 
     * @param request
     *            the request
     * @return the (unconfigured) instance to be used
     */
    protected abstract Pair<D, I> createQueryInstance(HttpServletRequest request);

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object execute(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws IllegalRequestParameterException {
        Pair<D, I> pair = createQueryInstance(request);
        queryInstanceConfigurator.configure(request.getParameterMap(), pair.getRight());
        postConfigureQueryInstance(pair.getRight());
        QueryResultConverter<L, IdentifiableEntityData> converter = createQueryConverter(request);
        PageableList<IdentifiableEntityData> list;
        QueryManagement queryManagement = ServiceLocator.instance().getService(
                QueryManagement.class);
        if (converter == null) {
            list = (PageableList<IdentifiableEntityData>) queryManagement.query(pair.getLeft(), pair.getRight());
        } else {
            list = queryManagement.query(pair.getLeft(), pair.getRight(), converter);
        }
        return postProcessList(request, list);
    }

    /**
     * Hook for additional configuration
     * 
     * @param queryInstance
     *            the half way configured query instance
     */
    protected abstract void postConfigureQueryInstance(I queryInstance);

    /**
     * Hook for post processing the result list
     * 
     * @param request
     *            the request
     * @param list
     *            the retrieved list
     * @return the final list
     */
    protected abstract List postProcessList(HttpServletRequest request,
            PageableList<? extends IdentifiableEntityData> list);
}
