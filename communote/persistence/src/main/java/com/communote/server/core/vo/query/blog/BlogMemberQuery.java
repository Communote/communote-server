package com.communote.server.core.vo.query.blog;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.vo.query.user.AbstractUserQuery;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.UserToBlogRoleMappingConstants;
import com.communote.server.model.user.CommunoteEntityConstants;
import com.communote.server.model.user.UserConstants;
import com.communote.server.model.user.UserProfileConstants;

/**
 * QueryInstance to retrieve blog members with read access to a blog.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class BlogMemberQuery extends AbstractUserQuery<UserData, BlogMemberQueryParameters> {

    /**
     * Builds the query with blog access restriction.
     * 
     * @param queryInstance
     *            the query instance holding the query parameters
     * @return the query as string
     */
    private String buildMemberRestrictedQuery(BlogMemberQueryParameters queryInstance) {
        StringBuilder query = new StringBuilder();
        StringBuilder whereQuery = new StringBuilder();

        query.append("select distinct ");
        query.append(getUserAlias());
        query.append(", lower(");
        query.append(getUserAlias());
        query.append("." + UserConstants.EMAIL + "), lower(");
        query.append(getUserAlias());
        query.append("." + UserConstants.PROFILE + "." + UserProfileConstants.FIRSTNAME
                + "), lower(");
        query.append(getUserAlias());
        query.append("." + UserConstants.PROFILE + "." + UserProfileConstants.LASTNAME);
        query.append(") from " + UserConstants.CLASS_NAME + " ");
        query.append(getUserAlias());

        // user Search
        String prefix = "";
        if (renderUserSearch(whereQuery, prefix, queryInstance)) {
            prefix = AND;
        }
        // include status filter
        if (renderIncludeStatusFilter(whereQuery, getUserAlias(), prefix, queryInstance)) {
            prefix = AND;
        }
        // exclude status filter
        if (renderExcludeStatusFilter(whereQuery, getUserAlias(), prefix, queryInstance)) {
            prefix = AND;
        }
        // roleFilter
        if (renderRoleFilter(query, whereQuery, prefix, queryInstance)) {
            prefix = AND;
        }
        if (renderBlogAccessRestriction(whereQuery, getUserAlias(), prefix, queryInstance)) {
            prefix = AND;
        }
        renderCommonFilters(whereQuery, getUserAlias(), prefix, queryInstance);
        if (whereQuery.length() > 0) {
            query.append(" WHERE ");
            query.append(whereQuery.toString());
        }

        renderOrderbyClause(query, queryInstance);

        return query.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildQuery(BlogMemberQueryParameters queryParameters) {
        Blog blog;
        try {
            blog = ServiceLocator.instance().getService(BlogManagement.class).getBlogById(
                    queryParameters.getBlogId(), false);
        } catch (BlogNotFoundException e) {
            throw BlogManagementHelper.convertException(e);
        } catch (BlogAccessException e) {
            throw BlogManagementHelper.convertException(e);
        }
        if (blog.isAllCanRead() || blog.isAllCanWrite() || blog.isPublicAccess()) {
            return super.buildQuery(queryParameters);
        } else {
            return buildMemberRestrictedQuery(queryParameters);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlogMemberQueryParameters createInstance() {
        return new BlogMemberQueryParameters();
    }

    /**
     * Renders the blog access restriction.
     * 
     * @param whereQuery
     *            the where query to write to
     * @param userObjectAlias
     *            alias for the user object in the query
     * @param prefix
     *            the connector prefix (e.g. 'AND')
     * @param queryInstance
     *            the query instance holding the query parameters
     * @return whether something was rendered
     */
    private boolean renderBlogAccessRestriction(StringBuilder whereQuery, String userObjectAlias,
            String prefix, BlogMemberQueryParameters queryInstance) {
        if (queryInstance.getBlogId() != null) {
            whereQuery.append(prefix);
            whereQuery.append(userObjectAlias);
            whereQuery.append("." + CommunoteEntityConstants.ID + " IN (SELECT mapping."
                    + UserToBlogRoleMappingConstants.USERID);
            whereQuery.append(" from " + UserToBlogRoleMappingConstants.CLASS_NAME
                    + " as mapping where mapping.");
            whereQuery.append(UserToBlogRoleMappingConstants.BLOGID);
            whereQuery.append("=:");
            whereQuery.append(BlogMemberQueryParameters.PARAM_BLOG_ID_FILTER + ") ");
        }
        return false;
    }
}
