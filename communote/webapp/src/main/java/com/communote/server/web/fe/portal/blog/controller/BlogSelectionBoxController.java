package com.communote.server.web.fe.portal.blog.controller;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.communote.common.util.PageableList;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.TimelineQueryParameters;
import com.communote.server.core.vo.query.blog.BlogQuery;
import com.communote.server.core.vo.query.blog.BlogQueryParameters;
import com.communote.server.core.vo.query.blog.TopicTimelineParameters;
import com.communote.server.core.vo.query.blog.UserTaggedBlogQuery;
import com.communote.server.core.vo.query.config.BlogQueryParametersConfigurator;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.web.fe.portal.blog.helper.BlogSearchHelper;

/**
 * Ajax functions for the blog selection box widgets and the blog auto suggest widget.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogSelectionBoxController extends MultiActionController {

    private static final BlogQuery<BlogData, BlogQueryParameters> BLOG_QUERY_DEFINITION =
            new BlogQuery<BlogData, BlogQueryParameters>(BlogData.class);
    private static final UserTaggedBlogQuery TIMELINE_TOPIC_QUERY_DEFINITION = new UserTaggedBlogQuery();

    private final FilterWidgetParameterNameProvider nameProvider = new FilterWidgetParameterNameProvider();

    /**
     * Searches for blogs and returns a JSON array containing JSON objects describing the found
     * blogs. The first element of the array is a summary JSON object holding the resultsTotal and
     * resultsReturned attributes.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             in case of an error
     */
    public void findBlogs(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        BlogQueryParameters instance = BLOG_QUERY_DEFINITION.createInstance();
        BlogQueryParametersConfigurator<BlogQueryParameters> configurator;
        configurator = new BlogQueryParametersConfigurator<BlogQueryParameters>(
                nameProvider, 0, false);
        configurator.configure(request.getParameterMap(), instance);
        PageableList<BlogData> results = BlogSearchHelper.findBlogs(BLOG_QUERY_DEFINITION,
                instance, true);
        boolean noSummary = ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                "noSummary", false);
        response.setContentType("application/json");
        JsonHelper.writeJsonTree(response.getWriter(),
                BlogSearchHelper.createBlogSearchJSONResult(results, !noSummary, false, true));
    }

    /**
     * Search for topics in which the notes matching the current filter parameters were published.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             in case of an error
     */
    public void findTimelineTopics(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        TopicTimelineParameters queryParameters = TIMELINE_TOPIC_QUERY_DEFINITION.createInstance();
        TimelineQueryParametersConfigurator<TimelineQueryParameters> configurator;
        configurator = new TimelineQueryParametersConfigurator<TimelineQueryParameters>(
                nameProvider);
        configurator.configure(request.getParameterMap(), queryParameters);
        PageableList<BlogData> results = BlogSearchHelper.findBlogs(
                TIMELINE_TOPIC_QUERY_DEFINITION,
                queryParameters, true);
        response.setContentType("application/json");
        JsonHelper.writeJsonTree(response.getWriter(),
                BlogSearchHelper.createBlogSearchJSONResult(results, true, false, true));
    }

    /**
     * Gets the last used blogs.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void getLastUsedBlogs(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Collection<BlogData> list = ServiceLocator.instance().getService(BlogManagement.class)
                .getLastUsedBlogs(5, false);
        response.setContentType("application/json");
        JsonHelper.writeJsonTree(response.getWriter(),
                BlogSearchHelper.createBlogSearchJSONResult(list, false, true));
    }

    /**
     * Gets the most used blogs.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void getMostUsedBlogs(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Collection<BlogData> list = ServiceLocator
                .instance().getService(BlogManagement.class)
                .getMostUsedBlogs(10,
                        ServletRequestUtils.getBooleanParameter(request, "sortByTitle", false));
        response.setContentType("application/json");
        JsonHelper.writeJsonTree(response.getWriter(),
                BlogSearchHelper.createBlogSearchJSONResult(list, false, true));
    }

    /**
     * Get the blogs the current user is manager in
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             in case of an error
     */
    public void getMyBlogs(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        BlogQueryParameters instance = BLOG_QUERY_DEFINITION.createInstance();
        BlogQueryParametersConfigurator.configureForAllManagedBlogs(instance);
        instance.sortByNameAsc();

        QueryManagement queryManagement = ServiceLocator.findService(QueryManagement.class);
        PageableList<BlogData> list = queryManagement.executeQuery(BLOG_QUERY_DEFINITION,
                instance);
        response.setContentType("application/json");
        JsonHelper.writeJsonTree(response.getWriter(),
                BlogSearchHelper.createBlogSearchJSONResult(list, false, true, true));
    }
}
