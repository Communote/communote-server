package com.communote.plugins.api.rest.v22.resource.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;

import com.communote.common.converter.Converter;
import com.communote.common.converter.IdentityConverter;
import com.communote.common.util.PageableList;
import com.communote.plugins.api.rest.v22.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v22.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v22.resource.DefaultParameter;
import com.communote.plugins.api.rest.v22.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v22.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v22.resource.tag.TagHelper;
import com.communote.plugins.api.rest.v22.resource.tag.TagResource;
import com.communote.plugins.api.rest.v22.resource.user.property.PropertyResourceHelper;
import com.communote.plugins.api.rest.v22.response.ResponseHelper;
import com.communote.plugins.api.rest.v22.service.IllegalRequestParameterException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.core.vo.query.user.UserQuery;
import com.communote.server.core.vo.query.user.UserQueryParameters;
import com.communote.server.model.user.PhoneNumber;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserProfile;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.UserProfileVO;

/**
 * Handler for {@link UserResource}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserResourceHandler
        extends
        DefaultResourceHandler<DefaultParameter, EditUserParameter, DefaultParameter, GetUserParameter, GetCollectionUserParameter> {

    private static final UserQuery USER_QUERY = new UserQuery();

    /**
     * Constructor.
     */
    public UserResourceHandler() {
        super(new UserResourceValidator());
    }

    /**
     * Get the {@link UserResource} of {@link User} an locale
     *
     * @param userId
     *            Id of the user to build
     * @param locale
     *            of current user
     * @return {@link UserResource}
     */
    private UserResource buildUserResource(Long userId, final Locale locale) {
        return getUserManagement().getUserById(userId, new Converter<User, UserResource>() {
            @Override
            public UserResource convert(User source) {
                UserResource userResource = new UserResource();
                new UserResourceConverter<User, UserResource>(locale).convert(source, userResource);
                return userResource;
            }
        });
    }

    /**
     * Configure the {@link UserQueryInstance} by the {@link GetCollectionUserParameter}
     *
     * @param getCollectionUserParameter
     *            The parameters.
     * @return The configured query instance.
     */
    private UserQueryParameters configureQueryInstance(
            GetCollectionUserParameter getCollectionUserParameter) {

        int maxCount = getCollectionUserParameter.getMaxCount() == null ? 10
                : getCollectionUserParameter.getMaxCount();
        int offset = getCollectionUserParameter.getOffset() == null ? 0
                : getCollectionUserParameter.getOffset();
        UserQueryParameters queryParameters = new UserQueryParameters(maxCount, offset);
        if (StringUtils.isNotBlank(getCollectionUserParameter.getSearchString())) {
            queryParameters.setUserSearchFilters(new String[] { getCollectionUserParameter
                    .getSearchString() });
        }
        queryParameters.sortByLastNameAsc();
        queryParameters.setIncludeStatusFilter(new UserStatus[] { UserStatus.ACTIVE });
        return queryParameters;
    }

    /**
     * Getter for the {@link PropertyManagement}
     *
     * @return the {@link PropertyManagement}
     */
    public PropertyManagement getPropertyManagement() {
        return ServiceLocator.instance().getService(PropertyManagement.class);
    }

    /**
     * @return The user management
     */
    private UserManagement getUserManagement() {
        return ServiceLocator.instance().getService(UserManagement.class);
    }

    /**
     * @return The user profile management
     */
    private UserProfileManagement getUserProfileManagement() {
        return ServiceLocator.findService(UserProfileManagement.class);
    }

    /**
     * {@inheritDoc}
     *
     * @param request
     *            - javax request
     * @throws NotFoundException
     *             can not found element
     * @throws IllegalRequestParameterException
     *             request parameter is not legal
     * @throws ResponseBuildException
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleEditInternally(EditUserParameter editUserParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
            throws AuthorizationException, NotFoundException, IllegalRequestParameterException,
            ResponseBuildException, ExtensionNotSupportedException {

        Long userId = editUserParameter.getUserId();

        if (!userId.equals(SecurityHelper.getCurrentUserId())) {
            throw new AuthorizationException("Unauthorized to edit user with id: " + userId);
        }

        User user = getUserManagement().getUserById(userId, new IdentityConverter<User>() {
            @Override
            public User convert(User source) {
                Hibernate.initialize(source.getProfile());
                return super.convert(source);
            }
        });
        updateTags(userId, editUserParameter.getTags());
        updateUserProfile(user, editUserParameter);

        getPropertyManagement().setObjectProperties(
                PropertyType.UserProperty,
                userId,
                PropertyResourceHelper.convertPropertyResourcesToStringProperties(editUserParameter
                        .getProperties()));

        return ResponseHelper.buildSuccessResponse(userId, request);
    }

    /**
     * {@inheritDoc}
     *
     * @param request
     *            - javax request
     * @throws ResponseBuildException
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleGetInternally(GetUserParameter getUserParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException {
        return ResponseHelper.buildSuccessResponse(
                buildUserResource(getUserParameter.getUserId(),
                        ResourceHandlerHelper.getCurrentUserLocale(request)), request);
    }

    /**
     * {@inheritDoc}
     *
     * @param request
     *            - javax request
     * @throws UserNotFoundException
     * @throws ResponseBuildException
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleListInternally(GetCollectionUserParameter getCollectionUserParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
            throws UserNotFoundException, ResponseBuildException, ExtensionNotSupportedException {
        ArrayList<UserResource> userResources = new ArrayList<UserResource>();
        String alias = getCollectionUserParameter.getF_userAlias();
        if (alias != null) {
            User user = getUserManagement().findUserByAlias(alias);
            if (user == null) {
                throw new UserNotFoundException("user with alias " + alias + " does not exist");
            }
            userResources.add(buildUserResource(user.getId(),
                    ResourceHandlerHelper.getCurrentUserLocale(request)));
            return ResponseHelper.buildSuccessResponse(userResources, request);
        } else {
            UserQueryParameters userQueryInstance = configureQueryInstance(getCollectionUserParameter);
            PageableList<UserResource> pageableUserList = ServiceLocator.findService(
                    QueryManagement.class).query(
                    USER_QUERY,
                    userQueryInstance,
                    new UserResourceConverter<User, UserResource>(ResourceHandlerHelper
                            .getCurrentUserLocale(request)));

            userResources.addAll(pageableUserList);

            Map<String, Object> metaData = ResourceHandlerHelper.generateMetaDataForPaging(
                    getCollectionUserParameter.getOffset(),
                    getCollectionUserParameter.getMaxCount(),
                    pageableUserList.getMinNumberOfElements());

            return ResponseHelper.buildSuccessResponse(userResources, request, metaData);
        }

    }

    /**
     * Update the tags given in array of {@link TagResource} for user identifier
     *
     * @param userId
     *            identifier of user
     * @param tagResources
     *            array {@link TagResource}
     * @throws IllegalRequestParameterException
     *             exception in request parameter
     */
    private void updateTags(Long userId, TagResource[] tagResources)
            throws IllegalRequestParameterException {
        if (tagResources != null) {
            HashSet<TagTO> tagTOs = new HashSet<TagTO>();
            for (TagResource tagResource : tagResources) {
                tagTOs.add(TagHelper.buildTagTO(tagResource, TagStoreType.Types.ENTITY));
            }
            getUserManagement().updateUserTags(userId, tagTOs);
        }
    }

    /**
     * Update user profile for {@link User} and {@link EditUserParameter}
     *
     * @param user
     *            {@link User}
     * @param editUserParameter
     *            {@link EditUserParameter}
     */
    private void updateUserProfile(User user, EditUserParameter editUserParameter) {
        UserProfile profile = user.getProfile();
        UserProfileVO userProfileVo = new UserProfileVO();
        userProfileVo.setPhone(PhoneNumber.newInstance("", "", ""));
        userProfileVo.setFax(PhoneNumber.newInstance("", "", ""));
        userProfileVo
                .setFirstName(StringUtils.isNotBlank(editUserParameter.getFirstName()) ? editUserParameter
                        .getFirstName() : profile.getFirstName());
        userProfileVo
                .setLastName(StringUtils.isNotBlank(editUserParameter.getLastName()) ? editUserParameter
                        .getLastName() : profile.getLastName());
        getUserProfileManagement().updateUserProfile(user.getId(), userProfileVo);
    }

}
