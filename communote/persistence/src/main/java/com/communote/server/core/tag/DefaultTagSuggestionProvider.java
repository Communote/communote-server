package com.communote.server.core.tag;

import org.apache.commons.lang.StringUtils;

/**
 * Defines a {@link TagSuggestionProvider} with an unchangeable alias. Two
 * DefaulTagSuggestionProviders are considered equal if they have the same alias.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public abstract class DefaultTagSuggestionProvider implements TagSuggestionProvider {

    private final String alias;

    /**
     * 
     * @param alias
     *            the alias of this {@link TagSuggestionProvider}
     */
    public DefaultTagSuggestionProvider(String alias) {
        if (alias == null) {
            throw new IllegalArgumentException("Alias cannot be null!");
        }
        this.alias = alias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof TagSuggestionProvider)) {
            return false;
        }
        TagSuggestionProvider other = (TagSuggestionProvider) object;
        return StringUtils.equals(alias, other.getAlias());
    }

    /**
     * {@inheritDoc}
     */
    public final String getAlias() {
        return alias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return alias.hashCode();
    }

}
