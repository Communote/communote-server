package com.communote.server.core.security.iprange;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3702441825343297955L;

    private String ip;

    /**
     * Constructs a new instance of IpRangeException
     *
     */
    public IpRangeException(String message, String ip) {
        super(message);
        this.ip = ip;
    }

    /**
     * Constructs a new instance of IpRangeException
     *
     */
    public IpRangeException(String message, Throwable throwable, String ip) {
        super(message, throwable);
        this.ip = ip;
    }

    /**
     *
     */
    public String getIp() {
        return this.ip;
    }

    @Override
    public String getMessage() {
        // TODO Auto-generated method stub
        String message = super.getMessage();
        if (this.ip != null) {
            message = message + " (IP: " + ip + ")";
        }
        return message;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

}
