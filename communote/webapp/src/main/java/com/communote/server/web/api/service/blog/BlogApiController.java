package com.communote.server.web.api.service.blog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.MatchMode;
import org.springframework.security.access.AccessDeniedException;

import com.communote.common.string.StringHelper;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.core.converter.blog.BlogToUserDetailBlogListItemConverter;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.listitems.blog.UserDetailBlogListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.tag.TagParserFactory;
import com.communote.server.core.vo.query.blog.BlogQuery;
import com.communote.server.core.vo.query.blog.BlogQueryParameters;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.config.FilterApiParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.model.blog.Blog;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.service.IllegalRequestParameterException;
import com.communote.server.web.api.service.RequestedResourceNotFoundException;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.commons.MessageHelper;

/**
 * Controller for handling api request for blogs.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class BlogApiController extends BaseRestApiController {
    /**
     * Type.
     */
    private enum BlogListType {
        MOST_USED, LAST_USED, LAST_MODIFIED, MANAGER, READ, WRITE
    }

    private static final String PARAM_LAST_MODIFICATION_DATE = "lastModificationDate";

    private static final String PARAM_MAX_RESULTS = "maxResults";

    private static final String PARAM_BLOG_LIST_TYPE = "blogListType";

    private final QueryParametersParameterNameProvider nameProvider = new FilterApiParameterNameProvider();

    /**
     * Do the get on a resource
     *
     * @param apiResult
     *            The reuslt to fill.
     * @param request
     *            the request
     * @param response
     *            the response
     * @return the post resource
     * @throws RequestedResourceNotFoundException
     *             the resource has not been found
     * @throws IllegalRequestParameterException
     *             in case of an illegal parameter
     */
    @Override
    protected Object doGet(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws RequestedResourceNotFoundException,
            IllegalRequestParameterException {
        Long blogId = getResourceId(request, false);

        // no blog id so get the list
        if (blogId == null) {
            List<BlogData> items = getBlogList(request, response);
            return items;
        } else {
            BlogToUserDetailBlogListItemConverter converter = new BlogToUserDetailBlogListItemConverter(
                    UserDetailBlogListItem.class, false, true, true, false, false, null);
            UserDetailBlogListItem blogListItem;
            try {
                blogListItem = getBlogManagement().getBlogById(blogId, converter);
            } catch (BlogAccessException e) {
                // for compatibility reasons throw an AccessDeniedException
                throw new AccessDeniedException(e.getMessage(), e);
            }
            return blogListItem;
        }
    }

    /**
     * Do a creation or edit of a post resource
     *
     * @param apiResult
     *            the apiResult
     * @param request
     *            the request
     * @param response
     *            the response
     * @return the id of the blog created/updated
     * @throws ApiException
     *             in case of an error
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws ApiException {
        Long blogId = getResourceId(request, false);

        String title = getNonEmptyParameter(request, "title");
        String identifier = ParameterHelper.getParameterAsString(request.getParameterMap(),
                "nameIdentifier");
        String description = ParameterHelper.getParameterAsString(request.getParameterMap(),
                "description");
        String unparsedTags = ParameterHelper.getParameterAsString(request.getParameterMap(),
                "tags");

        CreationBlogTO blogTO = new CreationBlogTO();
        blogTO.setCreatorUserId(SecurityHelper.assertCurrentUserId());
        blogTO.setDescription(description);
        blogTO.setTitle(title);
        blogTO.setNameIdentifier(identifier);
        blogTO.setUnparsedTags(TagParserFactory.instance().getDefaultTagParser()
                .parseTags(unparsedTags));

        Object result;
        try {
            if (blogId == null) {
                // its an create
                Blog blog = getBlogManagement().createBlog(blogTO);
                result = blog.getId();
            } else {
                // its an update
                Blog blog = getBlogManagement().updateBlog(blogId, blogTO);
                result = blog.getId();
            }
        } catch (NonUniqueBlogIdentifierException e) {
            String errorMsg = MessageHelper.getText(request, "error.blog.identifier.noneunique");
            throw new ApiException(errorMsg);
        } catch (BlogIdentifierValidationException e) {
            String errorMsg = MessageHelper.getText(request, "error.blog.identifier.notvalid");
            throw new ApiException(errorMsg);
        } catch (BlogAccessException e) {
            String errorMsg = MessageHelper.getText(request,
                    "error.blogpost.blog.no.access.no.manager");
            throw new ApiException(errorMsg);
        } catch (BlogNotFoundException e) {
            throw new ApiException("unexpected exception");
        }

        return result;
    }

    /**
     * Method to get the blog list based on the blog list type parameter
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @return the list of requested blogs
     * @throws IllegalRequestParameterException
     *             in case of an illegal parameter
     */
    protected List<BlogData> getBlogList(HttpServletRequest request,
            HttpServletResponse response) throws IllegalRequestParameterException {
        BlogListType blogListType = getBlogListType(request);
        int offset = ParameterHelper.getParameterAsInteger(request.getParameterMap(), nameProvider
                .getNameForOffset(), 0);
        int maxCount = ParameterHelper.getParameterAsInteger(request.getParameterMap(),
                nameProvider.getNameForMaxCount(), 5);
        ResultSpecification resultSpecification = new ResultSpecification(offset, maxCount);
        Collection<BlogData> blogs;
        String searchString;
        switch (blogListType) {
        case LAST_MODIFIED:
            // backwards compatibility for the max result parameter
            if (StringUtils.isEmpty(request.getParameter(nameProvider.getNameForMaxCount()))) {
                maxCount = getIntegerParameter(request, PARAM_MAX_RESULTS);
                resultSpecification = new ResultSpecification(offset, maxCount);
            }
            Date lastModificationDate;
            try {
                lastModificationDate = new Date(getLongParameter(request,
                        PARAM_LAST_MODIFICATION_DATE));
            } catch (Exception e) {
                lastModificationDate = null;
            }
            blogs = getLastModifiedBlogs(SecurityHelper.assertCurrentUserId(),
                    lastModificationDate, resultSpecification);
            break;
        case LAST_USED:
            blogs = getBlogManagement().getLastUsedBlogs(maxCount, false);
            break;
        case MANAGER:
            blogs = getManageableBlogs(resultSpecification);
            break;
        case MOST_USED:
            blogs = getBlogManagement().getMostUsedBlogs(maxCount, false);
            break;
        case READ:
            searchString = ParameterHelper.getParameterAsString(request.getParameterMap(),
                    "searchString");

            blogs = getReadableBlogs(searchString, TopicAccessLevel.READ, resultSpecification);
            break;
        case WRITE:
            searchString = ParameterHelper.getParameterAsString(request.getParameterMap(),
                    "searchString");

            blogs = getReadableBlogs(searchString, TopicAccessLevel.WRITE, resultSpecification);
            break;
        default:
            throw new IllegalRequestParameterException(PARAM_BLOG_LIST_TYPE, blogListType.name(),
                    "Invalid value. Allowed values are: "
                            + StringHelper.toString(BlogListType.values(), "|"));
        }
        return new ArrayList<BlogData>(blogs);
    }

    /**
     * @param request
     *            the request
     * @return Get the type of blogs to get out of the request
     * @throws IllegalRequestParameterException
     *             invalid blog list type
     */
    private BlogListType getBlogListType(HttpServletRequest request)
            throws IllegalRequestParameterException {
        String parameterValue = getNonEmptyParameter(request, PARAM_BLOG_LIST_TYPE);
        try {
            return BlogListType.valueOf(parameterValue.toUpperCase());
        } catch (Exception e) {
            throw new IllegalRequestParameterException(PARAM_BLOG_LIST_TYPE, parameterValue, e
                    .getMessage());
        }
    }

    /**
     * @return the blog management
     */
    private BlogManagement getBlogManagement() {
        return ServiceLocator.findService(BlogManagement.class);
    }

    /**
     * Retrieves the last modified blogs a user can access, sort by the oldest modified blog first.
     *
     * @param userId
     *            id of the user
     * @param minimumLastModificationDate
     *            the minimum last modification the blog must be modified AFTER
     * @param resultSpecification
     *            the result specification
     * @return the found blogs
     */
    private List<BlogData> getLastModifiedBlogs(Long userId,
            Date minimumLastModificationDate, ResultSpecification resultSpecification) {
        BlogQueryParameters blogQueryInstance = BlogQuery.DEFAULT_QUERY.createInstance();
        blogQueryInstance.setAccessLevel(TopicAccessLevel.READ);
        blogQueryInstance.setUserId(userId);
        blogQueryInstance.setMinimumLastModificationDate(minimumLastModificationDate);
        blogQueryInstance.sortByLastModificationDateAsc();
        blogQueryInstance.setResultSpecification(resultSpecification);
        QueryManagement qm = ServiceLocator.findService(QueryManagement.class);
        return qm.query(BlogQuery.DEFAULT_QUERY, blogQueryInstance);
    }

    /**
     * @param resultSpecification
     *            the {@link ResultSpecification} states the offset and the maximum number of
     * @return get the blogs the current user is manager of
     */
    private List<BlogData> getManageableBlogs(ResultSpecification resultSpecification) {
        List<BlogData> blogs;

        BlogQueryParameters instance = BlogQuery.DEFAULT_QUERY.createInstance();
        instance.setAccessLevel(TopicAccessLevel.MANAGER);
        instance.setUserId(SecurityHelper.getCurrentUserId());
        instance.setResultSpecification(resultSpecification);
        instance.sortByNameAsc();

        QueryManagement qm = ServiceLocator.findService(QueryManagement.class);
        blogs = qm.executeQuery(BlogQuery.DEFAULT_QUERY, instance);

        return blogs;
    }

    /**
     * @param searchString
     *            the search string
     * @param blogAccessLevel
     *            Level the user must have to access the blog.
     * @param resultSpecification
     *            the {@link ResultSpecification} states the offset and the maximum number of
     *            elements to get
     * @return get the blogs the current user can read
     */
    private List<BlogData> getReadableBlogs(String searchString,
            TopicAccessLevel blogAccessLevel, ResultSpecification resultSpecification) {
        List<BlogData> blogs;
        BlogQueryParameters instance = BlogQuery.DEFAULT_QUERY.createInstance();

        instance.setAccessLevel(blogAccessLevel);
        instance.setUserId(SecurityHelper.assertCurrentUserId());

        if (StringUtils.isNotEmpty(searchString)) {
            instance.setSearchFieldMask(BlogQueryParameters.SEARCH_FIELD_TITLE);
            instance.setTextFilter(searchString.split(" "));
            instance.setMatchMode(MatchMode.ANYWHERE);
        }

        instance.sortByNameAsc();
        instance.setResultSpecification(resultSpecification);

        QueryManagement queryManagement = ServiceLocator.instance().getService(
                QueryManagement.class);
        blogs = queryManagement.executeQuery(BlogQuery.DEFAULT_QUERY, instance);

        return blogs;
    }
}
