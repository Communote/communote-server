package com.communote.server.web.fe.portal.blog.helper;

import java.util.Collection;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.image.CoreImageType;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.Query;
import com.communote.server.core.vo.query.QueryParameters;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.web.commons.helper.ImageUrlHelper;

/**
 * Helper class for blog search operations.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class BlogSearchHelper {
    private static final String JSON_KEY_BLOG_ID = "id";
    private static final String JSON_KEY_BLOG_NAME_ID = "alias";
    private static final String JSON_KEY_BLOG_TITLE = "title";
    private static final String JSON_KEY_BLOG_DESCRIPTION = "description";
    private static final String JSON_KEY_BLOG_IMAGE = "imagePath";

    /**
     * Add the JSON objects of the blogs to the JSON response.
     * 
     * @param jsonResponse
     *            the response object to write to
     * @param list
     *            list to process
     * @param includeDescription
     *            whether to include the description
     * @param includeImagePath
     *            whether to include the path to the image
     */
    private static void addBlogsToJsonResponse(ArrayNode jsonResponse,
            Collection<BlogData> list, boolean includeDescription, boolean includeImagePath) {
        for (BlogData item : list) {
            ObjectNode entry;
            if (includeDescription) {
                entry = BlogSearchHelper.createBlogSearchJSONResult(item.getId(),
                        item.getNameIdentifier(), item.getTitle(), item.getDescription(),
                        includeImagePath);
            } else {
                entry = BlogSearchHelper.createBlogSearchJSONResult(item.getId(),
                        item.getNameIdentifier(), item.getTitle(), includeImagePath);
            }
            jsonResponse.add(entry);
        }
    }

    /**
     * Creates a JSON array with JSON objects of the blog list items.
     * 
     * @param list
     *            list to process
     * @param includeDescription
     *            whether to include the description
     * @param includeImagePath
     *            whether to include the path to the image
     * @return the JSON array
     */
    public static ArrayNode createBlogSearchJSONResult(Collection<BlogData> list,
            boolean includeDescription, boolean includeImagePath) {
        ArrayNode jsonResponse = JsonHelper.getSharedObjectMapper().createArrayNode();
        addBlogsToJsonResponse(jsonResponse, list, includeDescription, includeImagePath);
        return jsonResponse;
    }

    /**
     * Creates a JSON object describing a blog by its id, nameId and title
     * 
     * @param blogId
     *            the blog id
     * @param nameIdentifier
     *            the alias
     * @param title
     *            the title
     * @param includeImagePath
     *            whether to include the path to the image
     * @return the JSON object
     */
    public static ObjectNode createBlogSearchJSONResult(Long blogId, String nameIdentifier,
            String title, boolean includeImagePath) {
        ObjectNode result = JsonHelper.getSharedObjectMapper().createObjectNode();
        // null safe put for Long
        result.putPOJO(JSON_KEY_BLOG_ID, blogId);
        result.put(JSON_KEY_BLOG_NAME_ID, nameIdentifier);
        result.put(JSON_KEY_BLOG_TITLE, title);
        if (includeImagePath) {
            result.put(JSON_KEY_BLOG_IMAGE,
                    ImageUrlHelper.buildImageUrl("topic." + blogId, CoreImageType.entityProfile,
                            ImageSizeType.SMALL));
        }
        return result;
    }

    /**
     * Creates a JSON object describing a blog by its id, nameId and title
     * 
     * @param blogId
     *            the blog id
     * @param nameIdentifier
     *            the alias
     * @param title
     *            the title
     * @param description
     *            the blog description
     * @param includeImagePath
     *            whether to include the path to the image
     * @return the JSON object
     */
    public static ObjectNode createBlogSearchJSONResult(Long blogId, String nameIdentifier,
            String title, String description, boolean includeImagePath) {
        ObjectNode result = createBlogSearchJSONResult(blogId, nameIdentifier, title,
                includeImagePath);
        result.put(JSON_KEY_BLOG_DESCRIPTION, description);
        return result;
    }

    /**
     * Creates a JSON array with JSON objects of the blog list items.
     * 
     * @param list
     *            list to process
     * @param writeSummary
     *            whether to include a summary about the number of items in the list. The summary
     *            will be the first item in the list.
     * @param includeDescription
     *            whether to include the description
     * @param includeImagePath
     *            whether to include the path to the image
     * @return the JSON array
     */
    public static ArrayNode createBlogSearchJSONResult(PageableList<BlogData> list,
            boolean writeSummary, boolean includeDescription, boolean includeImagePath) {
        ArrayNode jsonResponse = JsonHelper.getSharedObjectMapper().createArrayNode();
        ObjectNode headerEntry;
        if (writeSummary) {
            headerEntry = UserSearchHelper.createSearchSummaryStatement(list);
            jsonResponse.add(headerEntry);
        }
        addBlogsToJsonResponse(jsonResponse, list, includeDescription, includeImagePath);
        return jsonResponse;
    }

    /**
     * runs a query for blogs
     * 
     * @param query
     *            The query to use.
     * @param queryParameters
     *            the query parameters to use
     * @param sortAndLocalizeResult
     *            whether to localize blog titles and sort the found blogs by title
     * @return the found blogs
     */
    public static <E extends QueryParameters> PageableList<BlogData> findBlogs(
            Query<BlogData, E> query, E queryParameters, boolean sortAndLocalizeResult) {
        PageableList<BlogData> results = ServiceLocator.instance()
                .getService(QueryManagement.class).executeQuery(query, queryParameters);
        if (sortAndLocalizeResult) {
            BlogManagementHelper.sortedBlogList(results);
        }

        return results;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private BlogSearchHelper() {
        // Do nothing
    }

}