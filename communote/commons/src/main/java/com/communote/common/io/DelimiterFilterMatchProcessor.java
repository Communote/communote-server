package com.communote.common.io;

/**
 * Handler that is invoked when a {@link DelimiterFilterReader} found a match.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface DelimiterFilterMatchProcessor {

    /**
     * Process a match found by the {@link DelimiterFilterReader}.
     * 
     * @param match
     *            the matched string including the start and end delimiters
     * @param startDelimiter
     *            the delimiter on the left side of the match
     * @param endDelimiter
     *            the delimiter on the right side of the match
     * @return the character sequence to return to the caller of the reader or null if the
     *         processing failed
     */
    String processMatch(String match, String startDelimiter, String endDelimiter);
}
