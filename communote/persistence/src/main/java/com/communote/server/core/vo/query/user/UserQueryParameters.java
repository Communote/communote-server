package com.communote.server.core.vo.query.user;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.MatchMode;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.vo.query.PropertyQueryParameters;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserConstants;
import com.communote.server.model.user.UserProfile;
import com.communote.server.model.user.UserProfileConstants;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.tag.TagStore;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserQueryParameters extends PropertyQueryParameters {
    /**
     * Placeholder for the user alias, which is used for sorting and will be replaced by the query.
     */
    public static final String PLACEHOLDER_USER_ALIAS = "{{USER_ALIAS}}";

    private static final String PARAM_USER_SEARCH_PREFIX = "userSearch";
    public static final String PARAM_USER_ROLE_FILTER = "userRoles";

    /**
     * the parameter name for the last modification date
     */
    public static final String PARAM_LAST_MODIFICATION_DATE = "lastModificationDate";

    /**
     * the parameter for tag prefix filtering
     */
    public static final String PARAM_TAG_PREFIX = "tagPrefix";

    private String[] userSearchFilters;
    private String[] userSearchParamNames;

    private Collection<UserRole> rolesToInclude;
    private Collection<UserRole> rolesToExclude;

    /** The include status filter. */
    private UserStatus[] includeStatusFilter = null;

    /** The exclude status filter. */
    private UserStatus[] excludeStatusFilter = null;

    private MatchMode matchMode;

    private boolean ignoreEmailField = true;

    /**
     * the user must be modified on or after exactly this date
     */
    private Timestamp lastModifiedAfter = null;
    private boolean retrieveOnlyFollowedUsers = false;

    private final Set<Long> userTagIds = new HashSet<Long>();

    private final Map<String, Set<String>> userTagStoreTagIds = new HashMap<String, Set<String>>();

    /** to hide selected tags */
    private boolean hideSelectedTags = true;

    private String tagPrefix;
    private Set<String> tagStoreAliases = new HashSet<String>();
    private Boolean multilingualTagPrefixSearch = null;

    /**
     * Create a query instance by definition
     */
    public UserQueryParameters() {
        matchMode = MatchMode.ANYWHERE;
        rolesToExclude = new HashSet<UserRole>();
        rolesToInclude = new HashSet<UserRole>();
    }

    /**
     * Create a query instance by definition.
     *
     * @param maxCount
     *            The number of max retrieved elements.
     * @param offset
     *            The offset, where to start.
     */
    public UserQueryParameters(int maxCount, int offset) {
        matchMode = MatchMode.ANYWHERE;
        ResultSpecification resultSpecification = new ResultSpecification(offset, maxCount);
        setResultSpecification(resultSpecification);
    }

    /**
     * Add a role to exclude in the result.
     *
     * @param role
     *            the role to exclude
     * @see #getRolesToExclude()
     */
    public void addRoleToExclude(UserRole role) {
        this.rolesToExclude.add(role);
    }

    /**
     * Add a role to include in the result.
     *
     * @param role
     *            the role to include
     * @see #getRolesToInclude()
     */
    public void addRoleToInclude(UserRole role) {
        this.rolesToInclude.add(role);
    }

    /**
     * Adds a tag by its id the users should be tagged with.
     *
     * @param tagIds
     *            TagIds to filter for.
     */
    public void addUserTagIds(Long... tagIds) {
        if (tagIds == null || tagIds.length == 0) {
            return;
        }
        for (Long tagId : tagIds) {
            userTagIds.add(tagId);
        }
    }

    /**
     * Adds a list of tags for a specific TagStore the users should be tagged with.
     *
     * @param tagStoreAlias
     *            The alias of the TagStore.
     * @param tagStoreTagIds
     *            Collection of tag ids to add for the given TagStore.
     */
    public void addUserTagStoreTagId(String tagStoreAlias, Collection<String> tagStoreTagIds) {
        Set<String> tags = this.userTagStoreTagIds.get(tagStoreAlias);
        if (tags == null) {
            tags = new HashSet<String>();
            this.userTagStoreTagIds.put(tagStoreAlias, tags);
        }
        tags.addAll(tagStoreTagIds);
    }

    /**
     * Adds a tag for a specific TagStore the users should be tagged with.
     *
     * @param tagStoreAlias
     *            The alias of the TagStore.
     * @param tagStoreTagId
     *            The id of the tag within the TagStore.
     */
    public void addUserTagStoreTagId(String tagStoreAlias, String tagStoreTagId) {
        Set<String> tags = userTagStoreTagIds.get(tagStoreAlias);
        if (tags == null) {
            tags = new HashSet<String>();
            userTagStoreTagIds.put(tagStoreAlias, tags);
        }
        tags.add(tagStoreTagId);
    }

    /**
     * @return the user status to exclude
     */
    public UserStatus[] getExcludeStatusFilter() {
        return excludeStatusFilter;
    }

    /**
     * Gets the status filter.
     *
     * @return the includeStatusFilter
     */
    public UserStatus[] getIncludeStatusFilter() {
        return includeStatusFilter;
    }

    /**
     * @return the lastModifiedAfter
     */
    public Timestamp getLastModifiedAfter() {
        return lastModifiedAfter;
    }

    /**
     * Returns the match mode used in user name based queries. The default is MatchMode.ANYWHERE.
     *
     * @return the match mode
     */
    public MatchMode getMatchMode() {
        return matchMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = super.getParameters();
        if (lastModifiedAfter != null) {
            putParameter(params, PARAM_LAST_MODIFICATION_DATE, lastModifiedAfter);
        }
        if (StringUtils.isNotBlank(tagPrefix)) {
            putParametersForSearch(params,
                    new String[] { UserQueryParameters.PARAM_TAG_PREFIX },
                    new String[] { tagPrefix.toLowerCase() }, MatchMode.START, true);
        }
        putParametersForSearch(params, userSearchParamNames, userSearchFilters, getMatchMode(),
                true);
        Collection<UserRole> roles = null;
        if (rolesToInclude.size() > 0) {
            roles = rolesToInclude;
        } else if (rolesToExclude.size() > 0) {
            roles = rolesToExclude;
        }
        if (roles != null) {
            String[] roleValues = new String[roles.size()];
            int i = 0;
            for (UserRole role : roles) {
                roleValues[i] = role.getValue();
                i++;
            }
            putParameter(params, PARAM_USER_ROLE_FILTER, roleValues);
        }
        return params;
    }

    /**
     * @return the roles of users to exclude in the result. If empty no roles are excluded. If also
     *         roles to include are defined the roles to exclude are ignored.
     */
    public Collection<UserRole> getRolesToExclude() {
        return this.rolesToExclude;
    }

    /**
     * @return the roles of users to include in the result. If empty all roles are included.
     */
    public Collection<UserRole> getRolesToInclude() {
        return this.rolesToInclude;
    }

    /**
     * @return the tagPrefix
     */
    public String getTagPrefix() {
        return tagPrefix;
    }

    /**
     * @return set of aliases identifying tag stores
     */
    public Set<String> getTagStoreAliases() {
        return tagStoreAliases;
    }

    /**
     * Get the user search strings
     *
     * @return the user search strings
     */
    public String[] getUserSearchFilters() {
        return userSearchFilters;
    }

    /**
     * @return the parameter names to be used in a user search or null if no user search filter was
     *         set
     */
    public String[] getUserSearchParameterNames() {
        return userSearchParamNames;
    }

    /**
     * @return the tagIds
     */
    public Set<Long> getUserTagIds() {
        return userTagIds;
    }

    /**
     * @return the tagStoreTagIds
     */
    public Map<String, Set<String>> getUserTagStoreTagIds() {
        return userTagStoreTagIds;
    }

    /**
     * @return the hideSelectedTags
     */
    public boolean isHideSelectedTags() {
        return hideSelectedTags;
    }

    /**
     * Returns whether to include the email field in the search.
     *
     * @return the ignoreEmailField
     */
    public boolean isIgnoreEmailField() {
        return ignoreEmailField;
    }

    /**
     * @return true if a tag prefix query should check the translations of tags
     */
    public boolean isMultilingualTagPrefixSearch() {
        if (multilingualTagPrefixSearch == null) {
            TagStoreManagement tagStoreManagement = ServiceLocator.instance().getService(
                    TagStoreManagement.class);

            for (String storeAlias : this.tagStoreAliases) {
                TagStore store = tagStoreManagement.getTagStore(storeAlias, null);
                if (store.isMultilingual()) {
                    multilingualTagPrefixSearch = Boolean.TRUE;
                    break;
                } else {
                    multilingualTagPrefixSearch = Boolean.FALSE;
                }
            }
            if (multilingualTagPrefixSearch == null) {
                multilingualTagPrefixSearch = tagStoreManagement
                        .hasMultilingualTagStore(TagStoreType.Types.ENTITY);
            }
        }
        return multilingualTagPrefixSearch;
    }

    /**
     * @return whether to retrieve only users followed by the current user.
     */
    public boolean isRetrieveOnlyFollowedUsers() {
        return retrieveOnlyFollowedUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needTransformListItem() {
        return true;
    }

    /**
     * @param excludeStatusFilter
     *            the user status to ignore
     */
    public void setExcludeStatusFilter(UserStatus[] excludeStatusFilter) {
        this.excludeStatusFilter = excludeStatusFilter;
    }

    /**
     * @param hideSelectedTags
     *            the hideSelectedTags to set
     */
    public void setHideSelectedTags(boolean hideSelectedTags) {
        this.hideSelectedTags = hideSelectedTags;
    }

    /**
     * Sets the status filter.
     *
     * @param includeStatusFilter
     *            the includeStatusFilter to set
     */
    public void setIncludeStatusFilter(UserStatus[] includeStatusFilter) {
        this.includeStatusFilter = includeStatusFilter;
    }

    /**
     * @param lastModifiedAfter
     *            the lastModifiedAfter to set
     */
    public void setLastModifiedAfter(Timestamp lastModifiedAfter) {
        this.lastModifiedAfter = lastModifiedAfter;
    }

    /**
     * Sets the match mode for name queries.
     *
     * @param mode
     *            the match mode
     */
    public void setMatchMode(MatchMode mode) {
        if (mode != null) {
            this.matchMode = mode;
        }
    }

    /**
     * Set whether to retrieve only users followed by the current user.
     *
     * @param onlyFollowedUsers
     *            true to retrieve only followed users
     */
    public void setRetrieveOnlyFollowedUsers(boolean onlyFollowedUsers) {
        this.retrieveOnlyFollowedUsers = onlyFollowedUsers;
    }

    /**
     * @param tagPrefix
     *            the tagPrefix to set
     */
    public void setTagPrefix(String tagPrefix) {
        this.tagPrefix = tagPrefix;
    }

    /**
     * Set aliases of tag stores to only consider tags from these stores
     *
     * @param tagStoreAliases
     *            set of aliases identifying tag stores
     */
    public void setTagStoreAliases(Set<String> tagStoreAliases) {
        if (tagStoreAliases == null) {
            tagStoreAliases = new HashSet<String>();
        }
        // reset the multilingual search
        this.multilingualTagPrefixSearch = false;
        this.tagStoreAliases = tagStoreAliases;
    }

    /**
     * Searches the user fields without email. See also
     * {@link #setUserSearchFilters(String[], boolean)}
     *
     * @param userSearchFilters
     *            the user search strings
     */
    public void setUserSearchFilters(String[] userSearchFilters) {
        this.setUserSearchFilters(userSearchFilters, false);
    }

    /**
     * Sets the user search string. If set the user must match either first, last or email. Each
     * String in the array is "AND" connected: {'user','bla'} -> finds all user having somewhere in
     * there name or email 'user' AND 'bla'
     *
     * @param userSearchFilters
     *            the user search string
     * @param includeEmailFieldInSearch
     *            true if the email field should be included in search
     */
    public void setUserSearchFilters(String[] userSearchFilters, boolean includeEmailFieldInSearch) {
        this.userSearchFilters = userSearchFilters;
        this.userSearchParamNames = createParameterNamesForSearch(PARAM_USER_SEARCH_PREFIX,
                userSearchFilters);
        this.ignoreEmailField = !includeEmailFieldInSearch;
    }

    /**
     * Sort by the last name ascending
     */
    public void sortByEmailAsc() {
        addSortField("lower(" + PLACEHOLDER_USER_ALIAS, UserConstants.EMAIL + ")",
                SORT_ASCENDING);
    }

    /**
     * Sort by the first name ascending
     */
    public void sortByFirstNameAsc() {
        addSortField("lower(" + PLACEHOLDER_USER_ALIAS, UserConstants.PROFILE + "."
                + UserProfileConstants.FIRSTNAME + ")", SORT_ASCENDING);
    }

    /**
     * Sort by the email ascending
     */
    public void sortByLastNameAsc() {
        addSortField("lower(" + PLACEHOLDER_USER_ALIAS, UserConstants.PROFILE + "."
                + UserProfileConstants.LASTNAME + ")", SORT_ASCENDING);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated User UserToUserDataQueryResultConverter instead.
     */
    @Deprecated
    @Override
    public IdentifiableEntityData transformResultItem(Object resultItem) {
        User user = (User) resultItem;
        UserProfile profile = user.getProfile();
        UserData item = new UserData();
        item.setAlias(user.getAlias());
        item.setEmail(user.getEmail());
        item.setFirstName(profile.getFirstName());
        item.setLastName(profile.getLastName());
        item.setSalutation(profile.getSalutation());
        item.setId(user.getId());
        item.setStatus(user.getStatus());
        return item;
    }
}
