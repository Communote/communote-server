package com.communote.plugins.api.rest.v30.resource.user.externallogin;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.ExternalUserAuthentication;

/**
 * Handler for {@link ExternalLoginResource}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalLoginResourceHandler
        extends
        DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter, DefaultParameter,
        GetCollectionExternalLoginParameter> {

    @Override
    protected Response handleListInternally(GetCollectionExternalLoginParameter listParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {

        Long userId = listParameter.getUserId();
        if (!SecurityHelper.isCurrentUserId(userId)) {
            if (!SecurityHelper.isInternalSystem()) {
                throw new AuthorizationException(
                        "Only the external logins of the current user can be accessed");
            }
        }

        Set<ExternalUserAuthentication> externalUserAuthentications = ServiceLocator.findService(
                UserManagement.class).getExternalExternalUserAuthentications(userId);

        List<ExternalLoginResource> externalLoginResources;

        ExternalLoginResourceConverter externalLoginResourceConverter = new ExternalLoginResourceConverter(
                userId);
        externalLoginResources = externalLoginResourceConverter
                .convertCollection(externalUserAuthentications);

        return ResponseHelper.buildSuccessResponse(externalLoginResources, request);
    }

}
