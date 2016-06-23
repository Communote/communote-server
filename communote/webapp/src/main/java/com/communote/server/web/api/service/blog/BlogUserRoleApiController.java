package com.communote.server.web.api.service.blog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.User;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.service.IllegalRequestParameterException;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.api.to.ApiResult.ResultStatus;
import com.communote.server.web.commons.MessageHelper;

/**
 * Api controller to assign blog user roles
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class BlogUserRoleApiController extends BaseRestApiController {

    private static final String USER_GROUP_ROLE_NONE = "NONE";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doGet(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws ApiException {
        Long blogId = getLongParameter(request, "blogId");

        BlogRole role = ServiceLocator.findService(BlogRightsManagement.class).getRoleOfUser(
                blogId, SecurityHelper.getCurrentUserId(), false);
        return role == null ? USER_GROUP_ROLE_NONE : role.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws ApiException {
        String roleParam = getNonEmptyParameter(request, "role");
        Long blogId = getLongParameter(request, "blogId");
        Long entityId = ParameterHelper.getParameterAsLong(request.getParameterMap(), "entityId",
                null);

        roleParam = roleParam.toUpperCase();
        BlogRole role = getUserGroupRole(roleParam);

        if (entityId == null) {
            entityId = getUserIdByAlias(request);
        }

        BlogRightsManagement blogRightsManagement = ServiceLocator
                .findService(BlogRightsManagement.class);

        // boolean isMember = brm.isUserDirectMember(blogId, entityId);

        String errorMessageKey = null;

        try {
            if (role == null) {
                // DELETE - member exists and role does not exists
                blogRightsManagement.removeMemberByEntityId(blogId, entityId);
            } else {
                // UPDATE - member exists and role exists
                // CREATE - a new member and role exists
                blogRightsManagement.assignEntity(blogId, entityId, role);
            }
        } catch (NoBlogManagerLeftException e) {
            errorMessageKey = "user.group.member.change.role.no.manager.left.exception";
        } catch (BlogNotFoundException e) {
            errorMessageKey = "error.blog.change.rights.failed.noBlog";
        } catch (CommunoteEntityNotFoundException e) {
            errorMessageKey = "error.blog.change.rights.failed.noEntity";
        } catch (BlogAccessException e) {
            errorMessageKey = "error.blog.change.rights.failed.noBlogAccess";
        }

        if (errorMessageKey != null) {
            String message = MessageHelper.getText(request, errorMessageKey);
            apiResult.setMessage(message);
            apiResult.setStatus(ResultStatus.ERROR.name());
        }
        // nothing to return
        return null;
    }

    /**
     * @param role
     *            the role to parse
     * @return the user group role or null if it is "NONE" for deletion
     * @throws IllegalRequestParameterException
     *             in case role cannot be parsed
     */
    private BlogRole getUserGroupRole(String role) throws IllegalRequestParameterException {
        BlogRole userGroupRole;
        if (USER_GROUP_ROLE_NONE.equals(role)) {
            userGroupRole = null;
        } else {
            try {
                userGroupRole = BlogRole.fromString(role);
            } catch (Exception e) {
                throw new IllegalRequestParameterException("role", role,
                        "Cannot parse role to a valid user role!");
            }
        }
        return userGroupRole;
    }

    /**
     * Determine the user id by extracting the userAlias parameter
     *
     * @param request
     *            the request
     * @return the user id
     * @throws ApiException
     *             in case the parameter is null or the user has not been found
     */
    private Long getUserIdByAlias(HttpServletRequest request) throws ApiException {
        Long userId;
        String userAlias = request.getParameter("userAlias");
        // get the user id by alias if its null
        if (StringUtils.isEmpty(userAlias)) {
            throw new IllegalRequestParameterException("userAlias", userAlias,
                    "Eiher userId or userAlias must be set!");
        }
        User user = getUserManagement().findUserByAlias(userAlias);
        if (user == null) {
            throw new ApiException(MessageHelper.getText(request,
                    "blog.management.error.user.not.found", new Object[] { userAlias }));
        }
        userId = user.getId();
        return userId;
    }

    /**
     * @return the user management
     */
    private UserManagement getUserManagement() {
        return ServiceLocator.findService(UserManagement.class);
    }
}
