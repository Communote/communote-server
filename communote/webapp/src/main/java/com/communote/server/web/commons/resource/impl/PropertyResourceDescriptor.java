package com.communote.server.web.commons.resource.impl;

/**
 * Helper holding details of a resource.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PropertyResourceDescriptor {

    private final String pluginIdentifier;
    private final boolean fallbackHasMinimized;
    private final String fallbackAbsolutePath;
    private final CachedFileDescriptor cachedFile;
    private final boolean propertyValue;

    /**
     * Create a new property resource descriptor
     * 
     * @param pluginIdentifier
     *            identifier of the plugin
     * @param fallbackAbsolutePath
     *            absolute path to the fallback resource that is part of the plugin. Msut be null if
     *            there is no fallback.
     * @param fallbackHasMinimized
     *            whether a minimized version of the fallback exists
     */
    public PropertyResourceDescriptor(String pluginIdentifier, String fallbackAbsolutePath,
            boolean fallbackHasMinimized) {
        this(pluginIdentifier, fallbackAbsolutePath, fallbackHasMinimized, null, false);
    }

    /**
     * Create a new property resource descriptor
     * 
     * @param pluginIdentifier
     *            identifier of the plugin
     * @param fallbackAbsolutePath
     *            absolute path to the fallback resource that is part of the plugin. Msut be null if
     *            there is no fallback.
     * @param fallbackHasMinimized
     *            whether a minimized version of the fallback exists
     * @param cachedFile
     *            the cached file. Should only be set for the current property resource. Can be null
     *            if the property has no value and no fallback
     * @param propertyValue
     *            whether the application property is set
     */
    public PropertyResourceDescriptor(String pluginIdentifier, String fallbackAbsolutePath,
            boolean fallbackHasMinimized, CachedFileDescriptor cachedFile, boolean propertyValue) {
        this.pluginIdentifier = pluginIdentifier;
        this.fallbackAbsolutePath = fallbackAbsolutePath;
        this.fallbackHasMinimized = fallbackHasMinimized;
        this.cachedFile = cachedFile;
        this.propertyValue = propertyValue;
    }

    /**
     * The cached property resource. Is usually only set if the property resource is the current
     * one.
     * 
     * @return the cached file
     */
    public CachedFileDescriptor getCachedFile() {
        return cachedFile;
    }

    /**
     * @return absolute path to the fallback resource that is part of the plugin. Will be null if
     *         there is no fallback.
     */
    public String getFallbackAbsolutePath() {
        return fallbackAbsolutePath;
    }

    /**
     * @return identifier of the plugin
     */
    public String getPluginIdentifier() {
        return pluginIdentifier;
    }

    /**
     * @return whether there is a fallback resource file
     */
    public boolean hasFallback() {
        return fallbackAbsolutePath != null;
    }

    /**
     * @return whether the application property exists. Is usually only set if the property resource
     *         is the current one.
     */
    public boolean hasPropertyValue() {
        return this.propertyValue;
    }

    /**
     * @return whether a minimized version of the fallback exists
     */
    public boolean isFallbackHasMinimized() {
        return fallbackHasMinimized;
    }
}
