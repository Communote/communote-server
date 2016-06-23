package com.communote.server.web.commons.resource;

import java.io.File;
import java.util.List;

import com.communote.common.util.Pair;

/**
 * A store that manages resources of a specific content type by grouping them into categories.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ConcatenatedResourceStore {

    /**
     * <p>
     * Add a category with initial resources. If the category already exists because it was added by
     * another plugin via a call to this method or it is one of the built-in categories, the initial
     * resources passed to this method will overlay those of the existing category. That is, the
     * existing initial resources won't be rendered until the plugin removes the category.
     * </p>
     * When rendering the resources of this category the provided initial resources will be rendered
     * first. Afterwards any plugin resources added to this category by
     * {@link #addToCategory(String, String, List)} will be rendered in the order they were added.
     * 
     * @param identifier
     *            the identifier of the plugin
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
     * @throws ConcatenatedResourceStoreException
     *             in case adding the category failed
     */
    public void addCategory(String identifier, String categoryName,
            List<String> coreResources, List<Pair<String, String>> extensionResources)
            throws ConcatenatedResourceStoreException;

    /**
     * Like {@link #addCategory(String, String, List, List)} but adds a category which supports the
     * definition of a resource as a PluginProperty. This so called property resource will be
     * rendered last.
     * 
     * @param identifier
     *            the identifier of the plugin
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
     * @param propertyResourceFallback
     *            a fallback of the property resource which will be included if the property is not
     *            set. Absolute file location of the resource on disk. If not provided or the
     *            resource that does not exist on disk there will be no fallback for the property
     *            resource.
     * @throws ConcatenatedResourceStoreException
     *             in case property resources are not supported by this store or adding the category
     *             failed
     */
    public void addCategoryWithPropertyResourceSupport(String identifier, String categoryName,
            List<String> coreResources, List<Pair<String, String>> extensionResources,
            String propertyResourceFallback)
            throws ConcatenatedResourceStoreException;

    /**
     * Add the resources of a plugin to a category. If the category does not exist the resources
     * will become available as soon as the category is added. If the category exists the provided
     * resources will be rendered after the resources that were added with the category. If another
     * plugin added resources for this category these resources will be rendered before the provided
     * resources.
     * 
     * @param identifier
     *            the identifier of the plugin
     * @param categoryName
     *            the name of the category to which the resources should be added
     * @param resources
     *            a collection of pairs where each pair describes a resource of the plugin. The left
     *            part refers to the relative location of the resource under which it can be
     *            downloaded. Therefore, it has to start with the bundle name of the plugin that is
     *            prefixed with a slash. The right part of the pair holds the absolute file location
     *            of the resource on disk. Any resource that does not exist on disk will be ignored.
     * @throws ConcatenatedResourceStoreException
     *             in case adding the resources to the category failed
     */
    public void addToCategory(String identifier, String categoryName,
            List<Pair<String, String>> resources) throws ConcatenatedResourceStoreException;

    /**
     * Return the concatenated file of a category.
     * 
     * @param categoryName
     *            the name of the category
     * @param minimized
     *            whether the minimized version of the file should be returned
     * @return the concatenated file or null if the category is not known
     */
    public File getConcatenatedFile(String categoryName, boolean minimized);

    /**
     * Return the timestamp of the last modification of the concatenated file. The concatenated file
     * of a category is modified each time a plugin adds or removes resources from the category or
     * adds an overlay for the category. If there is a property resources for a category, changes to
     * the property will also lead to a new modification timestamp.
     * 
     * @param categoryName
     *            the name of the category
     * @return the timestamp of the last modification or -1 if the category is not known
     */
    public long getConcatenatedFileLastModified(String categoryName);

    /**
     * @return the content/MIME type of the files managed by the store
     */
    public String getContentType();

    /**
     * Return the relative locations of the core resources of a given category. This covers all
     * resources that are assigned to the category and are provided by the application. The relative
     * location represents the location under which a resource can be downloaded.
     * 
     * @param categoryName
     *            the name of the category
     * @param minimized
     *            whether to get the location of the minimized version
     * @param timestamped
     *            if true the last modification timestamp of the file will be append to the location
     *            in the form of a request parameter with name 't'.
     * @return the locations of the resources or null if the category is not known
     */
    public List<String> getCoreResources(String categoryName, boolean minimized, boolean timestamped);

    /**
     * @return the filename suffix that marks a resource as the minimized version
     */
    public String getMinimizedSuffix();

    /**
     * Return the relative locations of the plugin resources of a given category. This covers all
     * resources that are provided by plugins and were added as part of the initial plugin resources
     * passed to {@link #addCategory(String, String, List, List)} or those added via
     * {@link #addToCategory(String, String, List)}. The relative location represents the location
     * under which the plugin resource can be downloaded. It starts with a slash followed by the
     * bundle name of the plugin.
     * 
     * @param categoryName
     *            the name of the category
     * @param minimized
     *            whether to get the location of the minimized version
     * @param timestamped
     *            if true the last modification timestamp of the file will be append to the location
     *            in the form of a request parameter with name 't'.
     * @return the locations of the resources or null if the category is not known
     */
    public List<String> getPluginResources(String categoryName, boolean minimized,
            boolean timestamped);

    /**
     * Get the content of a property resource for category that was added by a given plugin. This
     * method is useful to get the content provided by a certain plugin especially if several
     * plugins provide a property resource for the same category and thus there are overlays for the
     * property resource. For cases where the current property resource is needed
     * {@link #getPropertyResource(String)} should be used.
     * 
     * @param identifier
     *            the identifier of the plugin
     * @param categoryName
     *            the name of the category
     * @return the content or null if the plugin did not define a property resource for the
     *         category. The value of the content might be null.
     * @throws ConcatenatedResourceStoreException
     *             in case getting the content failed
     */
    PropertyResourceContent getPropertyResourceContent(String identifier, String categoryName)
            throws ConcatenatedResourceStoreException;

    /**
     * Return the file containing the current value of the property resource of a category.
     * 
     * @param categoryName
     *            the name of the category
     * @param minimized
     *            whether the minimized version of the file should be returned
     * @return the file or null if the category is not known
     */
    public File getPropertyResourceFile(String categoryName, boolean minimized);

    /**
     * Return the timestamp of the last modification of the property resource. The property resource
     * of a category is modified each time the property is updated or another plugin adds an overlay
     * for the category and that overlay contains a property resource.
     * 
     * @param categoryName
     *            the name of the category
     * @return the timestamp of the last modification or -1 if the category is not known
     */
    long getPropertyResourceLastModified(String categoryName);

    /**
     * Remove a category previously added by the plugin with the given identifier.
     * 
     * @param identifier
     *            the identifier of the plugin
     * @param categoryName
     *            the name of the category to remove
     * @throws ConcatenatedResourceStoreException
     *             in case the category couldn't be removed. The store can be in an inconsistent
     *             state.
     */
    public void removeCategory(String identifier, String categoryName)
            throws ConcatenatedResourceStoreException;

    /**
     * Remove the resources of a category that were added by the plugin with the given identifier
     * 
     * @param identifier
     *            the identifier of the plugin
     * @param categoryName
     *            the name of the category for which the resources should be removed
     * @throws ConcatenatedResourceStoreException
     *             in case the resources of the category couldn't be removed. The store can be in an
     *             inconsistent state.
     */
    public void removeFromCategory(String identifier, String categoryName)
            throws ConcatenatedResourceStoreException;

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
     * 
     * @throws ConcatenatedResourceStoreException
     *             in case the update failed or the store does not support property resources
     */
    void updatePropertyResource(String identifier, String categoryName, String propertyValue)
            throws ConcatenatedResourceStoreException;
}
