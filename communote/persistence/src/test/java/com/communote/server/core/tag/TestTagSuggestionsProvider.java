package com.communote.server.core.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.testng.Assert;

import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.core.tag.impl.InternalTagSuggestionProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TestTagSuggestionsProvider implements TagSuggestionProvider {

    private final TagStoreType type;
    private final Collection<TagSuggestion> tagSuggestions = new HashSet<TagSuggestion>();
    private final String alias;
    private final List<TagSuggestionConfiguration> tagSuggestionConfigurations;

    /**
     * @param alias
     *            The alias of this provider.
     * @param type
     *            The type, this provider supports.
     * @param tagSuggestions
     *            The tag suggestions this provider returns.
     */
    public TestTagSuggestionsProvider(String alias, TagStoreType type,
            TagSuggestion... tagSuggestions) {
        Assert.assertNotNull(alias);
        Assert.assertNotNull(type);
        Assert.assertNotNull(tagSuggestions);
        this.alias = alias;
        this.type = type;
        this.tagSuggestions.addAll(Arrays.asList(tagSuggestions));
        Map<String, TagSuggestionConfiguration> tagSuggestionConfigurationsMapping = new HashMap<String, TagSuggestionConfiguration>();
        for (TagSuggestion tagSuggestion : tagSuggestions) {
            tagSuggestionConfigurationsMapping.put(tagSuggestion.getAlias(),
                    new TagSuggestionConfiguration(alias, tagSuggestion.getAlias(),
                            InternalTagSuggestionProvider.DEFAULT_MESSAGE_KEY_PREFIX
                            + tagSuggestion.getAlias()));

        }
        tagSuggestionConfigurations = new ArrayList<TagSuggestionConfiguration>(
                tagSuggestionConfigurationsMapping.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(TagStoreType type) {
        return this.type.equals(type);
    }

    /**
     * {@inheritDoc}
     *
     * @return All suggestions this provider knows.
     */
    @Override
    public Collection<TagSuggestion> findTagSuggestions(
            Collection<String> tagSuggestionProviderAliases, boolean assignedOnly,
            Map<String, Object> filters, QueryParametersParameterNameProvider nameProvider,
            Locale locale) {
        if (tagSuggestionProviderAliases == null) {
            return tagSuggestions;
        }
        Collection<TagSuggestion> suggestions = new ArrayList<TagSuggestion>();
        for (TagSuggestion suggestion : tagSuggestions) {
            if (tagSuggestionProviderAliases.contains(suggestion.getAlias())) {
                suggestions.add(suggestion);
            }
        }
        return suggestions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAlias() {
        return alias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TagSuggestionConfiguration> getTagSuggestionConfigurations(boolean assignedOnly) {
        return tagSuggestionConfigurations;
    }
}
