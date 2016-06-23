package com.communote.server.web.commons.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that manages templates which are identified by a name and have a file location.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TemplateManager {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateManager.class);

    private final Map<String, Map<String, String>> dynamicTemplates = new HashMap<String, Map<String, String>>();
    private final ArrayList<String> orderedProviderIds = new ArrayList<String>();

    private Map<String, String> templates;

    /**
     * Create a new instance
     */
    public TemplateManager() {
        // don't fire an event on creation because listeners are probably not ready
        rebuildTemplateMapping(false);
    }

    /**
     * Add the core templates
     *
     * @param templateMapping
     *            a mapping from template key to template file where the templates should be added
     *            to
     */
    protected void addCoreTemplates(Map<String, String> templateMapping) {
    }

    /**
     * Return the location of the template file for a given template name
     *
     * @param templateName
     *            the name of the template to retrieve
     * @return the location of the template file for a given template name or null if the template
     *         is not registered
     */
    public String getTemplate(String templateName) {
        if (templateName == null) {
            return null;
        }
        return templates.get(templateName);
    }

    /**
     * @return all registered template names
     */
    public String[] getTemplateNames() {
        return templates.keySet().toArray(new String[templates.size()]);
    }

    /**
     * Rebuild template mapping
     *
     * @param fireEvent
     *            whether to notify listeners about the change
     */
    private synchronized void rebuildTemplateMapping(boolean fireEvent) {
        // use a new object to avoid rendering problems while we are rebuilding the templates
        Map<String, String> newTemplates = new ConcurrentHashMap<String, String>();

        addCoreTemplates(newTemplates);
        // process registered templates in order of their registration
        // TODO: this is not 100% correct because templates are added individually, so we would have
        // to remember the registration order per template name
        for (String providerId : orderedProviderIds) {
            Map<String, String> templatesOfProvider = dynamicTemplates.get(providerId);
            newTemplates.putAll(templatesOfProvider);
        }
        templates = newTemplates;
        /*
         * TODO we should fire an event to notify listeners about changed templates, however the
         * installer makes it impossible since the core spring app context is not available as long
         * as the installation hasn't been completed. This mainly caused by bad separation of the
         * installer which should have its own spring web context and should not reuse this one
         */
        /*
         * if (fireEvent) { getEventDispatcher().fire(new TemplatesChangedEvent()); }
         */
    }

    /**
     * Register a template. If there is already a template with the same name, it will be replaced.
     *
     * @param providerId
     *            ID that identifies the owner/provider of the template, this is usually the ID of a
     *            plugin.
     * @param templateName
     *            the template name
     * @param templateLocation
     *            the file location of the template. The location can be relative if the template
     *            rendering engine knows how to resolve the full path.
     */
    public synchronized void registerTemplate(String providerId, String templateName,
            String templateLocation) {
        if (providerId == null) {
            throw new IllegalArgumentException("providerId cannot be null!");
        }
        if (StringUtils.isBlank(templateName)) {
            throw new IllegalArgumentException("templateName cannot be null or blank!");
        }
        if (StringUtils.isBlank(templateLocation)) {
            throw new IllegalArgumentException("templateLocation cannot be null or blank!");
        }
        if (!dynamicTemplates.containsKey(providerId)) {
            dynamicTemplates.put(providerId, new HashMap<String, String>());
            orderedProviderIds.add(providerId);
        }
        // TODO implement a checked exception or at least log a warning if file does not exist? can
        // we determine it here?
        dynamicTemplates.get(providerId).put(templateName, templateLocation);
        // don't reload because we just can add the view directly and inform all listeners
        templates.put(templateName, templateLocation);
        // TODO sould fire an event here, but this is currently not possible (see note above)
        // getEventDispatcher().fire(new TemplatesChangedEvent());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Added template for " + providerId + " with templateName=" + templateName
                    + " velocityName=" + templateLocation);
        }
    }

    /**
     * Unregister all templates previously registered for a given provider. If one of the templates
     * replaced another template with the same name the replaced template will be accessible again.
     *
     * @param providerId
     *            ID that identifies the owner/provider of the templates
     */
    public synchronized void unregisterTemplates(String providerId) {
        if (dynamicTemplates.remove(providerId) != null) {
            orderedProviderIds.remove(providerId);
            rebuildTemplateMapping(true);
        }
    }
}
