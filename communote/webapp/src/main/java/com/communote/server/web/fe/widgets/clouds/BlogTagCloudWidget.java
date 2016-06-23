package com.communote.server.web.fe.widgets.clouds;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.filter.listitems.NormalizedRankListItem;
import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.vo.query.blog.BlogQueryParameters;
import com.communote.server.core.vo.query.config.BlogQueryParametersConfigurator;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.converters.RankTagListItemToRankTagListItemQueryResultConverter;
import com.communote.server.core.vo.query.tag.BlogRankQuery;
import com.communote.server.core.vo.query.tag.BlogTagQueryParameters;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;


/**
 * Widget for displaying a TagCloud of blog tags.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogTagCloudWidget extends
        AbstractPagedListWidget<NormalizedRankListItem<RankTagListItem>> {

    private static final String PARAMETER_SELECTED_TAGS = "selectedTags";

    /**
     * Parameter, which defines, that selected tags should be hidden or not (default is
     * <code>true</code> for hidden.
     */
    public static final String PARAMETER_HIDE_SELECTED_TAGS = "hideSelectedTags";

    /** */
    private final BlogRankQuery query = new BlogRankQuery();

    /** */
    private final static QueryParametersParameterNameProvider NAME_PROVIDER = new FilterWidgetParameterNameProvider();

    /** per default the tag list of blog tags shows 50 entries */
    private final static int DEFAULT_MAX_COUNT = 50;

    private final BlogQueryParametersConfigurator<BlogQueryParameters> queryInstanceConfigurator =
            new BlogQueryParametersConfigurator<BlogQueryParameters>(NAME_PROVIDER,
                    DEFAULT_MAX_COUNT,
                    false);

    /**
     * {@inheritDoc}
     * 
     * @return "core.widget.tag.cloud"
     */
    @Override
    public String getTile(String type) {
        return "core.widget.filter.blog.tag.cloud";
    }

    /**
     * query the tag list on the tagging core api
     * 
     * @return list of RankedTag elements
     */
    @Override
    public PageableList<NormalizedRankListItem<RankTagListItem>> handleQueryList() {
        BlogTagQueryParameters queryParameters = query.createInstance();
        queryInstanceConfigurator.configure(getParameters(), queryParameters);
        String tags = getParameter(NAME_PROVIDER.getNameForTags());
        if (StringUtils.isNotBlank(tags)) {
            setParameter(PARAMETER_SELECTED_TAGS, "," + tags + ",");
        }
        queryParameters
                .setHideSelectedTags(getBooleanParameter(PARAMETER_HIDE_SELECTED_TAGS, true));
        Locale locale = SessionHandler.instance()
                .getCurrentLocale(getRequest());
        QueryManagement queryManagement = ServiceLocator.findService(QueryManagement.class);
        PageableList<NormalizedRankListItem<RankTagListItem>> normalizedItems = queryManagement
                .query(query, queryParameters,
                        new RankTagListItemToRankTagListItemQueryResultConverter(locale,
                                ServiceLocator
                                        .instance().getService(TagManagement.class)));
        return normalizedItems;
    }

    /**
     * init the widget parameters to these values: maxCount = 0
     */
    @Override
    protected void initParameters() {
        setParameter(NAME_PROVIDER.getNameForMaxCount(), "0");
        setParameter(NAME_PROVIDER.getNameForCheckAtLeastMoreResults(), "0");
    }

    /**
     * @return See {@link TagStoreManagement#hasMoreThanOneTagStore(TagStoreType)}
     */
    public boolean moreThanOneTagStore() {
        return ServiceLocator.instance().getService(TagStoreManagement.class)
                .hasMoreThanOneTagStore(TagStoreType.Types.BLOG);
    }
}
