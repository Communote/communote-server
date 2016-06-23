package com.communote.plugins.api.rest.v30.resource.group;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.common.converter.Converter;
import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.server.model.user.group.Group;

/**
 * Handler for {@link GroupResource}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GroupResourceHandler
        extends
        DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter, GetGroupParameter,
        DefaultParameter> {

    private final Converter<Group, GroupResource> converter = new GroupResourceConverter();

    @Override
    protected Response handleGetInternally(GetGroupParameter getParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {

        GroupResource groupResource = GroupResourceHelper.getGroup(getParameter.getGroupId(),
                EGroupIdentifier.ALIAS.equals(getParameter.getGroupIdentifier()), converter);

        return ResponseHelper.buildSuccessResponse(groupResource, request);
    }
}
