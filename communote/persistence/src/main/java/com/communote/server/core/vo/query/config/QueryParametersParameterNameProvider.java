package com.communote.server.core.vo.query.config;

/**
 * Interface for parameter name provider.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface QueryParametersParameterNameProvider {
    /**
     * Returns the parameter name for the blog access level
     * 
     * @return the parameter name
     */
    public String getNameForAccessLevel();

    /**
     * 
     * @return parameter name for a comma separated list of content ids of attachments
     */
    public String getNameForAttachmentContentIds();

    /**
     * 
     * @return parameter name for a comma separated list of repository connectors ids. The ids will
     *         be used in conjunction with the content ids. If only one is provided it will be used
     *         for all.
     */
    public String getNameForAttachmentRepositoryConnectorIds();

    /**
     * Returns the parameter name for blog aliases.
     * 
     * @return The parameter name.
     */
    public String getNameForBlogAliases();

    /**
     * Returns the parameter name for blog ids.
     * 
     * @return The parameter name.
     */
    public String getNameForBlogIds();

    /**
     * Returns the parameter name for blog ids to exclude.
     * 
     * @return The parameter name.
     */
    public String getNameForBlogIdsToExclude();

    /**
     * Returns the parameter name for the blog search string.
     * 
     * @return The parameter name.
     */
    public String getNameForBlogSearchString();

    /**
     * With this parameter it is possible to define a number of additional entities, which should be
     * loaded when requesting data. These will be used to build information for paging, endless
     * scrolling etc..
     * 
     * @return The parameter name.
     */
    public String getNameForCheckAtLeastMoreResults();

    /**
     * Returns the parameter name for filtering direct messages.
     * 
     * @return The parameter name.
     */
    public String getNameForDirectMessages();

    /**
     * Returns the name for the parameter for
     * {@link com.communote.server.core.vo.query.DiscussionFilterMode} .
     * 
     * @return The parameter name.
     */
    public String getNameForDiscussionFilterMode();

    /**
     * Returns the name for the parameter for the discussion id. So if the user wants to filter for
     * a specific discussion.
     * 
     * @return the name for the discussion id
     */
    public String getNameForDiscussionId();

    /**
     * Returns the parameter name for the end date .
     * 
     * @return The parameter name.
     */
    public String getNameForEndDate();

    /**
     * @return The name for excluding top level topics from the result.
     */
    public String getNameForExcludeToplevelTopics();

    /**
     * Returns the name for the parameter to filter for topics with an external object with the
     * given externalId
     * 
     * @return The parameter name.
     */
    public String getNameForExternalObjectId();

    /**
     * Returns the name for the parameter to filter for topics with an external object with the
     * given externalSystemId
     * 
     * @return The parameter name.
     */
    public String getNameForExternalObjectSystemId();

    /**
     * Returns the parameter name for filtering favorites.
     * 
     * @return The parameter name.
     */
    public String getNameForFavorite();

    /**
     * Returns the parameter name for filtering for followed blogs.
     * 
     * @return The parameter name.
     */
    public String getNameForFollowedBlogs();

    /**
     * Returns the parameter name for filtering for followed notes.
     * 
     * @return The parameter name.
     */
    public String getNameForFollowedNotes();

    /**
     * Returns the name for the force all topics flags on topic queries.
     * 
     * @return The parameter name.
     */
    public String getNameForForceAllTopics();

    /**
     * Returns the parameter name for the full text search.
     * 
     * @return The parameter name.
     */
    public String getNameForFullTextSearchString();

    /**
     * @return The name for the boolean parameter to include results, i.e. notes or tags, from child
     *         topics within the query. This only works in combination with setting a target blog id
     *         or a list of blog ids.
     */
    public String getNameForIncludeChildTopics();

    /**
     * @return the name for the boolean parameter that describes whether comments should be
     *         considered when retrieving notes.
     */
    public String getNameForIncludeComments();

    /**
     * Returns the parameter name for max post count.
     * 
     * @return The parameter name.
     */
    public String getNameForMaxCount();

    /**
     * 
     * @return the name for the parameter for the maximum rank of the note must have for the user.
     */
    public String getNameForMaximumRank();

    /**
     * 
     * @return the name for the parameter for the minimum rank of the note must have for the user.
     */
    public String getNameForMinimumRank();

    /**
     * Returns the parameter name for the posts offset.
     * 
     * @return The parameter name.
     */
    public String getNameForOffset();

    /**
     * Returns the parameter name for parent post id.
     * 
     * @return The parameter name.
     */
    public String getNameForParentPostId();

    /**
     * @return The name for the parameter containing a list of parent topics ids to filter for.
     */
    public String getNameForParentTopicIds();

    /**
     * Returns the parameter name for the post search string.
     * 
     * @return The parameter name.
     */
    public String getNameForPostSearchString();

    /**
     * @return The name for the property filter parameter.
     */
    public String getNameForPropertyFilter();

    /**
     * @see com.communote.server.core.vo.query.post.NoteQueryParameters#setRetrieveOnlyNotesAfterDate
     * 
     * @return The parameter name.
     */
    public String getNameForRetrieveOnlyNotesAfterDate();

    /**
     * @see com.communote.server.core.vo.query.post.NoteQueryParameters#setRetrieveOnlyNotesAfterId
     * 
     * @return The parameter name.
     */
    public String getNameForRetrieveOnlyNotesAfterId();

    /**
     * @see com.communote.server.core.vo.query.post.NoteQueryParameters#setRetrieveOnlyNotesBeforeDate
     * 
     * @return The parameter name.
     */
    public String getNameForRetrieveOnlyNotesBeforeDate();

    /**
     * @see com.communote.server.core.vo.query.post.NoteQueryParameters#setRetrieveOnlyNotesBeforeId
     * 
     * @return The parameter name.
     */
    public String getNameForRetrieveOnlyNotesBeforeId();

    /**
     * Returns the name for the parameter, which defines the selected view type.
     * 
     * @return The parameter name.
     */
    public String getNameForSelectedViewType();

    /**
     * @return the showDiscussionParticipation.
     */
    public String getNameForShowDiscussionParticipation();

    /**
     * @return Parameter name for showing only root topics.
     */
    public String getNameForShowOnlyRootTopics();

    /**
     * @return The name for showing only toplevel topics.
     */
    public String getNameForShowOnlyToplevelTopics();

    /**
     * Returns the parameter name for show posts only for me.
     * 
     * @return The parameter name.
     */
    public String getNameForShowPostsForMe();

    /**
     * 
     * @return parameter name for sorting by the day and the rank
     */
    public String getNameForSortByDayDateAndRank();

    /**
     * Returns the parameter name for the start date.
     * 
     * @return The parameter name.
     */
    public String getNameForStartDate();

    /**
     * @return The parameter name for tag ids.
     */
    public String getNameForTagIds();

    /**
     * @return The parameter name for tag ids to exclude within the result set.
     */
    public String getNameForTagIdsToExclude();

    /**
     * Returns the parameter name for the tag prefix.
     * 
     * @return The parameter name.
     */
    public String getNameForTagPrefix();

    /**
     * Returns the parameter name for tags.
     * 
     * @return The parameter name.
     */
    public String getNameForTags();

    /**
     * Returns the name for the target blog ID which is a single blog ID.
     * 
     * @return The parameter name.
     */
    public String getNameForTargetBlogId();

    /**
     * 
     * @return name of parameter to sort topics by the youngest note (based on creation date)
     */
    public String getNameForTopicSortByLatestNote();

    /**
     * Returns the parameter name for the user aliases.
     * 
     * @return The parameter name.
     */
    public String getNameForUserAliases();

    /**
     * Returns the parameter name for the user ids.
     * 
     * @return The parameter name.
     */
    public String getNameForUserIds();

    /**
     * Returns the parameter name for the user ids to ignore.
     * 
     * @return The parameter name.
     */
    public String getNameForUserIdsToIgnore();

    /**
     * Returns the parameter name for the user search string.
     * 
     * @return The parameter name.
     */
    public String getNameForUserSearchString();

    /**
     * Returns the name for the parameter for the UserTaggedResource. So if the user wants to filter
     * for a specific element only.
     * 
     * @return The parameter name.
     */
    public String[] getNamesForNoteId();
}
