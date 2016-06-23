package com.communote.server.core.security.iprange;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InvalidIpAddressException extends
com.communote.server.core.security.iprange.IpRangeException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7045454455793423219L;

    /**
     * Constructs a new instance of InvalidIpAddressException
     *
     */
    public InvalidIpAddressException(String message, String ip) {
        super(message, ip);
    }

}
