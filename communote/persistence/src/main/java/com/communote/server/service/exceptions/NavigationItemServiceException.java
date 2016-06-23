package com.communote.server.service.exceptions;

/**
 * Exception thrown by the {@link com.communote.server.service.NavigationItemService} in case of
 * unexpected errors.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NavigationItemServiceException extends RuntimeException {

    /**
     * Default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with detail message and cause
     * 
     * @param message
     *            the details
     * @param cause
     *            the cause
     */
    public NavigationItemServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
