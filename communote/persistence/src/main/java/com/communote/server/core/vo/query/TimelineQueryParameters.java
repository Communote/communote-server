package com.communote.server.core.vo.query;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.criterion.MatchMode;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.vo.query.logical.AtomicTagFormula;
import com.communote.server.core.vo.query.logical.LogicalTagFormula;
import com.communote.server.core.vo.query.user.UserQueryParameters;
import com.communote.server.model.note.NoteStatus;
import com.communote.server.model.user.note.UserNoteEntityHelper;
import com.communote.server.persistence.tag.TagStore;

/**
 * The base instance for {@link CoreItemQueryDefinition} It defines filter attributes for getting
 * resources.
 *
 * <b>In case of adding filter parameters also check
 * {@link com.communote.server.core.vo.query.post.NoteQueryParameters #clone(TimelineQueryParameters)}
 * </b>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class TimelineQueryParameters extends UserQueryParameters {

    /**
     * the parameter name for the tag prefix
     */
    public static final String PARAM_TAG_PREFIX = "tagPrefix";
    /** hql parameter for the user to be notified. */
    public static final String PARAM_USER_TO_BE_NOTIFIED = "userToBeNotified";
    /**
     * the parameter name for the user ids
     */
    public static final String PARAM_USER_ID = "userId";
    /**
     * the parameter name for the blog id
     */
    public static final String PARAM_BLOG_ID = "blogId";
    /**
     * the parameter of the lower tag date
     */
    public static final String PARAM_LOWER_TAG_DATE = "lowerTagDate";
    /**
     * the parameter of the upper tag date
     */
    public static final String PARAM_UPPER_TAG_DATE = "upperTagDate";
    /**
     * the parameter of the resource id
     */
    public static final String PARAM_RESOURCE_ID = "resourceId";
    /**
     * the parameter of an discussion
     */
    public static final String PARAM_DISCUSSION_ID = "discussionId";
    /**
     * the parameter of a filter for at least the newsfeed
     */
    public static final String PARAM_FILTER = "filter";
    /**
     * the parameter of the minimum rank the note should have for the user
     */
    public static final String PARAM_MIN_RANK = "minimumRank";
    /**
     * the parameter of the maximum rank the note should have for the user
     */
    public static final String PARAM_MAX_RANK = "maximumRank";
    /**
     * the parameter of the resource id
     */
    public static final String PARAM_NOTE_ID = "noteId";

    /**
     * the parameter of the repo connector id
     */
    public static final String PARAM_ATTACHMENT_REPO_CONNECTOR_ID = "repoId";

    private static final String PARAM_PREFIX_FULLTEXT = "fullText";

    /**
     * the parameter name for a case sensitive tag
     */
    private static final String PARAM_TAG = "tag";
    private final Set<Long> tagIds = new HashSet<Long>();
    private final Map<String, String> tagToQueryParameterMapping;
    private final Map<String, Set<String>> tagStoreTagIds = new HashMap<String, Set<String>>();

    private DiscussionFilterMode discussionFilterMode = DiscussionFilterMode.ALL;
    private boolean includeStartDate = true;
    private boolean searchOnlyExtensionSpecificFields;
    private boolean retrieveOnlyFollowedItems;
    private boolean timeLimitsQueryRequired;
    // retrieve only those resources created by one of the users
    private Long[] userIds;
    private Long[] userIdsToIgnore = new Long[0];
    private Set<String> userAliases = new HashSet<String>();
    private Long resourceId;
    private LogicalTagFormula logicalTags;
    private boolean favorites = false;
    // retrieve the resource only if it has been tagged before this date
    private Date upperTagDate;
    // retrieve the resource only if it has been tagged after this date
    private Date lowerTagDate;
    // the id of the discussion;
    private Long discussionId;
    private Long noteId;
    private String tagPrefix;
    private String[] fullTextSearchFilters;
    private String[] fullTextSearchParamNames;
    private String[] topicSearchFilters;
    private String[] topicSearchParamNames;
    // the type of uti (with its specific data) we are interested in
    private TaggingCoreItemUTPExtension typeSpecificExtension;
    private NoteStatus[] status;
    // the direct message flag
    private boolean directMessage;
    private Double minimumRank;
    private Double maximumRank;
    private boolean sortByDayDateAndRank;
    private Set<String> tagStoreAliases = new HashSet<String>();
    private Boolean multilingualTagPrefixSearch = null;
    private boolean mentionTopicReaders;
    private boolean mentionTopicAuthors;
    private boolean mentionTopicManagers;
    private boolean mentionDiscussionAuthors;
    /**
     * true if the result may contain distinct result (it will be faster tho). In detail no distinct
     * will be used in the query.
     */
    private boolean allowDuplicateResults;
    /** The user to be notified. */
    private Long[] usersToBeNotified;
    private boolean queryForAdditionalDMs = false;

    // consider only notes with attachments of one of the content ids
    private String[] attachmentContentIds;
    // only used if attachmentContentIds is set, checks that the content id in
    // attachmentContentIds[index] is part of the repository in index
    private String[] attachmentRepositoryConnectorIds;

    // consider only notes with attachments of the given repo, if set
    // attachmentRepositoryConnectorIds is ignored
    private String attachmentRepositoryConnectorId;

    /**
     * Construct me with the right query definition
     */
    public TimelineQueryParameters() {
        this(new TaggingCoreItemUTPExtension());
    }

    /**
     * Construct me with the right query definition
     *
     * @param extension
     *            extension to use cannot be null
     */
    public TimelineQueryParameters(TaggingCoreItemUTPExtension extension) {
        typeSpecificExtension = extension;
        if (extension == null) {
            throw new IllegalArgumentException("extension cannot be null!");

        }
        tagToQueryParameterMapping = new HashMap<String, String>();
    }

    /**
     * @param tagId
     *            TagId to filter for.
     */
    public void addTagId(Long tagId) {
        tagIds.add(tagId);
    }

    /**
     * @param tagStoreAlias
     *            The alias of the TagStore.
     * @param tagStoreTagIds
     *            Collection of tag ids to add for the given TagStore.
     */
    public void addTagStoreTagId(String tagStoreAlias, Collection<String> tagStoreTagIds) {
        Set<String> tags = this.tagStoreTagIds.get(tagStoreAlias);
        if (tags == null) {
            tags = new HashSet<String>();
            this.tagStoreTagIds.put(tagStoreAlias, tags);
        }
        tags.addAll(tagStoreTagIds);
    }

    /**
     * @param tagStoreAlias
     *            The alias of the TagStore.
     * @param tagStoreTagId
     *            The id of the tag within the TagStore.
     */
    public void addTagStoreTagId(String tagStoreAlias, String tagStoreTagId) {
        Set<String> tags = tagStoreTagIds.get(tagStoreAlias);
        if (tags == null) {
            tags = new HashSet<String>();
            tagStoreTagIds.put(tagStoreAlias, tags);
        }
        tags.add(tagStoreTagId);
    }

    /**
     * Creates a named parameter from a atomic tag formula. The parameter can be used in the query
     * and will be included in the map returned by {@link #getParameters()}. The tag is converted to
     * lower case.
     *
     * @param atom
     *            the atomic tag formula for which the parameter is needed
     * @return the named parameter
     */
    protected String createParameterName(AtomicTagFormula atom) {
        // make lower case with English locale because this is the one used when storing the tag
        String tagValue = atom.getTag().toLowerCase(Locale.ENGLISH);
        String paramName = tagToQueryParameterMapping.get(tagValue);
        if (paramName == null) {
            paramName = PARAM_TAG + tagToQueryParameterMapping.size();
            tagToQueryParameterMapping.put(tagValue, paramName);
        }
        return paramName;
    }

    public String[] getAttachmentContentIds() {
        return attachmentContentIds;
    }

    public String getAttachmentRepositoryConnectorId() {
        return attachmentRepositoryConnectorId;
    }

    public String[] getAttachmentRepositoryConnectorIds() {
        return attachmentRepositoryConnectorIds;
    }

    /**
     * The discussion filter mode that should be used for getting results.
     *
     * @return the discussionFilterMode
     */
    public DiscussionFilterMode getDiscussionFilterMode() {
        return discussionFilterMode;
    }

    /**
     * The discussion filter mode that should be used for filtering.
     *
     * @return the discussionFilterMode
     */
    public DiscussionFilterMode getDiscussionFilterModeForFiltering() {
        return discussionFilterMode;
    }

    /**
     * @return the discussionId
     */
    public Long getDiscussionId() {
        return discussionId;
    }

    /**
     * @return Array of NoteStatus, which should be ignored.
     */
    public NoteStatus[] getExcludeNoteStatus() {
        return status;
    }

    /**
     * @return the fields for full text search
     */
    public String[] getFullTextSearchFilters() {
        return fullTextSearchFilters;
    }

    /**
     * @return the parameter names to be used in a full text search or null if no text search filter
     *         was set
     */
    public String[] getFullTextSearchParameterNames() {
        return this.fullTextSearchParamNames;
    }

    /**
     * Returns the logical tag definitions.
     *
     * @return the formula describing the logically combined tags or null if they are not set
     */
    public LogicalTagFormula getLogicalTags() {
        return logicalTags;
    }

    /**
     * the date after a resource must be tagged
     *
     * @return the date
     */
    public Date getLowerTagDate() {
        return lowerTagDate;
    }

    /**
     *
     * @return the maximum rank the note should have for the user, null to ignore
     */
    public Double getMaximumRank() {
        return maximumRank;
    }

    /**
     *
     * @return the minimum rank the note should have for the user, null to ignore
     */
    public Double getMinimumRank() {
        return minimumRank;
    }

    /**
     * @return Id of the note to load. This will result in at max. one result.
     */
    public Long getNoteId() {
        return noteId;
    }

    /**
     * @return counts the number of tags to filter for using {@link #getTagIds()} and
     *         {@link #getTagStoreTagIds()}
     */
    public int getNumberOfTagsToFilter() {
        int count = 0;
        if (this.getTagIds() != null) {
            count = this.getTagIds().size();
        }
        if (this.getTagStoreTagIds() != null) {
            for (Set<String> value : this.getTagStoreTagIds().values()) {
                count += value.size();
            }
        }
        if (this.getLogicalTags() != null) {
            count += this.getLogicalTags().countAtomicTags();
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = super.getParameters();
        putParameter(parameters, PARAM_LOWER_TAG_DATE, lowerTagDate);
        putParameter(parameters, PARAM_UPPER_TAG_DATE, upperTagDate);
        putParameter(parameters, PARAM_RESOURCE_ID, resourceId);
        putParameter(parameters, PARAM_NOTE_ID, noteId);

        setRankParameters(parameters);
        if (discussionId != null) {
            putParameter(parameters, PARAM_DISCUSSION_ID, discussionId);
        }
        Map<String, Object> extensionParams = typeSpecificExtension.getParameters();
        for (Map.Entry<String, Object> p : extensionParams.entrySet()) {
            putParameter(parameters, p.getKey(), p.getValue());
        }

        for (Map.Entry<String, String> tagToParam : tagToQueryParameterMapping.entrySet()) {
            // the tagToParameterMapping contains a mapping from lower case tag to parameter
            parameters.put(tagToParam.getValue(), tagToParam.getKey());
        }

        if (StringUtils.isNotBlank(tagPrefix)) {
            putParametersForSearch(parameters,
                    new String[] { TimelineQueryParameters.PARAM_TAG_PREFIX },
                    new String[] { tagPrefix }, MatchMode.START, true);
        }

        putParametersForSearch(parameters, this.fullTextSearchParamNames,
                this.fullTextSearchFilters,
                this.getMatchMode());
        putParametersForSearch(parameters, this.topicSearchParamNames,
                this.topicSearchFilters, MatchMode.ANYWHERE, true);

        if (!ArrayUtils.isEmpty(this.usersToBeNotified)) {
            parameters.put(PARAM_USER_TO_BE_NOTIFIED, usersToBeNotified);
        }

        if (this.attachmentRepositoryConnectorId != null) {
            parameters
            .put(PARAM_ATTACHMENT_REPO_CONNECTOR_ID, this.attachmentRepositoryConnectorId);
        }

        return parameters;
    }

    /**
     * Get the resource of this instance
     *
     * @return the resource id
     */
    public Long getResourceId() {
        return resourceId;
    }

    /**
     * @return the tagIds
     */
    public Set<Long> getTagIds() {
        return tagIds;
    }

    /**
     * Get the tag prefix
     *
     * @return the tag prefix
     */
    @Override
    public String getTagPrefix() {
        return tagPrefix;
    }

    /**
     * @return set of aliases identifying tag stores
     */
    @Override
    public Set<String> getTagStoreAliases() {
        return tagStoreAliases;
    }

    /**
     * @return the tagStoreTagIds
     */
    public Map<String, Set<String>> getTagStoreTagIds() {
        return tagStoreTagIds;
    }

    /**
     * @return the parameter names to be used in a topic title search or null if no title search
     *         filter was set
     */
    public String[] getTopicSearchParameterNames() {
        return this.topicSearchParamNames;
    }

    /**
     * @return the extension
     */
    public TaggingCoreItemUTPExtension getTypeSpecificExtension() {
        return typeSpecificExtension;
    }

    /**
     * @return the upper tag date
     */
    public Date getUpperTagDate() {
        return upperTagDate;
    }

    /**
     * @return Set of user aliases, which are allowed to be a notes author.
     */
    public Set<String> getUserAliases() {
        return userAliases;
    }

    /**
     * the user ids
     *
     * @return the user ids, which can be null or an array with at least one value
     */
    public Long[] getUserIds() {
        return userIds;
    }

    /**
     * @return An array of user ids, which should be ignored, when retrieving data. This may be
     *         empty but never null.
     */
    public Long[] getUserIdsToIgnore() {
        return userIdsToIgnore;
    }

    /**
     * Gets the user to be notified.
     *
     * @return the user to be notified
     */
    public Long[] getUserToBeNotified() {
        return usersToBeNotified;
    }

    /**
     *
     * @return true if the result may contain distinct result (it will be faster though). In detail
     *         no distinct will be used in the query.
     */
    public boolean isAllowDuplicateResults() {
        return allowDuplicateResults;
    }

    /**
     * @return the directMessage, which can be {@code true} or {@code false}
     */
    public boolean isDirectMessage() {
        return directMessage;
    }

    /**
     * @return the favorites
     */
    public boolean isFavorites() {
        return favorites;
    }

    /**
     * @return the includeStartDate
     */
    public boolean isIncludeStartDate() {
        return includeStartDate;
    }

    /**
     * @return the mentionDiscussionAuthors
     */
    public boolean isMentionDiscussionAuthors() {
        return mentionDiscussionAuthors;
    }

    /**
     * @return the mentionTopicAuthors
     */
    public boolean isMentionTopicAuthors() {
        return mentionTopicAuthors;
    }

    /**
     * @return the mentionTopicManagers
     */
    public boolean isMentionTopicManagers() {
        return mentionTopicManagers;
    }

    /**
     * @return the mentionTopicReaders
     */
    public boolean isMentionTopicReaders() {
        return mentionTopicReaders;
    }

    /**
     * @return true if a tag prefix query should check the translations of tags
     */
    @Override
    public boolean isMultilingualTagPrefixSearch() {
        if (multilingualTagPrefixSearch == null) {
            TagStoreManagement tagStoreManagement = ServiceLocator.instance().getService(
                    TagStoreManagement.class);

            for (String storeAlias : this.tagStoreAliases) {
                TagStore store = tagStoreManagement.getTagStore(storeAlias, null);
                if (store.isMultilingual()) {
                    multilingualTagPrefixSearch = Boolean.TRUE;
                    break;
                } else {
                    multilingualTagPrefixSearch = Boolean.FALSE;
                }
            }
            if (multilingualTagPrefixSearch == null) {
                multilingualTagPrefixSearch = tagStoreManagement
                        .hasMultilingualTagStore(TagStoreType.Types.NOTE);
            }
        }
        return multilingualTagPrefixSearch;
    }

    /**
     * See setQueryForAdditionalDMs for a description.
     *
     * @return True, if this query is used to retrieve additional DM's
     */
    public boolean isQueryForAdditionalDMs() {
        return queryForAdditionalDMs;
    }

    /**
     *
     * @return true of either the rank filter is active
     */
    public boolean isRankFilterActive() {
        return maximumRank != null || minimumRank != null;
    }

    /**
     * @return whether to retrieve only followed items or items that are related to these items
     */
    public boolean isRetrieveOnlyFollowedItems() {
        return retrieveOnlyFollowedItems;
    }

    /**
     * @return true to only search extension specific fields
     */
    public boolean isSearchOnlyExtensionSpecificFields() {
        return searchOnlyExtensionSpecificFields;
    }

    /**
     *
     * @return true if to sort only by the date and then by the rank (per date). a minimum or
     *         maximum rank must be set for this to work.
     */
    public boolean isSortByDayDateAndRank() {
        return sortByDayDateAndRank;
    }

    /**
     * Determines whether an additional query for time limits is required. For details see
     * {@link #setTimeLimitsQueryRequired(boolean)}.
     *
     * @return true if the query is required
     */
    public boolean isTimeLimitsQueryRequired() {
        return timeLimitsQueryRequired;
    }

    /**
     * @return true if the transform list item function should be used after retrieving the objects
     */
    @Override
    public boolean needTransformListItem() {
        return false;
    }

    /**
     *
     * @param allowDuplicateResults
     *            true if the result may contain distinct result (it will be faster tho). In detail
     *            no distinct will be used in the query.
     */
    public void setAllowDuplicateResults(boolean allowDuplicateResults) {
        this.allowDuplicateResults = allowDuplicateResults;
    }

    public void setAttachmentContentIds(String[] attachmentContentIds) {
        this.attachmentContentIds = attachmentContentIds;
    }

    public void setAttachmentRepositoryConnectorId(String attachmentRepositoryConnectorId) {
        this.attachmentRepositoryConnectorId = attachmentRepositoryConnectorId;
    }

    public void setAttachmentRepositoryConnectorIds(String[] attachmentRepositoryConnectorIds) {
        this.attachmentRepositoryConnectorIds = attachmentRepositoryConnectorIds;
    }

    /**
     * @param directMessage
     *            Set to true, if only direct messages should be loaded.
     */
    public void setDirectMessage(boolean directMessage) {
        this.directMessage = directMessage;
    }

    /**
     * @param discussionFilterMode
     *            the discussionFilterMode to set
     */
    public void setDiscussionFilterMode(DiscussionFilterMode discussionFilterMode) {
        if (discussionFilterMode != null) {
            this.discussionFilterMode = discussionFilterMode;
        }
    }

    /**
     * Sets the discussion id and resets the note id! There cannot be searched for a note and a
     * discussion id on the same time. If a single note is needed use the note id only, that is
     * discussion id must be null.
     *
     * @param discussionId
     *            the discussionId to set
     */
    public void setDiscussionId(Long discussionId) {
        this.discussionId = discussionId;
    }

    /**
     * Sets the {@link NoteStatus} which should be excluded from the query.
     *
     * @param statuss
     *            Array of {@link NoteStatus}.
     */
    public void setExcludeNoteStatus(NoteStatus[] statuss) {
        this.status = statuss;
    }

    /**
     * @param favorites
     *            the favorites to set
     */
    public void setFavorites(Boolean favorites) {
        this.favorites = favorites == null ? false : favorites;
    }

    /**
     * @param fullTextSearchFilters
     *            the full text search filters
     */
    public void setFullTextSearchFilters(String[] fullTextSearchFilters) {
        // create arrays with param names and one with values
        this.fullTextSearchParamNames = createParameterNamesForSearch(PARAM_PREFIX_FULLTEXT,
                fullTextSearchFilters);
        this.fullTextSearchFilters = fullTextSearchFilters;
    }

    /**
     * @param includeStartDate
     *            If true, also notes written exactly at the start date will be filtered, else only
     *            notes written after the set start date.
     */
    public void setIncludeStartDate(boolean includeStartDate) {
        this.includeStartDate = includeStartDate;
    }

    /**
     * @param formula
     *            the formula to match the tags
     */
    public void setLogicalTags(LogicalTagFormula formula) {
        this.logicalTags = formula;
        this.tagToQueryParameterMapping.clear();
    }

    /**
     * @param lowerTagDate
     *            the lower tag date
     */
    public void setLowerTagDate(Date lowerTagDate) {
        this.lowerTagDate = lowerTagDate;
    }

    /**
     *
     * @param maximumRank
     *            the maximum rank the note should have for the user, null to ignore
     */
    public void setMaximumRank(Double maximumRank) {
        this.maximumRank = maximumRank;
    }

    /**
     * @param mentionDiscussionAuthors
     *            the mentionDiscussionAuthors to set
     */
    public void setMentionDiscussionAuthors(boolean mentionDiscussionAuthors) {
        this.mentionDiscussionAuthors = mentionDiscussionAuthors;
    }

    /**
     * @param mentionTopicAuthors
     *            the mentionTopicAuthors to set
     */
    public void setMentionTopicAuthors(boolean mentionTopicAuthors) {
        this.mentionTopicAuthors = mentionTopicAuthors;
    }

    /**
     * @param mentionTopicManagers
     *            the mentionTopicManagers to set
     */
    public void setMentionTopicManagers(boolean mentionTopicManagers) {
        this.mentionTopicManagers = mentionTopicManagers;
    }

    /**
     * @param mentionTopicReaders
     *            the mentionTopicReaders to set
     */
    public void setMentionTopicReaders(boolean mentionTopicReaders) {
        this.mentionTopicReaders = mentionTopicReaders;
    }

    /**
     *
     * @param minimumRank
     *            the minimum rank the note should have for the user, null to ignore
     */
    public void setMinimumRank(Double minimumRank) {
        this.minimumRank = minimumRank;
    }

    /**
     * Set the note id. Will be ignored if there is an discussion
     *
     * @param noteId
     *            the noteId to set
     */
    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    /**
     * This method is just a hook for KENMEI-5766 and mainly KENMEI-5661 (the stupid desktop
     * client), to make it possible to AND combine following and direct messages.
     *
     * @param queryForAdditionalDMs
     *            Set to true, if this query is used to retrieve additional DM's
     */
    public void setQueryForAdditionalDMs(boolean queryForAdditionalDMs) {
        this.queryForAdditionalDMs = queryForAdditionalDMs;
    }

    /**
     * This method puts the ranks parameters into the given map if set.
     *
     * @param parameters
     *            The map to fill.
     */
    private void setRankParameters(Map<String, Object> parameters) {
        if (this.minimumRank != null) {
            putParameter(parameters, PARAM_MIN_RANK,
                    UserNoteEntityHelper.convertNormalizedRank(this.minimumRank));
        }
        if (this.maximumRank != null) {
            putParameter(parameters, PARAM_MAX_RANK,
                    UserNoteEntityHelper.convertNormalizedRank(this.maximumRank));
        }
    }

    /**
     * @param resourceId
     *            the resource id to filter for
     */
    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * Whether to retrieve only followed items or items that are related to these items.
     *
     * @param onlyFollowedItems
     *            true for retrieving only followed items
     */
    public void setRetrieveOnlyFollowedItems(boolean onlyFollowedItems) {
        retrieveOnlyFollowedItems = onlyFollowedItems;
    }

    /**
     *
     * @param sortByDayDateAndRank
     *            true if to sort only by the date and then by the rank (per date). a minimum or
     *            maximum rank must be set for this to work.
     */
    public void setSortByDayDateAndRank(boolean sortByDayDateAndRank) {
        this.sortByDayDateAndRank = sortByDayDateAndRank;
    }

    /**
     * @param tagIds
     *            the tagIds to set
     */
    public void setTagIds(Collection<Long> tagIds) {
        this.tagIds.clear();
        if (tagIds != null) {
            this.tagIds.addAll(tagIds);
        }
    }

    /**
     * set the tag prefix. The resulting tags must be starting with this prefix
     *
     * @param tagPrefix
     *            the tag prefix
     */
    @Override
    public void setTagPrefix(String tagPrefix) {
        this.tagPrefix = tagPrefix;
    }

    /**
     * Set aliases of tag stores to only consider tags from these stores
     *
     * @param tagStoreAliases
     *            set of aliases identifying tag stores
     */
    @Override
    public void setTagStoreAliases(Set<String> tagStoreAliases) {
        if (tagStoreAliases == null) {
            tagStoreAliases = new HashSet<String>();
        }
        // reset the multilingual search
        this.multilingualTagPrefixSearch = false;
        this.tagStoreAliases = tagStoreAliases;
    }

    /**
     * Set whether a query for time limits is required. If set to true a separate query which
     * retrieves the upper and lower creation date of those UTIs which match the current filter
     * settings (not including time filters) will be executed. The query result will change to a
     * subtype of {@link com.communote.common.util.PageableList PageableList}, namely
     * {@link com.communote.common.util.AugmentedPageableList AugmentedPageableList} which holds all
     * list items and the time limits.
     *
     * @param timeLimitsQueryRequired
     *            true to get the time limits
     */
    public void setTimeLimitsQueryRequired(boolean timeLimitsQueryRequired) {
        this.timeLimitsQueryRequired = timeLimitsQueryRequired;
    }

    /**
     * Set the strings to match against the topic titles
     *
     * @param topicSearchFilters
     *            the strings to match against the titles
     */
    public void setTopicSearchFilters(String[] topicSearchFilters) {
        if (topicSearchFilters == null || topicSearchFilters.length == 0) {
            this.topicSearchFilters = null;
            this.topicSearchParamNames = null;
        } else {
            this.topicSearchFilters = topicSearchFilters;
            // create arrays with param names
            this.topicSearchParamNames = createParameterNamesForSearch("topicSearch",
                    topicSearchFilters);
        }
    }

    /**
     * Focuses this query for Notes to one of the sub-types UserTaggedResource or Note. In case this
     * method is not called explicitly the type will default to UserTaggedResource.
     *
     * @param extension
     *            the sub-type of Note on which the query should focus with its specific data
     */
    public void setTypeSpecificExtension(TaggingCoreItemUTPExtension extension) {
        this.typeSpecificExtension = extension;
    }

    /**
     * @param upperTagDate
     *            set the upper tag date
     */
    public void setUpperTagDate(Date upperTagDate) {
        this.upperTagDate = upperTagDate;
    }

    /**
     * @param userAliases
     *            the userAliases to set
     */
    public void setUserAliases(Set<String> userAliases) {
        if (userAliases == null) {
            return;
        }
        this.userAliases = userAliases;
    }

    /**
     * set the user ids parameter
     *
     * @param userIds
     *            the user ids parameter
     */
    public void setUserIds(Long[] userIds) {
        if (userIds != null) {
            if (userIds.length == 0 || userIds.length == 1 && userIds[0] == null) {
                this.userIds = null;
            } else {
                this.userIds = userIds;
            }
        } else {
            this.userIds = userIds;
        }
    }

    /**
     * Method to ignore users.
     *
     * @param userIdsToIgnore
     *            An array of user ids, which should be ignored, when retrieving content.
     */
    public void setUserIdsToIgnore(Long... userIdsToIgnore) {
        if (userIdsToIgnore == null) {
            userIdsToIgnore = new Long[0];
        }
        this.userIdsToIgnore = userIdsToIgnore;
    }

    /**
     * Sets the user to be notified.
     *
     * @param userToBeNotified
     *            the new user to be notified
     */
    public void setUserToBeNotified(Long[] userToBeNotified) {
        this.usersToBeNotified = userToBeNotified;
    }
}
