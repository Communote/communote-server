package com.communote.server.core.tag;

import java.util.Collection;
import java.util.Locale;

import com.communote.server.api.core.tag.TagData;
import com.communote.server.persistence.common.messages.ResourceBundleManager;


/**
 * TagSuggestion are suggested tags from a foreign system or within Communote. These suggestions can
 * be used as tags.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagSuggestion {
    private final String messageKey;
    private final Collection<TagData> tags;
    private final String alias;
    private final String providerAlias;

    /**
     * @param alias
     *            The alias for this suggestion
     * @param providerAlias
     *            the alias of the tag suggestion provider that found the suggestions
     * @param messageKey
     *            The message key for creating a localized name of this TagSuggestion
     * @param tags
     *            The found tags
     */
    public TagSuggestion(String alias, String providerAlias, String messageKey,
            Collection<TagData> tags) {
        this.alias = alias;
        this.providerAlias = providerAlias;
        this.messageKey = messageKey;
        this.tags = tags;

    }

    /**
     * @return the alias for this {@link TagSuggestion}
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return the messageKey that gives that {@link TagSuggestion} a name
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * @param locale
     *            The locale to use.
     * @return The localized name of this suggestion
     */
    public String getName(Locale locale) {
        return ResourceBundleManager.instance().getText(messageKey, locale);
    }

    /**
     * 
     * @return the alias of the tag suggestion provider.
     */
    public String getProviderAlias() {
        return providerAlias;
    }

    /**
     * @return the tags
     */
    public Collection<TagData> getTags() {
        return tags;
    }
}
