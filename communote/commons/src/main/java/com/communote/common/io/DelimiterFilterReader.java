package com.communote.common.io;

import java.io.IOException;
import java.io.Reader;

/**
 * <p>
 * Reader that allows on-the-fly filtering of a character-stream read from another reader. The
 * reader searches for character sequences that are surrounded by a start and an end delimiter. If
 * such a sequence is found it is passed to the provided {@link DelimiterFilterMatchProcessor} which
 * can manipulate the sequence before it is returned to the caller.
 * </p>
 * <p>
 * Since this reader is just reading from the provided reader, it is advisable to wrap that reader
 * in a BufferedReader if the read operation is costly.
 * </p>
 * Note: the reader is not thread-safe
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DelimiterFilterReader extends Reader {

    private final Reader reader;
    private final char[] startDelimiter;
    private final char[] endDelimiter;
    private final String startDelimiterString;
    private final String endDelimiterString;
    private String bufferedContent;
    private int bufferedContentReadPos = 0;
    private boolean eofReached = false;
    private final DelimiterFilterMatchProcessor matchProcessor;

    /**
     * Create a new filtering reader
     * 
     * @param reader
     *            the underlying reader that provides the character stream that should be filtered
     * @param startDelimiter
     *            the delimiter marking the start of a match
     * @param endDelimiter
     *            the delimiter marking the end of a match
     * @param matchProcessor
     *            the processor to invoke when a match was found
     */
    public DelimiterFilterReader(Reader reader, String startDelimiter, String endDelimiter,
            DelimiterFilterMatchProcessor matchProcessor) {
        if (startDelimiter == null || startDelimiter.length() == 0) {
            throw new IllegalArgumentException("The startDelimiter must not be null or empty");
        }
        if (endDelimiter == null || endDelimiter.length() == 0) {
            throw new IllegalArgumentException("The endDelimiter must not be null or empty");
        }
        this.reader = reader;
        this.startDelimiterString = startDelimiter;
        this.startDelimiter = startDelimiter.toCharArray();
        this.endDelimiterString = endDelimiter;
        this.endDelimiter = endDelimiter.toCharArray();
        this.matchProcessor = matchProcessor;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public int read() throws IOException {
        // return any buffered content before continuing to read from the reader
        if (bufferedContent != null) {
            if (bufferedContentReadPos < bufferedContent.length()) {
                return bufferedContent.charAt(bufferedContentReadPos++);
            } else {
                bufferedContent = null;
                bufferedContentReadPos = 0;
                if (eofReached) {
                    return -1;
                }
            }
        }
        int nextChar = reader.read();
        if (nextChar == startDelimiter[0]) {
            // read ahead
            StringBuilder matchedContent = new StringBuilder();
            if (readToEndDelimiter(matchedContent)) {
                String processedContent = this.matchProcessor.processMatch(
                        matchedContent.toString(), startDelimiterString, endDelimiterString);
                if (processedContent == null) {
                    throw new IOException(
                            "Filtering character stream failed because match couldn't be processed");
                }
                bufferedContent = processedContent;
            } else {
                bufferedContent = matchedContent.toString();
            }
            // bufferedContent can be empty or first character might be replaced, so must return
            // result from next read
            nextChar = read();
        }

        return nextChar;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        // just delegate to read()
        int charactersRead = 0;
        for (int i = 0; i < len; i++) {
            int nextChar = this.read();
            if (nextChar == -1) {
                charactersRead = i;
                if (charactersRead == 0) {
                    charactersRead = -1;
                }
                break;
            }
            cbuf[off + i] = (char) nextChar;
        }
        return charactersRead;
    }

    /**
     * Read and store the read characters in the provided buffer. The read operation will stop if
     * EOF was reached or a mismatch in the startDelimiter or endDelimiter was found. If EOF was
     * reached eofReached will be set to true. Expects to be called after the first character of the
     * startDelimiter was read.
     * 
     * @param buffer
     *            the buffer to store the read characters. The first character of the startDelimiter
     *            will also be contained.
     * @return true if the startDelimiter and the endDelimiter matched.
     * @throws IOException
     *             in case of an IOException while reading from the reader
     */
    private boolean readToEndDelimiter(StringBuilder buffer) throws IOException {
        int startDelimiterMatchPos = 1;
        int endDelimiterMatchPos = 0;
        buffer.append(startDelimiter[0]);
        int nextChar;
        // read until startDelimiter was read completely or
        while (startDelimiterMatchPos != startDelimiter.length) {
            nextChar = reader.read();
            if (nextChar != startDelimiter[startDelimiterMatchPos]) {
                if (nextChar == -1) {
                    eofReached = true;
                } else {
                    buffer.append((char) nextChar);
                }
                return false;
            }
            buffer.append((char) nextChar);
            startDelimiterMatchPos++;
        }
        // start delimiter matched and was saved in buffer, now read until end delimiter matched
        nextChar = reader.read();
        while (nextChar != -1) {
            buffer.append((char) nextChar);
            if (nextChar == endDelimiter[endDelimiterMatchPos]) {
                endDelimiterMatchPos++;
                if (endDelimiterMatchPos == endDelimiter.length) {
                    // end found and all chars buffered
                    return true;
                }
            } else {
                if (endDelimiterMatchPos != 0) {
                    endDelimiterMatchPos = 0;
                }
            }
            nextChar = reader.read();
        }
        eofReached = true;
        return false;
    }

    @Override
    public void reset() throws IOException {
        reader.reset();
        eofReached = false;
        bufferedContent = null;
        bufferedContentReadPos = 0;
    }

}
