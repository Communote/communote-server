package com.communote.server.core.security.iprange;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CurrentIpNotInRange extends
        com.communote.server.core.security.iprange.IpRangeException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -9195599209771439302L;

    /**
     * Constructs a new instance of CurrentIpNotInRange
     *
     */
    public CurrentIpNotInRange(String message, String ip) {
        super(message, ip);
    }

}
