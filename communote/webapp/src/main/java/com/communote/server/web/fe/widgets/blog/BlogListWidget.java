package com.communote.server.web.fe.widgets.blog;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.communote.common.util.PageableList;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.blog.TopicTimelineParameters;
import com.communote.server.core.vo.query.blog.UserTaggedBlogQuery;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.model.note.NoteStatus;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;

/**
 * Widget for filtering for topics.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO should be named TopicFilter
public class BlogListWidget extends AbstractPagedListWidget<BlogData> {

    private final static FilterWidgetParameterNameProvider NAME_PROVIDER = FilterWidgetParameterNameProvider.INSTANCE;

    /**
     * returns 'blog'
     *
     * @return the message key suffix
     * @see com.communote.server.web.fe.widgets.PagedWidget#getDefaultPagingMessageKeySuffix()
     */
    @Override
    public String getDefaultPagingMessageKeySuffix() {
        return "blog";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.blog.blogList";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PageableList<BlogData> handleQueryList() {
        // alphabetic is default sort mode
        boolean sortByLatestNote = ParameterHelper.getParameterAsBoolean(getParameters(),
                NAME_PROVIDER.getNameForTopicSortByLatestNote(), false);
        getRequest().setAttribute("sortAlphabetic", !sortByLatestNote);
        UserTaggedBlogQuery query = QueryDefinitionRepository.instance()
                .getQueryDefinition(UserTaggedBlogQuery.class);
        TopicTimelineParameters queryParameters = query.createInstance();
        TimelineQueryParametersConfigurator<TopicTimelineParameters> qic =
                new TimelineQueryParametersConfigurator<TopicTimelineParameters>(
                        NAME_PROVIDER);
        qic.configure(getParameters(), queryParameters);
        TaggingCoreItemUTPExtension utpExt = queryParameters
                .getTypeSpecificExtension();
        utpExt.setIncludeEmptyBlogs(true);

        if (sortByLatestNote) {
            queryParameters.sortByLatestNote();
        } else {
            // default
            queryParameters.sortByBlogNameAsc();
        }
        queryParameters.setExcludeNoteStatus(new NoteStatus[] { NoteStatus.AUTOSAVED });

        List<BlogData> result = ServiceLocator.findService(QueryManagement.class)
                .executeQuery(query, queryParameters);
        int offset = queryParameters.getResultSpecification().getOffset();
        boolean singleTopic = utpExt.getBlogFilter() != null && utpExt.getBlogFilter().length == 1;

        // remove the provided topic if it is the only response for a query that should fetch the
        // sub topics
        if (result.size() == 1 && offset == 0 && utpExt.isIncludeChildTopics() && singleTopic
                && result.get(0).getId().equals(utpExt.getBlogFilter()[0])) {
            result.clear();
        } else {
            if (!sortByLatestNote) {
                result = BlogManagementHelper.sortedBlogList(result);
            }
        }
        if ("paging".equals(getRequest().getParameter("loadMoreMode"))) {
            setPageInformation(queryParameters, (PageableList<BlogData>) result);
        }
        return (PageableList<BlogData>) result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        setParameter(NAME_PROVIDER.getNameForTags(), StringUtils.EMPTY);
        setParameter(NAME_PROVIDER.getNameForOffset(), "0");
        setParameter(NAME_PROVIDER.getNameForMaxCount(), "10");
    }

}
