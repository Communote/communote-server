package com.communote.server.web.api.service.filter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import liquibase.util.StringUtils;

import com.communote.common.util.PageableList;
import com.communote.common.util.Pair;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.core.vo.query.TimelineQueryParameters;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.blog.TopicTimelineParameters;
import com.communote.server.core.vo.query.blog.UserTaggedBlogQuery;
import com.communote.server.web.api.service.post.convert.BlogListItemConverter;

/**
 * Api Controller to filter for blogs
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class BlogFilterApiController extends BaseFilterApiController {

    private static UserTaggedBlogQuery BLOG_QUERY = QueryDefinitionRepository.instance()
            .getQueryDefinition(UserTaggedBlogQuery.class);

    @Override
    protected QueryResultConverter createQueryConverter(HttpServletRequest request) {
        return new BlogListItemConverter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Pair<UserTaggedBlogQuery, TimelineQueryParameters> createQueryInstance(
            HttpServletRequest request) {
        return new Pair<UserTaggedBlogQuery, TimelineQueryParameters>(
                BLOG_QUERY, new TopicTimelineParameters());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postConfigureQueryInstance(TimelineQueryParameters queryInstance) {
        TopicTimelineParameters blogQueryInstance = (TopicTimelineParameters) queryInstance;
        if (StringUtils.trimToNull(blogQueryInstance.getSortString()) == null) {
            blogQueryInstance.sortByBlogNameAsc();
        }
        queryInstance.getTypeSpecificExtension().setUserId(SecurityHelper.getCurrentUserId());
        queryInstance.getTypeSpecificExtension().setTopicAccessLevel(TopicAccessLevel.READ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List postProcessList(HttpServletRequest request, PageableList list) {
        return new ArrayList(list);
    }

}
