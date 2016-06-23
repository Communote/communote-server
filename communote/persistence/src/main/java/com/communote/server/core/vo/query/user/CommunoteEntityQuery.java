package com.communote.server.core.vo.query.user;

import java.util.ArrayList;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.filter.listitems.blog.member.CommunoteEntityData;
import com.communote.server.core.filter.listitems.blog.member.EntityGroupListItem;
import com.communote.server.core.filter.listitems.blog.member.UserListItem;
import com.communote.server.model.blog.BlogMemberConstants;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.CommunoteEntityConstants;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserConstants;
import com.communote.server.model.user.UserProfileConstants;
import com.communote.server.model.user.group.Group;
import com.communote.server.model.user.group.GroupConstants;
import com.communote.server.persistence.user.CommunoteEntityDao;

/**
 *
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteEntityQuery extends
AbstractUserQuery<CommunoteEntityData, CommunoteEntityQueryParameters> {

    private static final String ENTITY = "entity";
    private static final String ALIAS_ENTITY_MEMBERSHIPS = "memberships";
    private static final String ALIAS_PROFILE = "profile";
    private static final String ALIAS_GROUP = "gr";

    @Override
    public String buildQuery(CommunoteEntityQueryParameters queryInstance) {
        StringBuilder query = new StringBuilder();
        StringBuilder whereQuery = new StringBuilder();
        query.append("select ");
        query.append(getUserAlias());
        query.append(".id from " + CommunoteEntityConstants.CLASS_NAME + " ");
        query.append(getUserAlias());
        query.append(" left join ");
        query.append(getUserAlias());
        query.append(".");
        query.append(UserConstants.PROFILE + " " + ALIAS_PROFILE + " ");

        // user Search
        String prefix = "";
        if (renderUserSearch(whereQuery, prefix, queryInstance)) {
            prefix = AND;
        }

        if (renderMembershipRestriction(whereQuery, prefix, queryInstance)) {
            query.append(" inner join ");
            query.append(getUserAlias());
            query.append("." + CommunoteEntityConstants.GROUPS + " " + ALIAS_ENTITY_MEMBERSHIPS
                    + " ");
            prefix = AND;
        }

        if (renderExcludeAssignedToBlog(whereQuery, prefix, queryInstance)) {
            prefix = AND;
        }

        if (renderExcludedEntityId(whereQuery, prefix, queryInstance)) {
            prefix = AND;
        }

        StringBuilder helpWhere;

        // include status filter
        helpWhere = new StringBuilder();
        if (renderIncludeStatusFilter(helpWhere, getUserAlias(), "", queryInstance)) {
            matchOnlyIfUserQuery(whereQuery, helpWhere, prefix);
            prefix = AND;
        }

        // exclude status filter
        helpWhere = new StringBuilder();
        if (renderExcludeStatusFilter(helpWhere, getUserAlias(), "", queryInstance)) {
            matchOnlyIfUserQuery(whereQuery, helpWhere, prefix);
            prefix = AND;
        }

        // roleFilter
        helpWhere = new StringBuilder();
        if (renderRoleFilter(query, helpWhere, "", queryInstance)) {
            matchOnlyIfUserQuery(whereQuery, helpWhere, prefix);
            prefix = AND;
        }

        if (whereQuery.length() > 0) {
            query.append(" WHERE ");
            query.append(whereQuery.toString());
        }

        // TODO put this somewhere nice, allow order by to be defined by the instance
        // group by
        query.append(" group by entity.id, entity.class, ");
        query.append(" entity.name, profile.lastName, profile.firstName, entity.alias");

        // order by
        query.append(" order by entity.class, entity.name, ");
        query.append(" profile.lastName, profile.firstName, entity.alias");

        // renderOrderbyClause(query, queryInstance);

        return query.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommunoteEntityQueryParameters createInstance() {
        return new CommunoteEntityQueryParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAlias() {
        return ENTITY;
    }

    /**
     * Search also the group fields
     *
     * @param queryInstance
     *            the query instance
     * @return the search fields for the user data
     */
    @Override
    protected String[] getUserSearchFields(CommunoteEntityQueryParameters queryInstance) {
        String[] fields;
        if (queryInstance.isIgnoreEmailField()) {
            fields = new String[] { ALIAS_PROFILE + "." + UserProfileConstants.FIRSTNAME,
                    ALIAS_PROFILE + "." + UserProfileConstants.LASTNAME,
                    ENTITY + "." + UserConstants.ALIAS,
                    ENTITY + "." + GroupConstants.NAME };
        } else {
            fields = new String[] { ENTITY + "." + UserConstants.EMAIL,
                    ALIAS_PROFILE + "." + UserProfileConstants.FIRSTNAME,
                    ALIAS_PROFILE + "." + UserProfileConstants.LASTNAME,
                    ENTITY + "." + UserConstants.ALIAS,
                    ENTITY + "." + GroupConstants.NAME };
        }
        return fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableList postQueryExecution(CommunoteEntityQueryParameters queryInstance,
            PageableList result) {
        PageableList<CommunoteEntityData> items = new PageableList<CommunoteEntityData>(
                new ArrayList<CommunoteEntityData>(result.size()));
        items.setMinNumberOfElements(result.getMinNumberOfElements());

        for (Long entityId : (PageableList<Long>) result) {
            if (entityId == null) {
                continue;
            }
            CommunoteEntity entity = ServiceLocator.findService(CommunoteEntityDao.class)
                    .loadWithImplementation(entityId);

            CommunoteEntityData entityItem;
            if (entity instanceof User) {
                User user = (User) entity;
                UserListItem userItem = new UserListItem();
                userItem.setEntityId(entityId);
                userItem.setAlias(user.getAlias());
                userItem.setFirstName(user.getProfile().getFirstName());
                userItem.setLastName(user.getProfile().getLastName());
                entityItem = userItem;
            } else if (entity instanceof Group) {
                Group group = (Group) entity;
                EntityGroupListItem groupItem = new EntityGroupListItem();
                groupItem.setEntityId(entityId);
                groupItem.setAlias(group.getAlias());
                groupItem.setName(group.getName());
                entityItem = groupItem;
            } else {
                // unknown entity from mars
                entityItem = null;
            }
            if (entityItem != null) {
                items.add(entityItem);
            }
        }

        return super.postQueryExecution(queryInstance, items);
    }

    /**
     * Render the where sub clause for filtering for entities that have not been assigned to a blg
     *
     * @param whereQuery
     *            the where query
     * @param prefix
     *            the prefix
     * @param queryInstance
     *            the query instance
     * @return if the where sub clause has been written
     */
    private boolean renderExcludeAssignedToBlog(StringBuilder whereQuery, String prefix,
            CommunoteEntityQueryParameters queryInstance) {
        if (queryInstance.getExcludedAssignedToBlogId() != null) {
            whereQuery.append(prefix);
            whereQuery.append(getUserAlias());
            whereQuery.append(".id not in (");
            whereQuery.append("select subMember.");
            whereQuery.append(BlogMemberConstants.MEMBERENTITY);
            whereQuery.append(" from ");
            whereQuery.append(BlogMemberConstants.CLASS_NAME);
            whereQuery.append(" subMember where subMember.");
            whereQuery.append(BlogMemberConstants.BLOG);
            whereQuery.append(".id = :");
            whereQuery.append(CommunoteEntityQueryParameters.PARAM_EXCLUDE_ASSIGNED_TO_BLOG_ID);
            whereQuery.append(") ");
            return true;
        }
        return false;
    }

    /**
     *
     * @param whereQuery
     *            the where query
     * @param prefix
     *            the prefix
     * @param queryInstance
     *            the query instance
     * @return True if the where sub clause has been written
     */
    private boolean renderExcludedEntityId(StringBuilder whereQuery, String prefix,
            CommunoteEntityQueryParameters queryInstance) {
        if (queryInstance.getExcludedEntityId() != null) {
            whereQuery.append(prefix);
            whereQuery.append(getUserAlias());
            whereQuery.append(".id != :");
            whereQuery.append(CommunoteEntityQueryParameters.PARAM_EXCLUDE_ENTITY_ID);
            return true;
        }
        return false;
    }

    /**
     * Render the where condition to restrict the results to users and groups which are direct
     * members of a given group.
     *
     * @param whereQuery
     *            the where condition to write to
     * @param prefix
     *            the prefix to prepend before adding the actual condition
     * @param queryParameters
     *            the current parameters
     * @return whether the restriction was added.
     */
    private boolean renderMembershipRestriction(StringBuilder whereQuery, String prefix,
            CommunoteEntityQueryParameters queryParameters) {
        if (queryParameters.isDirectGroupMembership()) {
            whereQuery.append(prefix);
            whereQuery.append(ALIAS_ENTITY_MEMBERSHIPS);
            whereQuery.append(".");
            whereQuery.append(CommunoteEntityConstants.ID);
            whereQuery.append("=:");
            whereQuery.append(CommunoteEntityQueryParameters.PARAM_MEMBERSHIP_GROUP_ID);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean renderUserSearch(StringBuilder whereQuery, String prefix,
            CommunoteEntityQueryParameters queryInstance) {
        String[] userSearchParamNames = queryInstance.getUserSearchParameterNames();
        if (userSearchParamNames != null && userSearchParamNames.length > 0) {
            StringBuilder userSearchQuery = new StringBuilder();
            renderSearch(userSearchQuery, getUserSearchFields(queryInstance), userSearchParamNames,
                    false, false);
            // append a subquery to search the group alias
            userSearchQuery.append(" OR ");
            userSearchQuery.append(getUserAlias());
            userSearchQuery.append(".id in (select " + ALIAS_GROUP + ".id from "
                    + GroupConstants.CLASS_NAME + " " + ALIAS_GROUP + " WHERE ");
            renderSearch(userSearchQuery, new String[] { ALIAS_GROUP + "."
                    + GroupConstants.ALIAS }, userSearchParamNames, false, false);
            userSearchQuery.append(") ");

            whereQuery.append("(");
            whereQuery.append(userSearchQuery.toString());
            whereQuery.append(")");
            return true;
        }
        return false;
    }
}
