package com.communote.server.core.plugin;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PluginPropertyManagementException extends Exception {

    private static final long serialVersionUID = 1158228355852411154L;

    /**
     * Constructor.
     * 
     * @param cause
     *            The cause.
     */
    public PluginPropertyManagementException(Throwable cause) {
        super(cause);
    }
}
