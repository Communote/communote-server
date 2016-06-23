package com.communote.server.plugins.api.externals;

import java.util.List;

import com.communote.server.plugins.exceptions.PluginException;


/**
 * Interface to retrieve object from remote systems.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface ExternalObjectAccessor {

    /**
     * 
     * @param <T>
     *            Type of the object.
     * @param externalObjectId
     *            The external objects id.
     * @param objectClass
     *            The class.
     * @return The object.
     * @throws PluginException
     *             Exception.
     */
    public <T> T findRemoteObject(String externalObjectId, Class<T> objectClass)
            throws PluginException;

    /**
     * 
     * @param <T>
     *            Type of the object.
     * @param externalObjectsId
     *            The external objects id.
     * @param objectClass
     *            The class of the Object.
     * @return List of Objects within the id.
     * @throws PluginException
     *             Exception.
     */
    public <T> List<T> findRemoteObjects(String externalObjectsId, Class<T> objectClass)
            throws PluginException;
}
