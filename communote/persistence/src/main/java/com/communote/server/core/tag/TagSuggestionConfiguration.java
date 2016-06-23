package com.communote.server.core.tag;

/**
 * Configuration of TagSuggestions.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagSuggestionConfiguration {

    private final String tagSuggestionProviderAlias;
    private final String tagSuggestionAlias;
    private final String messageKey;

    /**
     * @param tagSuggestionProviderAlias
     *            Alis of the provider.
     * @param tagSuggestionAlias
     *            Alias of the suggestions.
     * @param messageKey
     *            The message key.
     */
    public TagSuggestionConfiguration(String tagSuggestionProviderAlias, String tagSuggestionAlias,
            String messageKey) {
        this.tagSuggestionProviderAlias = tagSuggestionProviderAlias;
        this.tagSuggestionAlias = tagSuggestionAlias;
        this.messageKey = messageKey;
    }

    /**
     * @return the localizedName
     */
    public String getLocalizedName() {
        return messageKey;
    }

    /**
     * @return the tagSuggestionAlias
     */
    public String getTagSuggestionAlias() {
        return tagSuggestionAlias;
    }

    /**
     * @return the tagSuggestionProviderAlias
     */
    public String getTagSuggestionProviderAlias() {
        return tagSuggestionProviderAlias;
    }
}
