package com.communote.server.web.commons.resource;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface ResourceCategoryRegistry {

    /**
     * Extend some of the CSS categories with CSS files of a plugin.
     * 
     * @param bundleName
     *            the name of the bundle for which the resources should be added
     * @param resources
     *            mapping from category name to a list of resources
     * @throws ConcatenatedResourceStoreException
     *             in case a category could not be updated
     */
    void registerCssCategoryExtensions(String bundleName, Map<String, List<String>> resources)
            throws ConcatenatedResourceStoreException;

    /**
     * Add the CSS resource category extensions and definitions defined in the extension.
     * 
     * @param bundleName
     *            the name of the bundle that provides the resource extension
     * @param extension
     *            the object containing the category extensions and definitions
     * @throws ConcatenatedResourceStoreException
     *             in case a category could not be updated or added
     */
    void registerCssResourceExtension(String bundleName, ResourceExtension extension)
            throws ConcatenatedResourceStoreException;

    /**
     * Extend some of the JavaScript categories with JavaScript files of a plugin. This method
     * should usually only be called if adding the JavaScript files depends on some condition like
     * the availability of a service.
     * 
     * @param bundleName
     *            the name of the bundle for which the resources should be added
     * @param resources
     *            mapping from category name to a list of resources
     * @throws ConcatenatedResourceStoreException
     *             in case a category could not be updated
     */
    void registerJsCategoryExtensions(String bundleName, Map<String, List<String>> resources)
            throws ConcatenatedResourceStoreException;

    /**
     * Add the JavaScript resource category extensions and definitions defined in the extension.
     * 
     * @param bundleName
     *            the name of the bundle that provides the resource extension
     * @param extension
     *            the object containing the category extensions and definitions
     * @throws ConcatenatedResourceStoreException
     *             in case a category could not be updated or added
     */
    void registerJsResourceExtension(String bundleName, ResourceExtension extension)
            throws ConcatenatedResourceStoreException;

    /**
     * Remove all CSS extensions and definitions added by the given bundle.
     * 
     * @param bundleName
     *            the name of the bundle
     */
    void unregisterCssCategories(String bundleName);

    /**
     * Remove all JavaScript extensions and definitions added by the given bundle.
     * 
     * @param bundleName
     *            the name of the bundle
     */
    void unregisterJsCategories(String bundleName);
}
