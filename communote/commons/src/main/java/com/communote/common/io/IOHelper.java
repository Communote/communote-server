package com.communote.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for IO stuff
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class IOHelper {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(IOHelper.class);

    /**
     * Close the AutoCloseable and log any exception that might be thrown.
     *
     * @param closable
     *            The closable to close. Can be null.
     */
    public static void close(AutoCloseable closable) {
        try {
            if (closable != null) {
                closable.close();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Close the closable and ignore but log thrown exceptions. Use
     * {@link org.apache.commons.io.IOUtils#closeQuietly(Closeable)} if you don't need any logging.
     *
     * @param closeable
     *            The closable to close. Can be null.
     */
    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Write the data of the input stream to the output stream
     *
     * @param in
     *            the in stream
     * @param out
     *            the out stream
     * @param maxLength
     *            Throws an {@link MaxLengthReachedException} when the inputstream is larger than
     *            the maximal allowed length. Use a value &lt;= 0 (zero) for no limit.
     * @return the length of the stream read
     * @throws IOException
     *             in case of an exception
     *
     */
    public static long write(InputStream in, OutputStream out, long maxLength) throws IOException {
        byte[] b = new byte[1024];
        int len;
        long overall = 0;
        do {
            len = in.read(b);
            if (len > 0) {
                overall += len;
                out.write(b, 0, len);
            }
            if (maxLength > 0 && maxLength < overall) {
                throw new MaxLengthReachedException();
            }
        } while (len > 0);
        return overall;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private IOHelper() {
        // Do nothing
    }
}
