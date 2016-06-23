package com.communote.server.api.core.security.permission;

import java.util.Set;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface PermissionManagement<T, N, E extends AuthorizationException> {

    /**
     * Adds a filter to the internal list of filters.
     *
     * @param filter
     *            The filter to add.
     */
    void addPermissionFilter(PermissionFilter<T, N> filter);

    /**
     * Returns a collection of permissions for the given entity.
     *
     * @param entityId
     *            ID of the entity
     * @return Collection of permissions for the given entity.
     */
    Set<Permission<T>> getPermissions(Long entityId);

    /**
     * Returns a collection of permissions for the given entity.
     *
     * @param entity
     *            The entity.
     * @return Collection of permissions for the given entity.
     */
    Set<Permission<T>> getPermissions(T entity);

    /**
     * Returns a collection of permissions for creation for the given entity.
     *
     * @param entity
     *            The entity, can be TO or null to check in general
     * @return Collection of permissions for the given entity.
     */
    Set<Permission<T>> getPermissionsForCreation(N entity);

    /**
     * Checks the permission and returns the given entity converted with the provided converter if
     * the current user has the requested permission.
     *
     * @param entityId
     *            ID of the entity
     * @param permission
     *            The permission the user must have.
     * @param converter
     *            the converter to convert the result
     * @return The entity.
     * @throws E
     *             in case the current user doesn't have the given permission.
     * @throws NotFoundException
     *             in case the entity does not exist
     * @param <R>
     *            the target type of the conversion
     */
    <R> R hasAndGetWithPermission(long entityId, Permission<T> permission, Converter<T, R> converter)
            throws E, NotFoundException;

    /**
     * Checks if the current user has the permission of the given entity.
     *
     * @param entityId
     *            ID of the entity
     * @param permission
     *            The permission to check.
     * @return True, if the user has the permission on the entity.
     */
    boolean hasPermission(long entityId, Permission<T> permission);

    /**
     * Checks if the current user has the permission of the given entity.
     *
     * @param entityId
     *            ID of the entity
     * @param permissionIdentifier
     *            The permission to check.
     * @return True, if the user has the permission on the entity.
     */
    boolean hasPermission(long entityId, String permissionIdentifier);

    /**
     * Checks if the current user has the permission to create the given entity, or to create
     * entities of the associated type in general
     *
     * @param entityId
     *            ID of the entity
     * @param permission
     *            The permission to check.
     * @return True, if the user has the permission on the entity.
     */
    boolean hasPermissionForCreation(Permission<T> permission);

    /**
     * Removes a filter from the internal list of filters.
     *
     * @param filter
     *            The filter to remove.
     */
    void removePermissionFilter(PermissionFilter<T, N> filter);

}