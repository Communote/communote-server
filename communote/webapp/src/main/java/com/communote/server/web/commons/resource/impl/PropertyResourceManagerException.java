package com.communote.server.web.commons.resource.impl;

/**
 * Exception thrown by the {@link PropertyResourceManager} when a property resource couldn't be
 * added, updated or removed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PropertyResourceManagerException extends Exception {

    private static final long serialVersionUID = 1L;

    private boolean currentPropertyResourceChanged;

    /**
     * Create a new exception with detail message.
     * 
     * @param message
     *            the details describing the exception
     * @param currentPropertyResourceChanged
     *            whether the current property resource was changed and caches containing this
     *            property resource should be refreshed
     */
    public PropertyResourceManagerException(String message, boolean currentPropertyResourceChanged) {
        super(message);
        this.currentPropertyResourceChanged = currentPropertyResourceChanged;
    }

    /**
     * Create a new exception with detail message.
     * 
     * @param message
     *            the details describing the exception
     * @param cause
     *            the cause of the exception
     * @param currentPropertyResourceChanged
     *            whether the current property resource was changed and caches containing this
     *            property resource should be refreshed
     */
    public PropertyResourceManagerException(String message, Throwable cause,
            boolean currentPropertyResourceChanged) {
        super(message, cause);
        this.currentPropertyResourceChanged = currentPropertyResourceChanged;
    }

    /**
     * @return whether the current property resource was changed and caches containing this property
     *         resource should be refreshed
     */
    public boolean isCurrentPropertyResourceChanged() {
        return currentPropertyResourceChanged;
    }

    /**
     * Set whether the current property resource was changed and caches containing this property
     * resource should be refreshed
     * 
     * @param currentPropertyResourceChanged
     *            true if it was changed
     */
    public void setCurrentPropertyResourceChanged(boolean currentPropertyResourceChanged) {
        this.currentPropertyResourceChanged = currentPropertyResourceChanged;
    }
}
