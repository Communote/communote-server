package com.communote.server.core.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.impl.BlogTagSuggestionProvider;
import com.communote.server.core.tag.impl.NoteTagSuggestionProvider;
import com.communote.server.core.tag.impl.UserTagSuggestionProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;


/**
 * Service for providing TagSuggestions.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class TagSuggestionManagement {

    @Autowired
    private QueryManagement queryManagement;

    private final List<TagSuggestionProvider> tagSuggestionProviders = new ArrayList<TagSuggestionProvider>();

    /** Parameter for the tag store aliases. */
    public final static String PARAMETER_TAG_STORE_ALIASES = "tagStoreAliases";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TagSuggestionManagement.class);

    /**
     * Adds a new {@link TagSuggestionProvider} provider. If an {@link TagSuggestionProvider} with
     * the same alias already exists the given one will replace this one.
     * 
     * @param provider
     *            The provider.
     */
    public synchronized void addTagSuggestionProvider(TagSuggestionProvider provider) {
        if (provider == null) {
            LOGGER.warn("Can not add \"null\" as provider.");
            return;
        }
        if (tagSuggestionProviders.contains(provider)) {
            tagSuggestionProviders.remove(provider);
        }
        tagSuggestionProviders.add(provider);
    }

    /**
     * Filters a collection of tag suggestion aliases to those that are supported by a tag
     * suggestion provider.
     * 
     * @param tagSuggestionAliases
     *            the aliases to filter. If null, null will be returned
     * @param provider
     *            the provider
     * @param assignedTagsOnly
     *            the operation mode of the provider
     * @return the filtered aliases. The returned collection will be empty if the provider does not
     *         support the requested aliases at all. If the return value is null the provider should
     *         return suggestions for all it's aliases
     */
    private Collection<String> filterTagSuggestionAliases(Collection<String> tagSuggestionAliases,
            TagSuggestionProvider provider,
            boolean assignedTagsOnly) {
        if (tagSuggestionAliases == null) {
            return null;
        }
        Collection<String> supportedAliases = new ArrayList<String>();
        Collection<TagSuggestionConfiguration> configs = provider
                .getTagSuggestionConfigurations(assignedTagsOnly);
        if (configs != null) {
            for (TagSuggestionConfiguration config : configs) {
                if (tagSuggestionAliases.contains(config.getTagSuggestionAlias())) {
                    supportedAliases.add(config.getTagSuggestionAlias());
                }
            }
        }
        return supportedAliases;
    }

    /**
     * Searches for tags with the specified filters.
     * 
     * @param type
     *            Type of the TagStore to filter for.
     * @param tagSuggestionProviderAliases
     *            The aliases of providers to use for searching. If null or empty all providers will
     *            be used.
     * @param tagSuggestionAliases
     *            Aliases of tag suggestions to consider. If null or empty all suggestions will be
     *            used.
     * @param assignedTagsOnly
     *            if true only suggestions of tags that are already assigned to Communote entities
     *            have to be be returned. If false, the tags don't have to be assigned to Communote
     *            entities.
     * @param filters
     *            The filters to use.
     * @param nameProvider
     *            The name provider to use.
     * 
     * @param locale
     *            The locale to use for filtering localized tags.
     * @return List of suggestions.
     */
    public Collection<TagSuggestion> findTagSuggestions(TagStoreType type,
            Collection<String> tagSuggestionProviderAliases,
            Collection<String> tagSuggestionAliases, boolean assignedTagsOnly,
            Map<String, Object> filters, QueryParametersParameterNameProvider nameProvider,
            Locale locale) {
        if (tagSuggestionProviderAliases == null) {
            tagSuggestionProviderAliases = Collections.emptyList();
        }
        if (tagSuggestionAliases != null && tagSuggestionAliases.isEmpty()) {
            tagSuggestionAliases = null;
        }
        Collection<TagSuggestion> suggestions = new ArrayList<TagSuggestion>();
        for (TagSuggestionProvider provider : tagSuggestionProviders) {
            if (skipProvider(type, tagSuggestionProviderAliases, provider)) {
                continue;
            }
            Collection<String> filteredTagSuggestionAliases = this.filterTagSuggestionAliases(
                    tagSuggestionAliases, provider, assignedTagsOnly);
            if (filteredTagSuggestionAliases == null || filteredTagSuggestionAliases.size() > 0) {
                Collection<TagSuggestion> tagSuggestions = provider.findTagSuggestions(
                        filteredTagSuggestionAliases, assignedTagsOnly, filters, nameProvider,
                        locale);
                if (tagSuggestions != null) {
                    suggestions.addAll(tagSuggestions);
                }
            }
        }
        return suggestions;
    }

    /**
     * @param type
     *            a tag store type to only consider providers that support that type. Can be null to
     *            allow all types.
     * @param assignedOnly
     *            If true, only providers that can return suggestions for tags that are already
     *            assigned to Communote entities will be considered
     * @return A list of all TagSuggestionConfigurations.
     */
    public List<TagSuggestionConfiguration> getTagSuggestionConfigurations(TagStoreType type,
            boolean assignedOnly) {
        List<TagSuggestionConfiguration> tagSuggestionConfigurations = new ArrayList<TagSuggestionConfiguration>();
        for (TagSuggestionProvider provider : tagSuggestionProviders) {
            if (type == null || provider.canHandle(type)) {
                List<TagSuggestionConfiguration> innerTagSuggestionConfigurations = provider
                        .getTagSuggestionConfigurations(assignedOnly);
                if (innerTagSuggestionConfigurations != null) {
                    tagSuggestionConfigurations.addAll(innerTagSuggestionConfigurations);
                }
            }
        }
        return tagSuggestionConfigurations;
    }

    /**
     * Adds the default {@link TagSuggestionProvider}.
     */
    @PostConstruct
    public void init() {
        addTagSuggestionProvider(new NoteTagSuggestionProvider(queryManagement));
        addTagSuggestionProvider(new BlogTagSuggestionProvider(queryManagement));
        addTagSuggestionProvider(new UserTagSuggestionProvider(queryManagement));
    }

    /**
     * Removes a the given provider.
     * 
     * @param provider
     *            The provider to remove.
     */
    public synchronized void removeTagSuggestionProvider(TagSuggestionProvider provider) {
        tagSuggestionProviders.remove(provider);
    }

    /**
     * Skip the provider, if the {@link TagSuggestionProvider} cannot handle the given
     * {@link TagStoreType}, or if the alias of the {@link TagSuggestionProvider} does not match the
     * tagSuggestionProviderAliases. Also skip if only existing tags should be retrieved.
     * 
     * @param type
     *            The type.
     * @param tagSuggestionProviderAliases
     *            Aliases of tag providers.
     * @param tagSuggestionProvider
     *            The provider.
     * @return <code>True</code> if this provider should be skipped.
     */
    private boolean skipProvider(TagStoreType type,
            Collection<String> tagSuggestionProviderAliases,
            TagSuggestionProvider tagSuggestionProvider) {

        // check if the provider can handle the type
        boolean valid = tagSuggestionProvider.canHandle(type);
        // check if the aliases list is either empty or contains the provider alias
        valid = valid
                && (tagSuggestionProviderAliases.isEmpty() || tagSuggestionProviderAliases
                        .contains(tagSuggestionProvider.getAlias()));

        return !valid;

    }
}
