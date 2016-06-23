package com.communote.server.web.fe.widgets.blog;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.PageableList;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.converter.blog.BlogToDetailBlogListItemConverter;
import com.communote.server.core.filter.listitems.blog.DetailBlogListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.blog.BlogQuery;
import com.communote.server.core.vo.query.blog.BlogQueryParameters;
import com.communote.server.core.vo.query.config.BlogQueryParametersConfigurator;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;

/**
 * Widget for displaying a list of blogs.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicListWidget extends AbstractPagedListWidget<BlogData> {

    /** */
    private final static QueryParametersParameterNameProvider NAME_PROVIDER = new FilterWidgetParameterNameProvider();

    /** per default the blog list overview shows 36 entries */
    private final static int DEFAULT_MAX_COUNT = 36;

    /** */
    private final BlogQueryParametersConfigurator<BlogQueryParameters> queryInstanceConfigurator =
            new BlogQueryParametersConfigurator<BlogQueryParameters>(NAME_PROVIDER,
                    DEFAULT_MAX_COUNT, false);

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicListWidget.class);

    private final TopicPermissionManagement topicPermissionManagement = ServiceLocator.findService(
            TopicPermissionManagement.class);

    /**
     * {@inheritDoc}
     */

    /**
     * @return the parents of the the blog
     * @throws BlogAccessException
     */
    public List<DetailBlogListItem> getParents() {
        Long blogId;
        List<DetailBlogListItem> parentsList = null;
        blogId = getLongParameter("parentTopicIds", -1L);

        if (blogId != -1) {
            BlogRightsManagement topicRightsManagement = ServiceLocator.instance()
                    .getService(BlogRightsManagement.class);
            BlogToDetailBlogListItemConverter<DetailBlogListItem> converter =
                    new BlogToDetailBlogListItemConverter<DetailBlogListItem>(
                            DetailBlogListItem.class, false, false, false, false, true,
                            topicRightsManagement, SessionHandler.instance()
                                    .getCurrentLocale(getRequest()));
            try {
                DetailBlogListItem list = ServiceLocator
                        .findService(BlogManagement.class)
                        .getBlogById(blogId, converter);
                if (list != null) {
                    parentsList = BlogManagementHelper.sortedBlogList(list.getParents());
                }
            } catch (BlogAccessException e) {
                // return empty result if no access
                LOGGER.debug(
                        "Current user has no access to the parent topics of topic with ID {}",
                        blogId);
            }

        }
        return parentsList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String type) {
        return "core.widget.topic.list";
    }

    @Override
    public PageableList<BlogData> handleQueryList() {
        BlogQueryParameters queryParameters = BlogQuery.DEFAULT_QUERY.createInstance();
        queryInstanceConfigurator.configure(getParameters(), queryParameters);
        QueryManagement queryManagement = ServiceLocator.findService(QueryManagement.class);
        queryParameters.setBlogIds(null);
        PageableList<BlogData> list = queryManagement.query(BlogQuery.DEFAULT_QUERY,
                queryParameters);
        getRequest().setAttribute("pageInformation", queryParameters.getPageInformation(list, 5));
        setResponseMetadata("nextOffset", list.getOffset() + list.size());
        return list;
    }

    /**
     * init the widget parameters to these values: maxCount = 36
     */
    @Override
    protected void initParameters() {
        setParameter(NAME_PROVIDER.getNameForMaxCount(), String.valueOf(DEFAULT_MAX_COUNT));
    }

    /**
     * @return whether the "add subtopic" should be shown
     */
    public boolean showAddSubtopic() {
        if (this.getBooleanParameter("showAdd", false)
                && topicPermissionManagement
                        .hasPermissionForCreation(TopicPermissionManagement.PERMISSION_CREATE_TOPIC)) {
            long parentTopicId = this.getLongParameter("parentTopicIds", -1);
            if (parentTopicId != -1) {
                return ServiceLocator.findService(BlogRightsManagement.class)
                        .currentUserHasManagementAccess(parentTopicId);
            }
        }
        return false;
    }

    /**
     * 
     * @return true if the create new topic options should be shown
     */
    public boolean showCreateNewTopic() {
        boolean showNew = ParameterHelper.getParameterAsBoolean(getRequest().getParameterMap(),
                "showNew", false);
        showNew = showNew
                && topicPermissionManagement
                        .hasPermissionForCreation(TopicPermissionManagement.PERMISSION_CREATE_TOPIC);
        return showNew;
    }
}
