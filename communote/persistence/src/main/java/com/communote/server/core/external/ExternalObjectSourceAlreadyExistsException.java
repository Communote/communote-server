package com.communote.server.core.external;

/**
 * Thrown if an ExternalObjectSource with the same identifier already exists.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ExternalObjectSourceAlreadyExistsException extends Exception {
    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception
     * 
     * @param sourceId
     *            the ID of the already existing source
     */
    public ExternalObjectSourceAlreadyExistsException(String sourceId) {
        super("External object source with ID " + sourceId + " already exists");
    }
}
