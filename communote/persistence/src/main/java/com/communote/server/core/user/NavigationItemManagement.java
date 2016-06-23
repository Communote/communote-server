package com.communote.server.core.user;

import java.util.Collection;
import java.util.List;

import com.communote.common.converter.CollectionConverter;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.vo.user.NavigationItemDataTO;
import com.communote.server.core.vo.user.NavigationItemTO;
import com.communote.server.model.user.NavigationItem;
import com.communote.server.service.exceptions.NavigationItemDataSerializationException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface NavigationItemManagement {

    /**
     * Create the built-in immutable navigation items for the current user if they do not yet exist.
     *
     * @param userId
     *            The ID of the user to update
     * @return true if the items where created, false if they already existed
     * @throws AuthorizationException
     *             If the current user is not the provided user or the internal system user
     */
    public boolean createBuiltInNavigationItems(Long userId) throws AuthorizationException;

    /**
     * Delete a given item.
     *
     * @param navigationItemId
     *            ID of the item to delete.
     * @return whether the item existed
     * @throws AuthorizationException
     *             in case the current user is not the owner of the item
     */
    public boolean delete(Long navigationItemId) throws AuthorizationException;

    /**
     * Method to find all items for the current user.
     *
     * @param type
     *            the type of the data of the item
     * @param navigationItemIds
     *            Id's of items to load or null, if all.
     * @return List of loaded navigation items.
     * @throws NavigationItemDataSerializationException
     *             Thrown, when the given data can't be converted into the given type.
     * @param <DATA_TYPE>
     *            the type of the data of the item
     */
    public <DATA_TYPE extends NavigationItemDataTO> List<NavigationItemTO<DATA_TYPE>> find(
            Class<DATA_TYPE> type, Long... navigationItemIds)
            throws NavigationItemDataSerializationException;

    /**
     * Method to find all navigation items for the current user.
     *
     * @param converter
     *            The converter to use for result converting.
     * @param ids
     *            Ids of items to get or null to get all items.
     * @param <T>
     *            Type of the result.
     * @return List of results. This never returns null.
     */
    public <T> Collection<T> get(CollectionConverter<NavigationItem, T> converter, Long... ids);

    /**
     * Method to get an item for the given user.
     *
     * @param notificationItemId
     *            Id of the item to get.
     * @param type
     *            Type of the data, if null, the data won't be deserialized.
     * @return Null, if the item doesn't exists for the given user (this exits quietly).
     * @throws NavigationItemDataSerializationException
     *             Thrown, when the given data can't be converted into the given type.
     * @param <DATA_TYPE>
     *            the type of the data of the item
     */
    public <DATA_TYPE extends NavigationItemDataTO> NavigationItemTO<DATA_TYPE> get(
            Long notificationItemId, Class<DATA_TYPE> type)
            throws NavigationItemDataSerializationException;

    /**
     * Updates the given item or creates a new one.
     *
     * @param navigationItem
     *            Item to update.
     * @return Id of the created or updated item.
     * @throws AuthorizationException
     *             in case the item to update exists but the current user is not the owner of it
     * @throws NavigationItemDataSerializationException
     *             Thrown, when the given data can't be converted into JSON
     * @param <T>
     *            the type of the data of the item
     */
    public <T extends NavigationItemDataTO> Long store(NavigationItemTO<T> navigationItem) throws
            AuthorizationException, NavigationItemDataSerializationException;

}