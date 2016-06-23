package com.communote.server.core.tag.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagSuggestion;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.config.BlogQueryParametersConfigurator;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.converters.TagListItemToTagListItemQueryResultConverter;
import com.communote.server.core.vo.query.tag.BlogTagQuery;
import com.communote.server.core.vo.query.tag.BlogTagQueryParameters;

/**
 * TagSuggestionProvider for blogs.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogTagSuggestionProvider extends InternalTagSuggestionProvider {

    private static final BlogTagQuery BLOG_TAG_QUERY_DEFINITION = QueryDefinitionRepository
            .instance().getQueryDefinition(BlogTagQuery.class);

    private final QueryManagement queryManagement;

    /**
     * @param queryManagement
     *            The query management to use.
     */
    public BlogTagSuggestionProvider(QueryManagement queryManagement) {
        super(Types.BLOG.getDefaultTagStoreId());
        this.queryManagement = queryManagement;
    }

    /**
     * @param type
     *            The type to check against.
     * @return <code>True</code>, when BLOG.
     */
    @Override
    public boolean canHandle(TagStoreType type) {
        return TagStoreType.Types.BLOG.equals(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TagSuggestion> findTagSuggestions(
            Collection<String> tagSuggestionProviderAliases, boolean assignedTagsOnly,
            Map<String, Object> filters, QueryParametersParameterNameProvider nameProvider,
            Locale locale) {
        BlogQueryParametersConfigurator<BlogTagQueryParameters> configurator;
        configurator = new BlogQueryParametersConfigurator<BlogTagQueryParameters>(nameProvider,
                10,
                false);
        BlogTagQueryParameters queryParameters = BLOG_TAG_QUERY_DEFINITION.createInstance();
        configurator.configure(filters, queryParameters, TopicAccessLevel.READ);
        queryParameters.setHideSelectedTags(true);
        // TODO currently this provider only has one suggestion alias thus we don't have to filter
        // the passed aliases, this must be changed if new aliases are supported
        for (String alias : getProvidedTagSuggestionAliases()) {
            queryParameters.getTagStoreAliases().add(alias);
        }
        queryParameters.setLimitResultSet(false);
        List<TagData> result = queryManagement.query(BLOG_TAG_QUERY_DEFINITION,
                queryParameters, new TagListItemToTagListItemQueryResultConverter(locale,
                        ServiceLocator.findService(TagManagement.class)));
        Collection<TagSuggestion> tagSuggestions = new ArrayList<TagSuggestion>();
        tagSuggestions.add(new TagSuggestion(Types.BLOG.getDefaultTagStoreId(), getAlias(),
                DEFAULT_MESSAGE_KEY_PREFIX + Types.BLOG.getDefaultTagStoreId(), result));
        return tagSuggestions;
    }

    /**
     * @return Types.BLOG.getDefaultTagStoreId();
     */
    @Override
    public String[] getProvidedTagSuggestionAliases() {
        return new String[] { Types.BLOG.getDefaultTagStoreId() };
    }

}
