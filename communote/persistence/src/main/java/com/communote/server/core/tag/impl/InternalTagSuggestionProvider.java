package com.communote.server.core.tag.impl;

import java.util.ArrayList;
import java.util.List;

import com.communote.server.core.tag.DefaultTagSuggestionProvider;
import com.communote.server.core.tag.TagSuggestionConfiguration;


/**
 * Base class for internal TagSuggestionProvider.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class InternalTagSuggestionProvider extends DefaultTagSuggestionProvider {

    /** Prefix for message keys. **/
    public final static String DEFAULT_MESSAGE_KEY_PREFIX = "tag.suggestion.name.";

    private final List<TagSuggestionConfiguration> tagSuggestionsConfigurations =
            new ArrayList<TagSuggestionConfiguration>();

    /**
     * 
     * @param alias
     *            the alias of the tag sugesstion provider
     */
    public InternalTagSuggestionProvider(String alias) {
        super(alias);
        for (String tagSuggestionAliases : getProvidedTagSuggestionAliases()) {
            TagSuggestionConfiguration tagSuggestionConfiguration = new TagSuggestionConfiguration(
                    getAlias(), tagSuggestionAliases, DEFAULT_MESSAGE_KEY_PREFIX
                            + tagSuggestionAliases);
            tagSuggestionsConfigurations.add(tagSuggestionConfiguration);
        }
    }

    /**
     * @return An array of the tagSuggestionAliases this provider provides.
     */
    public abstract String[] getProvidedTagSuggestionAliases();

    /**
     * {@inheritDoc}
     */
    public List<TagSuggestionConfiguration> getTagSuggestionConfigurations(boolean assignedTagsOnly) {
        // an internal provider always returns assigned suggestions, thus no need to eval
        // assignedOnly argument
        return tagSuggestionsConfigurations;
    }
}
