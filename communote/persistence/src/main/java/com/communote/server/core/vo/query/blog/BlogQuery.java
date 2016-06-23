package com.communote.server.core.vo.query.blog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.blog.BlogData;
import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.PropertyQuery;
import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.ResolvedTopicToTopicConstants;
import com.communote.server.model.blog.UserToBlogRoleMappingConstants;
import com.communote.server.model.external.ExternalObjectConstants;
import com.communote.server.model.i18n.MessageConstants;
import com.communote.server.model.tag.TagConstants;
import com.communote.server.model.user.UserConstants;
import com.communote.server.model.user.LanguageConstants;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <I>
 *            The Query Instance which is valid for this definition
 * @param <R>
 *            Type of the result for this query.
 */
public class BlogQuery<R, I extends BlogQueryParameters> extends PropertyQuery<R, I> {

    /** Static default query. */
    public static final BlogQuery<BlogData, BlogQueryParameters> DEFAULT_QUERY =
            new BlogQuery<BlogData, BlogQueryParameters>(BlogData.class);

    /** blog query alias */
    public static final String ALIAS_BLOG = "blog";

    public static final String RESULT_ALIAS_BLOG = ALIAS_BLOG + ".";

    /** tags query alias */
    public static final String ALIAS_TAGS = "tags";

    private static final String QUERY_FOLLOW = ALIAS_BLOG + "." + BlogConstants.GLOBALID
            + " IN (select fuseritems.id from " + UserConstants.CLASS_NAME
            + " as fuser inner join fuser." + UserConstants.FOLLOWEDITEMS
            + " as fuseritems where fuser.id=";

    private final Class<R> resultListItemType;

    /**
     * blog group query alias
     */
    public static final String ALIAS_BLOG_GROUP = "blog_group";

    /**
     * blog group query member alias
     */
    public static final String ALIAS_BLOG_GROUP_MEMBER = "blog_group_member";

    /**
     * blog group member query alias
     */
    public static final String ALIAS_BLOG_GROUP_MEMBER_USER = "blog_group_member_user";

    /**
     * alias for the external object
     */
    public static final String ALIAS_EXTERNAL_OBJECT = "external_object";

    private List<String> constructorParameter;

    /**
     * Constructor.
     * 
     * @param resultListItemType
     *            Type of the result item.
     */
    public BlogQuery(Class<R> resultListItemType) {
        this.resultListItemType = resultListItemType;
    }

