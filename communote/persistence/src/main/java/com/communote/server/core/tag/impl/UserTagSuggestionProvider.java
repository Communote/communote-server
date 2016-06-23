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
import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagSuggestion;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.config.UserQueryParametersConfigurator;
import com.communote.server.core.vo.query.converters.TagListItemToTagListItemQueryResultConverter;
import com.communote.server.core.vo.query.tag.UserTagQuery;
import com.communote.server.core.vo.query.tag.UserTagQueryParameters;

/**
 * TagSuggestionProvider for notes.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserTagSuggestionProvider extends InternalTagSuggestionProvider {

    private final QueryManagement queryManagement;
    private final UserTagQuery tagQueryDefinition = QueryDefinitionRepository.instance()
            .getQueryDefinition(UserTagQuery.class);

    /**
     * @param queryManagement
     *            The query management to use.
     */
    public UserTagSuggestionProvider(QueryManagement queryManagement) {
        super(Types.ENTITY.getDefaultTagStoreId());
        this.queryManagement = queryManagement;
    }

    /**
     * @param type
     *            The type to check.
     *
     * @return True for {@link TagStoreType.Types#ENTITY}.
     */
    @Override
    public boolean canHandle(TagStoreType type) {
        return TagStoreType.Types.ENTITY.equals(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TagSuggestion> findTagSuggestions(
            Collection<String> tagSuggestionProviderAliases, boolean assignedTagsOnly,
            Map<String, Object> filters, QueryParametersParameterNameProvider nameProvider,
            Locale locale) {
        UserTagQueryParameters queryInstance = tagQueryDefinition.createInstance();
        UserQueryParametersConfigurator queryInstanceConfigurator =
                new UserQueryParametersConfigurator(nameProvider);
        queryInstanceConfigurator.configure(filters, queryInstance, locale);
        queryInstance.setHideSelectedTags(true);
        // TODO currently this provider only has one suggestion alias thus we don't have to filter
        // the passed aliases, this must be changed if new aliases are supported
        for (String alias : getProvidedTagSuggestionAliases()) {
            queryInstance.getTagStoreAliases().add(alias);
        }
        List<TagData> results = queryManagement.query(tagQueryDefinition, queryInstance,
                new TagListItemToTagListItemQueryResultConverter<RankTagListItem>(locale,
                        ServiceLocator.findService(TagManagement.class)));
        Collection<TagSuggestion> tagSuggestions = new ArrayList<TagSuggestion>();
        tagSuggestions.add(new TagSuggestion(Types.ENTITY.getDefaultTagStoreId(), getAlias(),
                DEFAULT_MESSAGE_KEY_PREFIX + Types.ENTITY.getDefaultTagStoreId(), results));
        return tagSuggestions;
    }

    /**
     * @return Types.ENTITY.getDefaultTagStoreId()
     */
    @Override
    public String[] getProvidedTagSuggestionAliases() {
        return new String[] { Types.ENTITY.getDefaultTagStoreId() };
    }
}
