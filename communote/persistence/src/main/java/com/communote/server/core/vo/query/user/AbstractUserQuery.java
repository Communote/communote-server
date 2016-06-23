package com.communote.server.core.vo.query.user;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.PropertyQuery;
import com.communote.server.core.vo.query.Query;
import com.communote.server.model.global.GlobalIdConstants;
import com.communote.server.model.i18n.MessageConstants;
import com.communote.server.model.note.NoteConstants;
import com.communote.server.model.tag.TagConstants;
import com.communote.server.model.user.CommunoteEntityConstants;
import com.communote.server.model.user.LanguageConstants;
import com.communote.server.model.user.UserConstants;
import com.communote.server.model.user.UserProfileConstants;
import com.communote.server.model.user.UserStatus;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <R>
 *            Type of the result for this query.
 * @param <I>
 *            Type the QueryParameters for this query.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractUserQuery<R, I extends UserQueryParameters> extends
        PropertyQuery<R, I> {
    private static final String USER_GID_ALIAS = "usergid";

    /**
     * The Constant FILTER_STATUS_ALL defines the filter value to include all user status in list.
     */
    public final static String FILTER_STATUS_ALL = "ALL";

    /**
     * alias for tag entity in query
     */
    public static final String ALIAS_TAGS = "tags";

    @Override
    public String buildQuery(I queryInstance)
    /* throws UnexpectedAuthorizationException */{
        StringBuilder query = new StringBuilder();
        StringBuilder whereQuery = new StringBuilder();
        String userAlias = getUserAlias();
        buildSelectQuery(query, queryInstance);
        if (queryInstance.isRetrieveOnlyFollowedUsers()) {
            query.append(" inner join " + userAlias + "." + CommunoteEntityConstants.GLOBALID + " "
                    + USER_GID_ALIAS);
        }
        // user Search
        String prefix = "";
        if (renderUserSearch(whereQuery, prefix, queryInstance)) {
            prefix = AND;
        }

        // include status filter
        if (renderIncludeStatusFilter(whereQuery, userAlias, prefix, queryInstance)) {
            prefix = AND;
        }

        // exclude status filter
        if (renderExcludeStatusFilter(whereQuery, userAlias, prefix, queryInstance)) {
            prefix = AND;
        }

        // roleFilter
        if (renderRoleFilter(query, whereQuery, prefix, queryInstance)) {
            prefix = AND;
        }
        prefix = renderSubQueryTags(queryInstance, whereQuery, prefix);
        prefix = renderFollowedUsersOnlyRestriction(whereQuery, prefix, queryInstance);

        renderCommonFilters(whereQuery, userAlias, prefix, queryInstance);
        if (whereQuery.length() > 0) {
            query.append(" WHERE ");
            query.append(whereQuery.toString());
        }
        renderOrderbyClause(query, queryInstance);
        renderGroupByClause(query, queryInstance);
        return query.toString();
    }

    /**
     * @param query
     *            The query to append the constructor to.
     * @param queryInstance
     *            the current query instance
     */
    public void buildSelectQuery(StringBuilder query, I queryInstance) {
        query.append("select distinct ");
        query.append(getUserAlias());
        query.append(", lower(");
        query.append(getUserAlias());
        query.append("." + UserConstants.EMAIL + "), lower(");
        query.append(getUserAlias() + "." + UserConstants.PROFILE + "."
                + UserProfileConstants.FIRSTNAME + "), lower(");
        query.append(getUserAlias() + "." + UserConstants.PROFILE + "."
                + UserProfileConstants.LASTNAME + ")");
        query.append(" from " + UserConstants.CLASS_NAME + " " + getUserAlias());
        if (needTagInQuery(queryInstance)) {
            query.append(" left outer join " + getUserAlias() + "." + CommunoteEntityConstants.TAGS
                    + " " + AbstractUserQuery.ALIAS_TAGS);
            if (StringUtils.isNotBlank(queryInstance.getTagPrefix())
                    && queryInstance.isMultilingualTagPrefixSearch()) {
                query.append(" LEFT JOIN " + AbstractUserQuery.ALIAS_TAGS + "."
                        + TagConstants.NAMES + " tagName LEFT JOIN tagName."
                        + MessageConstants.LANGUAGE + " language");
            }
        }
    }

    /**
     * Get the query prefix for the user
     *
     * @return the user alias in the querz
     */
    public String getUserAlias() {
        return "user";
    }

    /**
     * @param queryInstance
     *            the query instance
     * @return the search fields for the user data
     */
    protected String[] getUserSearchFields(I queryInstance) {
        String[] fields;
        if (queryInstance.isIgnoreEmailField()) {
            fields = new String[] {
                    getUserAlias() + "." + UserConstants.PROFILE + "."
                            + UserProfileConstants.FIRSTNAME,
                    getUserAlias() + "." + UserConstants.PROFILE + "."
                            + UserProfileConstants.LASTNAME,
                    getUserAlias() + "." + UserConstants.ALIAS };
        } else {
            fields = new String[] {
                    getUserAlias() + "." + UserConstants.EMAIL,
                    getUserAlias() + "." + UserConstants.PROFILE + "."
                            + UserProfileConstants.FIRSTNAME,
                    getUserAlias() + "." + UserConstants.PROFILE + "."
                            + UserProfileConstants.LASTNAME,
                    getUserAlias() + "." + UserConstants.ALIAS };
        }
        return fields;
    }

    /**
     * It takes the help where query and wraps it in such a way that it will only match if the class
     * is a user
     *
     * @param whereQuery
     *            the where query to append it all to
     * @param helpWhere
     *            the hel where
     * @param prefix
     *            the prefix to use
     */
    protected void matchOnlyIfUserQuery(StringBuilder whereQuery, StringBuilder helpWhere,
            String prefix) {
        whereQuery.append(prefix + " ((" + helpWhere + ")");
        whereQuery.append(" or " + getUserAlias() + ".class !=" + UserConstants.CLASS_NAME);
        whereQuery.append(")");
    }

    /**
     * Whether the tag entity is needed in the query.
     *
     * @param queryInstance
     *            the current query instance
     * @return true if the query returns tags or the result is filtered by a tag prefix or a single
     *         tag
     */
    protected boolean needTagInQuery(I queryInstance) {
        boolean needed = StringUtils.isNotBlank(queryInstance.getTagPrefix())
                || !queryInstance.getUserTagIds().isEmpty();
        return needed;
    }

    /**
     * Renders a restriction to exclude users having specific status.
     *
     * @param whereQuery
     *            the whereQuery to which the restriction will be added
     * @param userObjectAlias
     *            alias used in query to identify user object (e.g. 'user')
     * @param prefix
     *            the connector prefix (e.g. 'AND')
     * @param queryInstance
     *            the query instance
     * @return whether something was written
     */
    protected boolean renderCommonFilters(StringBuilder whereQuery, String userObjectAlias,
            String prefix, I queryInstance) {
        if (queryInstance.getLastModifiedAfter() != null) {
            whereQuery.append(prefix);
            whereQuery.append(" ");
            whereQuery.append(userObjectAlias);
            whereQuery.append(".");
            whereQuery.append(UserConstants.PROFILE);
            whereQuery.append(".");
            whereQuery.append(UserProfileConstants.LASTMODIFICATIONDATE);
            whereQuery.append(">=:");
            whereQuery.append(UserQueryParameters.PARAM_LAST_MODIFICATION_DATE);
        }
        return queryInstance.getLastModifiedAfter() != null;
    }

    /**
     * Renders a restriction to exclude users having specific status.
     *
     * @param whereQuery
     *            the whereQuery to which the restriction will be added
     * @param userObjectAlias
     *            alias used in query to identify user object (e.g. 'user')
     * @param prefix
     *            the connector prefix (e.g. 'AND')
     * @param queryInstance
     *            the query instance
     * @return whether something was written
     */
    protected boolean renderExcludeStatusFilter(StringBuilder whereQuery, String userObjectAlias,
            String prefix, I queryInstance) {
        String excludeStatusFilterQuery = renderStatusFilter(queryInstance.getExcludeStatusFilter());
        if (!StringUtils.isEmpty(excludeStatusFilterQuery)) {
            whereQuery.append(prefix);
            whereQuery.append(" ");
            whereQuery.append(userObjectAlias);
            whereQuery.append(".status NOT IN ");
            whereQuery.append(excludeStatusFilterQuery);
            return true;
        }
        return false;
    }

    /**
     * Renders a restriction to only return users the current user is following.
     *
     * @param whereQuery
     *            the where clause to append to
     * @param prefix
     *            a prefix to prepend
     * @param queryInstance
     *            the query instance
     * @return the prefix that subsequent renderers should append before writing to the where clause
     */
    private String renderFollowedUsersOnlyRestriction(StringBuilder whereQuery, String prefix,
            I queryInstance) {
        if (!queryInstance.isRetrieveOnlyFollowedUsers()) {
            return prefix;
        }
        whereQuery.append(prefix);
        whereQuery.append(" exists (select fuser." + CommunoteEntityConstants.ID);
        whereQuery.append(" from " + UserConstants.CLASS_NAME + " fuser inner join fuser."
                + UserConstants.FOLLOWEDITEMS);
        whereQuery.append(" fitems where fuser." + CommunoteEntityConstants.ID + "="
                + SecurityHelper.getCurrentUserId());
        whereQuery.append(" AND fitems." + GlobalIdConstants.ID + "=" + USER_GID_ALIAS + "."
                + GlobalIdConstants.ID + ")");
        return AND;
    }

    /**
     * Renders the grouping for this query.
     *
     * @param query
     *            The query.
     * @param queryInstance
     *            The query instance.
     */
    public void renderGroupByClause(StringBuilder query, I queryInstance) {
        // Do nothing.
    }

    /**
     * Renders a restriction to only include users having specific status.
     *
     * @param whereQuery
     *            the whereQuery to which the restriction will be added
     * @param userObjectAlias
     *            alias used in query to identify user object (e.g. 'user')
     * @param prefix
     *            the connector prefix (e.g. 'AND')
     * @param queryInstance
     *            the query instance
     * @return whether something was written
     */
    protected boolean renderIncludeStatusFilter(StringBuilder whereQuery, String userObjectAlias,
            String prefix, I queryInstance) {
        String includeStatusFilterQuery = renderStatusFilter(queryInstance.getIncludeStatusFilter());
        if (!StringUtils.isEmpty(includeStatusFilterQuery)) {
            whereQuery.append(prefix);
            whereQuery.append(" ");
            whereQuery.append(userObjectAlias);
            whereQuery.append(".status IN ");
            whereQuery.append(includeStatusFilterQuery);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void renderOrderbyClause(StringBuilder mainQuery, I queryParameters) {
        String order = queryParameters.getSortString();
        if (StringUtils.isNotBlank(order)) {
            order = order.replace(UserQueryParameters.PLACEHOLDER_USER_ALIAS, getUserAlias());
            mainQuery.append(" order by ");
            mainQuery.append(order);
        }
    }

    /**
     * Render the role filter.
     *
     * @param mainQuery
     *            the main query containing the selection clause
     * @param whereQuery
     *            the whereQuery to which the restriction will be added
     * @param prefix
     *            the connector prefix (e.g. 'AND')
     * @param queryInstance
     *            the query instance
     * @return whether something was rendered
     */
    protected boolean renderRoleFilter(StringBuilder mainQuery, StringBuilder whereQuery,
            String prefix, I queryInstance) {
        int rolesToIncludeCount = queryInstance.getRolesToInclude().size();
        int rolesToExcludeCount = queryInstance.getRolesToExclude().size();
        if (rolesToIncludeCount > 0 || rolesToExcludeCount > 0) {

            mainQuery.append(" left join ");
            mainQuery.append(getUserAlias());
            mainQuery.append("." + UserConstants.USERAUTHORITIES + " as auth ");

            whereQuery.append(prefix);
            if (rolesToIncludeCount > 0) {
                whereQuery.append("auth.role in (:" + UserQueryParameters.PARAM_USER_ROLE_FILTER
                        + ") ");
            } else {
                whereQuery.append("auth.role not in (:"
                        + UserQueryParameters.PARAM_USER_ROLE_FILTER + ") ");
            }
            return true;
        }
        return false;
    }

    /**
     * Render the user status filter.
     *
     * @param status
     *            the status which should included
     * @return the query as string
     */
    private String renderStatusFilter(UserStatus[] status) {
        String result = StringUtils.EMPTY;
        if (status != null && status.length > 0) {
            String[] statusStr = new String[status.length];
            for (int i = 0; i < status.length; i++) {
                statusStr[i] = "'" + status[i].getValue() + "'";
            }
            result = " (" + StringUtils.join(statusStr, ",") + ")";
        }
        return result;
    }

    /**
     * Renders the part for hiding the selected tags.
     *
     * @param queryInstance
     *            The query instance.
     * @param prefix
     *            The current prefix.
     * @param query
     *            The query.
     * @return The next prefix.
     */
    private String renderSubQueryHideSelectedTags(I queryInstance, String prefix,
            StringBuilder query) {
        if (!queryInstance.isHideSelectedTags()) {
            return prefix;
        }
        if (!queryInstance.getUserTagIds().isEmpty()) {
            query.append(Query.AND + AbstractUserQuery.ALIAS_TAGS + "." + TagConstants.ID
                    + " not in (");
            query.append(StringUtils.join(queryInstance.getUserTagIds(), ","));
            query.append(")");
        }
        return prefix;
    }

    /**
     * Renders the subquery for tag filterings.
     *
     * @param queryInstance
     *            The query instance.
     * @param whereQuery
     *            The where query to append.
     * @param prefix
     *            The prefix.
     * @return The next prefix.
     */
    public String renderSubQueryTags(I queryInstance, StringBuilder whereQuery, String prefix) {
        prefix = renderSubQueryTagsFilterByPrefix(queryInstance, prefix, whereQuery);
        prefix = renderTagsQueryFilterByTagStoreAliases(queryInstance, prefix, whereQuery);
        prefix = renderSubQueryTagsFilterByTagId(queryInstance, prefix, whereQuery);
        prefix = renderSubQueryTagsFilterByTagStores(queryInstance, prefix, whereQuery);
        prefix = renderSubQueryHideSelectedTags(queryInstance, prefix, whereQuery);
        return prefix;
    }

    /**
     * Renders the tag prefix search.
     *
     * @param queryInstance
     *            The query instance.
     * @param prefix
     *            The prefix.
     * @param query
     *            The query.
     * @return The next prefix.
     */
    private String renderSubQueryTagsFilterByPrefix(I queryInstance, String prefix,
            StringBuilder query) {
        if (StringUtils.isBlank(queryInstance.getTagPrefix())) {
            return prefix;
        }
        query.append(prefix);
        if (queryInstance.isMultilingualTagPrefixSearch()) {
            String languageCode = queryInstance.addParameter(queryInstance.getLanguageCode());
            query.append("((lower(" + AbstractUserQuery.ALIAS_TAGS + "." + TagConstants.DEFAULTNAME
                    + ") LIKE (:" + UserQueryParameters.PARAM_TAG_PREFIX + ") AND tagName."
                    + MessageConstants.MESSAGE + " is null) OR (lower("
                    + AbstractUserQuery.ALIAS_TAGS + "." + TagConstants.DEFAULTNAME + ") LIKE :"
                    + UserQueryParameters.PARAM_TAG_PREFIX + " AND tagName."
                    + MessageConstants.MESSAGE + " is not null AND language."
                    + LanguageConstants.LANGUAGECODE + " <> ");
            query.append(languageCode);
            query.append(") OR (lower(tagName." + MessageConstants.MESSAGE + ") LIKE :"
                    + UserQueryParameters.PARAM_TAG_PREFIX + " AND language."
                    + LanguageConstants.LANGUAGECODE + " = ");
            query.append(languageCode);
            query.append("))");
        } else {
            query.append("lower(" + AbstractUserQuery.ALIAS_TAGS + "." + TagConstants.DEFAULTNAME
                    + ") LIKE (:" + UserQueryParameters.PARAM_TAG_PREFIX + ")");
        }
        return prefix;
    }

    /**
     * Renders the query for filtering by tag ids.
     *
     * @param queryInstance
     *            The query parameters
     * @param innerPrefix
     *            The inner prefix.
     * @param query
     *            The sub query.
     * @return The next inner prefix.
     */
    private String renderSubQueryTagsFilterByTagId(I queryInstance, String innerPrefix,
            StringBuilder query) {
        if (queryInstance.getUserTagIds().isEmpty()) {
            return innerPrefix;
        }
        query.append(innerPrefix);
        query.append("EXISTS(SELECT 1 FROM " + CommunoteEntityConstants.CLASS_NAME
                + " kenmeiEntity3 LEFT JOIN kenmeiEntity3." + CommunoteEntityConstants.TAGS
                + " tag3 WHERE kenmeiEntity3." + NoteConstants.ID + " = " + getUserAlias() + "."
                + CommunoteEntityConstants.ID + " AND tag3." + TagConstants.ID + " IN (");
        query.append(StringUtils.join(queryInstance.getUserTagIds(), ","));
        query.append(") GROUP BY kenmeiEntity3." + CommunoteEntityConstants.ID
                + " HAVING count(kenmeiEntity3." + NoteConstants.ID + ") = ");
        query.append(queryInstance.getUserTagIds().size());
        query.append(")");
        return AND;
    }

    /**
     * Renders the query for filtering by TagStores.
     *
     * @param instance
     *            The query parameters
     * @param innerPrefix
     *            The inner prefix.
     * @param subQuery
     *            The sub query.
     * @return The next inner prefix.
     */
    private String renderSubQueryTagsFilterByTagStores(I instance, String innerPrefix,
            StringBuilder subQuery) {
        if (instance.getUserTagStoreTagIds().isEmpty()) {
            return innerPrefix;
        }
        Map<String, Set<String>> userTagStoreTagIds = instance.getUserTagStoreTagIds();
        for (Entry<String, Set<String>> tagStore : userTagStoreTagIds.entrySet()) {
            if (tagStore.getValue() == null || tagStore.getValue().isEmpty()) {
                continue;
            }
            String tagStoreQueryName = instance.addParameter(tagStore.getKey());
            subQuery.append(innerPrefix);
            subQuery.append("EXISTS(SELECT 1 FROM " + CommunoteEntityConstants.CLASS_NAME
                    + " note LEFT JOIN kenmeiEntity." + CommunoteEntityConstants.TAGS
                    + " tagTagStore WHERE kenmeiEntity." + CommunoteEntityConstants.ID + " = "
                    + getUserAlias() + "." + CommunoteEntityConstants.ID + " AND tagTagStore."
                    + TagConstants.TAGSTOREALIAS + "= " + tagStoreQueryName + " AND tagTagStore."
                    + TagConstants.TAGSTORETAGID + " IN (");
            String seperator = "";
            for (String tagStoreTagId : tagStore.getValue()) {
                String tagStoreTagIdQueryName = instance.addParameter(tagStoreTagId);
                instance.addParameter(tagStoreTagIdQueryName, tagStoreTagId);
                subQuery.append(seperator + tagStoreTagIdQueryName);
                seperator = ",";
            }
            subQuery.append(") GROUP BY kenmeiEntity." + CommunoteEntityConstants.ID
                    + " HAVING count(kenmeiEntity." + CommunoteEntityConstants.ID + ") = ");
            subQuery.append(tagStore.getValue().size());
            subQuery.append(")");
            innerPrefix = AND;
        }
        return innerPrefix;
    }

    /**
     * @param instance
     *            The query instance.
     * @param prefix
     *            The prefix to use.
     * @param whereQuery
     *            The where query.
     * @return The next prefix.
     */
    private String renderTagsQueryFilterByTagStoreAliases(I instance, String prefix,
            StringBuilder whereQuery) {
        if (instance.getTagStoreAliases().isEmpty()) {
            return prefix;
        }
        whereQuery.append(prefix + "tags." + TagConstants.TAGSTOREALIAS + " IN (");
        String seperator = "";
        for (Object tagStoreAlias : instance.getTagStoreAliases()) {
            whereQuery.append(seperator + instance.addParameter(tagStoreAlias.toString()));
            seperator = ",";
        }
        whereQuery.append(")");
        return AND;
    }

    /**
     * Renders the user search query.
     *
     * @param whereQuery
     *            the where part
     * @param prefix
     *            the connector prefix (e.g. 'AND')
     * @param queryInstance
     *            the query instance holding the query parameters
     * @return whether something was rendered
     */
    protected boolean renderUserSearch(StringBuilder whereQuery, String prefix, I queryInstance) {
        String[] userSearchParamNames = queryInstance.getUserSearchParameterNames();
        if (userSearchParamNames != null && userSearchParamNames.length > 0) {
            renderSearch(whereQuery, getUserSearchFields(queryInstance), userSearchParamNames,
                    true, false);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        // Do nothing.
    }
}
