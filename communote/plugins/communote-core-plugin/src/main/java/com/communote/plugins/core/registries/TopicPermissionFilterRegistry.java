package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.core.permission.filters.TopicPermissionFilter;

/**
 * Registry for {@link TopicPermissionFilter}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "TopicPermissionFilterRegistry")
public class TopicPermissionFilterRegistry {
    /**
     * Registers the given filter.
     * 
     * @param permissionFilter
     *            The filter to register.
     */
    @Bind(id = "registerFilter", optional = true, aggregate = true)
    public void registerPermissionFilter(TopicPermissionFilter permissionFilter) {
        ServiceLocator.instance().getService(TopicPermissionManagement.class)
                .addPermissionFilter(permissionFilter);
    }

    /**
     * Removes the given filter.
     * 
     * @param permissionFilter
     *            The filter to remove.
     */
    @Unbind(id = "registerFilter", optional = true, aggregate = true)
    public void removePermissionFilter(TopicPermissionFilter permissionFilter) {
        ServiceLocator.instance().getService(TopicPermissionManagement.class)
                .removePermissionFilter(permissionFilter);
    }

}
