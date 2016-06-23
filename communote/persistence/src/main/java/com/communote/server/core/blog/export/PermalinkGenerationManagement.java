package com.communote.server.core.blog.export;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.core.integration.IntegrationService;

/**
 * Service for generating permanent links. The service is a {@link PermalinkGenerator} itself.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Service
public class PermalinkGenerationManagement extends DefaultPermalinkGenerator {

    @Autowired
    private IntegrationService integrationService;

    private final List<PermalinkGenerator> generators = new ArrayList<PermalinkGenerator>();
    private PermalinkGenerator lastGenerator;
    {
        lastGenerator = new DefaultPermalinkGenerator();
        generators.add(lastGenerator);
    }

    /**
     * Delegates to the last added generator or the default generator.
     * 
     * {@inheritDoc}
     */
    @Override
    public String extractPermaLinkIdentifier(String[] uriFragments) {
        return lastGenerator.extractPermaLinkIdentifier(uriFragments);
    }

    /**
     * Delegates to the last added generator or the default generator.
     * 
     * {@inheritDoc}
     */
    @Override
    public String getBlogLink(String blogAlias, boolean secure) {
        return lastGenerator.getBlogLink(blogAlias, secure);
    }

    /**
     * Returns a link to the given object within the external system.
     * 
     * @param externalSystemId
     *            The external systems id.
     * @param externalObjectId
     *            The external objects id.
     * @return The link or null, if there is no generator for the given System.
     */
    public String getExternalObjectLink(String externalSystemId, String externalObjectId) {
        return integrationService.getIntegrationLink(externalSystemId, externalObjectId);
    }

    /**
     * Delegates to the last added generator or the default generator.
     * 
     * {@inheritDoc}
     */

    @Override
    public String getNoteLink(String blogAlias, Long noteId, boolean secure) {
        return lastGenerator.getNoteLink(blogAlias, noteId, secure);
    }

    /**
     * Delegates to the last added generator or the default generator.
     * 
     * {@inheritDoc}
     */
    @Override
    public String getTagLink(long tagId, boolean secure) {
        return lastGenerator.getTagLink(tagId, secure);
    }

    /**
     * Delegates to the last added generator or the default generator.
     * 
     * {@inheritDoc}
     */
    @Override
    public String getUserLink(String userAlias, boolean secure) {
        return lastGenerator.getUserLink(userAlias, secure);
    }

    /**
     * @param permalinkGenerator
     *            Generator to register.
     */
    public void registerPermalinkGenerator(PermalinkGenerator permalinkGenerator) {
        generators.add(permalinkGenerator);
        lastGenerator = permalinkGenerator;
    }

    /**
     * @param permalinkGenerator
     *            Generator to remove.
     */
    public void unregisterPermalinkGenerator(PermalinkGenerator permalinkGenerator) {
        generators.remove(permalinkGenerator);
        lastGenerator = generators.get(generators.size() - 1);
    }
}
