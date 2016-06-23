package com.communote.server.web.commons.resource;

import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Object that defines a JavaScript or CSS category with initial resources.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResourceCategoryDefinition {

    private final String categoryName;
    private final List<String> coreResources;
    private final List<String> pluginResources;
    private boolean propertyResource;
    private String propertyResourceFallback;

    /**
     * Create a new category definition.
     * 
     * @param categoryName
     *            the name of the category. This can be the name of an existing built-in category or
     *            a new one.
     * @param coreResources
     *            a collection of resources which are delivered with the application and should be
     *            added first when rendering the resources of this category. Each entry of the
     *            collection refers to the relative location of a resource under which it can be
     *            downloaded. Can be null if the category needs no core resources.
     * @param pluginResources
     *            the initial resources of this category that are provided by the plugin and should
     *            be included after the coreResources when rendering the category's resources. The
     *            resources are expected to be list of file paths relative to the 'static' directory
     *            within the plugin resources directory. Can be null if the category needs no plugin
     *            resources.
     */
    @JsonCreator
    public ResourceCategoryDefinition(@JsonProperty(value = "categoryName") String categoryName,
            @JsonProperty(value = "coreResources") List<String> coreResources,
            @JsonProperty(value = "pluginResources") List<String> pluginResources) {
        if (categoryName == null) {
            throw new IllegalArgumentException("The category name must not be null");
        }
        this.categoryName = categoryName;
        this.coreResources = coreResources;
        this.pluginResources = pluginResources;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResourceCategoryDefinition other = (ResourceCategoryDefinition) obj;
        if (categoryName == null) {
            if (other.categoryName != null) {
                return false;
            }
        } else if (!categoryName.equals(other.categoryName)) {
            return false;
        }
        return true;
    }

    /**
     * @return the name of the category defined by this object. This can be the name of an existing
     *         built-in category or a new one.
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Return a collection of resources which are delivered with the application and should be added
     * first when rendering the resources of this category. Each entry of the collection refers to
     * the relative location of a resource under which it can be downloaded.
     * 
     * @return the initial resources of the category that are delivered with the application. Can be
     *         null if the category needs no core resources.
     */
    public List<String> getCoreResources() {
        return coreResources;
    }

    /**
     * Return the initial resources of this category that are provided by the plugin and should be
     * included after the coreResources when rendering the category's resources. The resources are
     * expected to be list of file paths relative to the 'static' directory within the plugin
     * resources directory. Can be null if the category needs no plugin resources.
     * 
     * @return the initial plugin resources
     */
    public List<String> getPluginResources() {
        return pluginResources;
    }

    /**
     * Return a fallback plugin resource to be used if {@link #hasPropertyResource()} returns true
     * but the PluginProperty does not exist.
     * 
     * @return a file path relative to the 'static' directory within the plugin resources directory
     *         or null if there is no fallback
     */
    public String getPropertyResourceFallback() {
        return propertyResourceFallback;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((categoryName == null) ? 0 : categoryName.hashCode());
        return result;
    }

    /**
     * Whether the category definition supports providing a resource with the help of a
     * PluginProperty. The PluginProperty that holds the resource has the property key
     * 'cssCategoryResource.[categoryName of this definiton]'. In case there is a property resource
     * it will be included after the pluginResources.
     * 
     * @return true if there can be a PluginProperty, false by default
     */
    public boolean hasPropertyResource() {
        return propertyResource;
    }

    /**
     * Set whether there can be a PluginProperty providing a resource
     * 
     * @param propertyResource
     *            true if there can be a PluginProperty
     * @see ResourceCategoryDefinition#hasPropertyResource()
     */
    public void setPropertyResource(boolean propertyResource) {
        this.propertyResource = propertyResource;
    }

    /**
     * Set a fallback plugin resource to be used if {@link #hasPropertyResource()} returns true but
     * the PluginProperty does not exist.
     * 
     * @param propertyResourceFallback
     *            a file path relative to the 'static' directory within the plugin resources
     *            directory
     */
    public void setPropertyResourceFallback(String propertyResourceFallback) {
        this.propertyResourceFallback = propertyResourceFallback;
    }
}
