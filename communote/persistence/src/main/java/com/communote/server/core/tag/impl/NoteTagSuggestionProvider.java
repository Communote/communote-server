package com.communote.server.core.tag.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagSuggestion;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.converters.TagListItemToTagListItemQueryResultConverter;
import com.communote.server.core.vo.query.tag.TagQuery;
import com.communote.server.core.vo.query.tag.TagQueryParameters;

/**
 * TagSuggestionProvider for notes.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteTagSuggestionProvider extends InternalTagSuggestionProvider {

    private final QueryManagement queryManagement;
    private final TagQuery tagQueryDefinition = QueryDefinitionRepository
            .instance().getQueryDefinition(TagQuery.class);

    /**
     * @param queryManagement
     *            The query management to use.
     */
    public NoteTagSuggestionProvider(QueryManagement queryManagement) {
        super(Types.NOTE.getDefaultTagStoreId());
        this.queryManagement = queryManagement;
    }

    /**
     * @param type
     *            The type to check.
     *
     * @return True for {@link TagStoreType.Types#NOTE}.
     */
    @Override
    public boolean canHandle(TagStoreType type) {
        return TagStoreType.Types.NOTE.equals(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TagSuggestion> findTagSuggestions(
            Collection<String> tagSuggestionProviderAliases, boolean assignedTagsOnly,
            Map<String, Object> filters, QueryParametersParameterNameProvider nameProvider,
            Locale locale) {
        TagQueryParameters queryParameters = tagQueryDefinition.createInstance();
        TimelineQueryParametersConfigurator<TagQueryParameters> queryInstanceConfigurator =
                new TimelineQueryParametersConfigurator<>(
                        nameProvider);
        queryParameters.sortByTagNameAsc();
        queryParameters.setHideSelectedTags(true);
        queryParameters.setTagIdsToExclude(ParameterHelper.getParameterAsLongList(filters,
                nameProvider.getNameForTagIdsToExclude()));
        queryInstanceConfigurator.configure(filters, queryParameters);
        // TODO currently this provider only has one suggestion alias thus we don't have to filter
        // the passed aliases, this must be changed if new aliases are supported
        for (String alias : getProvidedTagSuggestionAliases()) {
            queryParameters.getTagStoreAliases().add(alias);
        }
        queryParameters.setLimitResultSet(true);
        List<TagData> results = queryManagement.query(tagQueryDefinition, queryParameters,
                new TagListItemToTagListItemQueryResultConverter(locale,
                        ServiceLocator.findService(TagManagement.class)));
        Collection<TagSuggestion> tagSuggestions = new ArrayList<TagSuggestion>();
        tagSuggestions.add(new TagSuggestion(Types.NOTE.getDefaultTagStoreId(), getAlias(),
                DEFAULT_MESSAGE_KEY_PREFIX + Types.NOTE.getDefaultTagStoreId(), results));
        return tagSuggestions;
    }

    /**
     * @return Types.NOTE.getDefaultTagStoreId()
     */
    @Override
    public String[] getProvidedTagSuggestionAliases() {
        return new String[] { Types.NOTE.getDefaultTagStoreId() };
    }
}
