package com.communote.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy the given stream.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO in contrast to documentation the temp file is not deleted
// TODO close operations on streams should be done in finally blocks
public class CopyInputStreamBuffer extends FilterInputStream {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CopyInputStreamBuffer.class);

    /**
     * Create a temporary file.
     * 
     * @param tempDir
     *            Path to temp directory
     * @param filePrefix
     *            Prefix string of temp file
     * @param fileSuffix
     *            Suffix string of temp file
     * @return Created file
     * @throws IOException
     *             Threw when a file does not created
     */
    public synchronized static File getTempFile(String tempDir, String filePrefix, String fileSuffix)
            throws IOException {
        File tempFile = null;
        // TODO use createNewFile directly as it is atomic and returns a flag whether it already
        // existed
        do {
            int i = new Random().nextInt();
            String filename = tempDir + filePrefix + i + fileSuffix;
            tempFile = new File(filename);
        } while (tempFile.exists());

        try {
            tempFile.createNewFile();
        } catch (IOException e) {
            // provide some details since createNewFile uses native code which is not very verbose
            // when an error occurs
            throw new IOException("Creating temp file " + tempFile.getAbsolutePath() + " failed", e);
        }
        return tempFile;
    }

    private boolean isClosed = Boolean.FALSE;

    private final OutputStream outputStream;

    private File tempFile = null;

    private boolean endReached = false;

    /**
     * Constructor
     * 
     * @param inputStream
     *            The input stream to copy
     * @param tempFile
     *            The file to copy the stream to
     * @throws FileNotFoundException
     *             in case the temporary file does not exist
     */
    public CopyInputStreamBuffer(InputStream inputStream, File tempFile)
            throws FileNotFoundException {
        super(inputStream);
        this.tempFile = tempFile;
        outputStream = new FileOutputStream(tempFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        close(Boolean.TRUE);
    }

    /**
     * Close the current stream
     * 
     * @param endOfStream
     *            If this Parameter is <code>true</code>, then the input stream will read all
     *            available bytes.
     * @throws IOException
     *             {@link IOException}
     */
    private void close(boolean endOfStream) throws IOException {
        if (isClosed) {
            return;
        }
        if (endOfStream) {
            gotoEndOfstream();
        }
        super.close();
        outputStream.close();
        isClosed = Boolean.TRUE;
    }

    /**
     * Close the original stream and the copy (which will deleted)
     */
    public void closeCompleteStream() {

        try {
            this.close();
            if (this.getInputStream() != null) {
                this.getInputStream().close();
            }
            this.tempFile = null;
        } catch (Exception e) {
            // if there an exception occurred, it's sufficient to log this exception
            LOGGER.error("Error closing CopyInputStreamBuffer", e);
        }
    }

    /**
     * Returns copied input stream
     * 
     * @return Original input stream
     * @throws IOException
     *             Thrown when an exception occurred
     */
    public InputStream getInputStream() throws IOException {
        if (tempFile == null) {
            throw new IOException(
                    "No tempFile available. Temporary File probably deleted due to #closeCompleteStream");
        }
        if (!isClosed) {
            LOGGER.warn("The input stream was not closed. Close it now");
            this.close();
        }

        final File tFile = tempFile;

        FileInputStream newStream = new FileInputStream(tempFile) {
            @Override
            public void close() throws IOException {
                super.close();
                LOGGER.debug("Delete temp file: {}", tFile.getAbsolutePath());
                tFile.delete();
            }
        };

        return newStream;
    }

    /**
     * Read in the rest of the bytes, until the stream ends.
     * 
     * @throws IOException
     *             {@link IOException}
     */
    private void gotoEndOfstream() throws IOException {
        while (!endReached) {
            this.read();
        }
    }

    /**
     * Indicate whether the stream was closed
     * 
     * @return Returns <code>true</code> when the stream was already closed
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        int c = super.read();
        if (c != -1) {
            outputStream.write(c);
        }
        endReached = c < 0;
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int c = super.read(b, off, len);
        if (c > 0) {
            outputStream.write(b, off, c);
        }
        return c;
    }
}
