package com.communote.server.web.commons.resource.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.io.FileHelper;
import com.communote.common.util.Pair;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ConfigurationInitializationException;
import com.communote.server.web.commons.resource.ConcatenatedResourceStore;
import com.communote.server.web.commons.resource.ConcatenatedResourceStoreException;
import com.communote.server.web.commons.resource.PropertyResourceContent;

/**
 * Default implementation of the ConcatenatedResourceStore
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConcatenatedResourceStoreImpl implements ConcatenatedResourceStore {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(ConcatenatedResourceStoreImpl.class);

    /**
     * Get the minimized version of a given file if it exists. The minimized version of a file has
     * the minimized file name suffix passed to the constructor.
     *
     * @param absoluteFilePath
     *            the absolute file name of the file
     * @param minimizedSuffix
     *            filename suffix that marks a file as the minimized version of the file with the
     *            same name but without the suffix
     * @return the minimized file or null if it does not exist or is not readable
     */
    protected static File getMinimizedFile(String absoluteFilePath, String minimizedSuffix) {
        String minimizedFilePath = getMinimizedResourceName(absoluteFilePath, minimizedSuffix);
        File minimizedFile = new File(minimizedFilePath);
        if (minimizedFile.isFile() && minimizedFile.canRead()) {
            return minimizedFile;
        }
        LOGGER.debug("Minimized file {} does not exist or is not readable", minimizedFilePath);
        return null;
    }

    /**
     * Return the minimized name of a resource by injecting the marker suffix.
     *
     * @param resourceName
     *            the resource name to process
     * @param minimizedSuffix
     *            filename suffix that marks a file as the minimized version of the file with the
     *            same name but without the suffix
     * @return the name of the minimized version of a resource
     */
    public static String getMinimizedResourceName(String resourceName, String minimizedSuffix) {
        int idx = resourceName.lastIndexOf('.');
        return resourceName.substring(0, idx) + minimizedSuffix + resourceName.substring(idx);
    }

    private final String contentType;
    private final String fileExtension;
    private String cacheSubdirName;
    private final String minimizedSuffix;
    private final Map<String, CachedFileDescriptor> concatenatedFiles;

    private CachedFileCleaner cachedFileCleaner;
    private File cacheDir;
    private final Map<String, List<ResourceDescriptor>> builtInCategoryResources;
    // mapping from category name to a list of identifiers created from category name and extension
    // identifier of the extensions that added the category
    private final Map<String, List<String>> addedCategories;
    // mapping from an identifier created from category name and extension identifier to the
    // resources of the category defined by the extension
    private final Map<String, List<ResourceDescriptor>> addedCategoryResources;
    // mapping from category name to a list of identifiers created from category name and extension
    // identifier of extensions that added resources to the category
    private final Map<String, List<String>> extendedCategories;
    private final PropertyResourceManager propertyResourceManager;

    // mapping from an identifier created from category name and extension identifier to the
    // resources added to the category by the extension
    private final Map<String, List<ResourceDescriptor>> extendedCategoryResources;

    private ResourceConcatenator resourceConcatenator;

    /**
     * <p>
     * Create a new file store to handle a specific type of files and initialize it with default
     * categories and some initial resources per category. The initial resources can be overlayed or
     * extended by plugins via calls to {@link #addCategory(String, String, List, List)} and
     * {@link #addToCategory(String, String, List)} respectively. Property resources are not
     * supported.
     * </p>
     * <p>
     * The filename suffix marking a file as minimized version is set to "-min".
     * </p>
     *
     * @param builtInResources
     *            mapping from category names to a collection of initial resources of that category.
     *            Each entry of a collection refers to the location of a resource delivered with the
     *            application under which it can be downloaded. The category names represent the
     *            default categories.
     * @param contentType
     *            the content/MIME type of the files managed by the store
     * @param fileExtension
     *            the extension that all files managed by the store need to have
     */
    public ConcatenatedResourceStoreImpl(Map<String, List<String>> builtInResources,
            String contentType, String fileExtension) {
        this(builtInResources, contentType, fileExtension, "-min");
    }

    /**
     * Create a new file store to handle a specific type of files and initialize it with default
     * categories and some initial resources per category. The initial resources can be overlayed or
     * extended by plugins via calls to {@link #addCategory(String, String, List, List)} and
     * {@link #addToCategory(String, String, List)} respectively. Property resources are not
     * supported.
     *
     * @param builtInResources
     *            mapping from category names to a collection of initial resources of that category.
     *            Each entry of a collection refers to the location of a resource delivered with the
     *            application under which it can be downloaded. The category names represent the
     *            default categories.
     * @param contentType
     *            the content/MIME type of the files managed by the store
     * @param fileExtension
     *            the extension that all files managed by the store need to have
     * @param minimizedSuffix
     *            filename suffix that marks a file as the minimized version of the file with the
     *            same name but without the suffix
     */
    public ConcatenatedResourceStoreImpl(Map<String, List<String>> builtInResources,
            String contentType, String fileExtension, String minimizedSuffix) {
        this(builtInResources, contentType, fileExtension, minimizedSuffix, null);
    }

    /**
     * Create a new file store to handle a specific type of files and initialize it with default
     * categories and some initial resources per category. The initial resources can be overlayed or
     * extended by plugins via calls to {@link #addCategory(String, String, List, List)} and
     * {@link #addToCategory(String, String, List)} respectively.
     *
     * @param builtInResources
     *            mapping from category names to a collection of initial resources of that category.
     *            Each entry of a collection refers to the location of a resource delivered with the
     *            application under which it can be downloaded. The category names represent the
     *            default categories.
     * @param contentType
     *            the content/MIME type of the files managed by the store
     * @param fileExtension
     *            the extension that all files managed by the store need to have
     * @param minimizedSuffix
     *            filename suffix that marks a file as the minimized version of the file with the
     *            same name but without the suffix
     * @param propertyResourceName
     *            identifier for property resources. This identifier is used as prefix for the
     *            property key when storing or retrieving from database. If omitted
     *            PropertyResources are not supported and an exception will be thrown when trying to
     *            add or update a property resource.
     */
    public ConcatenatedResourceStoreImpl(Map<String, List<String>> builtInResources,
            String contentType, String fileExtension, String minimizedSuffix,
            String propertyResourceName) {
        this.contentType = contentType;
        if (fileExtension.startsWith(".")) {
            this.fileExtension = fileExtension;
        } else {
            this.fileExtension = "." + fileExtension;
        }
        this.minimizedSuffix = minimizedSuffix;
        builtInCategoryResources = new HashMap<String, List<ResourceDescriptor>>();
        addedCategories = new ConcurrentHashMap<String, List<String>>();
        addedCategoryResources = new ConcurrentHashMap<String, List<ResourceDescriptor>>();
        extendedCategories = new ConcurrentHashMap<String, List<String>>();
        extendedCategoryResources = new ConcurrentHashMap<String, List<ResourceDescriptor>>();
        concatenatedFiles = new ConcurrentHashMap<>();
        // create default concatenator
        resourceConcatenator = new ResourceConcatenator();
        // store the built-in resources
        for (String categoryName : builtInResources.keySet()) {
            ArrayList<ResourceDescriptor> descriptors = new ArrayList<ResourceDescriptor>();
            resolveCoreResources(builtInResources.get(categoryName), descriptors);
            builtInCategoryResources.put(categoryName, descriptors);
        }
        if (propertyResourceName != null) {
            this.propertyResourceManager = new PropertyResourceManager(propertyResourceName,
                    this.minimizedSuffix);
        } else {
            this.propertyResourceManager = null;
        }
    }

    @Override
    public synchronized void addCategory(String identifier, String categoryName,
            List<String> coreResources, List<Pair<String, String>> extensionResources)
                    throws ConcatenatedResourceStoreException {
        identifier = normalizePluginIdentifier(identifier);
        if (internalAddCategory(identifier, categoryName, coreResources, extensionResources)) {
            updateConcatenatedFiles(categoryName);
            // TODO disable current property resource in case this is an overlay for a category with
            // property resource support?
        }
    }

    @Override
    public synchronized void addCategoryWithPropertyResourceSupport(String identifier,
            String categoryName, List<String> coreResources,
            List<Pair<String, String>> extensionResources, String propertyResourceFallback)
                    throws ConcatenatedResourceStoreException {
        if (this.propertyResourceManager == null) {
            throw new ConcatenatedResourceStoreException(
                    "Property resources are not supported by this resource store");
        }
        String normalizedIdentifier = normalizePluginIdentifier(identifier);
        if (internalAddCategory(normalizedIdentifier, categoryName, coreResources,
                extensionResources)) {
            // add property resource
            try {
                propertyResourceManager.addPropertyResource(identifier, categoryName,
                        propertyResourceFallback);
            } catch (PropertyResourceManagerException e) {
                // TODO better not ignore but cleanup added category and throw exception?
                LOGGER.warn(
                        "Adding property resource for plugin {} and category {} failed, property resource ignored",
                        e);
            }
            updateConcatenatedFiles(categoryName);
        }
    }

    /**
     * Create a resource descriptor for a resource and add it to the collection of descriptors if
     * the resource exists on disk.
     *
     * @param resourceLocation
     *            the relative location of the resource under which it can be downloaded
     * @param absolutePath
     *            the absolute location of the file on disk. If that file does not exist or cannot
     *            be read no descriptor will be created.
     * @param isCoreResource
     *            whether the resource is a core resource or a resource of an extension/plugin
     * @param descriptors
     *            the collection to add the descriptor to
     */
    private void addResourceIfExists(String resourceLocation, String absolutePath,
            boolean isCoreResource, List<ResourceDescriptor> descriptors) {
        File resourceFile = new File(absolutePath);
        if (resourceFile.isFile()) {
            if (resourceFile.canRead()) {
                boolean hasMinimized = getMinimizedFile(absolutePath, this.minimizedSuffix) != null;
                descriptors.add(new ResourceDescriptor(resourceLocation, absolutePath,
                        hasMinimized, isCoreResource));
            } else {
                LOGGER.warn("Resource file {} cannot be read", absolutePath);
            }
        } else {
            LOGGER.warn("Resource file {} does not exist", absolutePath);
        }
    }

    @Override
    public synchronized void addToCategory(String identifier, String categoryName,
            List<Pair<String, String>> resources) throws ConcatenatedResourceStoreException {
        identifier = normalizePluginIdentifier(identifier);
        List<String> resourcesIdentifiers = extendedCategories.get(categoryName);
        if (resourcesIdentifiers == null) {
            resourcesIdentifiers = new ArrayList<String>();
            extendedCategories.put(categoryName, resourcesIdentifiers);
        }
        String resourcesId = createResourcesIdentifier(identifier, categoryName);
        List<ResourceDescriptor> descriptors = new ArrayList<ResourceDescriptor>();
        // add existing
        if (this.extendedCategoryResources.get(resourcesId) != null) {
            descriptors.addAll(this.extendedCategoryResources.get(resourcesId));
        }
        resolvePluginResources(resources, descriptors);
        this.extendedCategoryResources.put(resourcesId, descriptors);
        if (!resourcesIdentifiers.contains(resourcesId)) {
            resourcesIdentifiers.add(resourcesId);
        }
        updateConcatenatedFiles(categoryName);
    }

    /**
     * Append the resource descriptors of the resources that were added to the category.
     *
     * @param categoryName
     *            the category for which the the added resources should be returned
     * @param descriptors
     *            the collection to append to
     */
    private void appendExtendedResources(String categoryName, List<ResourceDescriptor> descriptors) {
        List<String> resourcesIds = extendedCategories.get(categoryName);
        if (resourcesIds != null) {
            for (String resourcesId : resourcesIds) {
                List<ResourceDescriptor> additionalDescriptors = extendedCategoryResources
                        .get(resourcesId);
                if (additionalDescriptors != null) {
                    descriptors.addAll(additionalDescriptors);
                }
            }
        }
    }

    /**
     * Create a unique filename for storing the concatenated resources of a category.
     *
     * @param categoryName
     *            the category
     * @param min
     *            true if a name for the minimized file should be created, false otherwise
     * @return the filename
     */
    private String createConcatenatedFileName(String categoryName, boolean min) {
        StringBuilder builder = new StringBuilder(categoryName);
        CachedFileDescriptor descriptor = this.concatenatedFiles.get(categoryName);
        String currentFileName;
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
     * Create an identifier which uniquely identifies the resources of a category that were added by
     * an extension
     *
     * @param normalizedExtensionIdentifier
     *            the normalized version of the identifier of the extension as created by
     *            {@link #normalizePluginIdentifier(String)}
     * @param categoryName
     *            the name of the category
     * @return the identifier of the resources
     */
    private String createResourcesIdentifier(String normalizedExtensionIdentifier,
            String categoryName) {
        return categoryName + " " + normalizedExtensionIdentifier;
    }

    /**
     * Extract the resource locations from a collection of descriptors.
     *
     * @param descriptors
     *            the descriptors to process
     * @param minimized
     *            whether to get the minimized version
     * @param timestamped
     *            if true the last modification timestamp of the file will be append to the location
     *            in the form of a request parameter with name 't'.
     * @return the resource locations
     */
    private List<String> extractResourceLocations(List<ResourceDescriptor> descriptors,
            boolean minimized, boolean timestamped) {
        List<String> resourceLocations = new ArrayList<String>();
        for (ResourceDescriptor descriptor : descriptors) {
            String resourceLocation;
            if (!minimized || !descriptor.hasMinimizedVersion()) {
                // fallback to unminimized if not available
                resourceLocation = descriptor.getResourceLocation();
            } else {
                resourceLocation = getMinimizedResourceName(descriptor.getResourceLocation(),
                        this.minimizedSuffix);
            }
            if (timestamped) {
                resourceLocation += "?t="
                        + (new File(descriptor.getAbsoluteFilePath())).lastModified();
            }
            resourceLocations.add(resourceLocation);
        }
        return resourceLocations;
    }

    @Override
    public File getConcatenatedFile(String categoryName, boolean minimized) {
        CachedFileDescriptor descriptor = concatenatedFiles.get(categoryName);
        if (descriptor != null) {
            return new File(cacheDir, minimized ? descriptor.getMinFileName()
                    : descriptor.getFileName());
        }
        return null;
    }

    @Override
    public long getConcatenatedFileLastModified(String categoryName) {
        CachedFileDescriptor descriptor = concatenatedFiles.get(categoryName);
        if (descriptor != null) {
            return descriptor.getTimestamp();
        }
        return -1L;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public List<String> getCoreResources(String categoryName, boolean minimized, boolean timestamped) {
        List<ResourceDescriptor> descriptors = getInitialResources(categoryName, true, false);
        if (descriptors == null) {
            return null;
        }
        return extractResourceLocations(descriptors, minimized, timestamped);
    }

    /**
     * Get the initial resources of a category. This will first check if there is an overlay for the
     * category in addedCategories and if there is return the resources of the newest overlay. If
     * there is no overlay the built-in resources are checked.
     *
     * @param categoryName
     *            the name of the category
     * @param addCoreResources
     *            whether to add the core resources
     * @param addPluginResources
     *            whether to add the plugin resources
     * @return the resources or null if the category is not known
     */
    private List<ResourceDescriptor> getInitialResources(String categoryName,
            boolean addCoreResources, boolean addPluginResources) {
        // if there is an overlay for the category in the addedCategories we use this otherwise we
        // use the built in
        List<String> resourcesIdentifiers = addedCategories.get(categoryName);
        List<ResourceDescriptor> initialResources = new ArrayList<ResourceDescriptor>();
        if (resourcesIdentifiers != null && resourcesIdentifiers.size() > 0) {
            // take last from list as it is the newest overlay
            String resourcesId = resourcesIdentifiers.get(resourcesIdentifiers.size() - 1);
            // contains core and plugin resources in that order
            for (ResourceDescriptor descriptor : addedCategoryResources.get(resourcesId)) {
                if (!descriptor.isCoreResource() && !addPluginResources) {
                    break;
                } else if (descriptor.isCoreResource() && !addCoreResources) {
                    continue;
                }
                initialResources.add(descriptor);
            }
        } else if (builtInCategoryResources.containsKey(categoryName)) {
            // contains only core resources
            if (addCoreResources) {
                initialResources.addAll(builtInCategoryResources.get(categoryName));
            }
        } else {
            return null;
        }
        return initialResources;
    }

    @Override
    public String getMinimizedSuffix() {
        return this.minimizedSuffix;
    }

    @Override
    public List<String> getPluginResources(String categoryName, boolean minimized,
            boolean timestamped) {
        List<ResourceDescriptor> descriptors = getInitialResources(categoryName, false, true);
        if (descriptors == null) {
            return null;
        }
        // append resources of extensions
        appendExtendedResources(categoryName, descriptors);
        return extractResourceLocations(descriptors, minimized, timestamped);
    }

    /**
     * Get the current property resource for the given category name
     *
     * @param categoryName
     *            the name of the category
     * @return the cached property resource or null if there is none for the given category
     */
    private CachedFileDescriptor getPropertyResource(String categoryName) {
        if (this.propertyResourceManager != null) {
            return this.propertyResourceManager.getPropertyResource(categoryName);
        }
        return null;
    }

    @Override
    public PropertyResourceContent getPropertyResourceContent(String identifier, String categoryName)
            throws ConcatenatedResourceStoreException {
        if (this.propertyResourceManager != null) {
            try {
                return this.propertyResourceManager.getPropertyResourceContent(identifier,
                        categoryName);
            } catch (PropertyResourceManagerException e) {
                throw new ConcatenatedResourceStoreException(
                        "Loading the content of the property resource failed", e);
            }
        }
        return null;
    }

    /**
     * Get the file handle to the property resource file.
     *
     * @param resource
     *            the property resource can be null
     * @param minimized
     *            whether to return the minimized or uncompressed version. If true but there is no
     *            minimized version the uncompressed one is returned.
     * @return the file or null
     */
    private File getPropertyResourceFile(CachedFileDescriptor resource, boolean minimized) {
        if (resource != null) {
            String fileName = null;
            if (minimized) {
                fileName = resource.getMinFileName();
                if (fileName == null) {
                    fileName = resource.getFileName();
                }
            } else {
                fileName = resource.getFileName();
            }
            if (fileName != null) {
                return new File(cacheDir, fileName);
            }
        }
        return null;
    }

    @Override
    public File getPropertyResourceFile(String categoryName, boolean minimized) {
        CachedFileDescriptor resource = getPropertyResource(categoryName);
        return getPropertyResourceFile(resource, minimized);
    }

    @Override
    public long getPropertyResourceLastModified(String categoryName) {
        CachedFileDescriptor propertyResource = getPropertyResource(categoryName);
        if (propertyResource != null) {
            return propertyResource.getTimestamp();
        }
        return -1L;
    }

    /**
     * Prepare the store after creation. Should be called after all properties (like the
     * ResourceConcatenator) have been set.
     */
    public void init() {
        File baseDir = new File(CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties().getCacheRootDirectory(), "concatenated");

        this.cacheDir = new File(baseDir,
                this.cacheSubdirName == null ? this.fileExtension.substring(1)
                        : this.cacheSubdirName);
        try {
            FileHelper.validateDir(cacheDir);
            // empty the cache dir
            FileUtils.cleanDirectory(this.cacheDir);
            this.cachedFileCleaner = new CachedFileCleaner(cacheDir);
            for (String categoryName : builtInCategoryResources.keySet()) {
                updateConcatenatedFiles(categoryName);
            }
        } catch (IOException e) {
            // TODO we need a generic InitializationException!
            throw new ConfigurationInitializationException(
                    "Initialization of resource store handling " + this.fileExtension
                    + " resources failed", e);
        } catch (ConcatenatedResourceStoreException e) {
            throw new ConfigurationInitializationException(
                    "Creating of the concatenated resources for the built-in categories failed", e);
        }
        if (this.propertyResourceManager != null) {
            this.propertyResourceManager.init(cacheDir);
        }
    }

    /**
     * Add a category.
     *
     * @param the
     *            identifier of the plugin
     * @param categoryName
     *            the name of the category to add or create an overlay for
     * @param coreResources
     *            a collection of resources which are delivered with the application and should be
     *            added first when rendering the resources of the given category. Each entry of the
     *            collection refers to the relative location of a resource under which it can be
     *            downloaded. If the file of a resource cannot be resolved it will be ignored. Can
     *            be null if the category does not need to have core resources.
     * @param extensionResources
     *            a collection of pairs where each pair describes an initial resource provided by
     *            the plugin. The left part refers to the relative location of the resource under
     *            which it can be downloaded. Therefore, it has to start with the bundle name of the
     *            plugin that is prefixed with a slash. The right part of the pair holds the
     *            absolute file location of the resource on disk. Any resource that does not exist
     *            on disk will be ignored. When rendering the resources of the provided category
     *            these resources will be added after the coreResources. Can be null if the category
     *            does not need to have plugin resources.
     * @return true if the category was added, false if the plugin already added it before
     */
    private boolean internalAddCategory(String identifier, String categoryName,
            List<String> coreResources, List<Pair<String, String>> extensionResources) {

        String resourcesIdentifier = createResourcesIdentifier(identifier, categoryName);
        List<String> resourcesIdentifiers = addedCategories.get(categoryName);
        if (resourcesIdentifiers == null) {
            resourcesIdentifiers = new ArrayList<String>();
            addedCategories.put(categoryName, resourcesIdentifiers);
        } else {
            if (resourcesIdentifiers.contains(resourcesIdentifier)) {
                LOGGER.warn("The plugin {} already added the category {}, ignored.", identifier,
                        categoryName);
                return false;
            }
        }
        List<ResourceDescriptor> descriptors = new ArrayList<ResourceDescriptor>();
        // first add the core resources because plugin resources can depend on core resources and
        // therefore they should be rendered after the core resources
        resolveCoreResources(coreResources, descriptors);
        resolvePluginResources(extensionResources, descriptors);
        // add category as an category overlay to the end of the existing overlays
        addedCategoryResources.put(resourcesIdentifier, descriptors);
        resourcesIdentifiers.add(resourcesIdentifier);
        return true;
    }

    /**
     * Normalizes the identifier of a plugin for internal use.
     *
     * @param identifier
     *            the identifier of the plugin
     * @return the trimmed identifier with underscores instead of spaces
     */
    private String normalizePluginIdentifier(String identifier) {
        return identifier.trim().replace(' ', '_');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeCategory(String identifier, String categoryName)
            throws ConcatenatedResourceStoreException {
        removeResources(identifier, categoryName, addedCategories, addedCategoryResources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeFromCategory(String identifier, String categoryName)
            throws ConcatenatedResourceStoreException {
        removeResources(identifier, categoryName, extendedCategories, extendedCategoryResources);
    }

    /**
     * Remove the resources of a category added by a plugin
     *
     * @param identifier
     *            the identifier of the plugin
     * @param categoryName
     *            the name of the category
     * @param categoryToResourcesIds
     *            the internal map addedCategories to remove a category or the map
     *            extendedCategories to remove resources added to a category
     * @param resourcesIdsToDescriptors
     *            the map that holds the resources defined by the plugin. The map has to be
     *            addedCategoryResources or extendedCategoryResources depending on the value passed
     *            to the previous parameter
     * @throws ConcatenatedResourceStoreException
     *             in case updating the concatenated files failed
     */
    private void removeResources(String identifier, String categoryName,
            Map<String, List<String>> categoryToResourcesIds,
            Map<String, List<ResourceDescriptor>> resourcesIdsToDescriptors)
                    throws ConcatenatedResourceStoreException {
        boolean updateConcatenatedFile = false;
        if (this.propertyResourceManager != null) {
            updateConcatenatedFile = this.propertyResourceManager.removePropertyResource(
                    identifier, categoryName);
        }
        String normalizedIdentifier = normalizePluginIdentifier(identifier);
        List<String> resourcesIdentifiers = categoryToResourcesIds.get(categoryName);
        if (resourcesIdentifiers != null) {
            // clone list for thread-safety
            ArrayList<String> cleanedResourcesIdentifiers = new ArrayList<String>(
                    resourcesIdentifiers);
            String resourcesId = createResourcesIdentifier(normalizedIdentifier, categoryName);
            if (cleanedResourcesIdentifiers.remove(resourcesId)) {
                categoryToResourcesIds.put(categoryName, cleanedResourcesIdentifiers);
                resourcesIdsToDescriptors.remove(resourcesId);
                updateConcatenatedFile = true;
            }
        }
        if (updateConcatenatedFile) {
            updateConcatenatedFiles(categoryName);
        }
    }

    /**
     * Create resource descriptors for the provided core resources and add them to a collection of
     * descriptors.
     *
     * @param resourceLocations
     *            a collection of resources which are delivered with the application. Each entry of
     *            the collection refers to the relative location of a resource under which it can be
     *            downloaded. If the file of a resource cannot be resolved it will be ignored. Can
     *            be null.
     * @param descriptors
     *            the collection to add the resolved resources to
     */
    private void resolveCoreResources(List<String> resourceLocations,
            List<ResourceDescriptor> descriptors) {
        if (resourceLocations != null) {
            String basePath = CommunoteRuntime.getInstance().getApplicationInformation()
                    .getApplicationRealPath();
            for (String locationPath : resourceLocations) {
                String absolutePath = basePath;
                // normalize location to start with / and end with extension
                if (!locationPath.startsWith("/")) {
                    locationPath = "/" + locationPath;
                }
                if (!locationPath.endsWith(fileExtension)) {
                    locationPath += fileExtension;
                }
                // since the application real path ends with a separator use substring
                absolutePath += locationPath.substring(1).replace('/', File.separatorChar);
                addResourceIfExists(locationPath, absolutePath, true, descriptors);
            }
        }
    }

    /**
     * Create resource descriptors for the provided plugin resources and add them to a collection of
     * descriptors.
     *
     * @param resources
     *            a collection of pairs where each pair describes a resource of a plugin. The left
     *            part refers to the relative location of the resource under which it can be
     *            downloaded. The right part of the pair holds the absolute file location of the
     *            resource on disk. Any resource that does not exist on disk will be ignored. Can be
     *            null.
     * @param descriptors
     *            the collection to add the resolved resources to
     */
    private void resolvePluginResources(List<Pair<String, String>> resources,
            List<ResourceDescriptor> descriptors) {
        if (resources != null) {
            for (Pair<String, String> resourceDef : resources) {
                addResourceIfExists(resourceDef.getLeft(), resourceDef.getRight(), false,
                        descriptors);
            }
        }
    }

    /**
     * Set the name of the cache sub-directory where concatenated files will be stored. Can be used
     * to override the default which is the file extension, without leading dot, handled by this
     * store. When having several resource stores at runtime it is important to define a unique
     * sub-directory for each store to avoid conflicts.
     *
     * @param subdirName
     *            the name of the cache sub-directory to use
     */
    public void setCacheSubdir(String subdirName) {
        this.cacheSubdirName = subdirName;
    }

    /**
     * Set the resource concatenator to use. Can be used to override the default for example with a
     * concatenator which does some in-place content substitution.
     *
     * @param concatenator
     *            the concatenator
     */
    public void setResourceConcatenator(ResourceConcatenator concatenator) {
        this.resourceConcatenator = concatenator;
    }

    /**
     * Create two files by concatenating the resources of a category. The first file is built from
     * the not minimized resources, the other from the minimized versions. If there are already
     * concatenated files for the category they are replaced.
     *
     * Note: this method should be called from a thread-safe context
     *
     * @param categoryName
     *            the name of the category
     * @throws ConcatenatedResourceStoreException
     *             in case one of the concatenated files couldn't be created
     */
    private void updateConcatenatedFiles(String categoryName)
            throws ConcatenatedResourceStoreException {
        List<ResourceDescriptor> resourcesToConcatenate = getInitialResources(categoryName, true,
                true);
        CachedFileDescriptor propertyResource = getPropertyResource(categoryName);
        if (resourcesToConcatenate == null && propertyResource == null) {
            // the category got removed try to remove the concatenated files
            this.cachedFileCleaner.delete(concatenatedFiles.remove(categoryName));
            return;
        }
        if (resourcesToConcatenate != null) {
            // add all additional resources for that category
            appendExtendedResources(categoryName, resourcesToConcatenate);
        } else {
            resourcesToConcatenate = Collections.emptyList();
        }
        if (resourcesToConcatenate.size() == 0 && propertyResource == null) {
            // empty built-in category and no additional resources provided by plugins, cleanup
            this.cachedFileCleaner.delete(concatenatedFiles.remove(categoryName));
            return;
        }
        String fileName = null;
        String minFileName = null;
        try {
            File propertyResourceFile = getPropertyResourceFile(propertyResource, false);
            fileName = createConcatenatedFileName(categoryName, false);
            this.resourceConcatenator.concatenateResources(cacheDir, fileName,
                    resourcesToConcatenate, propertyResourceFile, null);
            propertyResourceFile = getPropertyResourceFile(propertyResource, true);
            minFileName = createConcatenatedFileName(categoryName, true);
            this.resourceConcatenator.concatenateResources(cacheDir, minFileName,
                    resourcesToConcatenate, propertyResourceFile, getMinimizedSuffix());
            CachedFileDescriptor previousConcatenatedFile = concatenatedFiles.get(categoryName);
            // save the timestamp of the current modification, if only built-in files are contained
            // use built timestamp. Since core files are always added before plugin files, check the
            // last resource for being a core resource
            long timestamp;
            if (resourcesToConcatenate.size() > 0
                    && resourcesToConcatenate.get(resourcesToConcatenate.size() - 1)
                    .isCoreResource()) {
                timestamp = CommunoteRuntime.getInstance().getApplicationInformation()
                        .getBuildTimestamp();
            } else {
                // if called fast enough the timestamp might be equal -> ensure the new one is
                // different
                timestamp = System.currentTimeMillis();
                if (previousConcatenatedFile != null
                        && previousConcatenatedFile.getTimestamp() >= timestamp) {
                    timestamp = previousConcatenatedFile.getTimestamp() + 1;
                }
            }
            concatenatedFiles.put(categoryName, new CachedFileDescriptor(fileName, minFileName,
                    timestamp));
            this.cachedFileCleaner.delete(previousConcatenatedFile);
        } catch (IOException e) {
            if (minFileName == null) {
                LOGGER.error("Creating concatenated file for category " + categoryName + " failed",
                        e);
            } else {
                LOGGER.error("Creating minimized concatenated file for category " + categoryName
                        + " failed", e);
                this.cachedFileCleaner.delete(minFileName);
            }
            this.cachedFileCleaner.delete(fileName);
            throw new ConcatenatedResourceStoreException("Updating the concatenated file failed", e);
        }
    }

    @Override
    public synchronized void updatePropertyResource(String identifier, String categoryName,
            String propertyValue) throws ConcatenatedResourceStoreException {
        if (this.propertyResourceManager == null) {
            LOGGER.error("Property resources are not supported by this resource store");
            throw new ConcatenatedResourceStoreException(
                    "Property resources are not supported by this resource store");
        }
        try {
            if (this.propertyResourceManager.updatePropertyResource(identifier, categoryName,
                    propertyValue)) {
                this.updateConcatenatedFiles(categoryName);
            }
        } catch (PropertyResourceManagerException e) {
            LOGGER.error("Updating the property resource of bundle " + identifier
                    + " for category " + categoryName + " failed", e);
            if (e.isCurrentPropertyResourceChanged()) {
                this.updateConcatenatedFiles(categoryName);
            }
            throw new ConcatenatedResourceStoreException("Updating the property resource failed", e);
        }
    }
}
