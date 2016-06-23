package com.communote.server.core.vo.query.config;

/**
 * {@link com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider} to be used
 * in conjunction with the blog filter widget.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FilterWidgetParameterNameProvider extends FilterApiParameterNameProvider {

    private final static String PARAM_TAGS = "filter";
    private final static String PARAM_USER_IDS = "userId";
    private final static String PARAM_USER_ALIASES = "userAliases";
    private final static String PARAM_ENTITY_ID = "entityId";
    private final static String PARAM_BLOG_IDS = "blogId";
    private final static String PARAM_BLOG_ALIASES = "blogAliases";
    private final static String PARAM_START_DATE = "startDate";
    private final static String PARAM_END_DATE = "endDate";
    private final static String PARAM_USER_SEARCH_STRING = "searchString";
    private final static String PARAM_TAG_PREFIX = "tagPrefix";
    private final static String PARAM_POST_SEARCH_STRING = "postTextSearchString";
    private final static String PARAM_FULL_TEXT_SEARCH_STRING = "fullTextSearchString";
    private final static String PARAM_SHOW_POSTS_FOR_ME = "showPostsForMe";
    private final static String PARAM_PARENT_POST_ID = "parentPostId";
    private final static String PARAM_OFFSET = "offset";
    private final static String PARAM_MAX_COUNT = "maxCount";
    private final static String[] PARAM_USER_TAGGED_RESOURCE_ID = { "noteId" };
    private final static String PARAM_TARGET_BLOG_ID = "targetBlogId";
    private final static String PARAM_DISCUSSION_ID = "discussionId";
    private final static String PARAM_SHOW_FAVORITES = "showFavorites";
    private final static String PARAM_SHOW_FOLLOWED_ITEMS = "showFollowedItems";
    private final static String PARAM_SHOW_DIRECT_MESSAGES = "showDirectMessages";
    private final static String PARAM_INCLUDE_COMMENTS = "includeComments";
    private final static String PARAM_PROPERTY_FILTER = "propertyFilter";
    private final static String PARAM_BLOG_ACCESS_LEVEL = "blogAccess";
    private final static String PARAM_BLOG_IDS_TO_EXCLUDE = "blogIdsToExclude";
    private final static String PARAM_SHOW_DISCUSSION_PARTICIPATION = "showDiscussionParticipation";
    private final static String PARAM_BLOG_SEARCH_STRING = "pattern";
    /**
     * Default instance for easy access
     */
    public final static FilterWidgetParameterNameProvider INSTANCE = new FilterWidgetParameterNameProvider();

    /**
     * Constructor with empty prefix.
     */
    public FilterWidgetParameterNameProvider() {
        super("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForAccessLevel() {
        return PARAM_BLOG_ACCESS_LEVEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForBlogAliases() {
        return PARAM_BLOG_ALIASES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForBlogIds() {
        return PARAM_BLOG_IDS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForBlogIdsToExclude() {
        return PARAM_BLOG_IDS_TO_EXCLUDE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForBlogSearchString() {
        return PARAM_BLOG_SEARCH_STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForDirectMessages() {
        return PARAM_SHOW_DIRECT_MESSAGES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForDiscussionId() {
        return PARAM_DISCUSSION_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForEndDate() {
        return PARAM_END_DATE;
    }

    /**
     * @return the paramEntityId
     */
    public String getNameForEntityId() {
        return PARAM_ENTITY_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForFavorite() {
        return PARAM_SHOW_FAVORITES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForFollowedBlogs() {
        return PARAM_SHOW_FOLLOWED_ITEMS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForFollowedNotes() {
        return PARAM_SHOW_FOLLOWED_ITEMS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForFullTextSearchString() {
        return PARAM_FULL_TEXT_SEARCH_STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForIncludeComments() {
        return PARAM_INCLUDE_COMMENTS;
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
        return PARAM_PARENT_POST_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForPostSearchString() {
        return PARAM_POST_SEARCH_STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForPropertyFilter() {
        return PARAM_PROPERTY_FILTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForShowDiscussionParticipation() {
        return PARAM_SHOW_DISCUSSION_PARTICIPATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForShowPostsForMe() {
        return PARAM_SHOW_POSTS_FOR_ME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForStartDate() {
        return PARAM_START_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForTagPrefix() {
        return PARAM_TAG_PREFIX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForTags() {
        return PARAM_TAGS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForTargetBlogId() {
        return PARAM_TARGET_BLOG_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForUserAliases() {
        return PARAM_USER_ALIASES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForUserIds() {
        return PARAM_USER_IDS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForUserSearchString() {
        return PARAM_USER_SEARCH_STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getNamesForNoteId() {
        return PARAM_USER_TAGGED_RESOURCE_ID;
    }
}
