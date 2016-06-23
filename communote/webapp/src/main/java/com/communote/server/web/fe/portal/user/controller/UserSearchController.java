package com.communote.server.web.fe.portal.user.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.hibernate.criterion.MatchMode;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.communote.common.converter.CollectionConverter;
import com.communote.common.converter.IdentityConverter;
import com.communote.common.util.PageableList;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.user.UserData;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.converter.user.UserToUserDataConverter;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.listitems.UserManagementListItem;
import com.communote.server.core.filter.listitems.blog.member.CommunoteEntityData;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.blog.BlogMemberQuery;
import com.communote.server.core.vo.query.blog.BlogMemberQueryParameters;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.user.CommunoteEntityQuery;
import com.communote.server.core.vo.query.user.CommunoteEntityQueryParameters;
import com.communote.server.core.vo.query.user.UserManagementQuery;
import com.communote.server.core.vo.query.user.UserManagementQueryParameters;
import com.communote.server.core.vo.query.user.UserTaggingCoreQuery;
import com.communote.server.core.vo.query.user.UserTaggingCoreQueryParameters;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.UserToBlogRoleMapping;
import com.communote.server.model.note.NoteStatus;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.web.fe.portal.blog.helper.UserSearchHelper;

/**
 * Controller to get a list of users
 *
 * TODO Configure url that it listens to /user/search/*.do
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @deprecated Use REST-API instead
 */
@Deprecated
public class UserSearchController extends MultiActionController {

    private final static UserToUserDataConverter<UserData> USER_CONVERTER = new UserToUserDataConverter<UserData>(
            UserData.class, false, null);

    private static final int LIMIT = 15;
    /**
     * the log.
     */
    private final static Logger LOG = Logger.getLogger(UserSearchController.class);
    /** The Constant USER_QUERY_DEFINITION. */
    private final static UserManagementQuery USER_QUERY_DEFINITION = QueryDefinitionRepository
            .instance().getQueryDefinition(UserManagementQuery.class);

    /** The Constant KENMEI_ENTITY_QUERY_DEFINITION. */
    private final static CommunoteEntityQuery ENTITY_QUERY = QueryDefinitionRepository.instance()
            .getQueryDefinition(CommunoteEntityQuery.class);
    private final static UserTaggingCoreQuery AUTHOR_QUERY_DEFINITION = QueryDefinitionRepository
            .instance().getQueryDefinition(UserTaggingCoreQuery.class);
    private final static BlogMemberQuery READER_QUERY_DEFINITION = QueryDefinitionRepository
            .instance().getQueryDefinition(BlogMemberQuery.class);

    /** The Constant BLANK. */
    private final static String BLANK = " ";
    private final FilterWidgetParameterNameProvider nameProvider = FilterWidgetParameterNameProvider.INSTANCE;

    /**
     * Extract the image size from the request.
     *
     * @param request
     *            the request
     * @param defaultSize
     *            the size to return if not in request, can be null
     * @return the image size or the default size if the parameter is missing
     */
    private ImageSizeType extractImageSize(HttpServletRequest request, ImageSizeType defaultSize) {
        String imageSize = request.getParameter("imageSize");
        ImageSizeType imageSizeType = imageSize == null ? defaultSize : ImageSizeType
                .fromString(imageSize);
        return imageSizeType;
    }

    /**
     * Retrieves the authors of the notes which match the current filtering.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             in case of an error
     */
    public void queryAuthor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UserTaggingCoreQueryParameters userQueryInstance = AUTHOR_QUERY_DEFINITION.createInstance();
        TimelineQueryParametersConfigurator<UserTaggingCoreQueryParameters> configurator = new TimelineQueryParametersConfigurator<UserTaggingCoreQueryParameters>(
                nameProvider);
        configurator.configure(request.getParameterMap(), userQueryInstance);
        userQueryInstance.setExcludeNoteStatus(new NoteStatus[] { NoteStatus.AUTOSAVED });

