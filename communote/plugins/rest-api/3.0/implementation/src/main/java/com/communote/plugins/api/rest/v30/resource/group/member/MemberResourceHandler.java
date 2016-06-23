package com.communote.plugins.api.rest.v30.resource.group.member;

import java.util.List;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.resource.group.GroupResourceHelper;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;

/**
 * Handler for {@link MemberResource}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MemberResourceHandler
        extends
        DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter, DefaultParameter,
        GetCollectionMemberParameter> {

    private MemberResourceConverter converter = new MemberResourceConverter();

    @Override
    protected Response handleListInternally(GetCollectionMemberParameter listParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {

        List<MemberResource> members = GroupResourceHelper.getGroup(
                listParameter.getGroupId(),
                EGroupIdentifier.ALIAS
                        .equals(listParameter.getGroupIdentifier()),
                converter);

        return ResponseHelper.buildSuccessResponse(members, request);
    }

}
