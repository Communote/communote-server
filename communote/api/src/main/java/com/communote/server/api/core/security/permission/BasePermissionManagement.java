package com.communote.server.api.core.security.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.Converter;
import com.communote.common.util.DescendingOrderComparator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;

/**
 * Service for providing permissions for a given entity.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            Type of the entity, this service works for.
 * @param <N>
 *            Type of the according creation entity, this service works for.
 * @param <E>
 *            The type of the exception to be thrown when the current user does not have a required
 *            permission.
 */
public abstract class BasePermissionManagement<T, N, E extends AuthorizationException> implements
        PermissionManagement<T, N, E> {

    private final List<PermissionFilter<T, N>> filters = new ArrayList<PermissionFilter<T, N>>();

    @Override
    public void addPermissionFilter(PermissionFilter<T, N> filter) {
        getFilters().add(filter);
        Collections.sort(getFilters(), new DescendingOrderComparator());
    }

    /**
     * Create an exception that should be thrown when the current user does not have a required
     * permission
     *
     * @param entity
     *            the entity for which a permission was required, never null
     * @param permission
     *            the permission the current user does not have
     * @return the exception
     */
    protected abstract E createPermissonViolationException(T entity, Permission<T> permission);

    /**
     * Returns the entity for the given id.
     *
     * @param entityId
     *            ID of the entity
     * @return The entity or null, if the entity does not exist
     */
    abstract protected T getEntity(Long entityId);

    /**
     * @return The registered filters.
     */
    protected List<PermissionFilter<T, N>> getFilters() {
        return filters;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Permission<T>> getPermissions(Long entityId) {
        return getPermissions(getEntity(entityId));
    }

    @Override
    public Set<Permission<T>> getPermissions(T entity) {
        Set<Permission<T>> permissions = new HashSet<Permission<T>>();
        if (entity == null) {
            return permissions;
        }
        for (PermissionFilter<T, N> filter : filters) {
            filter.filter(entity, permissions);
        }
        return permissions;
    }

    @Override
    public Set<Permission<T>> getPermissionsForCreation(N entity) {
        Set<Permission<T>> permissions = new HashSet<Permission<T>>();
        for (PermissionFilter<T, N> filter : filters) {
            filter.filterForCreation(entity, permissions);
        }
        return permissions;
    }

    @Override
    @Transactional(readOnly = true)
    public <R> R hasAndGetWithPermission(long entityId, Permission<T> permission,
            Converter<T, R> converter) throws E, NotFoundException {
        T entity = getEntity(entityId);
        if (entity != null) {
            if (getPermissions(entity).contains(permission)) {
                return converter.convert(entity);
            }
            throw createPermissonViolationException(entity, permission);
        }
        throw new NotFoundException("The entity with ID " + entityId + " does not exist");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(long entityId, Permission<T> permission) {
        return getPermissions(entityId).contains(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(long entityId, String permissionIdentifier) {
        return hasPermission(entityId, new Permission<T>(permissionIdentifier));
    }

    @Override
    public boolean hasPermissionForCreation(Permission<T> permission) {
        return getPermissionsForCreation(null).contains(permission);
    }

    @Override
    public void removePermissionFilter(PermissionFilter<T, N> filter) {
        getFilters().remove(filter);
    }

}
