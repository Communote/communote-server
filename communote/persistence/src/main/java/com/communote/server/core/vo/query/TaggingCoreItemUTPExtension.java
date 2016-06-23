package com.communote.server.core.vo.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.ResolvedTopicToTopicConstants;
import com.communote.server.model.blog.UserToBlogRoleMappingConstants;
import com.communote.server.model.note.NoteConstants;

/**
 * Extension for the {@link CoreItemQueryInstance} which holds Note specific data.
 *
 * <b>Note:</b> Instances of this class can't be used for multiple users without setting a new user
 * id, as the user id is per default set to the id of the first user, who created an instances of
 * this class.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TaggingCoreItemUTPExtension {

    /** hql alias for the parent post. */
    public static final String ALIAS_PARENT_POST = "parent";
    /** the alias for the blog. */
    public static final String ALIAS_BLOG = "blog";
    /** the alias for the blog group */
    public static final String ALIAS_BLOG_GROUP = "blog_group";
    /** the alias for the blog group member */
    public static final String ALIAS_BLOG_GROUP_MEMBER = "blog_group_member";
    /** the alais for the user data of a blog group member */
    public static final String ALIAS_BLOG_GROUP_MEMBER_USER = "bgmu";
    /** hql parameter for the blog id. */
    private static final String PARAM_BLOG_ID = "blogFilter";
    /** hql parameter for the blog alias. */
    private static final String PARAM_BLOG_ALIAS = "blogAliasFilter";
    /** hql parameter for the parent post id filter. */
    private static final String PARAM_PARENT_POST = "parentPostId";
    /** alias of the note */
    public static final String ALIAS_NOTE = "utr";
    /** The blog id. */
    private Long[] blogFilter;
    /** The blog id. */
    private String[] blogAliasFilter;
    /** The user id. */
    private Long userId;
    /** The parent post id. */
    private Long parentPostId;
    /** The blog access level. */
    private TopicAccessLevel topicAccessLevel = TopicAccessLevel.READ;
    /** If set this will be used in conjunction with usersToBeNotified. */
    private boolean showDiscussionParticipation;
    /**
     * the parameter shows whether the request is public or not
     */
    private boolean publicAccess = false;
    /** The include empty blogs. */
    private boolean includeEmptyBlogs = false;
    private boolean includeChildTopics = false;

    /**
     * Default constructor.
     */
    public TaggingCoreItemUTPExtension() {
        // TODO maybe the userId shouldn't be configurable at all?
        userId = SecurityHelper.getCurrentUserId();
    }

    /**
     * Appends the role constraint.
     *
     * @param where
     *            the string builder to hold the where clause
     * @param numericRole
     *            the parameter referencing the roles to contain
     * @param topicAlias
     *            Alias of the topic within the query.
     */
    private void appendRoleConstraint(StringBuilder where, int numericRole, String topicAlias) {
        where.append(topicAlias + "." + BlogConstants.ID + " IN (SELECT ");
        where.append(UserToBlogRoleMappingConstants.BLOGID);
        where.append(" FROM " + UserToBlogRoleMappingConstants.CLASS_NAME);
        where.append(" WHERE " + UserToBlogRoleMappingConstants.USERID + " = :");
        where.append(TimelineQueryParameters.PARAM_USER_ID + " AND ");
        where.append(UserToBlogRoleMappingConstants.NUMERICROLE + ">=");
        where.append(numericRole);

        // this nothing else as the join if blog to notes of the inner query. but why here: see
        // see KENMEI-5083 "MSSQL - bad Performance with TagCloud-request"
        if (CommunoteRuntime.getInstance().getConfigurationManager().getDatabaseConfiguration()
                .isExtendSubselectsWithOuterConditions()) {
            where.append(" AND " + ALIAS_NOTE + "." + NoteConstants.BLOG + " = " + topicAlias + "."
                    + BlogConstants.ID);
        }

        where.append(") ");
    }

    /**
     * @return the blogAliasFilter
     */
    public String[] getBlogAliasFilter() {
        return blogAliasFilter;
    }

    /**
     * Returns the blog id's which should be used to filter the results.
     *
     * @return the blog id's or null if unset
     */
    public Long[] getBlogFilter() {
        return blogFilter;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        if (!ArrayUtils.isEmpty(this.blogFilter)) {
            params.put(PARAM_BLOG_ID, blogFilter);
        }
        if (!ArrayUtils.isEmpty(this.blogAliasFilter)) {
            params.put(PARAM_BLOG_ALIAS, blogAliasFilter);
        }
        if (userId != null) {
            params.put(TimelineQueryParameters.PARAM_USER_ID, userId);
        }
        if (this.parentPostId != null) {
            params.put(PARAM_PARENT_POST, this.parentPostId);
        }
        return params;
    }

    /**
     * Gets the parent post id.
     *
     * @return the parent post id
     */
    public Long getParentPostId() {
        return parentPostId;
    }

    /**
     * Gets the topic access level.
     *
     * @return the topic access level. Default value is {@link TopicAccessLevel#READ}.
     */
    public TopicAccessLevel getTopicAccessLevel() {
        return topicAccessLevel;
    }

    /**
     * Get the User Id for which the rights to access a blog is checked.
     *
     * @return User ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @return true if a sinlge blog is selected for filtering
     */
    public boolean isFilteredForSingleBlog() {
        return blogFilter != null && blogFilter.length == 1 && blogFilter[0] > 0;
    }

    /**
     * @return True, if results of child topics should be included too.
     */
    public boolean isIncludeChildTopics() {
        return includeChildTopics;
    }

    /**
     * Checks if is include empty blogs.
     *
     * @return true, if is include empty blogs
     */
    public boolean isIncludeEmptyBlogs() {
        return includeEmptyBlogs;
    }

    /**
     * the parameter shows whether the request is public or not
     *
     * @return the publicAccess
     */
    public boolean isPublicAccess() {
        return publicAccess;
    }

    /**
     * @return the showDiscussionParticipation
     */
    public boolean isShowDiscussionParticipation() {
        return showDiscussionParticipation;
    }

    /**
     * Lowercases the blog alias filter
     */
    private void lowerCaseBlogAliases() {
        if (this.blogAliasFilter != null) {
            for (int i = 0; i < this.blogAliasFilter.length; i++) {
                this.blogAliasFilter[i] = this.blogAliasFilter[i].toLowerCase();
            }
        }
    }

    /**
     * Render access query.
     *
     * @param where
     *            the where
     * @param prefix
     *            The prefix to use.
     * @param topicAlias
     *            Alias of the topic within the query.
     */
    public void renderAccessQuery(StringBuilder where, String prefix, String topicAlias) {
        if (TopicAccessLevel.SYSTEM.equals(topicAccessLevel)) {
            if (SecurityHelper.isInternalSystem()) {
                return;
            }
            throw new AccessDeniedException("The current user " + SecurityHelper.getCurrentUserId()
                    + " must be the internal system user to use the topic access level SYSTEM");
        }
        where.append(prefix + "( ");

        if (isPublicAccess()) {
            renderAccessQueryForPublicUser(where, topicAccessLevel, topicAlias);
        } else {
            switch (topicAccessLevel) {
            case READ:
                where.append(topicAlias + "." + BlogConstants.ALLCANREAD + " = " + Boolean.TRUE
                        + " OR " + topicAlias + "." + BlogConstants.ALLCANWRITE + " = "
                        + Boolean.TRUE + " OR " + topicAlias + "." + BlogConstants.PUBLICACCESS
                        + " = " + Boolean.TRUE);
                if (userId != null) {
                    where.append(" OR ");
                    appendRoleConstraint(where,
                            BlogRoleHelper.convertRoleToNumeric(BlogRole.VIEWER), topicAlias);
                }
                break;
            case WRITE:
                where.append(topicAlias + "." + BlogConstants.ALLCANWRITE + " = " + Boolean.TRUE);
                if (userId != null) {
                    where.append(" OR ");
                    appendRoleConstraint(where,
                            BlogRoleHelper.convertRoleToNumeric(BlogRole.MEMBER), topicAlias);
                }
                break;
            case MANAGER:
                if (userId == null) {
                    // force zero results with user id being -1
                    this.setUserId(new Long(-1));
                }
                appendRoleConstraint(where, BlogRoleHelper.convertRoleToNumeric(BlogRole.MANAGER),
                        topicAlias);
                break;
            default:
                throw new IllegalArgumentException("unsupported topic access level :"
                        + topicAccessLevel);
            }
        }
        where.append(") ");
    }

    /**
     * Renders the access query for the public user.
     *
     * @param where
     *            The where query.
     * @param level
     *            The level.
     * @param topicAlias
     *            Alias of the topic within the query.
     */
    private void renderAccessQueryForPublicUser(StringBuilder where, TopicAccessLevel level,
            String topicAlias) {
        if (level.equals(TopicAccessLevel.READ)) {
            where.append(topicAlias + "." + BlogConstants.PUBLICACCESS + " = " + Boolean.TRUE + " ");
            setUserId(null);
        } else {
            // return an empty result if public access and access level not read
            this.setUserId(new Long(-1));
            appendRoleConstraint(where, BlogRoleHelper.convertRoleToNumeric(BlogRole.MANAGER),
                    topicAlias);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean renderSubQuery(StringBuilder mainQuery, StringBuilder whereQuery, String prefix,
            String utiPrefix) {

        int whereQueryLength = whereQuery.length();

        if (this.getParentPostId() != null) {
            mainQuery.append(" left join " + ALIAS_NOTE + ".");
            mainQuery.append(NoteConstants.PARENT + " ");
            mainQuery.append(ALIAS_PARENT_POST);
        }
        if (isIncludeEmptyBlogs()) {
            mainQuery.append(" right ");
        } else {
            mainQuery.append(" left ");
        }
        mainQuery.append("join " + ALIAS_NOTE + "." + NoteConstants.BLOG + " " + ALIAS_BLOG + " ");
        renderSubQueryWhereClause(whereQuery, prefix);

        // return true if something was wrote
        return whereQueryLength < whereQuery.length();
    }

    /**
     * This method renders the where clause for the sub query.
     *
     * @param whereQuery
     *            The where clause.
     * @param prefix
     *            The prefix.
     */
    private void renderSubQueryWhereClause(StringBuilder whereQuery, String prefix) {
        if (this.topicAccessLevel != null) {
            renderAccessQuery(whereQuery, prefix, ALIAS_BLOG);
            prefix = " and ";
        }

        List<String> topicFilters = new ArrayList<String>();
        if (ArrayUtils.isNotEmpty(blogFilter)) {
            topicFilters.add(" " + ALIAS_BLOG + "." + BlogConstants.ID + " IN (:" + PARAM_BLOG_ID
                    + " )");
            if (includeChildTopics) {
                topicFilters.add(ALIAS_BLOG + "." + BlogConstants.ID + " IN (SELECT "
                        + ResolvedTopicToTopicConstants.CHILDTOPICID + " FROM "
                        + ResolvedTopicToTopicConstants.CLASS_NAME + " WHERE "
                        + ResolvedTopicToTopicConstants.PARENTTOPICID + " IN (:" + PARAM_BLOG_ID
                        + "))");
            }
        }
        if (ArrayUtils.isNotEmpty(blogAliasFilter)) {
            topicFilters.add(ALIAS_BLOG + "." + BlogConstants.NAMEIDENTIFIER + " IN (:"
                    + PARAM_BLOG_ALIAS + ")");
        }
        if (!topicFilters.isEmpty()) {
            whereQuery.append(prefix + "(");
            whereQuery.append(StringUtils.join(topicFilters, " or "));
            whereQuery.append(")");
            prefix = " and ";
        }

        if (this.parentPostId != null) {
            whereQuery.append(prefix);
            whereQuery.append(" ( " + ALIAS_NOTE + "." + NoteConstants.PARENT + "."
                    + NoteConstants.ID + " = :" + PARAM_PARENT_POST + " or " + ALIAS_NOTE + "."
                    + NoteConstants.ID + " = :" + PARAM_PARENT_POST + " ) ");
        }
    }

    /**
     * @param blogAliasFilter
     *            the blogAliasFilter to set
     */
    public void setBlogAliasFilter(String[] blogAliasFilter) {
        this.blogAliasFilter = blogAliasFilter;
        lowerCaseBlogAliases();
    }

    /**
     * Can be used to filter Notes for a blog association.
     *
     * @param topicIds
     *            Array of topic ids to filter for.
     */
    public void setBlogFilter(Long[] topicIds) {
        this.blogFilter = topicIds;
    }

    /**
     * Sets the blog id.
     *
     * @param blogId
     *            the new blog id
     * @deprecated Use {@link #setBlogFilter} instead.
     */
    @Deprecated
    public void setBlogId(Long blogId) {
        if (blogId != null) {
            this.blogFilter = new Long[] { blogId };
        }
    }

    /**
     * @param includeChildTopic
     *            True, if results of child topics should be included too.
     */
    public void setIncludeChildTopics(boolean includeChildTopic) {
        this.includeChildTopics = includeChildTopic;
    }

    /**
     * Sets the include empty blogs.
     *
     * @param includeEmptyBlogs
     *            the new include empty blogs
     */
    public void setIncludeEmptyBlogs(boolean includeEmptyBlogs) {
        this.includeEmptyBlogs = includeEmptyBlogs;
    }

    /**
     * Sets the parent post id.
     *
     * @param parentPostId
     *            the new parent post id
     */
    public void setParentPostId(Long parentPostId) {
        this.parentPostId = parentPostId;
    }

    /**
     * the parameter shows whether the request is public or not
     *
     * @param publicAccess
     *            the publicAccess to set
     */
    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    /**
     * @param showDiscussionParticipation
     *            the showDiscussionParticipation to set
     */
    public void setShowDiscussionParticipation(boolean showDiscussionParticipation) {
        this.showDiscussionParticipation = showDiscussionParticipation;
    }

    /**
     * Sets the topic access level.
     *
     * @param topicAccessLevel
     *            the new topic access level.
     */
    public void setTopicAccessLevel(TopicAccessLevel topicAccessLevel) {
        if (topicAccessLevel == null) {
            throw new IllegalArgumentException("The topicAccessLevel may not be null.");
        }
        this.topicAccessLevel = topicAccessLevel;
    }

    /**
     * Set the User Id for which the rights to access a blog is checked.
     *
     * @param userId
     *            User ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
