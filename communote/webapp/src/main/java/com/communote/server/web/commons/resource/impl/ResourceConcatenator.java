package com.communote.server.web.commons.resource.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Class that provides means to concatenate resources
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResourceConcatenator {

    /**
     * Append the property resource to the output stream
     * 
     * @param outputStream
     *            the stream to write to
     * @param propertyResource
     *            the cached file of the property resource that contains the property value or the
     *            fallback. If null nothing is appended.
     * @throws IOException
     *             in case appending failed
     */
    protected void appendPropertyResource(OutputStream outputStream, File propertyResource)
            throws IOException {
        if (propertyResource != null) {
            FileInputStream resourceStream = null;
            try {
                resourceStream = new FileInputStream(propertyResource);
                IOUtils.copy(resourceStream, outputStream);
                resourceStream.close();
            } finally {
                IOUtils.closeQuietly(resourceStream);
            }

        }
    }

    /**
     * Append the resource to the output stream
     * 
     * @param outputStream
     *            the stream to write to
     * @param descriptor
     *            the descriptor of the resource to append
     * @param minimizedSuffix
     *            the suffix marking the minimized version of a resource or null if the not
     *            minimized files should be concatenated
     * @throws IOException
     *             in case appending failed
     */
    protected void appendResource(OutputStream outputStream, ResourceDescriptor descriptor,
            String minimizedSuffix) throws IOException {
        FileInputStream resourceStream = null;
        try {
            resourceStream = new FileInputStream(getFile(descriptor, minimizedSuffix));
            IOUtils.copy(resourceStream, outputStream);
            resourceStream.close();
        } finally {
            IOUtils.closeQuietly(resourceStream);
        }
    }

    /**
     * Concatenate the provided resources and write the result to the given file
     * 
     * @param cacheDir
     *            the directory to store the file in
     * @param fileName
     *            the name of the file to write to
     * @param resourcesToConcatenate
     *            the resources to concatenate
     * @param propertyResource
     *            the property resource of the category, can be null
     * @param minimizedSuffix
     *            the suffix marking the minimized version of a resource or null if the not
     *            minimized files should be concatenated
     * @throws IOException
     *             in case the concatenation failed
     */
    public void concatenateResources(File cacheDir, String fileName,
            List<ResourceDescriptor> resourcesToConcatenate, File propertyResource,
            String minimizedSuffix) throws IOException {
        FileOutputStream concatenatedFileStream = null;
        try {
            concatenatedFileStream = new FileOutputStream(new File(cacheDir, fileName));
            for (ResourceDescriptor descriptor : resourcesToConcatenate) {
                appendResource(concatenatedFileStream, descriptor, minimizedSuffix);
            }
            appendPropertyResource(concatenatedFileStream, propertyResource);
            concatenatedFileStream.flush();
        } finally {
            IOUtils.closeQuietly(concatenatedFileStream);
        }
    }

    /**
     * Get the file of the resource to append.
     * 
     * @param descriptor
     *            the descriptor of the resource to append
     * @param minimizedSuffix
     *            the suffix marking the minimized version of a resource or null if the not
     *            minimized files should be concatenated
     * @return the file handle
     */
    protected File getFile(ResourceDescriptor descriptor, String minimizedSuffix) {
        if (minimizedSuffix == null || !descriptor.hasMinimizedVersion()) {
            return new File(descriptor.getAbsoluteFilePath());
        } else {
            return new File(
                    ConcatenatedResourceStoreImpl.getMinimizedResourceName(
                            descriptor.getAbsoluteFilePath(), minimizedSuffix));
        }
    }
}
