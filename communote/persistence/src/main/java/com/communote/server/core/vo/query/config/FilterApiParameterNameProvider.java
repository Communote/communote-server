package com.communote.server.core.vo.query.config;

/**
 * Resolves the query instance parameter names of the api.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FilterApiParameterNameProvider implements QueryParametersParameterNameProvider {

    private final static String PARAM_ATTACHMENT_CONTENT_IDS = "contentIds";
    private final static String PARAM_ATTACHMENT_REPOSITORY_CONNECTOR_IDS = "repositoryIds";
    private final static String PARAM_DISCUSSION_FILTER_MODE = "discussionFilterMode";
    private final static String PARAM_TAGS = "tags";
    private final static String PARAM_USER_IDS = "userIds";
    private final static String PARAM_USER_IDS_TO_IGNORE = "userIdsToIgnore";
    private final static String PARAM_USER_ALIASES = "userAliases";
    private final static String PARAM_BLOG_IDS = "blogIds";
    private final static String PARAM_BLOG_ALIASES = "blogAliases";
    private final static String PARAM_START_DATE = "startDate";
    private final static String PARAM_END_DATE = "endDate";
    private final static String PARAM_USER_SEARCH_STRING = "userSearchString";
    private final static String PARAM_TAG_PREFIX = "tagPrefix";
    private final static String PARAM_NOTE_SEARCH_STRING = "postSearchString";
    private final static String PARAM_FULL_TEXT_SEARCH_STRING = "fullTextSearchString";
    private final static String PARAM_SHOW_NOTES_FOR_ME = "showPostsForMe";
    private final static String PARAM_PARENT_NOTE_ID = "parentPostId";
    private final static String PARAM_OFFSET = "offset";
    private final static String PARAM_MAX_COUNT = "maxCount";
    private final static String[] PARAM_NOTE_ID = { "userTaggedResourceID",
            "noteId" };
    private final static String PARAM_TARGET_BLOG_ID = "targetBlogId";
    private final static String PARAM_DISCUSSION_ID = "discussionId";
    private final static String PARAM_SHOW_FAVORITES = "showFavorites";
    private final static String PARAM_SHOW_FOLLOWED_ITEMS = "showFollowedItems";
    private final static String PARAM_SHOW_DIRECT_MESSAGES = "showDirectMessages";
    private final static String PARAM_BLOG_ACCESS_LEVEL = "blogAccess";
    private final static String PARAM_BLOG_IDS_TO_EXCLUDE = "blogIdsToExclude";
    private final static String PARAM_PROPERTY_FILTER = "propertyFilter";
    private final static String PARAM_INCLUDE_COMMENTS = "includeComments";
    private final static String PARAM_BLOG_SEARCH_STRING = "blogSearchString";
    private final static String PARAM_SHOW_DISCUSSION_PARTICIPATION = "showDiscussionParticipation";
    private static final String PARAM_SELECTED_VIEW_TYPE = "selectedViewType";
    private static final String PARAM_CHECK_AT_LEAST_MORE_RESULTS = "checkAtLeastMoreResults";
    private static final String PARAM_RETRIEVE_ONLY_NOTES_BEFORE_ID = "retrieveOnlyNotesBeforeId";
    private static final String PARAM_RETRIEVE_ONLY_NOTES_BEFORE_DATE = "retrieveOnlyNotesBeforeDate";
    private static final String PARAM_RETRIEVE_ONLY_NOTES_AFTER_ID = "retrieveOnlyNotesAfterId";
    private static final String PARAM_RETRIEVE_ONLY_NOTES_AFTER_DATE = "retrieveOnlyNotesAfterDate";

    private static final String PARAM_MINIMUM_RANK = "minRank";
    private static final String PARAM_MAXIMUM_RANK = "maxRank";
    private static final String PARAM_SORT_BY_DAY_DATE_AND_RANK = "sortByDayDateAndRank";
    private static final String PARAM_TOPIC_SORT_BY_LATEST_NOTE = "topicSortByLatestNote";
    private static final String PARAM_FORCE_ALL_TOPICS = "forceAllTopics";
    private static final String PARAM_SHOW_ONLY_ROOT_TOPICS = "showOnlyRootTopics";
    private static final String PARAM_SHOW_ONLY_TOPLEVEL_TOPICS = "showOnlyToplevelTopics";
    private static final String PARAM_EXCLUDE_TOPLEVEL_TOPICS = "excludeToplevelTopics";
    private static final String PARAM_PARENT_TOPIC_IDS = "parentTopicIds";
    private static final String PARAM_NAME_FOR_INCLUDE_CHILD_TOPICS = "includeChildTopics";

    private static final String PARAM_EXTERNAL_OBJECT_SYSTEM_ID = "externalObjectSystemId";
    private static final String PARAM_EXTERNAL_OBJECT_ID = "externalObjectId";

    private final String prefix;
    /**
     * Default instance for easy access
     */
    public final static FilterApiParameterNameProvider INSTANCE = new FilterApiParameterNameProvider();

    /**
     * Constructor without any parameters. Prefix is "f_";
     */
    public FilterApiParameterNameProvider() {
        this("f_");
    }

    /**
     * Constructor that takes a preifx as parameter.
     * 
     * @param prefix
     *            Prefix.
     */
    public FilterApiParameterNameProvider(String prefix) {
        for (int i = 0; i < PARAM_NOTE_ID.length; i++) {
            PARAM_NOTE_ID[i] = prefix + PARAM_NOTE_ID[i];
        }
        this.prefix = prefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForAccessLevel() {
        return prefix + PARAM_BLOG_ACCESS_LEVEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForAttachmentContentIds() {
        return prefix + PARAM_ATTACHMENT_CONTENT_IDS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForAttachmentRepositoryConnectorIds() {
        return prefix + PARAM_ATTACHMENT_REPOSITORY_CONNECTOR_IDS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForBlogAliases() {
        return prefix + PARAM_BLOG_ALIASES;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForBlogIds() {
        return prefix + PARAM_BLOG_IDS;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForBlogIdsToExclude() {
        return prefix + PARAM_BLOG_IDS_TO_EXCLUDE;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForBlogSearchString() {
        return prefix + PARAM_BLOG_SEARCH_STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForCheckAtLeastMoreResults() {
        return PARAM_CHECK_AT_LEAST_MORE_RESULTS;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForDirectMessages() {
        return prefix + PARAM_SHOW_DIRECT_MESSAGES;
    }

    /**
     * {@inheritDoc}
     * 
     * @return {@link #prefix} + {@link #PARAM_DISCUSSION_FILTER_MODE}
     */
    @Override
    public String getNameForDiscussionFilterMode() {
        return prefix + PARAM_DISCUSSION_FILTER_MODE;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForDiscussionId() {
        return prefix + PARAM_DISCUSSION_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForEndDate() {
        return prefix + PARAM_END_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForExcludeToplevelTopics() {
        return prefix + PARAM_EXCLUDE_TOPLEVEL_TOPICS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForExternalObjectId() {
        return prefix + PARAM_EXTERNAL_OBJECT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForExternalObjectSystemId() {
        return prefix + PARAM_EXTERNAL_OBJECT_SYSTEM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForFavorite() {
        return prefix + PARAM_SHOW_FAVORITES;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForFollowedBlogs() {
        return prefix + PARAM_SHOW_FOLLOWED_ITEMS;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForFollowedNotes() {
        return prefix + PARAM_SHOW_FOLLOWED_ITEMS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForForceAllTopics() {
        return prefix + PARAM_FORCE_ALL_TOPICS;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForFullTextSearchString() {
        return prefix + PARAM_FULL_TEXT_SEARCH_STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForIncludeChildTopics() {
        return prefix + PARAM_NAME_FOR_INCLUDE_CHILD_TOPICS;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForIncludeComments() {
        return prefix + PARAM_INCLUDE_COMMENTS;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForMaxCount() {
        return PARAM_MAX_COUNT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForMaximumRank() {
        return prefix + PARAM_MAXIMUM_RANK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForMinimumRank() {
        return prefix + PARAM_MINIMUM_RANK;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForOffset() {
        return PARAM_OFFSET;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForParentPostId() {
        return prefix + PARAM_PARENT_NOTE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForParentTopicIds() {
        return prefix + PARAM_PARENT_TOPIC_IDS;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForPostSearchString() {
        return prefix + PARAM_NOTE_SEARCH_STRING;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getNameForPropertyFilter() {
        return prefix + PARAM_PROPERTY_FILTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForRetrieveOnlyNotesAfterDate() {
        return prefix + PARAM_RETRIEVE_ONLY_NOTES_AFTER_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForRetrieveOnlyNotesAfterId() {
        return prefix + PARAM_RETRIEVE_ONLY_NOTES_AFTER_ID;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForRetrieveOnlyNotesBeforeDate() {
        return prefix + PARAM_RETRIEVE_ONLY_NOTES_BEFORE_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForRetrieveOnlyNotesBeforeId() {
        return prefix + PARAM_RETRIEVE_ONLY_NOTES_BEFORE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForSelectedViewType() {
        return PARAM_SELECTED_VIEW_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForShowDiscussionParticipation() {
        return prefix + PARAM_SHOW_DISCUSSION_PARTICIPATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForShowOnlyRootTopics() {
        return prefix + PARAM_SHOW_ONLY_ROOT_TOPICS;
    }

    @Override
    public String getNameForShowOnlyToplevelTopics() {
        return prefix + PARAM_SHOW_ONLY_TOPLEVEL_TOPICS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForShowPostsForMe() {
        return prefix + PARAM_SHOW_NOTES_FOR_ME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForSortByDayDateAndRank() {
        return PARAM_SORT_BY_DAY_DATE_AND_RANK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForStartDate() {
        return prefix + PARAM_START_DATE;
    }

    /**
     * @return "tagIds"
     */
    @Override
    public String getNameForTagIds() {
        return prefix + "tagIds";
    }

    /**
     * @return The parameter name for tag ids to exclude within the result set.
     */
    @Override
    public String getNameForTagIdsToExclude() {
        return prefix + "tagIdsToExclude";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForTagPrefix() {
        return prefix + PARAM_TAG_PREFIX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForTags() {
        return prefix + PARAM_TAGS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForTargetBlogId() {
        return prefix + PARAM_TARGET_BLOG_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForTopicSortByLatestNote() {
        return prefix + PARAM_TOPIC_SORT_BY_LATEST_NOTE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForUserAliases() {
        return prefix + PARAM_USER_ALIASES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForUserIds() {
        return prefix + PARAM_USER_IDS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForUserIdsToIgnore() {
        return prefix + PARAM_USER_IDS_TO_IGNORE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForUserSearchString() {
        return prefix + PARAM_USER_SEARCH_STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getNamesForNoteId() {
        return PARAM_NOTE_ID;
    }
}