    /**
     * Appends the role constraint.
     * 
     * @param where
     *            the stringbuilder to hold the where clause
     * @param numericRole
     *            the parameter referencing the roles to contain
     */
    private void appendRoleConstraint(StringBuilder where, int numericRole) {
        where.append(ALIAS_BLOG + "." + BlogConstants.ID + " IN (SELECT DISTINCT "
                + UserToBlogRoleMappingConstants.BLOGID + " FROM "
                + UserToBlogRoleMappingConstants.CLASS_NAME
                + " WHERE " + UserToBlogRoleMappingConstants.USERID + " = :"
                + BlogQueryParameters.PARAM_USER_ID + " AND "
                + UserToBlogRoleMappingConstants.NUMERICROLE + ">=");
        where.append(numericRole);
        where.append(")");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildQuery(I queryParameters) {
        StringBuilder mainQuery = new StringBuilder("SELECT ");
        StringBuilder whereQuery = new StringBuilder();
        this.renderSelect(mainQuery);
        renderFromClause(queryParameters, mainQuery);

        boolean renderAccess = queryParameters.getAccessLevel() != null;
        boolean renderUser = queryParameters.getUserId() != null;
        String wherePrefix = StringUtils.EMPTY;

        if (renderAccess || renderUser) {
            wherePrefix = this.renderAccessQuery(whereQuery, queryParameters);
        }
        wherePrefix = renderBlogConstraints(queryParameters, whereQuery, wherePrefix);
        wherePrefix = renderLastModificationConstraint(queryParameters, whereQuery,
                wherePrefix);

        if (renderTextSearch(queryParameters, whereQuery, wherePrefix)) {
            wherePrefix = AND;
        }
        if (renderFollowedItemsOnlyQuery(queryParameters, whereQuery, wherePrefix)) {
            wherePrefix = AND;
        }
        wherePrefix = renderTagPrefixQuery(queryParameters, whereQuery, wherePrefix);
        wherePrefix = renderTagsQueryFilterByTagStoreAliases(queryParameters, whereQuery,
                wherePrefix);
        wherePrefix = renderSubQueryTagsFilterByTagId(queryParameters, wherePrefix,
                whereQuery);
        wherePrefix = renderSubQueryTagsFilterByTagStores(queryParameters, wherePrefix,
                whereQuery);
        wherePrefix = renderSubQueryForTopicHierarchies(queryParameters, wherePrefix, mainQuery,
                whereQuery);
        wherePrefix = renderPropertyFilters(queryParameters, whereQuery, wherePrefix,
                RESULT_ALIAS_BLOG);
        wherePrefix = renderExternalObjectFilter(queryParameters, wherePrefix, whereQuery);

        renderTagsQuery(queryParameters, whereQuery, wherePrefix);

        if (whereQuery.length() > 0) {
            mainQuery.append(" WHERE ");
            mainQuery.append(whereQuery);
        }
        renderGroupBy(mainQuery);
        renderOrderbyClause(mainQuery, queryParameters);
        return mainQuery.toString();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public I createInstance() {
        return (I) new BlogQueryParameters();
    }

    /**
     * Returns an array with the field names which should be checked against a text string.
     * 
     * @param instance
     *            the query instance
     * @return the array with field names. The array can be empty in case no search field was set.
     */
    private String[] getBlogSearchFields(I instance) {
        List<String> fields = new ArrayList<String>(3);
        if (instance.isSearchInField(BlogQueryParameters.SEARCH_FIELD_DESCRIPTION)) {
            fields.add(ALIAS_BLOG + "." + BlogConstants.DESCRIPTION);
        }
        if (instance.isSearchInField(BlogQueryParameters.SEARCH_FIELD_IDENTIFIER)) {
            fields.add(ALIAS_BLOG + "." + BlogConstants.NAMEIDENTIFIER);
        }
        if (instance.isSearchInField(BlogQueryParameters.SEARCH_FIELD_TITLE)) {
            fields.add(ALIAS_BLOG + "." + BlogConstants.TITLE);
        }
        if (instance.isSearchInField(BlogQueryParameters.SEARCH_FIELD_BLOG_TAGS)) {
            fields.add(ALIAS_TAGS + "." + TagConstants.DEFAULTNAME);
        }
        return fields.toArray(new String[fields.size()]);
    }

    /**
     * @return the list with constructor parameters
     */
    protected List<String> getConstructorParameter() {
        return this.constructorParameter;
    }

    /**
     * @return the list with group by parameters
     */
    protected List<String> getGroupByParameter() {
        return this.constructorParameter;
    }

    /**
     * @return the class of list items
     */
    protected Class<R> getResultListItem() {
        return resultListItemType;
    }

    /**
     * @param where
     *            the where clause to append to
     * @param parameters
     *            the instance to use
     * @return The next WHERE prefix.
     */
    protected String renderAccessQuery(StringBuilder where, I parameters) {
        if (parameters.isForceAllTopics()) {
            SecurityHelper.assertCurrentUserIsClientManager();
            return "";
        }
        TopicAccessLevel level = parameters.getAccessLevel();
        if (level != null) {
            renderTopicAccessLevelQuery(where, parameters, level);
        }
        return AND;

    }

    /**
     * Renders the blog constraints.
     * 
     * @param queryInstance
     *            the query instance
     * @param whereQuery
     *            the where clause to append to
     * @param whereQueryPrefix
     *            the prefix to prepend
     * @return whether something was appended
     */
    private String renderBlogConstraints(I queryInstance, StringBuilder whereQuery,
            String whereQueryPrefix) {
        if (!ArrayUtils.isEmpty(queryInstance.getBlogsToExclude())) {
            whereQuery.append(whereQueryPrefix);
            whereQuery.append(ALIAS_BLOG);
            whereQuery.append(".");
            whereQuery.append(BlogConstants.ID);
            whereQuery.append(" NOT IN (:");
            whereQuery.append(BlogQueryParameters.PARAM_BLOGS_TO_IGNORE);
            whereQuery.append(" )");
            whereQueryPrefix = AND;
        }

        if (!ArrayUtils.isEmpty(queryInstance.getBlogIds())) {
            whereQuery.append(whereQueryPrefix);
            whereQuery.append("(" + ALIAS_BLOG);
            whereQuery.append(".");
            whereQuery.append(BlogConstants.ID);
            whereQuery.append(" IN (:");
            whereQuery.append(BlogQueryParameters.PARAM_BLOG_IDS);
            whereQuery.append(" )");
            if (queryInstance.isIncludeChildTopics()) {
                whereQuery.append(OR);
                whereQuery.append(ALIAS_BLOG
                        + "."
                        + BlogConstants.ID
                        + " IN (SELECT " + ResolvedTopicToTopicConstants.CHILDTOPICID
                        + " FROM " + ResolvedTopicToTopicConstants.CLASS_NAME
                        + " WHERE " + ResolvedTopicToTopicConstants.PARENTTOPICID + " IN (:");
                whereQuery.append(BlogQueryParameters.PARAM_BLOG_IDS);
                whereQuery.append(")))");
            } else {
                whereQuery.append(")");
            }
            whereQueryPrefix = AND;
        }

        if (!ArrayUtils.isEmpty(queryInstance.getBlogAliases())) {
            whereQuery.append(whereQueryPrefix);
            whereQuery.append(ALIAS_BLOG);
            whereQuery.append(".");
            whereQuery.append(BlogConstants.NAMEIDENTIFIER);
            whereQuery.append(" IN (:");
            whereQuery.append(BlogQueryParameters.PARAM_BLOG_ALIASES);
            whereQuery.append(" )");
            whereQueryPrefix = AND;
        }
        return whereQueryPrefix;
    }

    private String renderExternalObjectFilter(I queryParameters, String wherePrefix,
            StringBuilder whereQuery) {

        if (queryParameters.getExternalObjectId() != null
                || queryParameters.getExternalObjectSystemId() != null) {

            whereQuery.append(wherePrefix);
            whereQuery.append(" EXISTS ( select external.id from "
                    + BlogConstants.CLASS_NAME + " extBlog "
                    + " left join extBlog." + BlogConstants.EXTERNALOBJECTS + " external "
                    + "where extBlog.id = " + RESULT_ALIAS_BLOG + "id ");

            if (queryParameters.getExternalObjectId() != null) {
                whereQuery.append(AND + "external." + ExternalObjectConstants.EXTERNALID + " = :"
                        + BlogQueryParameters.PARAM_EXTERNAL_OBJECT_ID);
            }
            if (queryParameters.getExternalObjectSystemId() != null) {
                whereQuery.append(AND + "external." + ExternalObjectConstants.EXTERNALSYSTEMID
                        + " = :" + BlogQueryParameters.PARAM_EXTERNAL_OBJECT_SYSTEM_ID);
            }

            whereQuery.append(" ) ");

            wherePrefix = AND;
        }

        return wherePrefix;
    }

    /**
     * renders the constraint to return only followed items
     * 
     * @param instance
     *            current query instance
     * @param whereQuery
     *            the query to write to
     * @param prefix
     *            the prefix to prepend before writing to the query
     * @return the whether something was appended to the query
     */
    private boolean renderFollowedItemsOnlyQuery(I instance, StringBuilder whereQuery, String prefix) {
        if (!instance.showOnlyFollowedItems()) {
            return false;
        }
        whereQuery.append(prefix);
        whereQuery.append(QUERY_FOLLOW);
        // TODO this should be a named parameter
        whereQuery.append(SecurityHelper.getCurrentUserId());
        whereQuery.append(" ) ");
        return true;
    }

    /**
     * Renders the from clause of the query.
     * 
     * @param queryInstance
     *            the query instance
     * @param mainQuery
     *            the main part of the query to append to
     */
    private void renderFromClause(I queryInstance, StringBuilder mainQuery) {
        mainQuery.append(" FROM ");
        mainQuery.append(BlogConstants.CLASS_NAME);
        mainQuery.append(" as ");
        mainQuery.append(ALIAS_BLOG);
        boolean tagPrefixNotBlank = StringUtils.isNotBlank(queryInstance.getTagPrefix());
        if (tagPrefixNotBlank
                || tagEntityRequiredInQuery()
                || queryInstance.isSearchInField(BlogQueryParameters.SEARCH_FIELD_BLOG_TAGS)
                && queryInstance.getTextFilter() != null
                && queryInstance.getTextFilter().length > 0) {
            mainQuery.append(" inner join " + ALIAS_BLOG + "." + BlogConstants.TAGS + " as ");
            mainQuery.append(ALIAS_TAGS);
            if (tagPrefixNotBlank && queryInstance.isMultilingualTagPrefixSearch()) {
                mainQuery.append(" LEFT JOIN " + ALIAS_TAGS + "."
                        + TagConstants.NAMES + " tagName LEFT JOIN tagName."
                        + MessageConstants.LANGUAGE
                        + " language");
            }
        }
    }

    /**
     * @param query
     *            The query.
     */
    protected void renderGroupBy(StringBuilder query) {
        query.append(" GROUP BY ");
        query.append(StringUtils.join(getGroupByParameter(), ","));
        query.append(" ");
    }

    /**
     * Renders the constraint for a minimum last modification date if necessary.
     * 
     * @param queryInstance
     *            the query instance
     * @param whereQuery
     *            the where clause to append to
     * @param whereQueryPrefix
     *            the current prefix
     * @return whether something was appended to the query
     */
    private String renderLastModificationConstraint(I queryInstance, StringBuilder whereQuery,
            String whereQueryPrefix) {
        if (queryInstance.getMinimumLastModificationDate() != null) {
            whereQuery.append(whereQueryPrefix);
            whereQuery.append(ALIAS_BLOG + "."
                    + BlogConstants.LASTMODIFICATIONDATE + " > :"
                    + BlogQueryParameters.PARAM_LAST_MODIFICATION_DATE);
            return AND;
        }
        return whereQueryPrefix;
    }

    /**
     * @param query
     *            The query.
     */
    protected void renderSelect(StringBuilder query) {
        query.append(" new ");
        query.append(getResultListItem().getName());
        query.append(" ( ");
        query.append(StringUtils.join(getConstructorParameter(), ","));
        query.append(" ) ");
    }

    /**
     * Renders the where part for topic hierarchies.
     * 
     * @param queryParameters
     *            The query parameters.
     * @param wherePrefix
     *            The prefix to use.
     * @param selectQuery
     *            The select query.
     * @param whereQuery
     *            The query to append.
     * @return The next prefix.
     */
    protected String renderSubQueryForTopicHierarchies(I queryParameters, String wherePrefix,
            StringBuilder selectQuery, StringBuilder whereQuery) {
        if (queryParameters.isShowOnlyToplevelTopics()) {
            whereQuery.append(AND + ALIAS_BLOG + "." + BlogConstants.TOPLEVELTOPIC
                    + " = true");
        } else if (queryParameters.isShowOnlyRootTopics()) {
            whereQuery.append(wherePrefix + "(" + ALIAS_BLOG + "." + BlogConstants.PARENTS
                    + ".size = 0");
            if (queryParameters.isExcludeToplevelTopics()) {
                whereQuery.append(AND + ALIAS_BLOG + "." + BlogConstants.TOPLEVELTOPIC
                        + " = false)");
            } else {
                whereQuery.append(")");
            }
            wherePrefix = AND;
        } else if (queryParameters.isExcludeToplevelTopics()) {
            whereQuery.append(wherePrefix);
            whereQuery.append(ALIAS_BLOG + "." + BlogConstants.TOPLEVELTOPIC + " = false");
        }
        if (queryParameters.getParentTopicIds().length > 0) {
            selectQuery.append(" LEFT JOIN " + ALIAS_BLOG + "." + BlogConstants.PARENTS
                    + " topic_parent ");
            whereQuery.append(wherePrefix + "topic_parent." + BlogConstants.ID + " IN (:"
                    + BlogQueryParameters.PARAM_PARENT_TOPIC_IDS + ")");
            wherePrefix = AND;
        }
        return wherePrefix;
    }

    /**
     * Renders the query for filtering by tag ids.
     * 
     * @param instance
     *            The {@link BlogQueryParameters}.
     * @param innerPrefix
     *            The inner prefix.
     * @param subQuery
     *            The sub query.
     * @return The next inner prefix.
     */
    private String renderSubQueryTagsFilterByTagId(I instance, String innerPrefix,
            StringBuilder subQuery) {
        if (instance.getTagIds().isEmpty()) {
            return innerPrefix;
        }
        subQuery.append(innerPrefix);
        subQuery.append("EXISTS(SELECT 1 FROM " + BlogConstants.CLASS_NAME
                + " blog3 LEFT JOIN blog3." + BlogConstants.TAGS + " tag3 WHERE blog3."
                + BlogConstants.ID + " = " + ALIAS_BLOG + "." + BlogConstants.ID + " AND tag3."
                + TagConstants.ID + " IN (");
        subQuery.append(StringUtils.join(instance.getTagIds(), ","));
        subQuery.append(") GROUP BY blog3." + BlogConstants.ID + " HAVING count(blog3."
                + BlogConstants.ID + ") = ");
        subQuery.append(instance.getTagIds().size());
        subQuery.append(")");
        return AND;
    }

    /**
     * Renders the query for filtering by TagStores.
     * 
     * @param instance
     *            The {@link BlogQueryParameters}.
     * @param innerPrefix
     *            The inner prefix.
     * @param subQuery
     *            The sub query.
     * @return The next inner prefix.
     */
    private String renderSubQueryTagsFilterByTagStores(I instance, String innerPrefix,
            StringBuilder subQuery) {
        if (instance.getTagStoreTagIds().isEmpty()) {
            return innerPrefix;
        }
        for (Entry<String, Set<String>> tagStore : instance
                .getTagStoreTagIds().entrySet()) {
            if (tagStore.getValue() == null || tagStore.getValue().isEmpty()) {
                continue;
            }
            String tagStoreQueryName = instance.addParameter(tagStore.getKey());
            subQuery.append(innerPrefix);
            subQuery.append("EXISTS(SELECT 1 FROM " + BlogConstants.CLASS_NAME
                    + " blog4 LEFT JOIN blog4." + BlogConstants.TAGS + " tagTagStore WHERE blog4."
                    + BlogConstants.ID + " = " + ALIAS_BLOG + "." + BlogConstants.ID
                    + " AND tagTagStore." + TagConstants.TAGSTOREALIAS + "=" + tagStoreQueryName
                    + " AND tagTagStore." + TagConstants.TAGSTORETAGID + " IN (");
            String seperator = "";
            for (String tagStoreTagId : tagStore.getValue()) {
                String tagStoreTagIdQueryName = instance.addParameter(tagStoreTagId);
                subQuery.append(seperator + tagStoreTagIdQueryName);
                seperator = ",";
            }
            subQuery.append(") GROUP BY blog4." + BlogConstants.ID + " HAVING count(blog4."
                    + BlogConstants.ID + ") = ");
            subQuery.append(tagStore.getValue().size());
            subQuery.append(")");
            innerPrefix = AND;
        }
        return innerPrefix;
    }

    /**
     * Renders the tag search prefix constraint to the query
     * 
     * @param queryInstance
     *            the current query instance
     * @param query
     *            the query to write to
     * @param prefix
     *            the prefix to prepend before writing to the query
     * @return whether something was appended to the query
     */
    protected String renderTagPrefixQuery(I queryInstance, StringBuilder query, String prefix) {
        String tagPrefix = queryInstance.getTagPrefix();
        if (StringUtils.isBlank(tagPrefix)) {
            return prefix;
        }
        query.append(prefix);
        if (queryInstance.isMultilingualTagPrefixSearch()) {
            String languageCode = queryInstance.addParameter(queryInstance.getLanguageCode());
            query.append("((lower(" + ALIAS_TAGS + ".");
            query.append(TagConstants.DEFAULTNAME + ") LIKE (:"
                    + BlogQueryParameters.PARAM_BLOG_TAGPREFIX_SEARCH + ") AND tagName."
                    + MessageConstants.MESSAGE + " is null) OR (lower(" + ALIAS_TAGS + "."
                    + TagConstants.DEFAULTNAME + ") LIKE :"
                    + BlogQueryParameters.PARAM_BLOG_TAGPREFIX_SEARCH + " AND tagName."
                    + MessageConstants.MESSAGE + " is not null AND language."
                    + LanguageConstants.LANGUAGECODE + " <> ");
            query.append(languageCode);
            query.append(") OR (lower(tagName." + MessageConstants.MESSAGE + ") LIKE :"
                    + BlogQueryParameters.PARAM_BLOG_TAGPREFIX_SEARCH + " AND language."
                    + LanguageConstants.LANGUAGECODE + " = ");
            query.append(languageCode);
            query.append("))");
        } else {
            query.append("lower(" + ALIAS_TAGS + "." + TagConstants.DEFAULTNAME + ") LIKE (:"
                    + BlogQueryParameters.PARAM_BLOG_TAGPREFIX_SEARCH + ")");
        }
        return AND;
    }

    /**
     * This method renders the query part for tags.
     * 
     * @param instance
     *            the current query instance
     * @param query
     *            the query to write to
     * @param prefix
     *            the prefix to prepend before writing to the query
     * @return whether something was appended to the query
     */
    protected boolean renderTagsQuery(I instance, StringBuilder query, String prefix) {
        if (instance.getTags() == null || instance.getTags().length == 0) {
            return false;
        }
        String[] tags = instance.getTags();
        query.append(prefix);
        query.append(" ( " + ALIAS_BLOG + ".id in ( select innerBlog.id from "
                + BlogConstants.CLASS_NAME + " innerBlog left join innerBlog." + BlogConstants.TAGS
                + " innerTag where innerTag." + TagConstants.TAGSTORETAGID + " in (");
        for (int i = 0; i < tags.length; i++) {
            if (i > 0) {
                query.append(", ");
            }
            query.append(":" + instance.getBlogTagConstant(i));
        }
        query.append(") group by innerBlog.id having count(innerBlog.id)=");
        query.append(tags.length);
        query.append(" ))");
        return true;
    }

    /**
     * @param instance
     *            The query instance.
     * @param whereQuery
     *            The where query.
     * @param prefix
     *            The prefix to use.
     * @return The next prefix.
     */
    private String renderTagsQueryFilterByTagStoreAliases(I instance,
            StringBuilder whereQuery, String prefix) {
        if (instance.getTagStoreAliases().isEmpty()) {
            return prefix;
        }
        whereQuery.append(prefix + BlogQuery.ALIAS_TAGS + "."
                + TagConstants.TAGSTOREALIAS
                + " IN (");
        String seperator = "";
        for (Object tagStoreAlias : instance.getTagStoreAliases()) {
            whereQuery.append(seperator + instance.addParameter(tagStoreAlias.toString()));
            seperator = ",";
        }
        whereQuery.append(")");
        return AND;
    }

    /**
     * Render the search constraint to search in character fields of a blog. Also takes care of
     * checking the fields of the default blog if necessary and adding a constraint to search in the
     * blog tags.
     * 
     * @param queryInstance
     *            the query instance
     * @param query
     *            the query to write to
     * @param prefix
     *            the prefix to add before writing to the query
     * @return whether something was appended to the query
     */
    private boolean renderTextSearch(I queryInstance, StringBuilder query, String prefix) {
        String[] blogSearchFields = getBlogSearchFields(queryInstance);
        String[] searchParamNames = queryInstance.getTextFilterParamNames();
        if (blogSearchFields.length != 0 && searchParamNames != null && searchParamNames.length > 0) {
            StringBuilder subquery = new StringBuilder();
            renderSearch(subquery, blogSearchFields, searchParamNames, true, false);
            query.append(prefix + subquery);
            return true;
        }
        return false;
    }

    /**
     * @param where
     *            The where query.
     * @param parameters
     *            The parameters.
     * @param level
     *            The level.
     */
    private void renderTopicAccessLevelQuery(StringBuilder where, I parameters,
            TopicAccessLevel level) {
        where.append("( ");
        if (SecurityHelper.isPublicUser()) {
            if (level.equals(TopicAccessLevel.READ)) {
                where.append(ALIAS_BLOG + "." + BlogConstants.PUBLICACCESS + " = "
                        + Boolean.TRUE + " ");
            } else {
                // return an empty result if public access and access level not read
                parameters.setUserId(new Long(-1));
                appendRoleConstraint(where,
                        BlogRoleHelper.convertRoleToNumeric(BlogRole.MANAGER));
            }
        } else {
            Long userId = parameters.getUserId();
            switch (level) {
            case READ:
                where.append(ALIAS_BLOG + "." + BlogConstants.ALLCANREAD + " = " + Boolean.TRUE
                        + " ");
                where.append(" OR " + ALIAS_BLOG + "." + BlogConstants.ALLCANWRITE + " = "
                        + Boolean.TRUE + " ");
                where.append(" OR " + ALIAS_BLOG + "." + BlogConstants.PUBLICACCESS + " = "
                        + Boolean.TRUE + " ");
                if (userId != null) {
                    where.append("OR ");
                    appendRoleConstraint(where,
                            BlogRoleHelper.convertRoleToNumeric(BlogRole.VIEWER));
                }
                break;
            case WRITE:
                where.append(ALIAS_BLOG + "." + BlogConstants.ALLCANWRITE + " = "
                        + Boolean.TRUE + " ");
                if (userId != null) {
                    where.append("OR ");
                    appendRoleConstraint(where,
                            BlogRoleHelper.convertRoleToNumeric(BlogRole.MEMBER));
                }
                break;
            case MANAGER:
                if (userId == null) {
                    // force zero results with user id being -1
                    parameters.setUserId(new Long(-1));
                }
                appendRoleConstraint(where,
                        BlogRoleHelper.convertRoleToNumeric(BlogRole.MANAGER));
                break;
            default:
                throw new RuntimeException("unsupported blog access level :" + level);
            }
        }
        where.append(") ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        constructorParameter = new ArrayList<String>();
        constructorParameter.add(ALIAS_BLOG + "." + BlogConstants.NAMEIDENTIFIER);
        /** no DESCRIPTION here, because oracle. DESCRIPTION is load later */
        // constructorParameter.add(ALIAS_BLOG + "." + BlogConstants.DESCRIPTION);
        constructorParameter.add(ALIAS_BLOG + "." + BlogConstants.ID);
        constructorParameter.add(ALIAS_BLOG + "." + BlogConstants.TITLE);
        constructorParameter.add(ALIAS_BLOG + "." + BlogConstants.LASTMODIFICATIONDATE);
    }

    /**
     * defines if the tag entity is required for the query
     * 
     * @return {@code true} if the tag entity is required
     */
    protected boolean tagEntityRequiredInQuery() {
        return false;
    }
}
