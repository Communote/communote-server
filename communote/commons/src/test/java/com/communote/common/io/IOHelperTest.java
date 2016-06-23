package com.communote.common.io;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.io.output.NullOutputStream;
import org.testng.annotations.Test;

import com.communote.common.io.IOHelper;
import com.communote.common.io.MaxLengthReachedException;

/**
 * Test for the {@link IOHelper}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class IOHelperTest {
    /**
     * @param length
     *            The length of the stream content.
     * @return An InputStream.
     * @throws IOException
     *             Exception.
     */
    private PipedInputStream getInputStream(long length) throws IOException {
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        for (int i = 0; i < length; i++) {
            outputStream.write(Byte.MIN_VALUE);
        }
        outputStream.flush();
        outputStream.close();
        return inputStream;
    }

    /**
     * Tests, if the max length is correctly handled.
     * 
     * @throws IOException
     *             Exception.
     */
    @Test
    public void testMaxLengthCheckEquals() throws IOException {
        long maxLength = 50;
        PipedInputStream inputStream = getInputStream(maxLength);
        IOHelper.write(inputStream, new NullOutputStream(), maxLength);
    }

    /**
     * Tests, if the max length is correctly handled.
     * 
     * @throws IOException
     *             Exception.
     */
    @Test(expectedExceptions = MaxLengthReachedException.class)
    public void testMaxLengthCheckGreater() throws IOException {
        long maxLength = 50;
        PipedInputStream inputStream = getInputStream(maxLength + 1);
        IOHelper.write(inputStream, new NullOutputStream(), maxLength);
    }

    /**
     * Tests, if the max length is correctly handled.
     * 
     * @throws IOException
     *             Exception.
     */
    @Test
    public void testMaxLengthCheckLess() throws IOException {
        long maxLength = 50;
        PipedInputStream inputStream = getInputStream(maxLength - 1);
        IOHelper.write(inputStream, new NullOutputStream(), maxLength);
    }
}
