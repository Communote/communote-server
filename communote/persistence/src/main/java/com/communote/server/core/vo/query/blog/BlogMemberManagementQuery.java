package com.communote.server.core.vo.query.blog;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.core.common.exceptions.UnexpectedAuthorizationException;
import com.communote.server.core.filter.listitems.blog.member.BlogMemberListItem;
import com.communote.server.core.filter.listitems.blog.member.BlogRoleEntityListItem;
import com.communote.server.core.filter.listitems.blog.member.EntityGroupListItem;
import com.communote.server.core.filter.listitems.blog.member.UserListItem;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.user.AbstractUserQuery;
import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.blog.BlogMember;
import com.communote.server.model.blog.BlogMemberConstants;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.external.ExternalObjectConstants;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserConstants;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.blog.BlogMemberDao;
import com.communote.server.persistence.user.CommunoteEntityDao;

/**
 * Query Definition to retrieve the list items
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogMemberManagementQuery extends
AbstractUserQuery<BlogRoleEntityListItem, BlogMemberManagementQueryParameters> {

    private final static String QUERY_PREFIX = "select entity.id from "
            + BlogConstants.CLASS_NAME
            + " blog left join blog."
            + BlogConstants.MEMBERS
            + " mem left join mem."
            + BlogMemberConstants.MEMBERENTITY
            + " entity left join entity."
            + UserConstants.PROFILE
            + " profile ";

    private final static String QUERY_SUFFIX = " group by entity.id, entity.class, "
            + " entity.name, profile.lastName, profile.firstName, entity.alias"
            + " order by min(mem.role), entity.class, entity.name, "
            + " profile.lastName, profile.firstName, entity.alias";

    private void authorizationCheck(BlogMemberManagementQueryParameters queryInstance)
    /* throws UnexpectedAuthorizationException */{
        boolean hasAccess = false;
        if (queryInstance.getBlogId() != null) {

            hasAccess = ServiceLocator
                    .instance()
                    .getService(BlogRightsManagement.class)
                    .currentUserHasReadAccess(queryInstance.getBlogId(), false);
        }

        if (!hasAccess) {
            throw new UnexpectedAuthorizationException("User "
                    + SecurityHelper.assertCurrentUserId()
                    + " has no read access for topic " + queryInstance.getBlogId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildQuery(BlogMemberManagementQueryParameters queryInstance)
    /* throws UnexpectedAuthorizationException */{

        authorizationCheck(queryInstance);

        StringBuilder query = new StringBuilder(QUERY_PREFIX);

        if (queryInstance.getExternalSystemId() != null) {
            query.append(" left join blog." + BlogConstants.EXTERNALOBJECTS + " external ");
        }

        String prefix = AND;
        query.append(" where blog.id = :" + BlogMemberManagementQueryParameters.PARAM_BLOG_ID);
        if (queryInstance.getExternalSystemId() != null) {
            query.append(prefix + " external." + ExternalObjectConstants.EXTERNALSYSTEMID + " = :"
                    + BlogMemberManagementQueryParameters.PARAM_EXTENRAL_SYSTEM_ID + " ");
        }

        // include status filter
        StringBuilder helpWhere = new StringBuilder();
        if (renderIncludeStatusFilter(helpWhere, "entity", "", queryInstance)) {
            matchOnlyIfUserQuery(query, helpWhere, prefix);
            prefix = AND;
        }

        // exclude status filter
        helpWhere = new StringBuilder();
        if (renderExcludeStatusFilter(helpWhere, "entity", "", queryInstance)) {
            matchOnlyIfUserQuery(query, helpWhere, prefix);
            prefix = AND;
        }
        prefix = renderIncludeBlogRoles(query, prefix, queryInstance);
        query.append(QUERY_SUFFIX);

        return query.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlogMemberManagementQueryParameters createInstance() {
        return new BlogMemberManagementQueryParameters();
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
    @Override
    protected void matchOnlyIfUserQuery(StringBuilder whereQuery, StringBuilder helpWhere,
            String prefix) {
        whereQuery.append(prefix + " ((" + helpWhere + ")");
        whereQuery.append(" or entity.class !=" + UserConstants.CLASS_NAME);
        whereQuery.append(")");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableList postQueryExecution(BlogMemberManagementQueryParameters queryInstance,
            PageableList result) {
        PageableList<BlogRoleEntityListItem> items = new PageableList<BlogRoleEntityListItem>(
                new ArrayList<BlogRoleEntityListItem>(result.size()));
        items.setMinNumberOfElements(result.getMinNumberOfElements());
        for (Long entityId : (PageableList<Long>) result) {
            if (entityId == null) {
                continue;
            }
            List<BlogMember> blogMembers = ServiceLocator.findService(BlogMemberDao.class)
                    .findByBlogAndEntity(queryInstance.getBlogId(), entityId);

            if (CollectionUtils.isEmpty(blogMembers)) {
                // TODO log.warn
                continue;
            }

            BlogRoleEntityListItem item = new BlogRoleEntityListItem();
            item.setRoles(new ArrayList<BlogMemberListItem>(blogMembers.size()));
            int highestRole = -1;
            for (BlogMember member : blogMembers) {
                BlogMemberListItem memberItem = new BlogMemberListItem();
                memberItem.setBlogRole(member.getRole());
                memberItem.setExternalSystemId(member.getExternalSystemId());
                highestRole = Math.max(BlogRoleHelper.convertRoleToNumeric(member.getRole()),
                        highestRole);
                item.getRoles().add(memberItem);
            }
            item.setGrantedBlogRole(BlogRoleHelper.convertNumericToRole(highestRole));

            CommunoteEntity entity = ServiceLocator.findService(CommunoteEntityDao.class)
                    .loadWithImplementation(entityId);

            if (entity instanceof User) {
                User user = (User) entity;
                UserListItem userItem = new UserListItem();
                userItem.setEntityId(entityId);
                userItem.setAlias(user.getAlias());
                userItem.setFirstName(user.getProfile().getFirstName());
                userItem.setLastName(user.getProfile().getLastName());
                item.setEntity(userItem);
            } else if (entity instanceof Group) {
                Group group = (Group) entity;
                EntityGroupListItem groupItem = new EntityGroupListItem();
                groupItem.setEntityId(entityId);
                groupItem.setAlias(group.getAlias());
                groupItem.setName(group.getName());
                item.setEntity(groupItem);
            } else {
                // unknown entity from mars
            }
            items.add(item);
        }

        return super.postQueryExecution(queryInstance, items);
    }

    /**
     *
     * Renders the blgo roles which should be included into the query.
     *
     * @param query
     *            The actual query.
     * @param prefix
     *            The actual prefix.
     * @param queryInstance
     *            The actual query instance.
     * @return The next prefix.
     */
    private String renderIncludeBlogRoles(StringBuilder query, String prefix,
            BlogMemberManagementQueryParameters queryInstance) {
        if (queryInstance.getIncludeBlogRoles() == null
                || queryInstance.getIncludeBlogRoles().length == 0) {
            return prefix;
        }
        List<BlogRole> blogRoles = new ArrayList<BlogRole>();
        org.apache.commons.collections.CollectionUtils.addAll(blogRoles, queryInstance
                .getIncludeBlogRoles());
        query.append(prefix + " mem." + BlogMemberConstants.ROLE);
        query.append(" in ("
                + StringUtils.collectionToDelimitedString(blogRoles, ",", "'", "'")
                + ")");
        return AND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        // Do nothing.
    }

}
