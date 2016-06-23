package com.communote.server.web.commons.resource.impl;
/**
 * Helper holding details of a resource.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResourceDescriptor {
    private final String resourceLocation;
    private final String absoluteFilePath;
    private final boolean hasMinimizedVersion;
    private final boolean isCoreResource;

    /**
     * Create a new resource descriptor
     * 
     * @param resourceLocation
     *            the relative location of the resource under which it can be downloaded
     * @param absoluteFilePath
     *            the absolute location of the resource on disk
     * @param hasMinimizedVersion
     *            whether there is a minimized version of the resource
     * @param isCoreResource
     *            whether the resource is a core resource or one that was provided by a plugin
     */
    public ResourceDescriptor(String resourceLocation, String absoluteFilePath,
            boolean hasMinimizedVersion, boolean isCoreResource) {
        this.resourceLocation = resourceLocation;
        this.absoluteFilePath = absoluteFilePath;
        this.hasMinimizedVersion = hasMinimizedVersion;
        this.isCoreResource = isCoreResource;
    }

    /**
     * @return the relative location of the resource under which it can be downloaded
     */
    public String getResourceLocation() {
        return resourceLocation;
    }

    /**
     * @return the absolute location of the resource on disk
     */
    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    /**
     * @return whether there is a minimized version of the resource
     */
    public boolean hasMinimizedVersion() {
        return hasMinimizedVersion;
    }

    /**
     * @return whether the resource is a core resource or one that was provided by a plugin
     */
    public boolean isCoreResource() {
        return isCoreResource;
    }
}