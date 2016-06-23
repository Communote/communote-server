package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.persistence.tag.TagStore;

/**
 * Registry for TagStores
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "TagStoreRegistry")
public class TagStoreRegistry {
    /**
     * @param tagStore
     *            The TagStore to add.
     */
    @Bind(id = "registerTagStore", optional = true, aggregate = true)
    public void registerTagStore(TagStore tagStore) {
        ServiceLocator.instance().getService(TagStoreManagement.class).addTagStore(tagStore);
    }

    /**
     * Removes the given TagStore from the list of processors.
     * 
     * @param tagStore
     *            The TagStore to remove.
     */
    @Unbind(id = "registerTagStore", optional = true, aggregate = true)
    public void removeTagStore(TagStore tagStore) {
        ServiceLocator.instance().getService(TagStoreManagement.class).removeTagStore(tagStore);
    }
}
