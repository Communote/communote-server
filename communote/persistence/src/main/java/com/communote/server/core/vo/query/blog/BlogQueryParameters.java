package com.communote.server.core.vo.query.blog;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.MatchMode;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.vo.query.PropertyQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.persistence.tag.TagStore;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogQueryParameters extends PropertyQueryParameters {

    /**
     * Parameter for the user ID.
     */
    public final static String PARAM_USER_ID = "userId";
    /**
     * the parameter name for tags starting with some pattern
     */
    public final static String PARAM_BLOG_TAGPREFIX_SEARCH = "blogTagPrefix";
    /**
     * Parameter for write access roles.
     */
    public final static String PARAM_WRITE_ACCESS_ROLES = "write_access_roles";
    /**
     * Parameter for read access roles.
     */
    public final static String PARAM_READ_ACCESS_ROLES = "read_access_roles";
    /**
     * Parameter for manager access roles.
     */
    public final static String PARAM_MANAGER_ACCESS_ROLES = "manager_access_roles";
    /**
     * Parameter for the IDs of the blogs to ignore.
     */
    public final static String PARAM_BLOGS_TO_IGNORE = "blogs_to_ignore";
    /**
     * Parameter for the blog IDs.
     */
    public final static String PARAM_BLOG_IDS = "blog_ids";
    /**
     * Parameter for the parent topic IDs.
     */
    public final static String PARAM_PARENT_TOPIC_IDS = "parent_topic_ids";
    /**
     * Parameter for the blog aliases.
     */
    public final static String PARAM_BLOG_ALIASES = "blog_aliases";
    /**
     * Paramater for the last modification date
     */
    public final static String PARAM_LAST_MODIFICATION_DATE = "lastModificationDate";
    /**
     * Paramater for the event type date
     */
    public final static String PARAM_EVENT_TYPE = "eventType";
    /**
     * Paramater for external system id
     */
    public final static String PARAM_EXTERNAL_OBJECT_SYSTEM_ID = "externalObjectSystemId";

    /**
     * Paramater for external object id
     */
    public final static String PARAM_EXTERNAL_OBJECT_ID = "externalObjectId";
    /** */
    public final static int SEARCH_FIELD_TITLE = 1;
    /** */
    public final static int SEARCH_FIELD_DESCRIPTION = 2;
    /** */
    public final static int SEARCH_FIELD_IDENTIFIER = 4;
    /** */
    public final static int SEARCH_FIELD_BLOG_TAGS = 8;
    /**
     * the parameter name for the selected tags
     */
    private final static String PARAM_BLOG_TAG_PREFIX = "blogTag";
    /** Parameter for blog searches. */
    private static final String PARAM_BLOG_TEXT_SEARCH_PREFIX = "blogTextSearch";

    /**
     * @return a map with the blog access roles, keys are the access parameters
     */

    public static Map<String, BlogRole[]> getBlogAccessParameter() {
        Map<String, BlogRole[]> result = new HashMap<String, BlogRole[]>();
        result.put(PARAM_READ_ACCESS_ROLES, new BlogRole[] { BlogRole.VIEWER, BlogRole.MEMBER,
                BlogRole.MANAGER });
        result.put(PARAM_WRITE_ACCESS_ROLES, new BlogRole[] { BlogRole.MEMBER, BlogRole.MANAGER });
        result.put(PARAM_MANAGER_ACCESS_ROLES, new BlogRole[] { BlogRole.MANAGER });
        return result;
    }

    private final Set<Long> tagIds = new HashSet<Long>();
    private final Map<String, Set<String>> tagStoreTagIds = new HashMap<String, Set<String>>();
    private Boolean multilingualTagPrefixSearch = null;
    private Set<String> tagStoreAliases = new HashSet<String>();
    private Long userId;
    private TopicAccessLevel accessLevel;
    private Date minimumLastModificationDate;
    private Long[] blogsToExclude;
    private Long[] blogIds;
    private String[] blogAliases;
    private int searchFieldMask;
    private MatchMode matchMode = MatchMode.ANYWHERE;
    private String[] textFilters;
    private String[] textFilterParamNames;
    private Boolean showOnlyFollowedItems = false;
    private boolean forceAllTopics = false;
    private boolean excludeToplevelTopics;
    private boolean showOnlyToplevelTopics;
    private boolean showOnlyRootTopics = false;
    private String[] tags;
    private String tagPrefix;
    private boolean renderTagsJoin;
    private boolean includeChildTopics;

    private String externalObjectSystemId;
    private String externalObjectId;

    private Long[] parentTopicIds = new Long[0];

    /**
     * Create a new parameters object.
     */
    public BlogQueryParameters() {
        // include top level topics by default if enabled
        excludeToplevelTopics = !ClientProperty.TOP_LEVEL_TOPICS_ENABLED
                .getValue(ClientProperty.DEFAULT_TOP_LEVEL_TOPICS_ENABLED);
    }

    /**
     * @param tagId
     *            TagId to filter for.
     */
    public void addTagId(Long tagId) {
        tagIds.add(tagId);
    }

    /**
     * @param tagStoreAlias
     *            The alias of the TagStore.
     * @param tagStoreTagIds
     *            Collection of tag ids to add for the given TagStore.
     */
    public void addTagStoreTagId(String tagStoreAlias, Collection<String> tagStoreTagIds) {
        Set<String> tags = this.tagStoreTagIds.get(tagStoreAlias);
        if (tags == null) {
            tags = new HashSet<String>();
            this.tagStoreTagIds.put(tagStoreAlias, tags);
        }
        tags.addAll(tagStoreTagIds);
    }

    /**
     * @param tagStoreAlias
     *            The alias of the TagStore.
     * @param tagStoreTagId
     *            The id of the tag within the TagStore.
     */
    public void addTagStoreTagId(String tagStoreAlias, String tagStoreTagId) {
        Set<String> tags = tagStoreTagIds.get(tagStoreAlias);
        if (tags == null) {
            tags = new HashSet<String>();
            tagStoreTagIds.put(tagStoreAlias, tags);
        }
        tags.add(tagStoreTagId);
    }

    /**
     * Returns the blog access level.
     * 
     * @return the access level
     */
    public TopicAccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * The blog aliases to which the result set will be reduced.
     * 
     * @return the aliases or null
     */
    public String[] getBlogAliases() {
        return blogAliases;
    }

    /**
     * The blog IDs to which the result set will be reduced.
     * 
     * @return the IDs or null
     */
    public Long[] getBlogIds() {
        return blogIds;
    }

    /**
     * Returns the blog IDs which will not be included in the response.
     * 
     * @return the blogsToExclude
     */
    public Long[] getBlogsToExclude() {
        return blogsToExclude;
    }

    /**
     * Get the parameter name for the selected tag of a given index (if its a parameter list)
     * 
     * @param index
     *            the index
     * @return the parameter name to the index
     */
    public String getBlogTagConstant(int index) {
        return PARAM_BLOG_TAG_PREFIX + index;
    }

    /**
     * If both {@link #getExternalObjectId()} and {@link #getExternalObjectSystemId()} are set, both
     * must match on the same external object
     * 
     * @return filter for topics which have an external object with the given external id assigned.
     */
    public String getExternalObjectId() {
        return externalObjectId;
    }

    /**
     * If both {@link #getExternalObjectId()} and {@link #getExternalObjectSystemId()} are set, both
     * must match on the same external object
     * 
     * @return filter for topics which have an external object with the given system id assigned.
     */
    public String getExternalObjectSystemId() {
        return externalObjectSystemId;
    }

    /**
     * @return the matchMode
     */
    public MatchMode getMatchMode() {
        return matchMode;
    }

    /**
     * @return the minimum last modification the blog must be modified AFTER
     */
    public Date getMinimumLastModificationDate() {
        return minimumLastModificationDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameter = super.getParameters();
        parameter.put(PARAM_USER_ID, userId);
        parameter.put(PARAM_BLOGS_TO_IGNORE, blogsToExclude);
        parameter.put(PARAM_BLOG_IDS, blogIds);
        parameter.put(PARAM_BLOG_ALIASES, blogAliases);

        if (getTags() != null) {
            for (int i = 0; i < getTags().length; i++) {
                parameter.put(getBlogTagConstant(i), getTags()[i].toLowerCase(Locale.ENGLISH));
            }
        }

        if (minimumLastModificationDate != null) {
            parameter.put(PARAM_LAST_MODIFICATION_DATE, minimumLastModificationDate);
        }
        if (searchFieldMask != 0 && textFilters != null) {
            putParametersForSearch(parameter, textFilterParamNames, textFilters, getMatchMode(),
                    true);
        }
        if (StringUtils.isNotBlank(tagPrefix)) {
            putParametersForSearch(parameter,
                    new String[] { PARAM_BLOG_TAGPREFIX_SEARCH },
                    new String[] { tagPrefix }, MatchMode.START, true);
        }
        if (getParentTopicIds().length > 0) {
            parameter.put(PARAM_PARENT_TOPIC_IDS, parentTopicIds);
        }
        parameter.putAll(getBlogAccessParameter());

        parameter.put(PARAM_EXTERNAL_OBJECT_ID, this.externalObjectId);
        parameter.put(PARAM_EXTERNAL_OBJECT_SYSTEM_ID, this.externalObjectSystemId);
        return parameter;
    }

    /**
     * @return Array of parent topics to filter for. Is never null.
     */
    public Long[] getParentTopicIds() {
        return parentTopicIds;
    }

    /**
     * @return the tagIds
     */
    public Set<Long> getTagIds() {
        return tagIds;
    }

    /**
     * @return the prefix tags must have
     */
    public String getTagPrefix() {
        return tagPrefix;
    }

    /**
     * @return the tags
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * @return set of aliases identifying tag stores
     */
    public Set<String> getTagStoreAliases() {
        return tagStoreAliases;
    }

    /**
     * @return the tagStoreTagIds
     */
    public Map<String, Set<String>> getTagStoreTagIds() {
        return tagStoreTagIds;
    }

    /**
     * Returns the strings to be found in title, description or identifier of blog.
     * 
     * @return the textFilter
     */
    public String[] getTextFilter() {
        return textFilters;
    }

    /**
     * @return the names of the parameters that substitute the text search values
     */
    public String[] getTextFilterParamNames() {
        return textFilterParamNames;
    }

    /**
     * @return the user id
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @return whether to exclude topics marked as top level topic from the result. If top level
     *         topics are disabled this method returns true by default. If
     *         {@link #isShowOnlyToplevelTopics()} returns true this flag is ignored.
     */
    public boolean isExcludeToplevelTopics() {
        return excludeToplevelTopics;
    }

    /**
     * @return the forceAllTopics flag. If true, all topics should be respected, regardless of
     *         access roles. The current user has to be a client manager to use this flag.
     */
    public boolean isForceAllTopics() {
        return forceAllTopics;
    }

    /**
     * @return Whether to include the child topics in the query. This parameter will only be ignored
     *         if the blogIDs parameter is unset.
     */
    public boolean isIncludeChildTopics() {
        return includeChildTopics;
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
                        .hasMultilingualTagStore(TagStoreType.Types.BLOG);
            }
        }
        return multilingualTagPrefixSearch;
    }

    /**
     * @return the renderTagsJoin
     */
    public boolean isRenderTagsJoin() {
        return renderTagsJoin;
    }

    /**
     * Tests whether the query should search in a specific field. The field is identified with one
     * of the SEARCH_FIELD_X constants.
     * 
     * @param fieldConstant
     *            one of the SEARCH_FIELD_X of this class
     * @return true if the constant is enabled in the search field mask set by
     *         {@link #setSearchFieldMask(int)}
     */
    protected boolean isSearchInField(int fieldConstant) {
        return (this.searchFieldMask & fieldConstant) != 0;
    }

    /**
     * @return Whether only root topics should be retrieved. Root topics are all topics which do not
     *         have a parent topic. Topics whose parent topics are not readable by the current user
     *         are not treated as root topics. This flag is ignored if {
     *         {@link #isShowOnlyToplevelTopics()} returns true.
     */
    public boolean isShowOnlyRootTopics() {
        return showOnlyRootTopics;
    }

    /**
     * @return True, if only topics marked as top level topic should be returned. If this flag is
     *         true, the return values of {@link #isShowOnlyRootTopics()} and
     *         {@link #isExcludeToplevelTopics()} will be ignored.
     */
    public boolean isShowOnlyToplevelTopics() {
        return showOnlyToplevelTopics;
    }

    /**
     * returns true.
     * 
     * @return true.
     */
    @Override
    public boolean needTransformListItem() {
        return true;
    }

    /**
     * Sets the blog access level
     * 
     * @param accessLevel
     *            the access level to filter for
     */
    public void setAccessLevel(TopicAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    /**
     * Used to reduce the result to blogs with specific aliases.
     * 
     * @param aliases
     *            the aliases of the blogs
     */
    public void setBlogAliases(String[] aliases) {
        this.blogAliases = aliases;
    }

    /**
     * Used to reduce the result to blogs with specific IDs.
     * 
     * @param blogIds
     *            the blog IDs
     */
    public void setBlogIds(Long[] blogIds) {
        this.blogIds = blogIds;
    }

    /**
     * Used to set blog IDs to exclude from search.
     * 
     * @param blogsToExclude
     *            the blogsToExclude to set
     */
    public void setBlogsToExclude(Long[] blogsToExclude) {
        this.blogsToExclude = blogsToExclude;
    }

    /**
     * Set whether to exclude top level topics from the result. This flag will be ignored if
     * {@link #isShowOnlyToplevelTopics()} returns true.
     * 
     * @param exclude
     *            True if top level topics should be excluded.
     * 
     */
    public void setExcludeToplevelTopics(boolean exclude) {
        this.excludeToplevelTopics = exclude;
    }

    public void setExternalObjectId(String externalObjectId) {
        this.externalObjectId = externalObjectId;
    }

    public void setExternalObjectSystemId(String externalObjectSystemId) {
        this.externalObjectSystemId = externalObjectSystemId;
    }

    /**
     * @param forceAllTopics
     *            True, if all topics should be shown, regardless of access roles. The current user
     *            has to be a client manager to use this flag.
     */
    public void setForceAllTopics(boolean forceAllTopics) {
        this.forceAllTopics = forceAllTopics;
    }

    /**
     * Whether to include the child topics in the query. This parameter will only be ignored if the
     * blogIDs parameter is unset.
     * 
     * @param includeChildTopics
     *            whether to include the child topics
     */
    public void setIncludeChildTopics(boolean includeChildTopics) {
        this.includeChildTopics = includeChildTopics;
    }

    /**
     * Sets the match mode text filtering. The default is to match anywhere.
     * 
     * @param matchMode
     *            the matchMode to set
     */
    public void setMatchMode(MatchMode matchMode) {
        this.matchMode = matchMode;
    }

    /**
     * @param minimumLastModificationDate
     *            the minimum last modification the blog must be modified AFTER
     */
    public void setMinimumLastModificationDate(Date minimumLastModificationDate) {
        this.minimumLastModificationDate = minimumLastModificationDate;
    }

    /**
     * @param parentTopicIds
     *            Array of parent topics.
     */
    public void setParentTopicIds(Long[] parentTopicIds) {
        if (parentTopicIds != null) {
            this.parentTopicIds = parentTopicIds;
        }
    }

    /**
     * @param renderTagsJoin
     *            the renderTagsJoin to set
     */
    public void setRenderTagsJoin(boolean renderTagsJoin) {
        this.renderTagsJoin = renderTagsJoin;
    }

    /**
     * Can be used to set the fields to be searched with the {@link #getTextFilter() text filter}.
     * 
     * @param searchFieldMask
     *            a bitwise combination of the SEARCH_*_FIELD flags.
     * @see #SEARCH_FIELD_DESCRIPTION
     * @see #SEARCH_FIELD_IDENTIFIER
     * @see #SEARCH_FIELD_TITLE
     * @see #SEARCH_FIELD_BLOG_TAGS
     */
    public void setSearchFieldMask(int searchFieldMask) {
        this.searchFieldMask = searchFieldMask;
    }

    /**
     * @param showFollowing
     *            the showFollowing to set
     */
    public void setShowOnlyFollowedItems(Boolean showFollowing) {
        this.showOnlyFollowedItems = showFollowing;
    }

    /**
     * Set whether only root topics should be returned. This flag will be ignored if
     * {@link #isShowOnlyToplevelTopics()} returns true.
     * 
     * @param showOnlyRootTopics
     *            True if only root topics should be returned.
     * 
     */
    public void setShowOnlyRootTopics(boolean showOnlyRootTopics) {
        this.showOnlyRootTopics = showOnlyRootTopics;
    }

    /**
     * Set whether only topics marked as top level topic should be returned. If set to true
     * {@link #isShowOnlyToplevelTopics()} and {@link #isExcludeToplevelTopics()} will be ignored.
     * 
     * @param showOnlyToplevelTopics
     *            True if only top level topics should be returned.
     * 
     */
    public void setShowOnlyToplevelTopics(boolean showOnlyToplevelTopics) {
        this.showOnlyToplevelTopics = showOnlyToplevelTopics;
    }

    /**
     * @param tagPrefix
     *            the prefix tags must have
     */
    public void setTagPrefix(String tagPrefix) {
        this.tagPrefix = tagPrefix;
    }

    /**
     * @param tags
     *            the tags to set
     */
    public void setTags(String[] tags) {
        this.tags = tags;
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
     * Set the strings to be found in title, description or identifier of blog.
     * 
     * @param textFilter
     *            the full text strings to search for
     */
    public void setTextFilter(String[] textFilter) {
        this.textFilterParamNames = createParameterNamesForSearch(PARAM_BLOG_TEXT_SEARCH_PREFIX,
                textFilter);
        this.textFilters = textFilter;
    }

    /**
     * @param userId
     *            the user id to filter for
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return {@code true} if only followed items are shown
     */
    public Boolean showOnlyFollowedItems() {
        return showOnlyFollowedItems;
    }

    /**
     * Sort ascending by the last modification date of the blog. This means the oldest modified blog
     * will be returned as first element
     */
    public void sortByLastModificationDateAsc() {
        this.addSortField(BlogQuery.ALIAS_BLOG, BlogConstants.LASTMODIFICATIONDATE,
                SORT_ASCENDING);
    }

    /**
     * sort by the name of the blogs
     */
    public void sortByNameAsc() {
        this.addSortField("lower(" + BlogQuery.ALIAS_BLOG, BlogConstants.TITLE + ")",
                SORT_ASCENDING);
    }

    /**
     * Transforms the BlogData. Sets the description for BlogData because the description is
     * not allow in query (clob in a oracle db environment)
     * 
     * @param resultItem
     *            The resultItem to transform
     * @return The transformed BlogData
     */
    @Override
    public IdentifiableEntityData transformResultItem(Object resultItem) {
        BlogData result = (BlogData) resultItem;
        BlogManagement blogManagement = ServiceLocator
                .findService(BlogManagement.class);
        Blog blog;
        try {
            blog = isForceAllTopics() && SecurityHelper.isClientManager()
                    ? blogManagement.findBlogByIdWithoutAuthorizationCheck(result.getId())
                    : blogManagement.getBlogById(result.getId(), false);
        } catch (BlogNotFoundException e) {
            throw BlogManagementHelper.convertException(e);
        } catch (BlogAccessException e) {
            throw BlogManagementHelper.convertException(e);
        }
        result.setDescription(blog.getDescription());
        return result;
    }
}
