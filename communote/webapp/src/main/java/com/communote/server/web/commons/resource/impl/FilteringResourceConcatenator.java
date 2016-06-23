package com.communote.server.web.commons.resource.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import com.communote.common.io.DelimiterFilterReader;

/**
 * Concatenator that uses a configurable {@link DelimiterFilterReader} to filter the content of the
 * resource files before concatenating them.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class FilteringResourceConcatenator extends ResourceConcatenator {
    private final String startDelimiter;
    private final String endDelimiter;
    private final ResourceFilterMatchProcessor matchProcessor;
    private String charset;

    /**
     * Create a new concatenator
     * 
     * @param startDelimiter
     *            the start delimiter the {@link DelimiterFilterReader} should look for
     * @param endDelimiter
     *            the end delimiter the {@link DelimiterFilterReader} should look for
     * @param matchProcessor
     *            the processor to invoke when the {@link DelimiterFilterReader} found a match
     */
    public FilteringResourceConcatenator(String startDelimiter, String endDelimiter,
            ResourceFilterMatchProcessor matchProcessor) {
        this.startDelimiter = startDelimiter;
        this.endDelimiter = endDelimiter;
        this.matchProcessor = matchProcessor;
        this.charset = "UTF-8";
    }

    /**
     * Append the resource to the writer
     * 
     * @param writer
     *            the writer to write to
     * @param descriptor
     *            the descriptor of the resource to append
     * @param minimizedSuffix
     *            the suffix marking the minimized version of a resource or null if the not
     *            minimized files should be concatenated
     * @throws IOException
     *             in case appending failed
     */
    protected void appendResource(Writer writer, ResourceDescriptor descriptor,
            String minimizedSuffix) throws IOException {
        File file = getFile(descriptor, minimizedSuffix);
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        BufferedReader bufferedReader = null;
        DelimiterFilterReader reader = null;
        // set currently processed resource
        matchProcessor.setProcessedResource(descriptor);
        // TODO should we cache a processed core resources on disk and just copy them
        // on subsequent concatenations?
        try {
            fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            // bufferedReader to optimize character conversion
            bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream,
                    this.charset));
            // create a filtering reader
            reader = new DelimiterFilterReader(bufferedReader, startDelimiter, endDelimiter,
                    matchProcessor);
            int nextChar;
            while ((nextChar = reader.read()) != -1) {
                writer.append((char) nextChar);
            }
            writer.flush();
        } finally {
            // close writer and streams, start with outermost wrapper
            if (reader != null) {
                // also closes the input stream
                reader.close();
            } else if (bufferedReader != null) {
                bufferedReader.close();
            } else if (bufferedInputStream != null) {
                bufferedInputStream.close();
            } else if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    @Override
    public void concatenateResources(File cacheDir, String fileName,
            List<ResourceDescriptor> resourcesToConcatenate, File propertyResource,
            String minimizedSuffix) throws IOException {
        FileOutputStream outputStream = null;
        OutputStreamWriter streamWriter = null;
        BufferedWriter concatenatingWriter = null;
        try {
            outputStream = new FileOutputStream(new File(cacheDir, fileName));
            streamWriter = new OutputStreamWriter(outputStream, charset);
            // buffered writer to optimize character conversion
            concatenatingWriter = new BufferedWriter(streamWriter);
            for (ResourceDescriptor descriptor : resourcesToConcatenate) {
                appendResource(concatenatingWriter, descriptor, minimizedSuffix);
            }
            concatenatingWriter.flush();
            // just copy cached property file without filtering
            // not sure if we can append directly to stream if there is a writer, so close and
            // re-open
            concatenatingWriter.close();
            concatenatingWriter = null;
            streamWriter.close();
            streamWriter = null;
            outputStream.close();
            outputStream = new FileOutputStream(new File(cacheDir, fileName), true);
            appendPropertyResource(outputStream, propertyResource);
            outputStream.flush();
        } finally {
            if (concatenatingWriter != null) {
                concatenatingWriter.close();
            } else if (streamWriter != null) {
                streamWriter.close();
            } else if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * Set the character set encoding for reading the resources. The default is "UTF-8".
     * 
     * @param charset
     *            the character set encoding to use
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }
}
