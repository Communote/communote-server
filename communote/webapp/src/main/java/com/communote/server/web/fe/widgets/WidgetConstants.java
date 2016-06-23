package com.communote.server.web.fe.widgets;

/**
 * This interface defines constants which are used by the widgets as parameters. Anyway most of ther
 * parameter are depracted and should either be defined in the using widget itself or by using
 * FilterWidgetParameterNameProvider
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface WidgetConstants {

    /**
     * parameter for the filter values
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public static final String PARAM_FILTER = "filter";

    /**
     * filter parameter for the start date
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public static final String PARAM_DATE_START = "startDate";

    /**
     * filter parameter for the end date
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public static final String PARAM_DATE_END = "endDate";

    /**
     * Parameter for the tagging url
     */

    public static final String PARAM_TAG_URL = "tagUrl";

    /**
     * parameter for the result offset
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public static final String PARAM_OFFSET = "offset";

    /**
     * parameter for the number of results to get
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public static final String PARAM_MAX_COUNT = "maxCount";

    /**
     * Value for max count to retrieve all data
     */
    @Deprecated
    public static final String NO_MAX_COUNT = "0";

    /**
     * Boolean parameter describing whether the results have to be blog/utp related.
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public static final String PARAM_BLOG_RELATED = "blogRelated";

    /**
     * Parameter for the blog id
     */
    public static final String PARAM_BLOG_ID = "blogId";

    /**
     * Parameter for the blog post id
     */
    public static final String PARAM_BLOG_POST_ID = "blogpostId";

    /**
     * Parameter for the group id
     */
    @Deprecated
    // TODO @tlu What to use instead?
    public static final String PARAM_GROUP_ID = "groupId";

    /**
     * Parameter for the user id
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public static final String PARAM_USER_ID = "userId";

    /**
     * Parameter for the pageing interval
     */
    @Deprecated
    public static final String PARAM_PAGING_INTERVAL = "pagingInterval";

    /**
     * Parameter for the search string
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public static final String PARAM_SEARCH_STRING = "searchString";

    /**
     * Parameter for the number of last days to consider (e.g. only retrieve the tags of the last 7
     * days); value must be an integer
     */
    @Deprecated
    public static final String PARAM_NUMBER_OF_LAST_DAYS = "numberOfLastDays";

    /**
     * Parameter for the tag cloud mode; see TagCloudMode for possible values
     */
    @Deprecated
    public final static String PARAM_TAG_CLOUD_MODE = "tagCloudMode";

    /**
     * Parameter for a tag prefix
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public final static String PARAM_TAG_PREFIX = "tagPrefix";

    /**
     * Parameter if the widget or the content is editable to the current user
     */
    public final static String PARAM_EDITABLE = "editable";

    /**
     * parameter full text search
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public final static String PARAM_FULL_TEXT_SEARCH_STRING = "fullTextSearchString";

    /**
     * parameter post text search
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public final static String PARAM_POST_TEXT_SEARCH_STRING = "postTextSearchString";
    /**
     * True to allow the creation of the group
     */
    public final static String PARAM_ALLOW_GROUP_CREATION = "allowGroupCreation";

    /**
     * Parameter to show only manageable user groups
     */
    public final static String PARAM_SHOW_ONLY_MANAGEABLE_GROUPS = "showOnlyManageableGroups";

    /**
     * Parameter for the UserTaggedResource.
     *
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    public final static String PARAM_USER_TAGGED_RESOURCE_ID = "postId";

    /**
     * Parameter for the original posts id.
     */
    public final static String PARAM_ORIGINAL_POST_ID = "originalPostId";
    /**
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public final static String PARAM_SHOW_POSTS_FOR_ME = "showPostsForMe";
    /**
     * @Deprecated Use FilterWidgetParameterNameProvider
     */
    @Deprecated
    public final static String PARAM_PARENT_POST = "parentPostId";

    /**
     * Parameter for the user filter.
     */
    @Deprecated
    public static final String PARAM_USER_FILTER = "userFilter";
}
