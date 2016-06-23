package com.communote.plugins.api.rest.v30.resource.user.navigationitem;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.common.converter.CollectionConverter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.vo.user.NavigationItemDataTO;
import com.communote.server.core.vo.user.NavigationItemTO;
import com.communote.server.service.NavigationItemService;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NavigationItemResourceHandler
        extends
        DefaultResourceHandler<CreateNavigationItemParameter, EditNavigationItemParameter,
        DeleteNavigationItemParameter, GetNavigationItemParameter, GetCollectionNavigationItemParameter> {

    private final NavigationItemService navigationItemService;

    private final static CollectionConverter<NavigationItemTO<NavigationItemDataTO>, NavigationItemResource> CONVERTER =
            new CollectionConverter<NavigationItemTO<NavigationItemDataTO>, NavigationItemResource>() {

                /**
                 * Converts the source object which is of type S into an object of type T
                 * 
                 * @param source
                 *            the object to convert
                 * @return the converted object
                 */
                @Override
                public NavigationItemResource convert(NavigationItemTO<NavigationItemDataTO> source) {
                    NavigationItemResource result = new NavigationItemResource();
                    result.setIndex(source.getIndex());
                    result.setName(source.getName());
                    result.setData(source.getDataAsJson());
                    result.setLastAccessDate(source.getLastAccessDate().getTime());
                    result.setNavigationItemId(source.getId());
                    return result;
                }
            };

    /**
     * Constructor.
     * 
     * @param navigationItemService
     *            The service to use.
     */
    public NavigationItemResourceHandler(NavigationItemService navigationItemService) {
        this.navigationItemService = navigationItemService;
    }

    @Override
    protected Response handleCreateInternally(CreateNavigationItemParameter createParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {
        NavigationItemDataTO data = JsonHelper.getSharedObjectMapper().readValue(
                createParameter.getData(), NavigationItemDataTO.class);
        Date lastAccessDate = null;
        if (createParameter.getLastAccessDate() != null) {
            lastAccessDate = new Date(createParameter.getLastAccessDate());
        }
        Long result = navigationItemService.store(createParameter.getName(),
                createParameter.getIndex(), lastAccessDate, data);
        return ResponseHelper.buildSuccessResponse(result, request);
    }

    @Override
    protected Response handleDeleteInternally(DeleteNavigationItemParameter deleteParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {
        navigationItemService.delete(deleteParameter.getNavigationItemId());
        return ResponseHelper.buildSuccessResponse(null, request);
    }

    @Override
    protected Response handleEditInternally(EditNavigationItemParameter editParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {
        NavigationItemDataTO data = null;
        if (editParameter.getData() != null) {
            data = JsonHelper.getSharedObjectMapper().readValue(
                    editParameter.getData(), NavigationItemDataTO.class);
        }
        Date lastAccessDate = null;
        if (editParameter.getLastAccessDate() != null) {
            lastAccessDate = new Date(editParameter.getLastAccessDate());
        }
        Long result = navigationItemService.update(editParameter.getNavigationItemId(),
                editParameter.getName(), editParameter.getIndex(), lastAccessDate, data);
        return ResponseHelper.buildSuccessResponse(result, request);
    }

    @Override
    protected Response handleGetInternally(GetNavigationItemParameter getParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {
        NavigationItemTO<NavigationItemDataTO> navigationItemTO = navigationItemService
                .get(getParameter
                        .getNavigationItemId());
        if (navigationItemTO == null) {
            throw new NotFoundException("The requested navigation item ("
                    + getParameter.getNavigationItemId() + ") can't be found.");
        }
        return ResponseHelper.buildSuccessResponse(CONVERTER.convert(navigationItemTO), request);
    }

    @Override
    protected Response handleListInternally(GetCollectionNavigationItemParameter listParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {
        List<NavigationItemTO<NavigationItemDataTO>> navigationItemTOs = listParameter
                .getF_navigationItemIds() == null ? navigationItemService
                .find() : navigationItemService.find(listParameter.getF_navigationItemIds());

        return ResponseHelper.buildSuccessResponse(CONVERTER.convert(navigationItemTOs), request);
    }
}
