package com.communote.server.web.commons.resource;

/**
 * Holds the content of a property resource with additional meta data.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class PropertyResourceContent {

    private final boolean loadedFromFallback;
    private final String content;

    /**
     * Create a content wrapper for a property resource
     * 
     * @param content
     *            the content
     * @param loadedFromFallback
     *            whether the content was loaded from the application property or from the fallback
     *            file
     */
    public PropertyResourceContent(String content, boolean loadedFromFallback) {
        this.content = content;
        this.loadedFromFallback = loadedFromFallback;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @return whether the content was loaded from the application property or from the fallback
     *         file
     */
    public boolean isLoadedFromFallback() {
        return loadedFromFallback;
    }
}
