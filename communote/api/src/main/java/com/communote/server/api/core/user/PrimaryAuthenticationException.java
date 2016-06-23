package com.communote.server.api.core.user;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PrimaryAuthenticationException extends Exception {

    /** Possible reasons the exception was thrown. */
    public enum Reasons {
        /** Not found */
        EXTERNAL_SYSTEM_NOT_FOUND,
        /** Not Allowed */
        EXTERNAL_AUTH_NOT_ALLOWED,
        /** Not enough admins */
        NOT_ENOUGH_ADMINS,
        /** Not enough admins within the internal db. */
        NOT_ENOUGH_INTERNAL_ADMINS,
    }

    private static final long serialVersionUID = 555676469073267604L;

    private final Reasons reason;
    private final String systemId;

    /**
     * Constructor.
     *
     * @param message
     *            A message for logging.
     * @param systemId
     *            The external systems id.
     * @param reason
     *            The reason.
     */
    public PrimaryAuthenticationException(String message, String systemId, Reasons reason) {
        super(message + "External system id:" + systemId);
        this.systemId = systemId;
        this.reason = reason;
    }

    /**
     * @return the reason
     */
    public Reasons getReason() {
        return reason;
    }

    /**
     * @return the systemId
     */
    public String getSystemId() {
        return systemId;
    }
}
