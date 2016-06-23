package com.communote.plugins.api.rest.v30.filter;

import com.communote.server.core.vo.query.config.FilterApiParameterNameProvider;

/**
 * Resolves the query instance parameter names of the api.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FilterRestApiParameterNameProvider extends FilterApiParameterNameProvider {
    private final static String PARAM_TAGS = "tags";
    private final static String PARAM_TAG_IDS = "tagIds";
    private final static String PARAM_USER_IDS = "userIds";
    private final static String PARAM_USER_ALIASES = "userAliases";
    private final static String PARAM_TOPIC_IDS = "topicIds";
    private final static String PARAM_TOPIC_ALIASES = "topicAliases";
    private final static String PARAM_START_DATE = "startDate";
    private final static String PARAM_END_DATE = "endDate";
    private final static String PARAM_USER_SEARCH_STRING = "userSearchString";
    private final static String PARAM_TAG_PREFIX = "tagPrefix";
    private final static String PARAM_NOTE_SEARCH_STRING = "noteSearchString";
    private final static String PARAM_FULL_TEXT_SEARCH_STRING = "fullTextSearchString";
    private final static String PARAM_SHOW_NOTES_FOR_ME = "showNotesForMe";
    private final static String PARAM_PARENT_NOTE_ID = "parentNoteId";
    private final static String PARAM_OFFSET = "offset";
    private final static String PARAM_MAX_COUNT = "maxCount";
    private final static String[] PARAM_USER_TAGGED_RESOURCE_ID = { "noteId",
            "userTaggedResourceID" };
    private final static String PARAM_TARGET_TOPIC_ID = "targetTopicId";
    private final static String PARAM_DISCUSSION_ID = "discussionId";
    private final static String PARAM_SHOW_FAVORITES = "showFavorites";
    private final static String PARAM_SHOW_FOLLOWED_ITEMS = "showFollowedItems";
    private final static String PARAM_SHOW_DIRECT_MESSAGES = "showDirectMessages";
    private final static String PARAM_TOPIC_ACCESS_LEVEL = "topicAccess";
    private final static String PARAM_TOPIC_IDS_TO_EXCLUDE = "topicIdsToExclude";
    private final static String PARAM_PROPERTY_FILTER = "propertyFilter";
    private final static String PARAM_INCLUDE_COMMENTS = "includeComments";
    private final static String PARAM_SHOW_ONLY_ROOT_NOTES = "showOnlyRootNotes";
    private final static String PARAM_SHOW_ONLY_DISCUSSIONS = "showOnlyDiscussions";
    private final static String PARAM_SHOW_DISCUSSION_PARTICIPATION = "showDiscussionParticipation";
    private final static String PARAM_TOPIC_SEARCH_STRING = "topicSearchString";

    private final String prefix;

    /**
     * Default instance for easy access
     */
    public final static FilterRestApiParameterNameProvider INSTANCE = new FilterRestApiParameterNameProvider();

    /**
     * Constructor without any parameters. Prefix is "f_";
     */
    public FilterRestApiParameterNameProvider() {
        this("f_");
    }

    /**
     * Constructor that takes a preifx as parameter.
     * 
     * @param prefix
     *            Prefix.
     */
    public FilterRestApiParameterNameProvider(String prefix) {
        for (int i = 0; i < PARAM_USER_TAGGED_RESOURCE_ID.length; i++) {
            PARAM_USER_TAGGED_RESOURCE_ID[i] = prefix + PARAM_USER_TAGGED_RESOURCE_ID[i];
        }
        this.prefix = prefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForAccessLevel() {
        return prefix + PARAM_TOPIC_ACCESS_LEVEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForBlogAliases() {
        return prefix + PARAM_TOPIC_ALIASES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForBlogIds() {
        return prefix + PARAM_TOPIC_IDS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForBlogIdsToExclude() {
        return prefix + PARAM_TOPIC_IDS_TO_EXCLUDE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForBlogSearchString() {
        return prefix + PARAM_TOPIC_SEARCH_STRING;
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
    public String getNameForFullTextSearchString() {
        return prefix + PARAM_FULL_TEXT_SEARCH_STRING;
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
    public String getNameForShowDiscussionParticipation() {
        return prefix + PARAM_SHOW_DISCUSSION_PARTICIPATION;
    }

    /**
     * {@inheritDoc}
     */
    public String getNameForShowOnlyDiscussions() {
        return prefix + PARAM_SHOW_ONLY_DISCUSSIONS;
    }

    /**
     * {@inheritDoc}
     */
    public String getNameForShowOnlyRootNotes() {
        return prefix + PARAM_SHOW_ONLY_ROOT_NOTES;
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
    public String getNameForStartDate() {
        return prefix + PARAM_START_DATE;
    }

    /**
     * @return "tagIds"
     */
    @Override
    public String getNameForTagIds() {
        return prefix + PARAM_TAG_IDS;
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
        return prefix + PARAM_TARGET_TOPIC_ID;
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
    public String getNameForUserSearchString() {
        return prefix + PARAM_USER_SEARCH_STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getNamesForNoteId() {
        return PARAM_USER_TAGGED_RESOURCE_ID;
    }
}