        PageableList<UserData> users = ServiceLocator.findService(QueryManagement.class).query(
                AUTHOR_QUERY_DEFINITION, userQueryInstance);
        ImageSizeType imageSize = extractImageSize(request, null);
        response.setContentType("application/json");
        JsonHelper.writeJsonTree(response.getWriter(),
                UserSearchHelper.createUserSearchJSONResult(users, true, imageSize));
    }

    /**
     * Query user and groups.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws Exception
     *             the exception
     */
    public void queryEntity(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        SecurityHelper.assertCurrentUser();

        String searchString = request.getParameter(nameProvider.getNameForUserSearchString());
        int offset = ParameterHelper.getParameterAsInteger(request.getParameterMap(),
                nameProvider.getNameForOffset(), 0);
        int limit = ParameterHelper.getParameterAsInteger(request.getParameterMap(),
                nameProvider.getNameForMaxCount(), LIMIT);
        limit = limit < 0 ? LIMIT : limit;

        CommunoteEntityQueryParameters parameters = ENTITY_QUERY.createInstance();
        parameters.setResultSpecification(new ResultSpecification(offset, limit));

        String[] entitySearchFilter = StringUtils.split(searchString, BLANK);
        parameters.setUserSearchFilters(entitySearchFilter);
        parameters.setExcludeStatusFilter(new UserStatus[] { UserStatus.DELETED,
                UserStatus.PERMANENTLY_DISABLED });
        parameters.sortByLastNameAsc();
        parameters.sortByFirstNameAsc();

        Long excludedId = ParameterHelper.getParameterAsLong(request.getParameterMap(),
                nameProvider.getNameForEntityId());
        parameters.setExcludedEntityId(excludedId);

        PageableList<CommunoteEntityData> list = ServiceLocator.findService(QueryManagement.class)
                .query(ENTITY_QUERY, parameters);
        JsonNode node = UserSearchHelper.createEntitySearchJSONResult(list, true,
                extractImageSize(request, null));
        response.setContentType("application/json");
        JsonHelper.writeJsonTree(response.getWriter(), node);
    }

    /**
     * Retrieves only topic manager of the current filtering.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             in case of an error
     */
    public void queryManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String searchString = ParameterHelper.getParameterAsString(request.getParameterMap(),
                nameProvider.getNameForUserSearchString());
        final String[] searchStrings = searchString.split(" ");
        final UserManagement userManagement = ServiceLocator.findService(UserManagement.class);

        Collection<UserData> userListItems = new HashSet<UserData>(ServiceLocator.findService(
                BlogRightsManagement.class).getMappedUsers(null,
                new CollectionConverter<UserToBlogRoleMapping, UserData>() {
                    @Override
                    public UserData convert(UserToBlogRoleMapping source) {
                        User user = userManagement.getUserById(source.getUserId(),
                                new IdentityConverter<User>());
                        for (String searchString : searchStrings) {
                            searchString = searchString.toLowerCase();
                            String firstName = "";
                            String lastName = "";
                            if (user.getProfile() != null) {
                                firstName = user.getProfile().getFirstName();
                                lastName = user.getProfile().getLastName();
                            }
                            if (user.getAlias().toLowerCase().contains(searchString)
                                    || (firstName != null && firstName.toLowerCase().contains(
                                            searchString))
                                    || (lastName != null && lastName.toLowerCase().contains(
                                            searchString))) {
                                return USER_CONVERTER.convert(user);
                            }
                        }
                        return null;
                    }
                }, BlogRole.MANAGER));

        ImageSizeType imageSize = extractImageSize(request, null);
        response.setContentType("application/json");
        JsonHelper.writeJsonTree(response.getWriter(), UserSearchHelper.createUserSearchJSONResult(
                new PageableList<UserData>(userListItems), true, imageSize));
    }

    /**
     * Creates a JSON array with detailed data about users with read-access to a blog and writes
     * this array to the response. The details include the total number of users found.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             in case of an error
     */
    public void queryReader(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String pattern = request.getParameter(nameProvider.getNameForUserSearchString());
        PageableList<UserData> results;
        Long blogId;
        Map<String, ?> parameters = request.getParameterMap();
        blogId = ParameterHelper.getParameterAsLong(parameters, nameProvider.getNameForBlogIds(),
                null);
        if (blogId == null) {
            LOG.debug("Received request with no or invalid blogId");
            // return an empty list because BlogMemberQuery requires a blog
            results = PageableList.emptyList();
        } else {
            BlogMemberQueryParameters queryParameters = READER_QUERY_DEFINITION.createInstance();
            queryParameters.setBlogId(blogId);
            queryParameters.setIncludeStatusFilter(new UserStatus[] { UserStatus.ACTIVE });
            if (pattern != null && pattern.trim().length() > 0) {
                queryParameters.setUserSearchFilters(StringUtils.split(pattern, BLANK));
                queryParameters.setMatchMode(MatchMode.START);
            }
            int offset = ParameterHelper.getParameterAsInteger(parameters,
                    nameProvider.getNameForOffset(), 0);
            int limit = ParameterHelper.getParameterAsInteger(parameters,
                    nameProvider.getNameForMaxCount(), 0);
            ResultSpecification resultSpecification = new ResultSpecification(offset, limit < 0 ? 0
                    : limit);
            resultSpecification.setCheckAtLeastMoreResults(1);
            queryParameters.setResultSpecification(resultSpecification);
            queryParameters.sortByLastNameAsc();
            queryParameters.sortByFirstNameAsc();
            results = ServiceLocator.findService(QueryManagement.class).query(
                    READER_QUERY_DEFINITION, queryParameters);
        }
        ImageSizeType imageSize = extractImageSize(request, null);
        ArrayNode jsonResponse = UserSearchHelper.createUserSearchJSONResult(results, true,
                imageSize);

        response.setContentType("application/json");
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }

    /**
     * Query user.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws Exception
     *             the exception
     */
    public void queryUser(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        SecurityHelper.assertCurrentUser();
        String searchString = request.getParameter(nameProvider.getNameForUserSearchString());
        int offset = ParameterHelper.getParameterAsInteger(request.getParameterMap(),
                nameProvider.getNameForOffset(), 0);
        int limit = ParameterHelper.getParameterAsInteger(request.getParameterMap(),
                nameProvider.getNameForMaxCount(), 10);
        limit = limit < 0 ? 10 : limit;

        UserManagementQueryParameters queryParameters = new UserManagementQueryParameters();
        String[] userSearchFilter = StringUtils.split(searchString, BLANK);
        queryParameters.setUserSearchFilters(userSearchFilter);
        queryParameters.setExcludeStatusFilter(new UserStatus[] { UserStatus.DELETED,
                UserStatus.PERMANENTLY_DISABLED });
        queryParameters.setResultSpecification(new ResultSpecification(offset, limit));
        queryParameters.sortByLastNameAsc();
        queryParameters.sortByFirstNameAsc();

        PageableList<UserManagementListItem> list = ServiceLocator.findService(
                QueryManagement.class).query(USER_QUERY_DEFINITION, queryParameters);

        response.setContentType("application/json");
        JsonHelper.writeJsonTree(
                response.getWriter(),
                UserSearchHelper.createUserSearchJSONResult(list, true,
                        extractImageSize(request, null)));
    }
}
