package com.communote.server.plugins.api;

import com.communote.server.plugins.exceptions.PluginException;

/**
 * Interface for plug-ins.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @deprecated Use OSGI / iPojo logic for start and stop methods
 */
@Deprecated
public interface Plugin {

    /**
     * This method will be called when the plug-in starts.
     */
    public void start() throws PluginException;

    /**
     * This method will be called when the plug-in should be stopped.
     */
    public void stop();

}
