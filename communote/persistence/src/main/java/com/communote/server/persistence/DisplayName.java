package com.communote.server.persistence;

/**
 * Interface for rendering a displayable name
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface DisplayName {

    /**
     * @return get the name that can be displayed
     */
    public String getDisplayName();

    /**
     * @return returns a shorter version of the display name. This should return the same value as
     *         {@link #getDisplayName()} if no shorter version is available.
     */
    public String getShortDisplayName();
}
