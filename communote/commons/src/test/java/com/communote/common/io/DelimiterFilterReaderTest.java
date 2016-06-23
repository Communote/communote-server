package com.communote.common.io;

import java.io.StringReader;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link DelimiterFilterReader}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class DelimiterFilterReaderTest {

    /**
     * Simple match processor that replaces the match with some string
     * 
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     * 
     */
    private class ReplacingMatchProcessor implements DelimiterFilterMatchProcessor {
        private final boolean keepDelimiter;
        private final String replacement;

        /**
         * Create a processor
         * 
         * @param keepDelimiter
         *            whether to keep the delimiter
         * @param replacement
         *            the replacement to return when processing a match
         */
        public ReplacingMatchProcessor(boolean keepDelimiter, String replacement) {
            this.keepDelimiter = keepDelimiter;
            this.replacement = replacement;
        }

        @Override
        public String processMatch(String match, String startDelimiter, String endDelimiter) {
            if (keepDelimiter) {
                return startDelimiter + replacement + endDelimiter;
            }
            return replacement;
        }
    }

    /**
     * Read into a string
     * 
     * @param reader
     *            the reader to read from
     * @return the read content
     * @throws Exception
     *             in case reading failed
     */
    private String readToString(DelimiterFilterReader reader) throws Exception {
        StringBuilder outBuffer = new StringBuilder();
        int nextChar = reader.read();
        while (nextChar != -1) {
            outBuffer.append((char) nextChar);
            nextChar = reader.read();
        }
        return outBuffer.toString();
    }

    /**
     * Test whether the filter can handle delimiters with more than one character.
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testMultiCharDelimiter() throws Exception {
        StringReader sr = new StringReader(
                "abc ${content ${'old - content '} more text ${'abc' test'}");
        DelimiterFilterReader filterReader = new DelimiterFilterReader(sr, "${'", "'}",
                new ReplacingMatchProcessor(true, "replaced"));
        Assert.assertEquals("abc ${content ${'replaced'} more text ${'replaced'}",
                readToString(filterReader));
        sr.reset();
        filterReader = new DelimiterFilterReader(sr, "${'", "'}",
                new ReplacingMatchProcessor(false, "replaced"));
        Assert.assertEquals("abc ${content replaced more text replaced",
                readToString(filterReader));
        sr.reset();
        filterReader = new DelimiterFilterReader(sr, "${'", "'}",
                new ReplacingMatchProcessor(false, ""));
        Assert.assertEquals("abc ${content  more text ",
                readToString(filterReader));
    }
}
