package com.communote.server.core.vo.query.config;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.MatchMode;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.blog.BlogQueryParameters;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.tag.BlogTagQueryParameters;

/**
 * Configurator for {@link BlogQueryParameters}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 */
public class BlogQueryParametersConfigurator<T extends BlogQueryParameters> extends
QueryParametersConfigurator {

    private static final String PARAM_EXCLUDE_DEFAULT_BLOG = "excludeDefaultBlog";

    private static final String BLOG_ACCESS_WRITE = "write";
    private static final String BLOG_ACCESS_MANAGER = "manager";

    /**
     * Helper method which configures the query queryParameters to return all managed blogs of the
     * current user.
     *
     * @param instance
     *            the queryParameters to configure
     */
    public static void configureForAllManagedBlogs(BlogQueryParameters instance) {
        instance.setAccessLevel(TopicAccessLevel.MANAGER);
        instance.setUserId(SecurityHelper.getCurrentUserId());
        instance.setResultSpecification(new ResultSpecification());
    }

    private final boolean defaultExcludeDefaultBlog;

    /**
     * Default constructor
     *
     * @param parameterNameProvider
     *            the name provider to use
     */
    public BlogQueryParametersConfigurator(
            QueryParametersParameterNameProvider parameterNameProvider) {
        this(parameterNameProvider, 10, true);
    }

    /**
     * Constructor
     *
     * @param parameterNameProvider
     *            the name provider to use
     * @param defaultMaxCount
     *            maxCount limit to use if this parameter is not set
     * @param defaultExcludeDefaultBlog
     *            whether to exclude the default blog when the parameter that defines the exclusion
     *            of the default blog is not set
     */
    public BlogQueryParametersConfigurator(
            QueryParametersParameterNameProvider parameterNameProvider, int defaultMaxCount,
            boolean defaultExcludeDefaultBlog) {
        super(parameterNameProvider, defaultMaxCount);
        this.defaultExcludeDefaultBlog = defaultExcludeDefaultBlog;
    }

    /**
     * Configures the query queryParameters
     *
     * @param parameters
     *            the parameters to evaluate
     * @param instance
     *            the queryParameters to configure
     */
    public void configure(Map<String, ?> parameters, T instance) {
        configureQueryInstance(parameters, instance, null);
    }

    /**
     * Configures the query queryParameters
     *
     * @param parameters
     *            the parameters to evaluate
     * @param instance
     *            the queryParameters to configure
     * @param forcedAccessLevel
     *            the blog access level to set. If this argument is set the access level parameter
     *            will be ignored.
     */
    public void configure(Map<String, ?> parameters, T instance, TopicAccessLevel forcedAccessLevel) {
        configureQueryInstance(parameters, instance, forcedAccessLevel);
    }

    /**
     * Configure the blog access level.
     *
     * @param parameters
     *            the parameters to evaluate
     * @param queryParameters
     *            the queryParameters to configure
     * @param forcedAccessLevel
     *            the blog access level to set. If this argument is set the access level parameter
     *            will be ignored.
     */
    private void configureBlogAccessLevel(Map<String, ?> parameters, T queryParameters,
            TopicAccessLevel forcedAccessLevel) {
        queryParameters.setForceAllTopics(ParameterHelper.getParameterAsBoolean(parameters,
                getParameterNameProvider().getNameForForceAllTopics(), false));
        if (forcedAccessLevel != null) {
            queryParameters.setAccessLevel(forcedAccessLevel);
            return;
        }
        String blogAccessLevel = ParameterHelper.getParameterAsString(parameters,
                getParameterNameProvider().getNameForAccessLevel());
        if (BLOG_ACCESS_MANAGER.equals(blogAccessLevel)) {
            queryParameters.setAccessLevel(TopicAccessLevel.MANAGER);
        } else if (BLOG_ACCESS_WRITE.equals(blogAccessLevel)) {
            queryParameters.setAccessLevel(TopicAccessLevel.WRITE);
        } else {
            queryParameters.setAccessLevel(TopicAccessLevel.READ);
        }
    }

    /**
     * Configures the exclusion of blogs from the result set
     *
     * @param parameters
     *            the parameters to evaluate
     * @param instance
     *            the queryParameters to configure
     */
    private void configureBlogExclusion(Map<String, ?> parameters, T instance) {
        List<Long> blogIdsToExclude = ParameterHelper.getParameterAsLongList(parameters,
                getParameterNameProvider().getNameForBlogIdsToExclude());

        boolean excludeDefaultBlog = ParameterHelper.getParameterAsBoolean(parameters,
                PARAM_EXCLUDE_DEFAULT_BLOG, defaultExcludeDefaultBlog);
        Long defaultBlogId = null;
        if (excludeDefaultBlog) {
            ConfigurationManager configManager = CommunoteRuntime.getInstance()
                    .getConfigurationManager();
            if (configManager.getClientConfigurationProperties().isDefaultBlogEnabled()) {
                defaultBlogId = configManager.getClientConfigurationProperties().getDefaultBlogId();
            }
        }
        if (blogIdsToExclude == null) {
            if (defaultBlogId != null) {
                instance.setBlogsToExclude(new Long[] { defaultBlogId });
            }
        } else {
            if (defaultBlogId != null && !blogIdsToExclude.contains(defaultBlogId)) {
                blogIdsToExclude.add(defaultBlogId);
            }
            instance.setBlogsToExclude(blogIdsToExclude.toArray(new Long[blogIdsToExclude.size()]));
        }
    }

    /**
     * Configures the restriction to some blogs
     *
     * @param parameters
     *            the parameters to evaluate
     * @param instance
     *            the queryParameters to configure
     */
    private void configureBlogInclusion(Map<String, ?> parameters, T instance) {
        Long[] blogIds = ParameterHelper.getParameterAsLongArray(parameters,
                getParameterNameProvider().getNameForBlogIds());
        String[] blogAliases = ParameterHelper.getParameterAsStringArray(parameters,
                getParameterNameProvider().getNameForBlogAliases(), ",");
        if (blogAliases != null && blogAliases.length > 0) {
            instance.setBlogAliases(blogAliases);
        }
        instance.setBlogIds(blogIds);
        if (blogIds.length > 0) {
            if (ParameterHelper.getParameterAsBoolean(parameters, getParameterNameProvider()
                    .getNameForIncludeChildTopics(), false)) {
                instance.setIncludeChildTopics(true);
            }
        }
    }

    /**
     * Set the external object filters
     *
     * @param parameters
     *            the parameters to evaluate
     * @param queryParameters
     *            the queryParameters to configure
     */
    private void configureExternalObject(Map<String, ?> parameters, T queryParameters) {

        queryParameters.setExternalObjectId(ParameterHelper.getParameterAsString(parameters,
                getParameterNameProvider().getNameForExternalObjectId(), null));
        queryParameters.setExternalObjectSystemId(ParameterHelper.getParameterAsString(parameters,
                getParameterNameProvider().getNameForExternalObjectSystemId(), null));
    }

    /**
     * Configures the query queryParameters
     *
     * @param parameters
     *            the parameters to evaluate
     * @param queryParameters
     *            the queryParameters to configure
     * @param forcedAccessLevel
     *            the blog access level to set. If this argument is set the access level parameter
     *            will be ignored.
     */
    protected void configureQueryInstance(Map<String, ?> parameters, T queryParameters,
            TopicAccessLevel forcedAccessLevel) {
        queryParameters.setUserId(SecurityHelper.getCurrentUserId());
        configureBlogAccessLevel(parameters, queryParameters, forcedAccessLevel);
        configureSearch(parameters, queryParameters);
        configureBlogExclusion(parameters, queryParameters);
        configureBlogInclusion(parameters, queryParameters);
        configureShowFollowedItems(parameters, queryParameters);
        configureTags(parameters, queryParameters);
        configureExternalObject(parameters, queryParameters);
        queryParameters.setResultSpecification(getResultSpecification(parameters));
        if (queryParameters instanceof BlogTagQueryParameters) {
            ((BlogTagQueryParameters) queryParameters).sortByTagCount();
        } else {
            queryParameters.sortByNameAsc();
        }
        queryParameters.setShowOnlyRootTopics(ParameterHelper.getParameterAsBoolean(parameters,
                getParameterNameProvider().getNameForShowOnlyRootTopics(), false));
        String paramName = getParameterNameProvider().getNameForExcludeToplevelTopics();
        if (ParameterHelper.getParameterAsString(parameters, paramName) != null) {
            // only set if provided to not override default
            queryParameters.setExcludeToplevelTopics(ParameterHelper.getParameterAsBoolean(
                    parameters, paramName));
        }
        queryParameters.setShowOnlyToplevelTopics(ParameterHelper.getParameterAsBoolean(parameters,
                getParameterNameProvider().getNameForShowOnlyToplevelTopics(), false));
        queryParameters.setParentTopicIds(ParameterHelper.getParameterAsLongArray(parameters,
                getParameterNameProvider().getNameForParentTopicIds()));
    }

    /**
     * Configures a string based search.
     *
     * @param parameters
     *            the parameters to evaluate
     * @param instance
     *            the queryParameters to configure
     */
    private void configureSearch(Map<String, ?> parameters, T instance) {
        String pattern = ParameterHelper.getParameterAsString(parameters,
                getParameterNameProvider().getNameForBlogSearchString());
        if (StringUtils.isNotBlank(pattern)) {
            instance.setMatchMode(MatchMode.ANYWHERE);
            boolean searchInTags = ParameterHelper.getParameterAsBoolean(parameters,
                    "searchInTags", false);
            if (searchInTags) {
                instance.setSearchFieldMask(BlogQueryParameters.SEARCH_FIELD_TITLE
                        | BlogQueryParameters.SEARCH_FIELD_BLOG_TAGS);
            } else {
                instance.setSearchFieldMask(BlogQueryParameters.SEARCH_FIELD_TITLE);
            }
            instance.setTextFilter(pattern.split(" "));
        }
    }

    /**
     * Configures to show only followed items
     *
     * @param parameters
     *            the parameters to evaluate
     * @param instance
     *            the queryParameters to configure
     */
    private void configureShowFollowedItems(Map<String, ?> parameters, T instance) {
        boolean showItems = ParameterHelper.getParameterAsBoolean(parameters,
                getParameterNameProvider().getNameForFollowedBlogs(), false);
        instance.setShowOnlyFollowedItems(showItems);
    }

    /**
     * Configures a tag based search.
     *
     * @param parameters
     *            the parameters to evaluate
     * @param instance
     *            the queryParameters to configure
     */
    private void configureTags(Map<String, ?> parameters, T instance) {
        String tags = ParameterHelper.getParameterAsString(parameters, getParameterNameProvider()
                .getNameForTags());
        if (StringUtils.isNotBlank(tags)) {
            instance.setTags(tags.split(","));
        }
        String tagPrefix = ParameterHelper.getParameterAsString(parameters,
                getParameterNameProvider().getNameForTagPrefix());
        if (StringUtils.isNotBlank(tagPrefix)) {
            instance.setTagPrefix(tagPrefix);
        }
        List<Long> tagIds = ParameterHelper.getParameterAsLongList(parameters,
                getParameterNameProvider().getNameForTagIds());
        if (tagIds != null) {
            instance.getTagIds().addAll(tagIds);
        }
    }
}
