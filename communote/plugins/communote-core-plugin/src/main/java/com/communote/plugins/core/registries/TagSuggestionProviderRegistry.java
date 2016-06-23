package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.tag.TagSuggestionManagement;
import com.communote.server.core.tag.TagSuggestionProvider;

/**
 * Registry for {@link TagSuggestionProvider}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "TagSuggestionProviderRegistry")
public class TagSuggestionProviderRegistry {
    /**
     * Adds the given provider.
     * 
     * @param tagSuggestionProvider
     *            The TagSuggestionProvider to add.
     */
    @Bind(id = "registerTagSuggestionProvider", optional = true, aggregate = true)
    public void registerTagSuggestionProvider(TagSuggestionProvider tagSuggestionProvider) {
        ServiceLocator.instance().getService(TagSuggestionManagement.class)
                .addTagSuggestionProvider(tagSuggestionProvider);
    }

    /**
     * Removes the given provider from the list of processors.
     * 
     * @param tagSuggestionProvider
     *            The TagSuggestionProvider to remove.
     */
    @Unbind(id = "registerTagSuggestionProvider", optional = true, aggregate = true)
    public void removeTagSuggestionProvider(TagSuggestionProvider tagSuggestionProvider) {
        ServiceLocator.instance().getService(TagSuggestionManagement.class)
                .removeTagSuggestionProvider(tagSuggestionProvider);
    }
}
