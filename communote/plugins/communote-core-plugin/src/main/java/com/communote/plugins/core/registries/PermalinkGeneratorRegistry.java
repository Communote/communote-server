package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.blog.export.PermalinkGenerationManagement;
import com.communote.server.core.blog.export.PermalinkGenerator;

/**
 * This waits for new {@link PermalinkGenerator} and registers them within the
 * {@link PermalinkGenerationManagement}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Instantiate(name = "PermalinkGeneratorRegistry")
public class PermalinkGeneratorRegistry {

    /**
     * Adds the given processor to the list of processors.
     * 
     * @param permalinkGenerator
     *            The generator.
     */
    @Bind(id = "registerPermalinkGenerator", optional = true, aggregate = true)
    public void registerNoteContentProcessor(PermalinkGenerator permalinkGenerator) {
        ServiceLocator.instance().getService(PermalinkGenerationManagement.class)
                .registerPermalinkGenerator(permalinkGenerator);
    }

    /**
     * Removes the given processor from the list of processors.
     * 
     * @param permalinkGenerator
     *            The generator.
     */
    @Unbind(id = "registerPermalinkGenerator", optional = true, aggregate = true)
    public void removeNoteContentProcessor(PermalinkGenerator permalinkGenerator) {
        ServiceLocator.instance().getService(PermalinkGenerationManagement.class)
                .unregisterPermalinkGenerator(permalinkGenerator);
    }
}
