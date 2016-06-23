package com.communote.server.core.security.iprange;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InvalidIpRangeException extends
        com.communote.server.core.security.iprange.IpRangeException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3552640659193492038L;

    /**
     * Constructs a new instance of InvalidIpRangeException
     *
     */
    public InvalidIpRangeException(String message, String ip) {
        super(message, ip);
    }

    /**
     * Constructs a new instance of InvalidIpRangeException
     *
     */
    public InvalidIpRangeException(String message, Throwable throwable, String ip) {
        super(message, throwable, ip);
    }

}
