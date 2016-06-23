package com.communote.server.web.commons.resource.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.plugin.PluginPropertyManagement;
import com.communote.server.core.plugin.PluginPropertyManagementException;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.web.commons.resource.PropertyResourceContent;

/**
 * Component that manages property resources for resource categories. A property resource is a
 * resource (e.g. set of CSS rules) that can be updated at runtime and is stored as an application
 * property. A property can also have a fallback file that is provided by the plugin that defines
 * the property resource. The fallback will be used when the application property has no value.
 *
 * When several plugins provide a property resource for the same category the property belonging to
 * the plugin that added the property resource as the last will overlay the others.
 *
 * The manager caches the resources as files on disk to avoid database access.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PropertyResourceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyResourceManager.class);

    // mapping from category name to a list of property resources of that category. Only the last
    // property resource in the list will be considered. The descriptors only contain minimal info
    // which are identifier of the providing plugin and whether a fallback exists.
    private final Map<String, List<PropertyResourceDescriptor>> categoryPropertyResources;
    // mapping from category name to the last added property resource. This is the active property
    // resource and the descriptor holds all details.
    private final Map<String, PropertyResourceDescriptor> currentCategoryPropertyResource;
    private final String keyPrefix;
    private final String minimizedSuffix;
    private File cacheDir;
    private CachedFileCleaner cachedFileCleaner;

    /**
     * Create a new manager
     *
     * @param keyPrefix
     *            the prefix to use when creating the application property key. The category name
     *            will be appended to that prefix.
     * @param minimizedSuffix
     *            the filename suffix that marks a resource as the minimized version
     */
    public PropertyResourceManager(String keyPrefix, String minimizedSuffix) {
        this.keyPrefix = keyPrefix;
        this.minimizedSuffix = minimizedSuffix;
        categoryPropertyResources = new HashMap<>();
        currentCategoryPropertyResource = new ConcurrentHashMap<>();
    }

    /**
     * Add a property resource for the given category and plugin. If another plugin has already
     * added a property resource for this category the new one will overlay it. This means that a
     * request for the property resource for the category only the application property and fallback
     * of this plugin will be considered until this property resource is removed again (via
     * {@link #removePropertyResource(String, String)}).
     *
     * @param identifier
     *            the identifier of the plugin
     * @param categoryName
     *            the name of the category
     * @param propertyResourceFallback
     *            absolute path to the location of the fallback resource file that should be
     *            rendered when the property is not set. If null there will be no fallback.
     * @return true if the current property resource for that category changed and the concatenated
     *         file needs to be re-created
     * @throws PropertyResourceManagerException
     *             in case the property resource could not be added
     */
    public synchronized boolean addPropertyResource(String identifier, String categoryName,
            String propertyResourceFallback) throws PropertyResourceManagerException {
        List<PropertyResourceDescriptor> propertyResources = this.categoryPropertyResources
                .get(categoryName);
        if (propertyResources != null) {
            if (getExistingPropertyResource(identifier, propertyResources) != null) {
                LOGGER.warn(
                        "The plugin {} already added a property resource for the category {}, ignored.",
                        identifier, categoryName);
                return false;
            }
        } else {
            propertyResources = new ArrayList<>();
            this.categoryPropertyResources.put(categoryName, propertyResources);
        }
        PropertyResourceDescriptor newPropertyResource = createResourceDescriptor(identifier,
                propertyResourceFallback);
        boolean currentPropertyResourceChanged = this.updateCurrentPropertyResource(categoryName,
                newPropertyResource);
        // everything went well -> save descriptor
        propertyResources.add(newPropertyResource);
        LOGGER.debug("Added property resource of plugin {} for category {}", identifier,
                categoryName);
        return currentPropertyResourceChanged;
    }

    /**
     * Throw an exception if the current client is not the global client.
     *
     * @param identifier
     *            plugin symbolic name, for logging
     * @param categoryName
     *            name of the category, for logging
     * @throws PropertyResourceManagerException
     *             in case the current client is not global
     */
    private void assertGlobalClient(String identifier, String categoryName)
            throws PropertyResourceManagerException {
        if (!ClientHelper.isCurrentClientGlobal()) {
            LOGGER.error("Cannot update property resource application plugin property of bundle "
                    + identifier + " for category " + categoryName + " from client "
                    + ClientHelper.getCurrentClientId());
            throw new PropertyResourceManagerException("Cannot update application property.", false);
        }
    }

    /**
     * Copy source file to target file
     *
     * @param sourceFile
     *            the source file
     * @param targetFilename
     *            the name of the target file that will be created in the cache directory
     * @throws IOException
     *             in case copying failed
     */
    private void copyToFile(File sourceFile, String targetFilename) throws IOException {
        FileUtils.copyFile(sourceFile, new File(cacheDir, targetFilename));
    }

    /**
     * Copy source file to target file
     *
     * @param sourceAbsoluteFilePath
     *            absolute path to source file
     * @param targetFilename
     *            the name of the target file that will be created in the cache directory
     * @throws IOException
     *             in case copying failed
     */
    private void copyToFile(String sourceAbsoluteFilePath, String targetFilename)
            throws IOException {
        copyToFile(new File(sourceAbsoluteFilePath), targetFilename);
    }

    /**
     * Cache the property resource for the category on disk. If no value is provided and a fallback
     * exists the fallback will copied to the cache.
     *
     * @param categoryName
     *            the name of the category
     * @param newPropertyResource
     *            the property resource for which the file should be cached
     * @param propertyValue
     *            the value of the property to store in the file, can be null
     * @param currentResource
     *            the current property resource that will be replaced or updated. Can be null if
     *            there is no property resource.
     * @return the descriptor containing the details of the cached files. Will be null if there was
     *         no value and no fallback.
     * @throws IOException
     *             in case storing the property value or copying the fallback failed
     */
    private CachedFileDescriptor createCachedFile(String categoryName,
            PropertyResourceDescriptor newPropertyResource, String propertyValue,
            PropertyResourceDescriptor currentResource) throws IOException {
        String filename = null;
        String minFilename = null;
        long timestamp = 0L;
        try {
            if (propertyValue != null) {
                // save property value in file
                filename = createCacheFileName(categoryName, false, currentResource);
                writeToFile(propertyValue, filename);
                timestamp = System.currentTimeMillis();
            } else {
                // store content of fallback in cached file
                if (newPropertyResource.hasFallback()) {
                    filename = createCacheFileName(categoryName, false, currentResource);
                    File fallbackFile = new File(newPropertyResource.getFallbackAbsolutePath());
                    copyToFile(fallbackFile, filename);
                    timestamp = fallbackFile.lastModified();
                    if (newPropertyResource.isFallbackHasMinimized()) {
                        minFilename = createCacheFileName(categoryName, true, currentResource);
                        copyToFile(
                                ConcatenatedResourceStoreImpl.getMinimizedResourceName(
                                        newPropertyResource.getFallbackAbsolutePath(),
                                        this.minimizedSuffix), minFilename);
                    }
                }
            }
            if (filename != null) {
                return new CachedFileDescriptor(filename, minFilename, timestamp);
            } else {
                return null;
            }
        } catch (IOException e) {
            LOGGER.error("Caching property resource on disk failed", e);
            // cleanup
            this.cachedFileCleaner.delete(filename);
            this.cachedFileCleaner.delete(minFilename);
            throw e;
        }
    }

    /**
     * Create a unique name for caching the content of a property resource on disk.
     *
     * @param categoryName
     *            the name of the category for which a property resource should be cached
     * @param min
     *            whether the minimized version of the file should be cached
     * @param currentPropertyResource
     *            the current property resource that will be replaced or updated. Can be null if
     *            there is no property resource.
     * @return the file name
     */
    private String createCacheFileName(String categoryName, boolean min,
            PropertyResourceDescriptor currentPropertyResource) {
        StringBuilder builder = new StringBuilder("propertyResource_");
        builder.append(categoryName);
        CachedFileDescriptor descriptor = null;
        if (currentPropertyResource != null) {
            descriptor = currentPropertyResource.getCachedFile();
        }
        String currentFileName = null;
        if (min) {
            builder.append("-min");
            currentFileName = descriptor != null ? descriptor.getMinFileName() : null;
        } else {
            currentFileName = descriptor != null ? descriptor.getFileName() : null;
        }
        builder.append("_");
        // make the name unique and avoid overwrite existing as it might currently be streamed to a
        // client
        builder.append(System.currentTimeMillis());
        String finalName = builder.toString();
        // in case this method was called twice within same millisecond make it unique
        if (finalName.equals(currentFileName)) {
            finalName += "_1";
        }
        return finalName;
    }

    /**
     * Create the key of the application property
     *
     * @param categoryName
     *            the name of the category for which the key should be created
     * @return the key
     */
    private String createPropertyKey(String categoryName) {
        return this.keyPrefix + "." + categoryName;
    }

    /**
     * Create a property resource descriptor. Will check whether the fallback file and a minimized
     * version of it exists.
     *
     * @param pluginIdentifier
     *            identifier of the plugin
     * @param propertyResourceFallbackPath
     *            absolute path to the fallback, will be null in descriptor if null or not
     *            existing/readable
     * @return the descriptor
     */
    private PropertyResourceDescriptor createResourceDescriptor(String pluginIdentifier,
            String propertyResourceFallbackPath) {
        if (propertyResourceFallbackPath == null) {
            return new PropertyResourceDescriptor(pluginIdentifier, propertyResourceFallbackPath,
                    false);
        }
        File resourceFile = new File(propertyResourceFallbackPath);
        if (resourceFile.isFile()) {
            if (resourceFile.canRead()) {
                boolean hasMinimized = ConcatenatedResourceStoreImpl.getMinimizedFile(
                        propertyResourceFallbackPath, this.minimizedSuffix) != null;
                return new PropertyResourceDescriptor(pluginIdentifier,
                        propertyResourceFallbackPath, hasMinimized);
            } else {
                LOGGER.warn("Fallback resource file {} cannot be read",
                        propertyResourceFallbackPath);
            }
        } else {
            LOGGER.warn("Fallback resource file {} does not exist", propertyResourceFallbackPath);
        }
        return null;
    }

    /**
     * Get property resource for the given plugin.
     *
     * Should be run from synchronized context.
     *
     * @param identifier
     *            the plugin identifier
     * @param resources
     *            the property resources to search
     * @return the found resource or null
     */
    private PropertyResourceDescriptor getExistingPropertyResource(String identifier,
            List<PropertyResourceDescriptor> resources) {
        for (PropertyResourceDescriptor propertyResource : resources) {
            if (propertyResource.getPluginIdentifier().equals(identifier)) {
                return propertyResource;
            }
        }
        return null;
    }

    /**
     * Get the current property resource for the given category name
     *
     * @param categoryName
     *            the name of the category
     * @return the cached property resource or null if there is none for the given category
     */
    public CachedFileDescriptor getPropertyResource(String categoryName) {
        PropertyResourceDescriptor propertyResource = this.currentCategoryPropertyResource
                .get(categoryName);
        if (propertyResource != null) {
            return propertyResource.getCachedFile();
        }
        return null;
    }

    /**
     * The content of the property resource.
     *
     * Note: when needing the current property resource {@link #getPropertyResource(String)} should
     * be used
     *
     * @param identifier
     *            the plugin bundle name
     * @param categoryName
     *            name of the category whose resource content should be updated. The category name
     *            is used for the property key
     * @return the content or null if the plugin did not define a property resource for the
     *         category. The value of the content might be null.
     * @throws PropertyResourceManagerException
     *             in case reading the content failed
     */
    public synchronized PropertyResourceContent getPropertyResourceContent(String identifier,
            String categoryName) throws PropertyResourceManagerException {
        // if the requested property resource is the current, get content directly from cached file
        PropertyResourceDescriptor currentPropertyResource = currentCategoryPropertyResource
                .get(categoryName);
        if (currentPropertyResource != null
                && currentPropertyResource.getPluginIdentifier().equals(identifier)) {
            String content = null;
            boolean loadedFromFallback = false;
            if (currentPropertyResource.getCachedFile() != null
                    && currentPropertyResource.getCachedFile().getFileName() != null) {
                content = readFromFile(new File(cacheDir, currentPropertyResource.getCachedFile()
                        .getFileName()), identifier, categoryName);
                loadedFromFallback = !currentPropertyResource.hasPropertyValue();
            }
            return new PropertyResourceContent(content, loadedFromFallback);
        }
        // check if there is a property resource for that category and if get value from DB or
        // fallback
        List<PropertyResourceDescriptor> resources = this.categoryPropertyResources
                .get(categoryName);
        if (resources != null) {
            PropertyResourceDescriptor propertyResource = getExistingPropertyResource(identifier,
                    resources);
            if (propertyResource != null) {
                String content = loadPropertyValue(identifier, categoryName);
                boolean loadedFromFallback = false;
                if (content == null && propertyResource.hasFallback()) {
                    content = readFromFile(new File(propertyResource.getFallbackAbsolutePath()),
                            identifier, categoryName);
                    loadedFromFallback = true;
                }
                return new PropertyResourceContent(content, loadedFromFallback);
            }
        }
        return null;
    }

    /**
     * Init the manager
     *
     * @param cacheDir
     *            the directory where the property resource will be cached
     */
    public void init(File cacheDir) {
        this.cacheDir = cacheDir;
        this.cachedFileCleaner = new CachedFileCleaner(cacheDir);
    }

    /**
     * Load the application property containing the property resource content.
     *
     * @param pluginIdentifier
     *            the bundle name of the plugin
     * @param categoryName
     *            the name of the category whose resource should be loaded
     * @return the value or null if the application property does not exist
     * @throws PropertyResourceManagerException
     *             in case loading the property failed
     */
    private String loadPropertyValue(String pluginIdentifier, String categoryName)
            throws PropertyResourceManagerException {
        PluginPropertyManagement propertyManagement = ServiceLocator
                .findService(PluginPropertyManagement.class);
        try {
            return propertyManagement.getApplicationPropertyUncached(pluginIdentifier,
                    createPropertyKey(categoryName));
        } catch (PluginPropertyManagementException e) {
            LOGGER.error("Getting the property resource application property failed for bundle "
                    + pluginIdentifier + " and category " + categoryName, e);
            throw new PropertyResourceManagerException("Loading property resource value failed", e,
                    false);
        }
    }

    /**
     * Load content of a file
     *
     * @param file
     *            the file to load
     * @param identifier
     *            bundle name of the plugin for which the content is loaded, only for logging
     * @param categoryName
     *            name of the category for which the content is loaded, only for logging
     * @return the content of the file
     * @throws PropertyResourceManagerException
     *             in case reading failed
     */
    private String readFromFile(File file, String identifier, String categoryName)
            throws PropertyResourceManagerException {
        try {
            return FileUtils.readFileToString(file, Charset.forName("UTF-8"));
        } catch (IOException e) {
            LOGGER.error("Reading property resource of plugin " + identifier + " for category "
                    + categoryName + " content failed", e);
            throw new PropertyResourceManagerException("Reading property resource content failed",
                    e, false);
        }
    }

    /**
     * Remove a property resource added by plugin for the given category. This method should be
     * called if the plugin is removed. If the property resource is the current property resource
     * for that category the current property resource is updated by the property resource of the
     * plugin that added the resource before this plugin. If there is no such resource the property
     * resource is removed.
     *
     * The stored application property is not removed.
     *
     * @param identifier
     *            the identifier of the plugin
     * @param categoryName
     *            the name of the category
     * @return if the current property resource changed and thus the concatenated file needs to
     *         re-built
     */
    public synchronized boolean removePropertyResource(String identifier, String categoryName) {
        List<PropertyResourceDescriptor> resources = categoryPropertyResources.get(categoryName);
        if (resources == null) {
            return false;
        }
        Iterator<PropertyResourceDescriptor> iter = resources.iterator();
        while (iter.hasNext()) {
            if (iter.next().getPluginIdentifier().equals(identifier)) {
                iter.remove();
                break;
            }
        }
        PropertyResourceDescriptor currentResource = currentCategoryPropertyResource
                .get(categoryName);
        boolean changed = false;
        if (currentResource != null && currentResource.getPluginIdentifier().equals(identifier)) {
            // replace the current with last from the list
            int idx = resources.size() - 1;
            if (idx >= 0) {
                PropertyResourceDescriptor newResource = resources.get(idx);
                try {
                    return updateCurrentPropertyResource(categoryName, newResource);
                } catch (PropertyResourceManagerException e) {
                    // property resource with given identifier already removed from list,
                    // but adding new one failed. Replace current with new one but without cached
                    // file
                    currentCategoryPropertyResource.put(
                            categoryName,
                            new PropertyResourceDescriptor(newResource.getPluginIdentifier(),
                                    newResource.getFallbackAbsolutePath(), newResource
                                            .isFallbackHasMinimized()));
                    LOGGER.error("Failed restoring previous property resource (category: "
                            + categoryName + ") of bundle " + newResource.getPluginIdentifier()
                            + " while removing that of bundle " + identifier, e);
                    // cleanup cached file of current
                    cachedFileCleaner.delete(currentResource.getCachedFile());
                    changed = true;
                }
            } else {
                // no resource left, cleanup
                currentCategoryPropertyResource.remove(categoryName);
                cachedFileCleaner.delete(currentResource.getCachedFile());
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Update the application property holding the resource content for the given bundle
     *
     * @param identifier
     *            the plugin bundle name
     * @param categoryName
     *            name of the category whose resource content should be updated. The category name
     *            is used for the property key
     * @param propertyValue
     *            the new value to set. If null, the property is removed
     * @throws PropertyResourceManagerException
     *             in case the current client is not global or the property exists but is not an
     *             application property
     */
    private void updateApplicationProperty(String identifier, String categoryName,
            String propertyValue) throws PropertyResourceManagerException {
        assertGlobalClient(identifier, categoryName);
        PluginPropertyManagement propertyManagement = ServiceLocator
                .findService(PluginPropertyManagement.class);
        String propertyKey = createPropertyKey(categoryName);
        try {
            propertyManagement.setApplicationProperty(identifier, propertyKey, propertyValue);
        } catch (IllegalArgumentException | AuthorizationException e) {
            // thrown if existing property is not an application property
            LOGGER.error("Unexpected exeption updating property {} of bundle {}", propertyKey,
                    identifier, e);
            throw new PropertyResourceManagerException(
                    "Unexpected exception updating application property", e, false);
        }
    }

    /**
     * Update the current property resource for the given category with the provided one. The value
     * of the application property will be loaded from the database and the cached files are created
     * from that value or fallback if the application property does not exist.
     *
     * Must be run from a synchronized context.
     *
     * @param categoryName
     *            the name of the category
     * @param newResource
     *            the new property resource to set
     * @return true if the property actually changed. Will for instance be false if the previous and
     *         new resource have both no value and no fallback.
     * @throws PropertyResourceManagerException
     *             in case updating failed
     */
    private boolean updateCurrentPropertyResource(String categoryName,
            PropertyResourceDescriptor newResource) throws PropertyResourceManagerException {
        String propertyValue = loadPropertyValue(newResource.getPluginIdentifier(), categoryName);
        return updateCurrentPropertyResource(categoryName, newResource, propertyValue);
    }

    /**
     * Update the current property resource for the given category with the provided one. The cached
     * files are created from the provided value or fallback.
     *
     * Must be run from a synchronized context.
     *
     * @param categoryName
     *            the name of the category
     * @param newResource
     *            the new property resource to set
     * @param propertyValue
     *            the new value to set. If null the fallback will be used.
     * @return true if the property actually changed. Will for instance be false if the previous and
     *         new resource have both no value and no fallback.
     * @throws PropertyResourceManagerException
     *             in case updating failed
     */
    private boolean updateCurrentPropertyResource(String categoryName,
            PropertyResourceDescriptor newResource, String propertyValue)
                    throws PropertyResourceManagerException {
        PropertyResourceDescriptor currentResource = currentCategoryPropertyResource
                .get(categoryName);
        if (propertyValue == null && currentResource != null && !currentResource.hasPropertyValue()
                && currentResource.getPluginIdentifier().equals(newResource.getPluginIdentifier())) {
            // nothing changed, no need to update
            return false;
        }
        boolean hadCachedFile = currentResource != null && currentResource.getCachedFile() != null;
        try {
            // create a new resource descriptor and replace the old one
            CachedFileDescriptor newCachedFile = createCachedFile(categoryName, newResource,
                    propertyValue, currentResource);
            currentCategoryPropertyResource.put(categoryName, new PropertyResourceDescriptor(
                    newResource.getPluginIdentifier(), newResource.getFallbackAbsolutePath(),
                    newResource.isFallbackHasMinimized(), newCachedFile, propertyValue != null));
            if (hadCachedFile) {
                this.cachedFileCleaner.delete(currentResource.getCachedFile());
            } else if (newCachedFile == null) {
                // nothing changed: had no cached file and new property resource didn't create one
                // either (because had no property value and no fallback)
                return false;
            }
            return true;
        } catch (IOException e) {
            throw new PropertyResourceManagerException(
                    "Updating current property resource failed for category " + categoryName
                    + " and bundle " + newResource.getPluginIdentifier(), e, false);
        }
    }

    /**
     * Update the property resource for the given category and plugin. The application property of
     * the plugin is updated or removed and if the property resource is the current one, it will be
     * updated.
     *
     * @param identifier
     *            the identifier of the plugin
     * @param categoryName
     *            the name of the category
     * @param propertyValue
     *            the new value. Can be null to clear the application property and use the fallback
     *            of the plugin.
     * @return whether the current property resource changed and thus the concatenated file for the
     *         category needs to be re-created
     * @throws PropertyResourceManagerException
     *             in case the category or plugin is not known or the update failed
     */
    public synchronized boolean updatePropertyResource(String identifier, String categoryName,
            String propertyValue) throws PropertyResourceManagerException {
        PropertyResourceDescriptor propertyResource = currentCategoryPropertyResource
                .get(categoryName);
        boolean updatingCurrent;
        if (propertyResource == null || !propertyResource.getPluginIdentifier().equals(identifier)) {
            updatingCurrent = false;
            List<PropertyResourceDescriptor> resources = this.categoryPropertyResources
                    .get(categoryName);
            if (resources == null) {
                throw new PropertyResourceManagerException(
                        "Cannot update property resource of unknown category " + categoryName,
                        false);
            }
            propertyResource = getExistingPropertyResource(identifier, resources);
            if (propertyResource == null) {
                throw new PropertyResourceManagerException(
                        "Cannot update property resource for unknown plugin " + identifier, false);
            }
        } else {
            updatingCurrent = true;
        }
        // update the application property but don't update the cached file if it is not the
        // current resource property, otherwise also update the cached file
        updateApplicationProperty(identifier, categoryName, propertyValue);
        if (updatingCurrent) {
            try {
                return updateCurrentPropertyResource(categoryName, propertyResource, propertyValue);
            } catch (PropertyResourceManagerException e) {
                // updating the cached file of the current resource failed, thus, it might contain
                // stale data -> remove property resource by setting empty cache
                this.currentCategoryPropertyResource.put(
                        categoryName,
                        new PropertyResourceDescriptor(propertyResource.getPluginIdentifier(),
                                propertyResource.getFallbackAbsolutePath(), propertyResource
                                .isFallbackHasMinimized()));
                // clear cached files
                this.cachedFileCleaner.delete(propertyResource.getCachedFile());
                // since we changed the category mark it accordingly before re-throwing
                e.setCurrentPropertyResourceChanged(true);
                throw e;
            }
        }
        return false;
    }

    /**
     * Write the given string to the provided file in UTF-8 encoding
     *
     * @param content
     *            the content to write
     * @param targetFilename
     *            the file to write to
     * @throws IOException
     *             in case writing failed
     */
    private void writeToFile(String content, String targetFilename) throws IOException {
        FileUtils.write(new File(cacheDir, targetFilename), content, Charset.forName("UTF-8"));
    }
}
