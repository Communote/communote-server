package com.communote.server.api.core.installer;


/**
 * Exception to be thrown if a step of the installer failed.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CommunoteInstallerException extends Exception {

    /**
     * default serial version ID
     */
    private static final long serialVersionUID = 1L;

    public CommunoteInstallerException(String message, Throwable t) {
        super(message, t);
    }

}
