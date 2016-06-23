package com.communote.server.web.commons.resource.impl;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for removing cached files
 * 
 * This implementation is not thread safe.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CachedFileCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedFileCleaner.class);
    private final File cacheDir;
    private final Set<String> concatenatedFilesToDelete;

    /**
     * Create a new cleaner
     * 
     * @param cacheDir
     *            the directory where the files are cached
     */
    public CachedFileCleaner(File cacheDir) {
        this.cacheDir = cacheDir;
        concatenatedFilesToDelete = new HashSet<>();
    }

    /**
     * Delete the cached files listed in a descriptor. If a file cannot be deleted immediately,
     * because it is still accessed, deletion will retried at a later time.
     * 
     * @param descriptor
     *            the descriptor holding the names of the file and its minimized version. Can be
     *            null.
     */
    public void delete(CachedFileDescriptor descriptor) {
        // first try to delete older files that couldn't be deleted
        Iterator<String> fileNameIt = concatenatedFilesToDelete.iterator();
        while (fileNameIt.hasNext()) {
            String previouslyFailedFileName = fileNameIt.next();
            File fileToDelete = new File(cacheDir, previouslyFailedFileName);
            if (!fileToDelete.delete()) {
                LOGGER.warn("Deleting cached file {} failed", previouslyFailedFileName);
            } else {
                fileNameIt.remove();
            }
        }
        if (descriptor != null) {
            delete(descriptor.getFileName());
            delete(descriptor.getMinFileName());
        }
    }

    /**
     * Delete a cached file by name. If the deletion fails, for instance if someone is still
     * accessing the file, the deletion will be retried at a later time.
     * 
     * @param fileName
     *            the name of the file to delete. Can be null.
     */
    public void delete(String fileName) {
        if (fileName != null) {
            File fileToDelete = new File(cacheDir, fileName);
            if (fileToDelete.exists() && fileToDelete.isFile()) {
                if (!fileToDelete.delete()) {
                    LOGGER.debug("Deleting cached file {} failed, will try again later", fileName);
                    concatenatedFilesToDelete.add(fileName);
                }
            }
        }
    }
}
