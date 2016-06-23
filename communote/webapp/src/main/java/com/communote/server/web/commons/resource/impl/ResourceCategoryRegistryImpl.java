package com.communote.server.web.commons.resource.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.Pair;
import com.communote.server.core.osgi.OSGiHelper;
import com.communote.server.web.commons.resource.ConcatenatedResourceStore;
import com.communote.server.web.commons.resource.ConcatenatedResourceStoreException;
import com.communote.server.web.commons.resource.ResourceCategoryDefinition;
import com.communote.server.web.commons.resource.ResourceCategoryRegistry;
import com.communote.server.web.commons.resource.ResourceExtension;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResourceCategoryRegistryImpl implements ResourceCategoryRegistry {
    private static final String CSS_STORE_NAME = "CSS";
    private static final String JS_STORE_NAME = "JavaScript";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ResourceCategoryRegistryImpl.class);

    private final ConcatenatedResourceStore jsConcatenatedResourceStore;

    private final ConcatenatedResourceStore cssConcatenatedResourceStore;

    private final Map<String, List<String>> addedCategories = new HashMap<String, List<String>>();
    private final Map<String, List<String>> extendedCategories = new HashMap<String, List<String>>();

    /**
     * Create a new registry.
     * 
     * @param jsConcatenatedResourceStore
     *            the store that manages the JavaScript resources
     * @param cssConcatenatedResourceStore
     *            the store that manages the CSS resources
     */
    public ResourceCategoryRegistryImpl(ConcatenatedResourceStore jsConcatenatedResourceStore,
            ConcatenatedResourceStore cssConcatenatedResourceStore) {
        this.jsConcatenatedResourceStore = jsConcatenatedResourceStore;
        this.cssConcatenatedResourceStore = cssConcatenatedResourceStore;
    }

    /**
     * Add categories to the file store
     * 
     * @param storeName
     *            a string that uniquely identifies the store
     * @param store
     *            the store to add to
     * @param bundleName
     *            the identifier of the plugin
     * @param basePath
     *            absolute path to the static directory in the resources directory of the plugin
     * @param definitions
     *            the definitions to add, can be null
     * @throws ConcatenatedResourceStoreException
     *             in case adding the definitions failed
     */
    private synchronized void addCategoryDefinitions(String storeName,
            ConcatenatedResourceStore store, String bundleName, String basePath,
            Set<ResourceCategoryDefinition> definitions, boolean ignorePropertyResources)
            throws ConcatenatedResourceStoreException {
        if (definitions != null) {
            String locationPrefix = "/" + bundleName;
            ArrayList<String> categoryNames = new ArrayList<String>();
            addedCategories.put(storeName + " " + bundleName, categoryNames);
            for (ResourceCategoryDefinition definition : definitions) {
                List<Pair<String, String>> pluginResources = prepareResourceLocations(
                        definition.getPluginResources(), locationPrefix, basePath);
                if (ignorePropertyResources || !definition.hasPropertyResource()) {
                    store.addCategory(bundleName, definition.getCategoryName(),
                            definition.getCoreResources(), pluginResources);
                } else {
                    String fallback;
                    if (definition.getPropertyResourceFallback() != null) {
                        fallback = getResourceLocation(definition.getPropertyResourceFallback(),
                                locationPrefix, basePath).getRight();
                    } else {
                        fallback = null;
                    }
                    store.addCategoryWithPropertyResourceSupport(bundleName,
                            definition.getCategoryName(), definition.getCoreResources(),
                            pluginResources, fallback);
                }
                categoryNames.add(definition.getCategoryName());
                LOGGER.debug("Added {} category {} provided by bundle {}", storeName,
                        definition.getCategoryName(), bundleName);
            }
        }
    }

    /**
     * Add category extensions
     * 
     * @param storeName
     *            a string that uniquely identifies the store
     * @param store
     *            the store to add to
     * @param bundleName
     *            the identifier of the plugin
     * @param basePath
     *            absolute path to the static directory in the resources directory of the plugin
     * @param resources
     *            the category to resource mapping, can be null
     * @throws ConcatenatedResourceStoreException
     *             in case adding the resources to one of the categories failed
     */
    private synchronized void addCategoryExtensions(String storeName,
            ConcatenatedResourceStore store, String bundleName, String basePath,
            Map<String, List<String>> resources) throws ConcatenatedResourceStoreException {
        if (resources != null) {
            String locationPrefix = "/" + bundleName;
            ArrayList<String> categoryNames = new ArrayList<String>();
            extendedCategories.put(storeName + " " + bundleName, categoryNames);
            for (String categoryName : resources.keySet()) {
                List<Pair<String, String>> pluginResources = prepareResourceLocations(
                        resources.get(categoryName), locationPrefix, basePath);
                if (pluginResources != null && pluginResources.size() > 0) {
                    store.addToCategory(bundleName, categoryName, pluginResources);
                    categoryNames.add(categoryName);
                    LOGGER.debug("Extended {} category {} with resources provided by bundle {}",
                            storeName, categoryName, bundleName);
                } else {
                    LOGGER.debug(
                            "Ignoring category {} extension of bundle {} since no resources are defined",
                            categoryName, bundleName);
                }
            }
        }
    }

    /**
     * Crate a pair describing a plugin resource. The left part holds the relative location of the
     * resource under which it can be downloaded, the right contains the absolute file path in the
     * bundle storage.
     * 
     * @param pluginResource
     *            the resource of the plugin
     * @param locationPrefix
     *            the prefix of the relative location
     * @param basePath
     *            absolute path to the static directory in the resources directory of the plugin
     * @return a pair describing the plugin resource
     */
    private Pair<String, String> getResourceLocation(String pluginResource, String locationPrefix,
            String basePath) {
        String location;
        String filePath;
        if (pluginResource.startsWith("/")) {
            location = locationPrefix + pluginResource;
            filePath = basePath
                    + pluginResource.substring(1).replace('/', File.separatorChar);
        } else {
            location = locationPrefix + "/" + pluginResource;
            filePath = basePath
                    + pluginResource.replace('/', File.separatorChar);
        }
        return new Pair<String, String>(location, filePath);
    }

    /**
     * Get the base path where the CSS and JavaScript resource files are stored within the bundle
     * storage.
     * 
     * @param bundleSymbolicName
     *            the symbolic name of the bundle for which the base path is to be returned
     * @return the base path
     */
    private String getResourcesBasePath(String bundleSymbolicName) {
        String basePath = OSGiHelper.getBundleStorage(bundleSymbolicName)
                + File.separator + "static" + File.separator;
        return basePath;
    }

    /**
     * Create list of pairs describing plugin resources. The left part of each pair holds the
     * relative location of the resource under which it can be downloaded, the right contains the
     * absolute file path in the bundle storage.
     * 
     * @param resources
     *            the resources of the plugin
     * @param locationPrefix
     *            the prefix of the relative location
     * @param basePath
     *            absolute path to the static directory in the resources directory of the plugin
     * @return list of pairs describing plugin resources
     */
    private List<Pair<String, String>> prepareResourceLocations(List<String> resources,
            String locationPrefix, String basePath) {
        if (resources == null) {
            return null;
        }
        List<Pair<String, String>> convertedResources = new ArrayList<Pair<String, String>>();
        for (String pluginResource : resources) {
            convertedResources.add(getResourceLocation(pluginResource, locationPrefix, basePath));
        }
        return convertedResources;
    }

    @Override
    public void registerCssCategoryExtensions(String bundleName, Map<String, List<String>> resources)
            throws ConcatenatedResourceStoreException {
        this.addCategoryExtensions(CSS_STORE_NAME, cssConcatenatedResourceStore, bundleName,
                getResourcesBasePath(bundleName), resources);
    }

    @Override
    public void registerCssResourceExtension(String bundleName, ResourceExtension extension)
            throws ConcatenatedResourceStoreException {
        String basePath = getResourcesBasePath(bundleName);
        addCategoryDefinitions(CSS_STORE_NAME, cssConcatenatedResourceStore, bundleName, basePath,
                extension.getDefinitions(), false);
        addCategoryExtensions(CSS_STORE_NAME, cssConcatenatedResourceStore, bundleName, basePath,
                extension.getExtensions());
    }

    @Override
    public void registerJsCategoryExtensions(String bundleName,
            Map<String, List<String>> resources) throws ConcatenatedResourceStoreException {
        addCategoryExtensions(JS_STORE_NAME, jsConcatenatedResourceStore, bundleName,
                getResourcesBasePath(bundleName), resources);
    }

    @Override
    public void registerJsResourceExtension(String bundleName, ResourceExtension extension)
            throws ConcatenatedResourceStoreException {
        String basePath = getResourcesBasePath(bundleName);
        // do not allow property resources for JavaScript
        addCategoryDefinitions(JS_STORE_NAME, jsConcatenatedResourceStore, bundleName, basePath,
                extension.getDefinitions(), true);
        addCategoryExtensions(JS_STORE_NAME, jsConcatenatedResourceStore, bundleName, basePath,
                extension.getExtensions());

    }

    /**
     * Remove the categories added by a given bundle.
     * 
     * @param storeName
     *            string that uniquely identifies the store
     * @param store
     *            the store to remove from to
     * @param bundleName
     *            the name of the bundle
     */
    private synchronized void removeCategories(String storeName, ConcatenatedResourceStore store,
            String bundleName) {
        String identifier = storeName + " " + bundleName;
        List<String> categories = addedCategories.remove(identifier);
        if (categories != null) {
            LOGGER.debug("Removing {} categories added by bundle {}", storeName, bundleName);
            for (String categoryName : categories) {
                try {
                    store.removeCategory(bundleName, categoryName);
                } catch (ConcatenatedResourceStoreException e) {
                    LOGGER.error("Removing category {} provided by bundle {} failed", categoryName,
                            bundleName, e);
                }
            }
        }
        categories = extendedCategories.remove(identifier);
        if (categories != null) {
            LOGGER.debug("Removing {} resources added by bundle {} from categories", storeName,
                    bundleName);
            for (String categoryName : categories) {
                try {
                    store.removeFromCategory(bundleName, categoryName);
                } catch (ConcatenatedResourceStoreException e) {
                    LOGGER.error("Removing resources of bundle {} from category {} failed",
                            bundleName, categoryName, e);
                }
            }
        }
    }

    @Override
    public void unregisterCssCategories(String bundleName) {
        removeCategories(CSS_STORE_NAME, cssConcatenatedResourceStore, bundleName);

    }

    @Override
    public void unregisterJsCategories(String bundleName) {
        removeCategories(JS_STORE_NAME, jsConcatenatedResourceStore, bundleName);
    }

}
