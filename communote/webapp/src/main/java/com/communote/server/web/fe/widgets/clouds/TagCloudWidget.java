package com.communote.server.web.fe.widgets.clouds;

import static com.communote.common.util.DateHelper.DAYS;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import com.communote.common.util.PageableList;
import com.communote.common.util.Pair;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.filter.SortType;
import com.communote.server.core.filter.SortedResultSpecification;
import com.communote.server.core.filter.listitems.NormalizedRankListItem;
import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagParser;
import com.communote.server.core.tag.TagParserFactory;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.converters.RankTagListItemToRankTagListItemQueryResultConverter;
import com.communote.server.core.vo.query.tag.AbstractTagQuery;
import com.communote.server.core.vo.query.tag.RankTagQuery;
import com.communote.server.core.vo.query.tag.RankTagQueryParameters;
import com.communote.server.core.vo.query.tag.RelatedRankTagQuery;
import com.communote.server.model.note.NoteStatus;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;


/**
 * Widget for displaying a TagCloud. If there is a logged in user, its personal tag cloud is
 * displayed. Otherwise the general cloud is displayed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagCloudWidget extends
        AbstractPagedListWidget<NormalizedRankListItem<RankTagListItem>> {

    private final static FilterWidgetParameterNameProvider NAME_PROVIDER = FilterWidgetParameterNameProvider.INSTANCE;

    /**
     * Parameter, which defines, that selected tags should be hidden or not (default is
     * <code>true</code> for hidden.
     */
    public static final String TAG_CLOUD_HIDE_SELECTED_TAGS = "hideSelectedTags";

    /**
     * the widget parameter for the heading of the tag cloud
     */
    public static final String TAG_CLOUD_HEADING = "tagCloudHeadline";
    /** Flag to enable/disable the tag cloud title, default to true */
    public static final String TAG_CLOUD_SHOW_TITLE = "showTitle";

    /** Flag to enable / disable the tag history, default to true */
    public static final String TAG_CLOUD_SHOW_HISTORY = "showHistory";

    /**
     * the widget attribute for the current filtered tags as <code>String[]</code>
     */
    public static final String TAG_CLOUD_ATTR_FILTER_PREFIX = "tagCloudFilterPrefix";

    /**
     * the widget attribute for the tag hierarchy as <code>HierarchyTagEntry[]</code>
     */
    public static final String TAG_CLOUD_ATTR_HIERARCHY_ENTRIES = "tagCloudHierarchyEntries";

    private static final TagParser TAG_PARSER = TagParserFactory.instance().getDefaultTagParser();

    /**
     * Parameter for the number of last days to consider (e.g. only retrieve the tags of the last 7
     * days); value must be an integer
     */
    public static final String PARAM_NUMBER_OF_LAST_DAYS = "numberOfLastDays";

    /**
     * Parameter for the group id
     */
    public static final String PARAM_GROUP_ID = "groupId";

    /**
     * Parameter for the tag cloud mode; see TagCloudMode for possible values
     */
    public final static String PARAM_TAG_CLOUD_MODE = "tagCloudMode";

    /** The Constant RELATED_RANK_TAG_QUERY_DEFINITION. */
    private static final RelatedRankTagQuery RELATED_RANK_TAG_QUERY_DEFINITION = QueryDefinitionRepository
            .instance().getQueryDefinition(RelatedRankTagQuery.class);

    /** The Constant RANK_TAG_QUERY_DEFINITION. */
    private static final RankTagQuery RANK_TAG_QUERY_DEFINITION = QueryDefinitionRepository
            .instance().getQueryDefinition(RankTagQuery.class);

    /**
     * Build the tag entries out of the related tags
     * 
     * @param relatedTags
     *            the related tags
     * @return the entries
     */
    private HierarchyTagEntry[] buildEntries(String[] relatedTags) {
        ArrayList<HierarchyTagEntry> aList = new ArrayList<HierarchyTagEntry>();

        String filter = StringUtils.EMPTY;
        for (String tag : relatedTags) {
            filter = TAG_PARSER.combineTags(filter, tag);
            aList.add(new HierarchyTagEntry(tag, filter));
        }
        return aList.toArray(new HierarchyTagEntry[aList.size()]);
    }

    /**
     * @param interval
     *            The interval.
     * @return the configured tagging instance
     */
    protected Pair<RankTagQueryParameters, AbstractTagQuery<RankTagListItem>> configureQueryInstance(
            long interval) {
        RankTagQueryParameters tagQueryInstance;

        String tagFilter = getParameter(NAME_PROVIDER.getNameForTags());
        Pair<RankTagQueryParameters, AbstractTagQuery<RankTagListItem>> result;
        if (StringUtils.isNotBlank(tagFilter)
                || StringUtils.isNotBlank(getParameter(NAME_PROVIDER.getNameForTagIds()))) {
            tagQueryInstance = new RankTagQueryParameters(RELATED_RANK_TAG_QUERY_DEFINITION);
            result = new Pair<RankTagQueryParameters, AbstractTagQuery<RankTagListItem>>(
                    tagQueryInstance,
                    RELATED_RANK_TAG_QUERY_DEFINITION);
        } else {
            tagQueryInstance = new RankTagQueryParameters(RANK_TAG_QUERY_DEFINITION);
            result = new Pair<RankTagQueryParameters, AbstractTagQuery<RankTagListItem>>(
                    tagQueryInstance,
                    RANK_TAG_QUERY_DEFINITION);
        }
        tagQueryInstance.setLimitResultSet("trend".equals(getParameter("mode", "trend")));
        TimelineQueryParametersConfigurator<RankTagQueryParameters> qic =
                new TimelineQueryParametersConfigurator<RankTagQueryParameters>(NAME_PROVIDER);

        qic.configure(getParameters(), tagQueryInstance);

        SortedResultSpecification resultSpec = getSortedResultSpecification();
        resultSpec.setSortType(SortType.COUNT);
        tagQueryInstance.setResultSpecification(resultSpec);
        tagQueryInstance
                .setHideSelectedTags(getBooleanParameter(TAG_CLOUD_HIDE_SELECTED_TAGS, true));
        // exclude all unpublished notes
        tagQueryInstance.setExcludeNoteStatus(new NoteStatus[] { NoteStatus.AUTOSAVED });

        // configure the lower tag dates
        if (interval > 0) {
            Date lowerDate = tagQueryInstance.getLowerTagDate();
            Date upperDate = tagQueryInstance.getUpperTagDate();
            if (upperDate == null) {
                // lower tag date is set
                Date startIntervallDate = new Date(new Date().getTime() - interval);
                if (lowerDate == null || lowerDate.compareTo(startIntervallDate) < 0) {
                    tagQueryInstance.setLowerTagDate(startIntervallDate);
                }
            } else {
                Date startIntervallDate = new Date(upperDate.getTime() - interval);
                if (lowerDate == null || lowerDate.compareTo(startIntervallDate) < 0) {
                    tagQueryInstance.setLowerTagDate(startIntervallDate);
                }
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @return "core.widget.tag.cloud"
     */
    @Override
    public String getTile(String type) {
        return "core.widget.filter.tag.cloud";
    }

    /**
     * query the tag list on the tagging core api
     * 
     * @return list of RankedTag elements
     */
    @Override
    public PageableList<NormalizedRankListItem<RankTagListItem>> handleQueryList() {

        int lastDays = getIntParameter(PARAM_NUMBER_OF_LAST_DAYS, 0);

        // user must be logged in
        SecurityHelper.assertCurrentUserId();

        String tagCloudModeSrc = getParameter(PARAM_TAG_CLOUD_MODE);
        TagCloudMode tagCloudMode = TagCloudMode.valueOf(tagCloudModeSrc);

        String msgKey;
        long interval;
        switch (tagCloudMode) {
        case MyTags:
            interval = -1;
            msgKey = "widget.tag.cloud.heading.my.tags";
            break;
        case PopularTags:
            interval = lastDays * DAYS;
            msgKey = "widget.tag.cloud.heading.popular.tags";
            break;
        case SomeTags:
        default:
            interval = lastDays * DAYS;
            msgKey = "widget.tag.cloud.heading.tags";
            break;
        }
        setParameter(TAG_CLOUD_HEADING, MessageHelper.getText(this.getRequest(), msgKey));

        Pair<RankTagQueryParameters, AbstractTagQuery<RankTagListItem>> pair = configureQueryInstance(interval);
        if (Boolean.parseBoolean(getParameter("isStaticCloud"))) {
            pair.getLeft().setLogicalTags(null);
        }
        Locale locale = SessionHandler.instance().getCurrentLocale(getRequest());
        PageableList<NormalizedRankListItem<RankTagListItem>> normalizedItems = internalFindTags(
                pair.getRight(), pair.getLeft(), locale);

        // set the related / filtered tags (do not use the logicaltagformula but the filter param)
        String tags = getParameter(NAME_PROVIDER.getNameForTags());
        if (StringUtils.isNotBlank(tags)) {
            String[] relatedTags = TAG_PARSER.parseTags(tags);
            if (relatedTags.length > 0) {
                setTagHierarchy(relatedTags);
            }
        }
        setParameter(NAME_PROVIDER.getNameForMaxCount(),
                getRequest().getParameter(NAME_PROVIDER.getNameForMaxCount()));
        return normalizedItems;
    }

    /**
     * Initialize the widget parameters to these values: filter = '' offset = 0 maxCount = 30
     */
    @Override
    protected void initParameters() {
        setParameter(NAME_PROVIDER.getNameForTags(), StringUtils.EMPTY);
        setParameter(NAME_PROVIDER.getNameForOffset(), "0");
        setParameter(NAME_PROVIDER.getNameForMaxCount(), "0");
        setParameter(PARAM_NUMBER_OF_LAST_DAYS, "0");
        setParameter(PARAM_TAG_CLOUD_MODE, TagCloudMode.SomeTags.name());
    }

    /**
     * Sets the properties which are common for UTRs and UTPs and executes the tag query.
     * 
     * @param query
     *            The query to use.
     * @param queryParameters
     *            the preinitialized query instance
     * @param locale
     *            The locale.
     * @return the ranked tag list
     */
    private PageableList<NormalizedRankListItem<RankTagListItem>> internalFindTags(
            AbstractTagQuery<RankTagListItem> query, RankTagQueryParameters queryParameters,
            Locale locale) {
        SortedResultSpecification sortedResultSpecification =
                (SortedResultSpecification) queryParameters.getResultSpecification();
        queryParameters.setResultSpecification(sortedResultSpecification);
        // if the most used tag are limited we must sort by the count to get the
        // top tags
        if (sortedResultSpecification != null
                && sortedResultSpecification.getNumberOfElements() > 0) {
            queryParameters.sortByTagCountDesc();
        } else {
            queryParameters.sortByTagNameAsc();
        }

        PageableList<NormalizedRankListItem<RankTagListItem>> results = ServiceLocator
                .instance().getService(QueryManagement.class)
                .query(query, queryParameters,
                        new RankTagListItemToRankTagListItemQueryResultConverter(locale,
                                ServiceLocator.instance().getService(TagManagement.class)));
        return results;
    }

    /**
     * Gets the flag to enable/disable the tag cloud history.
     * 
     * @return true, if the tag history should be displayed
     */
    public boolean isShowHistory() {
        return BooleanUtils.toBoolean(getParameter(TAG_CLOUD_SHOW_HISTORY, "true"));
    }

    /**
     * Gets the flag to enable/disable the tag cloud title.
     * 
     * @return true, if the tag hisotry should be displayed.
     */
    public boolean isShowTitle() {
        return BooleanUtils.toBoolean(getParameter(TAG_CLOUD_SHOW_TITLE, "true"));
    }

    /**
     * @return See {@link TagStoreManagement#hasMoreThanOneTagStore(TagStoreType)}
     */
    public boolean moreThanOneTagStore() {
        return ServiceLocator.instance().getService(TagStoreManagement.class)
                .hasMoreThanOneTagStore(TagStoreType.Types.NOTE);
    }

    /**
     * Set the tag hierarchy
     * 
     * @param relatedTags
     *            the related tags
     */
    private void setTagHierarchy(String[] relatedTags) {
        HierarchyTagEntry[] entries = buildEntries(relatedTags);

        String filterPrefix = entries[entries.length - 1].getFilter();
        filterPrefix = TAG_PARSER.combineTags(filterPrefix, StringUtils.EMPTY);

        setAttribute(TAG_CLOUD_ATTR_HIERARCHY_ENTRIES, entries);
        setAttribute(TAG_CLOUD_ATTR_FILTER_PREFIX, filterPrefix);
    }
}
