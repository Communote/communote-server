package com.communote.server.web.commons.resource.impl;

/**
 * Holds details about a cached resource.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CachedFileDescriptor {
    private final String fileName;
    private final String minFileName;
    private final long timestamp;

    /**
     * Create a new descriptor.
     * 
     * @param fileName
     *            the name of the cached file
     * @param minFileName
     *            the name of the minimized version of the cached file
     * @param timestamp
     *            the timestamp when the cached files were created
     */
    public CachedFileDescriptor(String fileName, String minFileName, long timestamp) {
        this.fileName = fileName;
        this.minFileName = minFileName;
        this.timestamp = timestamp;
    }

    /**
     * @return the name of the cached file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the name of the minimized version of the cached file, can be null if there is no
     *         minimized file
     */
    public String getMinFileName() {
        return minFileName;
    }

    /**
     * @return the timestamp when the cached files were created
     */
    public long getTimestamp() {
        return timestamp;
    }
}
