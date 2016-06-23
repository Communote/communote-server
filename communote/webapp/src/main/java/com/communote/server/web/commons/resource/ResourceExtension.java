package com.communote.server.web.commons.resource;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * <p>
 * Pojo describing an extension that allows a plugin to provide additional JavaScript or CSS that
 * should be included into different pages.
 * </p>
 * <p>
 * The inclusion into a page is achieved with the help of categories for JavaScript and CSS. A
 * Category for JavaScript or CSS is identified by a name and is associated with a collection of
 * resources. The author of a page decides which categories and thus which resources are required
 * for the page. This extension provides means to extend existing categories with additional
 * resources or define new ones.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ResourceExtension {
    private Set<ResourceCategoryDefinition> definitions;
    private Map<String, List<String>> extensions;

    /**
     * Return the definitions of categories for resources. A category will temporarily replace an
     * existing one with the same name until the plugin is removed or another plugin is added that
     * defines a category with the same name.
     * 
     * @return the category definitions or null if the plugin does not need to define new categories
     */
    public Set<ResourceCategoryDefinition> getDefinitions() {
        return definitions;
    }

    /**
     * Return the category names that should be extended with additional resources provided by the
     * plugin. The resources to be added to the category have to be provided as a list of file paths
     * relative to the 'static' directory within the plugins resources directory. The order of the
     * resource list will be respected when including them. The category names can refer to built-in
     * categories or categories provided by plugins.
     * 
     * @return a mapping from category name to a collection of resources that should be added to
     *         that category. Can be null if the plugin does not need to extend categories.
     */
    public Map<String, List<String>> getExtensions() {
        return extensions;
    }

    /**
     * @param definitions
     *            the category definitions to set or null if the plugin does not need to define new
     *            categories
     * @see #getDefinitions()
     */
    public void setDefinitions(Set<ResourceCategoryDefinition> definitions) {
        this.definitions = definitions;
    }

    /**
     * @param extensions
     *            a mapping from category name to a collection of resources that should be added to
     *            that category. Can be null if the plugin does not need to extend categories.
     * @see ResourceExtension#getExtensions()
     */
    public void setExtensions(Map<String, List<String>> extensions) {
        this.extensions = extensions;
    }
}
