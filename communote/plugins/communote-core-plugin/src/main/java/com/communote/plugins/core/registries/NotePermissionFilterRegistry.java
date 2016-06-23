package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.core.permission.filters.NotePermissionFilter;

/**
 * Registry for {@link NotePermissionFilter}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "NotePermissionFilterRegistry")
public class NotePermissionFilterRegistry {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NotePermissionFilterRegistry.class);

    /**
     * Registers the given filter.
     * 
     * @param permissionFilter
     *            The filter to register.
     */
    @Bind(id = "registerFilter", optional = true, aggregate = true)
    public void registerPermissionFilter(NotePermissionFilter permissionFilter) {
        ServiceLocator.instance().getService(NotePermissionManagement.class)
                .addPermissionFilter(permissionFilter);
        LOGGER.debug("Added note permission filter {}", permissionFilter.getClass().getName());
    }

    /**
     * Removes the given filter.
     * 
     * @param permissionFilter
     *            The filter to remove.
     */
    @Unbind(id = "registerFilter", optional = true, aggregate = true)
    public void removePermissionFilter(NotePermissionFilter permissionFilter) {
        ServiceLocator.instance().getService(NotePermissionManagement.class)
                .removePermissionFilter(permissionFilter);
        LOGGER.debug("Removed note permission filter {}", permissionFilter.getClass().getName());
    }
}
