package com.communote.server.api.core.security.permission;

import java.util.Set;

import com.communote.common.util.Orderable;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <T>
 *            Type of the entity.
 * @param <N>
 *            Type of the TO that is used for a new entity, to check if it can be created
 */
public interface PermissionFilter<T, N> extends Orderable {
    /**
     * Filters the given permissions for the given entity.
     * 
     * @param entity
     *            The entity.
     * @param permissions
     *            The set of permissions.
     */
    void filter(T entity, Set<Permission<T>> permissions);

    /**
     * Filters the given permissions for the given entity/TO.
     * 
     * @param entity
     *            The creation entity, in most cases a TO. Can be null. If null check the general
     *            permissions for the type of entity.
     * @param permissions
     *            The set of permissions.
     */
    void filterForCreation(N entity, Set<Permission<T>> permissions);
}
