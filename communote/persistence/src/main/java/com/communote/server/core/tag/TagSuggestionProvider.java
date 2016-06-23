package com.communote.server.core.tag;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;

/**
 * A TagSuggestionProvider allows searching for tags in arbitrary systems. The results will be shown
 * to the user as suggestions.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface TagSuggestionProvider {
    /**
     * @param type
     *            The class to check.
     * @return True, if this TagSuggestionProvider can handle the given class.
     */
    public boolean canHandle(TagStoreType type);

    /**
     * Searches for tags with the specified filters.
     *
     * @param tagSuggestionProviderAliases
     *            the suggestion aliases to consider. If null the provider should consider all
     *            supported suggestions. The {@link TagSuggestionManagement} will only pass aliases
     *            this provider supports.
     * @param assignedTagsOnly
     *            if true only suggestions of tags that are already assigned to Communote entities
     *            have to be be returned. If false, the tags don't have to be assigned to Communote
     *            entities.
     * @param filters
     *            The filters to use. See the specified {@link QueryParametersParameterNameProvider}
     *            for all possible filter parameters.
     * @param nameProvider
     *            The name provider to use when interpreting the filter paramters.
     * @param locale
     *            The locale to use for filtering.
     * @return List of suggestions.
     */
    public Collection<TagSuggestion> findTagSuggestions(
            Collection<String> tagSuggestionProviderAliases, boolean assignedTagsOnly,
            Map<String, Object> filters, QueryParametersParameterNameProvider nameProvider,
            Locale locale);

    /**
     * @return The alias of this TagSuggestionProvider.
     */
    public String getAlias();

    /**
     * @param assignedTagsOnly
     *            if true only configurations of this provider that would return suggestions of tags
     *            that are already assigned to Communote entities should be returned
     * @return A list of configurations of this provider, can be null
     */
    public List<TagSuggestionConfiguration> getTagSuggestionConfigurations(boolean assignedTagsOnly);
}
