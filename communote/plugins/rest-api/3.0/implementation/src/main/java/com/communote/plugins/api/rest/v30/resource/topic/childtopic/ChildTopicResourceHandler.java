package com.communote.plugins.api.rest.v30.resource.topic.childtopic;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.service.TopicHierarchyService;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ChildTopicResourceHandler extends
        DefaultResourceHandler<CreateChildTopicParameter, DefaultParameter,
        DeleteChildTopicParameter, DefaultParameter, DefaultParameter> {
    /**
     * @{inheritDoc
     */
    @Override
    protected Response handleCreateInternally(CreateChildTopicParameter createParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {
        Long childTopicId = createParameter.getChildTopicId();
        String parentTopicIdOrAlias = createParameter.getTopicId();
        Long parentTopicId;
        if (EParentTopicIdentifier.ALIAS.equals(createParameter.getParentTopicIdentifier())) {
            parentTopicId = ServiceLocator.findService(BlogManagement.class)
                    .findBlogByIdentifier(parentTopicIdOrAlias).getId();
        } else {
            parentTopicId = Long.parseLong(parentTopicIdOrAlias);
        }
        ServiceLocator.findService(TopicHierarchyService.class).addTopic(parentTopicId,
                childTopicId);
        return ResponseHelper.buildSuccessResponse(childTopicId, request);
    }

    /**
     * @{inheritDoc
     */
    @Override
    protected Response handleDeleteInternally(DeleteChildTopicParameter deleteParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {
        Long childTopicId = deleteParameter.getChildTopicId();
        String parentTopicIdOrAlias = deleteParameter.getTopicId();
        Long parentTopicId;
        if (EParentTopicIdentifier.ALIAS.equals(deleteParameter.getParentTopicIdentifier())) {
            parentTopicId = ServiceLocator.findService(BlogManagement.class)
                    .findBlogByIdentifier(parentTopicIdOrAlias).getId();
        } else {
            parentTopicId = Long.parseLong(parentTopicIdOrAlias);
        }
        ServiceLocator.findService(TopicHierarchyService.class).removeTopic(parentTopicId,
                childTopicId);
        return ResponseHelper.buildSuccessResponse(null, request);
    }
}
