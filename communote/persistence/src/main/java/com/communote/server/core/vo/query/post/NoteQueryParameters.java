package com.communote.server.core.vo.query.post;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.vo.query.DiscussionFilterMode;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.TimelineQueryParameters;
import com.communote.server.core.vo.query.filter.PropertyFilter;
import com.communote.server.model.note.NoteStatus;

/**
 * Query instance to find notes
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteQueryParameters extends TimelineQueryParameters {

    /**
     * Create a new NoteQueryParameters by cloning others.
     *
     * @param parameters
     *            The parameters to clone.
     * @return The {@link NoteQueryParameters} with the filters set as in queryInstance
     */
    public static NoteQueryParameters clone(NoteQueryParameters parameters) {
        NoteQueryParameters noteQueryParameters = clone((TimelineQueryParameters) parameters);
        noteQueryParameters.setOriginalPostId(parameters.getOriginalPostId());
        noteQueryParameters.setRetrieveOnlyNotesAfterDate(parameters
                .getRetrieveOnlyNotesAfterDate());
        noteQueryParameters.setRetrieveOnlyNotesAfterId(parameters.getRetrieveOnlyNotesAfterId());
        noteQueryParameters.setRetrieveOnlyNotesBeforeDate(parameters
                .getRetrieveOnlyNotesBeforeDate());
        noteQueryParameters.setRetrieveOnlyNotesBeforeId(parameters.getRetrieveOnlyNotesBeforeId());
        noteQueryParameters.setSortByDate(parameters.getSortByDate());
        noteQueryParameters.setSortById(parameters.getSortById());
        noteQueryParameters.setStatus(parameters.getStatus());
        return noteQueryParameters;
    }

    /**
     * Create a new NoteQueryInstance that takes the common {@link TimelineQueryParameters}
     * Parameters.
     *
     * @param queryParameters
     *            The query instance containing all parameters.
     * @return The {@link NoteQueryParameters} with the filters set as in queryInstance
     */
    public static NoteQueryParameters clone(TimelineQueryParameters queryParameters) {
        NoteQueryParameters noteQueryParameters = new NoteQueryParameters();
        noteQueryParameters.setSortByDate(OrderDirection.DESCENDING);
        for (PropertyFilter propertyFilter : queryParameters.getPropertyFilters()) {
            noteQueryParameters.addPropertyFilter(propertyFilter);
        }
        for (Long tagId : queryParameters.getTagIds()) {
            noteQueryParameters.addTagId(tagId);
        }
        for (Map.Entry<String, Set<String>> entry : queryParameters.getTagStoreTagIds().entrySet()) {
            noteQueryParameters.addTagStoreTagId(entry.getKey(), entry.getValue());
        }
        noteQueryParameters.setDirectMessage(queryParameters.isDirectMessage());
        noteQueryParameters.setDiscussionId(queryParameters.getDiscussionId());
        noteQueryParameters.setExcludeNoteStatus(queryParameters.getExcludeNoteStatus());
        noteQueryParameters.setExcludeStatusFilter(queryParameters.getExcludeStatusFilter());
        noteQueryParameters.setFavorites(queryParameters.isFavorites());
        noteQueryParameters.setFullTextSearchFilters(queryParameters.getFullTextSearchFilters());
        noteQueryParameters.setHideSelectedTags(queryParameters.isHideSelectedTags());

        noteQueryParameters.setIncludeStatusFilter(queryParameters.getIncludeStatusFilter());
        noteQueryParameters.setLastModifiedAfter(queryParameters.getLastModifiedAfter());
        noteQueryParameters.setMatchMode(queryParameters.getMatchMode());
        noteQueryParameters.setIncludeStartDate(queryParameters.isIncludeStartDate());
        noteQueryParameters.setLanguageCode(queryParameters.getLanguageCode());
        noteQueryParameters.setLogicalTags(queryParameters.getLogicalTags());
        noteQueryParameters.setLowerTagDate(queryParameters.getLowerTagDate());
        noteQueryParameters.setNoteId(queryParameters.getNoteId());
        noteQueryParameters.setResourceId(queryParameters.getResourceId());
        noteQueryParameters.setRetrieveOnlyFollowedItems(queryParameters
                .isRetrieveOnlyFollowedItems());
        noteQueryParameters.setRetrieveOnlyFollowedUsers(queryParameters
                .isRetrieveOnlyFollowedUsers());
        noteQueryParameters.getRolesToExclude().addAll(queryParameters.getRolesToExclude());
        noteQueryParameters.getRolesToInclude().addAll(queryParameters.getRolesToInclude());
        noteQueryParameters.setDiscussionFilterMode(noteQueryParameters.getDiscussionFilterMode());
        noteQueryParameters.setTagIds(queryParameters.getTagIds());
        noteQueryParameters.setTagPrefix(queryParameters.getTagPrefix());
        noteQueryParameters.setTagStoreAliases(queryParameters.getTagStoreAliases());
        noteQueryParameters.setTimeLimitsQueryRequired(queryParameters.isTimeLimitsQueryRequired());
        noteQueryParameters.setTypeSpecificExtension(queryParameters.getTypeSpecificExtension());
        noteQueryParameters.setUpperTagDate(queryParameters.getUpperTagDate());
        noteQueryParameters.setUserAliases(queryParameters.getUserAliases());
        noteQueryParameters.setUserIds(queryParameters.getUserIds());
        noteQueryParameters.setUserSearchFilters(queryParameters.getUserSearchFilters(),
                !queryParameters.isIgnoreEmailField());
        noteQueryParameters.setUserToBeNotified(queryParameters.getUserToBeNotified());
        noteQueryParameters.setUserIdsToIgnore(queryParameters.getUserIdsToIgnore());
        noteQueryParameters.setTypeSpecificExtension(queryParameters.getTypeSpecificExtension());
        noteQueryParameters.setMinimumRank(queryParameters.getMinimumRank());
        noteQueryParameters.setMaximumRank(queryParameters.getMaximumRank());
        noteQueryParameters.setSortByDayDateAndRank(queryParameters.isSortByDayDateAndRank());
        return noteQueryParameters;
    }

    private static final int DEFAULT_NUMBER_OF_NOTES = 25;

    private static final int MAX_NUMBER_OF_NOTES = 400;

    /**
     * Parameter name in Query for the NoteStatus
     */
    public static final String PARAM_NOTE_STATUS = "noteStatus";

    private TimelineFilterViewType timelineFilterViewType = TimelineFilterViewType.CLASSIC;

    private OrderDirection sortByDate = OrderDirection.DESCENDING;

    private OrderDirection sortById = OrderDirection.DESCENDING;

    private NoteStatus status = NoteStatus.PUBLISHED;

    private String originalPostId;

    private Long retrieveOnlyNotesBeforeId;
    private Date retrieveOnlyNotesBeforeDate;

    private Long retrieveOnlyNotesAfterId;
    private Date retrieveOnlyNotesAfterDate;

    /**
     * Construct me with the right query definition
     */
    public NoteQueryParameters() {
        super(new TaggingCoreItemUTPExtension());
        this.setLimitResultSet(false);
    }

    /**
     * The discussion filter mode that should be used for filtering.
     *
     * @return the discussionFilterMode
     */
    @Override
    public DiscussionFilterMode getDiscussionFilterModeForFiltering() {
        // in case the filtering for the root notes is used than we should filter for all notes but
        // only get the root ones
        return isDiscussionDependentRootNotesFilter() ? DiscussionFilterMode.ALL
                : getDiscussionFilterMode();
    }

    /**
     * @return the originalPostId
     */
    public String getOriginalPostId() {
        return originalPostId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = super.getParameters();
        if (this.getStatus() != null) {
            parameters.put(PARAM_NOTE_STATUS, this.getStatus());
        }
        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSpecification getResultSpecification() {
        ResultSpecification resultSpecification = super.getResultSpecification();
        if (resultSpecification == null) {
            this.setResultSpecification(new ResultSpecification(0, DEFAULT_NUMBER_OF_NOTES));
            resultSpecification = super.getResultSpecification();
        }
        if (resultSpecification.getNumberOfElements() <= 0
                || resultSpecification.getNumberOfElements() > MAX_NUMBER_OF_NOTES) {
            // avoid a denial of service attack by limiting the number of notes
            resultSpecification.setNumberOfElements(MAX_NUMBER_OF_NOTES);
        }
        return resultSpecification;
    }

    /**
     * @return The date to be used for getting a newer note.
     */
    public Date getRetrieveOnlyNotesAfterDate() {
        return retrieveOnlyNotesAfterDate;
    }

    /**
     * @return tId of the note, which defines an upper bound for returning notes or
     *         <code>null</code> for no boundary.
     */
    public Long getRetrieveOnlyNotesAfterId() {
        return retrieveOnlyNotesAfterId;
    }

    /**
     * @return The date to be used for getting an older note. If null the value returned by
     *         {@link #getRetrieveOnlyNotesBeforeId()} will be ignored.
     */
    public Date getRetrieveOnlyNotesBeforeDate() {
        return retrieveOnlyNotesBeforeDate;
    }

    /**
     * @return Id of the note, which defines an lower bound for returning notes or <code>null</code>
     *         for no boundary. If null the value returned by
     *         {@link #getRetrieveOnlyNotesBeforeDate()} will be ignored. In case the discussion
     *         filter mode is set to {@link DiscussionFilterMode#IS_ROOT} this ID should be a
     *         discussion ID.
     */
    public Long getRetrieveOnlyNotesBeforeId() {
        return retrieveOnlyNotesBeforeId;
    }

    /**
     * @return If set, this returns the direction of the date sorting. If no sorting is desired,
     *         null will be returned.
     */
    public OrderDirection getSortByDate() {
        return sortByDate;
    }

    /**
     * @return If set, this returns the direction of the id sorting. If no sorting is desired, null
     *         will be returned.
     */
    public OrderDirection getSortById() {
        return sortById;
    }

    /**
     * @return the status
     */
    public NoteStatus getStatus() {
        return status;
    }

    /**
     * See also {@link #isDiscussionIndependentFilter()}
     *
     * @return true if it is a filtering for root notes
     */
    public boolean isDiscussionDependentRootNotesFilter() {
        return DiscussionFilterMode.IS_ROOT.equals(this.getDiscussionFilterMode())
                && !isDiscussionIndependentFilter();
    }

    /**
     * Return whether the current filters are discussion independent which means that, no matter
     * what value a filter has the result set will always contain all the notes of a discussion the
     * current user is allowed to read. For example the filter for a topic is discussion independent
     * because all notes of one discussion are in the same topic. The same applies for the start and
     * end date filter because the filtering will not use the date of the notes but the
     * lastDiscussionNoteCreationDate of the discussion. In contrast to these filters the tag filter
     * for instance is not discussion independent because some notes of the discussion can have
     * other, more or less tags.
     *
     * @return true if the feed filters are discussion independent
     */
    public boolean isDiscussionIndependentFilter() {
        boolean isContinious = !this.isRetrieveOnlyFollowedItems()
                && !this.isFavorites() && !this.isNotificationFeed();
        if (isContinious) {
            isContinious = !this.isFeedFiltered(true, true, true);
        }
        return isContinious;
    }

    /**
     * @return true if the user uses an unfiltered favorite feed
     */
    public boolean isFavoriteFeedAndUnfiltered() {
        return isFavorites() && !isFeedFiltered(false, false, false);
    }

    /**
     * Checks if the user has filtered the message feed by standard filters.
     *
     * @return True, if the user set a filter, else false.
     */
    public boolean isFeedFiltered() {
        return StringUtils.isNotBlank(this.getTagPrefix())
                || ArrayUtils.isNotEmpty(this.getFullTextSearchFilters())
                || ArrayUtils.isNotEmpty(this.getUserSearchFilters())
                || ArrayUtils.isNotEmpty(this.getUserIds())
                || ArrayUtils.isNotEmpty(this.getUserIdsToIgnore())
                || CollectionUtils.isNotEmpty(this.getPropertyFilters())
                || CollectionUtils.isNotEmpty(this.getTagIds());
    }

    /**
     * Checks if the user has filtered the message feed allowing to ignore specific parameters
     *
     * @param allowBlogFilter
     *            true to ignore the topic filter when checking for set filters
     * @param allowDateFilter
     *            true to ignore the date filter when checking for set filters
     * @param allowDiscussionIdFilter
     *            true to ignore the discussionId filter when checking for set filters
     *
     * @return True, if there is a filter, false otherwise.
     */
    public boolean isFeedFiltered(boolean allowBlogFilter, boolean allowDateFilter,
            boolean allowDiscussionIdFilter) {
        return isFeedFiltered()
                || !allowDateFilter
                && (this.getLowerTagDate() != null || this.getUpperTagDate() != null)
                || !allowBlogFilter && ArrayUtils.isNotEmpty(getTypeSpecificExtension()
                        .getBlogFilter())
                        || !allowDiscussionIdFilter && getDiscussionId() != null;
    }

    /**
     * @return True, if the following feed is set and there are no filters set for the following
     *         feed.
     */
    public boolean isFollowingFeedAndUnfiltered() {
        return isRetrieveOnlyFollowedItems() && !isFeedFiltered(false, false, false);
    }

    /**
     *
     * @return True, if the user notification filter is set.
     */
    public boolean isNotificationFeed() {
        Long[] users = this.getUserToBeNotified();
        return users != null && users.length > 0;
    }

    /**
     * @return true if it is a threaded view
     */
    public boolean isThreadedView() {
        if (getDiscussionId() != null) {
            return ArrayUtils.isEmpty(getFullTextSearchFilters())
                    && ArrayUtils.isEmpty(getUserIds())
                    && ArrayUtils.isEmpty(getUserIdsToIgnore())
                    && getLogicalTags() == null
                    && StringUtils.isEmpty(getTagPrefix()) && getLastModifiedAfter() == null
                    && getLowerTagDate() == null && getUpperTagDate() == null;
        }

        return false;
    }

    /**
     * @param originalPostId
     *            the originalPostId to set
     */
    public void setOriginalPostId(String originalPostId) {
        this.originalPostId = originalPostId;
    }

    /**
     * @param retrieveOnlyNotesAfterDate
     *            Date, which is used to get another note, if the original note for
     *            RetrieveOnlyNotesAfterId was deleted. This should be the same date as the creation
     *            date of the original note.
     */
    public void setRetrieveOnlyNotesAfterDate(Date retrieveOnlyNotesAfterDate) {
        this.retrieveOnlyNotesAfterDate = retrieveOnlyNotesAfterDate;
    }

    /**
     * <b>Note:</b> If also the lowerTagDate filter is set and the lowerTagDate > creationDate of
     * retrieveOnlyNotesBeforeId a standard filtering for lowerTagDate will be done.
     *
     * @param noteId
     *            All returning notes will have a higher id than this one.
     */
    public void setRetrieveOnlyNotesAfterId(Long noteId) {
        this.retrieveOnlyNotesAfterId = noteId;
    }

    /**
     * Filter to restrict the result to notes that were created before this date. In case the
     * discussion filter mode is set {@link DiscussionFilterMode#IS_ROOT} this date is checked
     * against the lastNoteCreationDate of the discussion. <b>Note:</b> If also the upperTagDate
     * filter is set and the upperTagDate < retrieveOnlyNotesBeforeDate a standard filtering for
     * upperTagDate will be done.
     *
     * @param retrieveOnlyNotesBeforeDate
     *            Date for restricting the result to notes that were create before this date
     */
    public void setRetrieveOnlyNotesBeforeDate(Date retrieveOnlyNotesBeforeDate) {
        this.retrieveOnlyNotesBeforeDate = retrieveOnlyNotesBeforeDate;
    }

    /**
     * Filter to restrict the result to notes that have an ID that is lower than this one. In case
     * the discussion filter mode is set to {@link DiscussionFilterMode#IS_ROOT} this ID should be a
     * discussion ID.
     *
     * @param noteId
     *            note ID to use for restricting the result to notes with an ID smaller than this
     */
    public void setRetrieveOnlyNotesBeforeId(Long noteId) {
        this.retrieveOnlyNotesBeforeId = noteId;
    }

    /**
     * Method to set the date sorting.
     *
     * @param orderDirection
     *            Sets the type of date sorting. Set <code>null</code> to disable ordering for the
     *            date.
     */
    public void setSortByDate(OrderDirection orderDirection) {
        this.sortByDate = orderDirection;
    }

    /**
     * Method to set the id sorting.
     *
     * @param sortById
     *            Sets the type of id sorting. Set <code>null</code> to disable ordering for the id.
     */
    public void setSortById(OrderDirection sortById) {
        this.sortById = sortById;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(NoteStatus status) {
        this.status = status;
    }

    /**
     *
     * @param timelineFilterViewType
     *            the view type to use. The view type defines mainly the result but also has impact
     *            on the filter. If setting it to COMMENT it will also set the DiscussionFilterMode
     *            to IS_ROOT
     */
    public void setTimelineFilterViewType(TimelineFilterViewType timelineFilterViewType) {
        if (timelineFilterViewType != null) {
            this.timelineFilterViewType = timelineFilterViewType;
        }

        if (this.timelineFilterViewType.equals(TimelineFilterViewType.COMMENT)) {
            this.setDiscussionFilterMode(DiscussionFilterMode.IS_ROOT);
        }
    }
}
