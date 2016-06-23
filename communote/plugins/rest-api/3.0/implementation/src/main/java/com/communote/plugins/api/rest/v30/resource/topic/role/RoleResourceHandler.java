package com.communote.plugins.api.rest.v30.resource.topic.role;

import java.util.List;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.communote.common.util.PageableList;
import com.communote.plugins.api.rest.v30.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v30.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.resource.topic.TopicResourceHelper;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.listitems.blog.member.BlogRoleEntityListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.blog.BlogMemberManagementQuery;
import com.communote.server.core.vo.query.blog.BlogMemberManagementQueryParameters;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RoleResourceHandler
        extends
        DefaultResourceHandler<CreateRoleParameter, DefaultParameter, DefaultParameter, GetRoleParameter, GetCollectionRoleParameter> {

    private static final BlogMemberManagementQuery BLOG_MEMBER_MANAGEMENT_QUERY = QueryDefinitionRepository
            .instance().getQueryDefinition(BlogMemberManagementQuery.class);

    private static final RoleResourceConverter ROLE_RESOURCE_CONVERTER = new RoleResourceConverter();

    private QueryManagement getQueryManagement() {
        return ServiceLocator.findService(QueryManagement.class);
    }

    /**
     * Returns the {@link BlogManagement}.
     *
     * @return Returns the {@link BlogManagement}.
     */
    private BlogManagement getTopicManagement() {
        return ServiceLocator.instance().getService(BlogManagement.class);
    }

    /**
     * Create the role of internal user for blog
     *
     * @param createRoleParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws BlogNotFoundException
     *             blog not found
     * @throws BlogAccessException
     *             can not access blog
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user/group)
     * @throws NoBlogManagerLeftException
     *             no active user with managment rights
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws AuthorizationException
     *             in case the the current user tries to become manager of a topic but is not the
     *             client manager
     */
    @Override
    public Response handleCreateInternally(CreateRoleParameter createRoleParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws BlogNotFoundException, CommunoteEntityNotFoundException,
            NoBlogManagerLeftException, ResponseBuildException, ExtensionNotSupportedException,
            AuthorizationException {

        Long blogId = TopicResourceHelper.getTopicIdByIdentifier((createRoleParameter
                .getTopicIdentifier() != null) ? createRoleParameter.getTopicIdentifier().name()
                : null, createRoleParameter.getTopicId(), getTopicManagement());

        if (createRoleParameter.getEntityId() != null) {
            RoleResourceHelper.assignOrRemoveRoleForEntity(blogId,
                    RoleResourceHelper.getBlogRole(createRoleParameter.getRole()),
                    createRoleParameter.getEntityId());
        } else if (StringUtils.isNotBlank(createRoleParameter.getUserAlias())) {
            RoleResourceHelper.assignOrRemoveRoleForUser(createRoleParameter.getUserAlias(),
                    blogId, RoleResourceHelper.getBlogRole(createRoleParameter.getRole()));
        } else if (StringUtils.isNotBlank(createRoleParameter.getGroupAlias())) {
            RoleResourceHelper.assignOrRemoveRoleForGroup(createRoleParameter.getGroupAlias(),
                    blogId, RoleResourceHelper.getBlogRole(createRoleParameter.getRole()));
        }

        return ResponseHelper.buildSuccessResponse(null, request);
    }

    /**
     * Get blog role
     *
     * @param getRoleParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return null because it is not implemented
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleGetInternally(GetRoleParameter getRoleParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException {

        return ResponseHelper.buildSuccessResponse(null, request);
    }

    @Override
    protected Response handleListInternally(GetCollectionRoleParameter listParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {

        Long blogId = TopicResourceHelper.getTopicIdByIdentifier((listParameter
                .getTopicIdentifier() != null) ? listParameter.getTopicIdentifier().name() : null,
                        listParameter.getTopicId(), getTopicManagement());

        BlogMemberManagementQueryParameters parameters = new BlogMemberManagementQueryParameters();

        parameters.setBlogId(blogId);
        parameters.setResultSpecification(new ResultSpecification(0, 0));

        PageableList<BlogRoleEntityListItem> blogRoles = getQueryManagement().query(
                BLOG_MEMBER_MANAGEMENT_QUERY, parameters);

        List<RoleResource> rolesResources = ROLE_RESOURCE_CONVERTER.convert(blogRoles);

        return ResponseHelper.buildSuccessResponse(rolesResources, request);

    }

}
