package com.communote.server.core.vo.content;

import java.io.IOException;

/**
 * Exception to indicate that a stream has already been closed and cannot be accessed any further
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class StreamAlreadyClosedExcpetion extends IOException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param message
     *            the message
     */
    public StreamAlreadyClosedExcpetion(String message) {
        super(message);
    }
}
