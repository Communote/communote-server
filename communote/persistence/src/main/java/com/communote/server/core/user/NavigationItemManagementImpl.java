package com.communote.server.core.user;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.CollectionConverter;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.user.NavigationItemDataTO;
import com.communote.server.core.vo.user.NavigationItemTO;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.NavigationItem;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.NavigationItemDao;
import com.communote.server.service.exceptions.NavigationItemDataSerializationException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class NavigationItemManagementImpl implements NavigationItemManagement {

    private final static Logger LOGGER = LoggerFactory.getLogger(NavigationItemManagementImpl.class);

    private static final String BUILT_IN_ITEM_NAME_MENTIONS = "mentions";
    private static final String BUILT_IN_ITEM_NAME_FOLLOWING = "following";
    private static final String BUILT_IN_ITEM_CONTEXT = "notesOverview";

    @Autowired
    private NavigationItemDao navigationItemDao;

    @Autowired
    private UserDao kenmeiUserDao;

    /**
     * Create the built-in immutable navigation items for the current user if they do not yet exist.
     * 
     * @param userId
     *            The ID of the user to update
     * @return true if the items where created, false if they already existed
     * @throws AuthorizationException
     *             If the current user is not the provided user or the internal system user
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean createBuiltInNavigationItems(Long userId) throws AuthorizationException {
        Long currentUserId = SecurityHelper.getCurrentUserId();
        if (!SecurityHelper.isInternalSystem() && !userId.equals(currentUserId)) {
            throw new AuthorizationException("The current user " + currentUserId
                    + " is not allowed to create the built-in navigation items for user " + userId);
        }
        User userToUpdate = kenmeiUserDao.load(userId);
        if (!ArrayUtils.contains(userToUpdate.getRoles(), UserRole.ROLE_KENMEI_USER)) {
            return false;
        }
        List<NavigationItem> items = navigationItemDao.find(userToUpdate.getId());
        // check if already exists
        boolean mentionsExist = false;
        boolean followingExists = false;
        for (NavigationItem item : items) {
            if (!isMutable(item)) {
                if (BUILT_IN_ITEM_NAME_FOLLOWING.equals(item.getName())) {
                    followingExists = true;
                    if (mentionsExist) {
                        break;
                    }
                } else if (BUILT_IN_ITEM_NAME_MENTIONS.equals(item.getName())) {
                    mentionsExist = true;
                    if (followingExists) {
                        break;
                    }
                }
            }
        }
        return createMentionsAndFollowingItem(followingExists, mentionsExist, userToUpdate);
    }

    /**
     * Helper to create the mentions and following immutable navigation items for the provided user
     * if they do not exist.
     * 
     * @param followingExists
     *            whether the item already exists
     * @param mentionsExist
     *            whether the item already exists
     * @param user
     *            the user to create the items for
     * @return true if at least one was created
     */
    private boolean createMentionsAndFollowingItem(boolean followingExists, boolean mentionsExist,
            User user) {
        if (followingExists && mentionsExist) {
            return false;
        }
        FilterWidgetParameterNameProvider nameProvider = FilterWidgetParameterNameProvider.INSTANCE;
        NavigationItemDataTO itemData = new NavigationItemDataTO();
        itemData.setContextType(BUILT_IN_ITEM_CONTEXT);
        if (!followingExists) {
            String followParamName = nameProvider.getNameForFollowedNotes();
            itemData.getFilters().put(followParamName, true);
            storeImmutableItem(BUILT_IN_ITEM_NAME_FOLLOWING, itemData, user);
            itemData.getFilters().remove(followParamName);
        }
        if (!mentionsExist) {
            itemData.getFilters().put(nameProvider.getNameForShowPostsForMe(), true);
            storeImmutableItem(BUILT_IN_ITEM_NAME_MENTIONS, itemData, user);
        }
        return true;
    }

    /**
     * Delete a given item.
     * 
     * @param navigationItemId
     *            ID of the item to delete.
     * @return whether the item existed
     * @throws AuthorizationException
     *             in case the current user is not the owner of the item
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean delete(Long navigationItemId) throws AuthorizationException {
        NavigationItem item = navigationItemDao.load(navigationItemId);
        User currentUser = kenmeiUserDao.load(SecurityHelper.assertCurrentUserId());
        if (item != null) {
            if (!item.getOwner().equals(currentUser)) {
                throw new AuthorizationException(
                        "You are not allowed to update this navigation item.");
            }
            // silently ignore immutable items
            if (isMutable(item)) {
                navigationItemDao.remove(navigationItemId);
                return true;
            }
        }
        return false;
    }

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
    @Override
    @Transactional(readOnly = true)
    public <DATA_TYPE extends NavigationItemDataTO> List<NavigationItemTO<DATA_TYPE>> find(
            Class<DATA_TYPE> type, Long... navigationItemIds)
            throws NavigationItemDataSerializationException {
        List<NavigationItemTO<DATA_TYPE>> result = new ArrayList<NavigationItemTO<DATA_TYPE>>();
        Long currentUserId = SecurityHelper.getCurrentUserId();
        if (currentUserId != null) {
            Collection<NavigationItem> navigationItems = navigationItemDao.find(
                    currentUserId, navigationItemIds);
            for (NavigationItem item : navigationItems) {
                result.add(toNavigationItemTO(item, type));
            }
        }
        return result;
    }

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
    @Override
    @Transactional(readOnly = true)
    public <T> Collection<T> get(CollectionConverter<NavigationItem, T> converter, Long... ids) {
        return converter.convert(navigationItemDao.find(SecurityHelper.getCurrentUserId(), ids));
    }

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
    @Override
    @Transactional(readOnly = true)
    public <DATA_TYPE extends NavigationItemDataTO> NavigationItemTO<DATA_TYPE> get(
            Long notificationItemId, Class<DATA_TYPE> type)
            throws NavigationItemDataSerializationException {
        Collection<NavigationItem> navigationItems = navigationItemDao.find(
                SecurityHelper.getCurrentUserId(), notificationItemId);
        if (navigationItems.size() == 0) {
            return null;
        }
        NavigationItem navigationItem = navigationItems.iterator().next();
        NavigationItemTO<DATA_TYPE> result = toNavigationItemTO(navigationItem, type);
        return result;
    }

    /**
     * Update the indexes of the navigation items of the current user after the index of one
     * navigation item was modified.
     * 
     * @param modifiedItem
     *            the item whose index was modified
     * @param currentUserId
     *            the ID of the current user
     */
    private void internalUpdateIndexes(NavigationItem modifiedItem, Long currentUserId) {
        List<NavigationItem> items = navigationItemDao.find(currentUserId);
        // filter current and immutable items
        Iterator<NavigationItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            NavigationItem curItem = iterator.next();
            if (curItem.getId().equals(modifiedItem.getId()) || !isMutable(curItem)) {
                iterator.remove();
            }
        }
        int index = modifiedItem.getItemIndex();
        if (items.size() == 0 || index >= items.size()) {
            items.add(modifiedItem);
        } else {
            items.add(index < 0 ? index = 0 : index, modifiedItem);
        }
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setItemIndex(i);
        }
    }

    /**
     * Return whether an item is mutable. An item is mutable if its index is greater than 0.
     * 
     * @param item
     *            the item to test
     * @return true if the item is mutable, false otherwise
     */
    private boolean isMutable(NavigationItem item) {
        return item.getItemIndex() >= 0;
    }

    /**
     * Convert the data into a JSON string.
     * 
     * @param data
     *            the data to convert
     * @return the JSON as string
     * @throws NavigationItemDataSerializationException
     *             in case the serialization failed
     * @param <T>
     *            the type of the data of the item
     */
    private <T extends NavigationItemDataTO> String serializeToJsonString(T data)
            throws NavigationItemDataSerializationException {
        try {
            return JsonHelper.getSharedObjectMapper().writeValueAsString(data);
        } catch (IOException e) {
            throw new NavigationItemDataSerializationException(
                    "Serialization into JSON string failed", e);
        }
    }

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
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public <T extends NavigationItemDataTO> Long store(NavigationItemTO<T> navigationItem) throws
            AuthorizationException, NavigationItemDataSerializationException {
        NavigationItem itemToStore = navigationItem.getId() == null ? null : navigationItemDao
                .load(navigationItem.getId());
        boolean indexChanged = true;
        // force positive index value as negative ones are preserved for the immutable items
        int indexToSet = navigationItem.getIndex() < 0 ? 0 : navigationItem.getIndex();
        User currentUser = kenmeiUserDao.load(SecurityHelper.assertCurrentUserId());
        Timestamp lastAccess = new Timestamp(navigationItem.getLastAccessDate() == null
                ? System.currentTimeMillis() : navigationItem.getLastAccessDate().getTime());
        if (itemToStore == null) {
            itemToStore = NavigationItem.Factory.newInstance();
            itemToStore.setOwner(currentUser);
            itemToStore.setLastAccessDate(lastAccess);
            itemToStore.setItemIndex(indexToSet);
            itemToStore.setData(serializeToJsonString(navigationItem.getData()));
            itemToStore.setName(navigationItem.getName());
            navigationItemDao.create(itemToStore);
        } else if (!itemToStore.getOwner().equals(currentUser)) {
            throw new AuthorizationException(
                    "You are not allowed to update this navigation item.");
        } else {
            // convert before updating any values so there is no need to roll back
            if (isMutable(itemToStore)) {
                String dataJsonString = serializeToJsonString(navigationItem.getData());
                itemToStore.setLastAccessDate(lastAccess);
                indexChanged = indexToSet != itemToStore.getItemIndex();
                itemToStore.setItemIndex(indexToSet);
                itemToStore.setData(dataJsonString);
                itemToStore.setName(navigationItem.getName());
            } else {
                // do not modify anything except lastAccess date of immutable items
                itemToStore.setLastAccessDate(lastAccess);
                indexChanged = false;
            }
        }
        if (indexChanged) {
            internalUpdateIndexes(itemToStore, currentUser.getId());
        }
        return itemToStore.getId();
    }

    /**
     * Create and store a new immutable navigation item.
     * 
     * @param name
     *            the name of the item
     * @param data
     *            the data to store
     * @param currentUser
     *            the current user for whom the item should be stored
     */
    private void storeImmutableItem(String name, NavigationItemDataTO data, User currentUser) {
        NavigationItem itemToStore = NavigationItem.Factory.newInstance();
        itemToStore.setOwner(currentUser);
        itemToStore.setLastAccessDate(new Timestamp(System.currentTimeMillis()));
        itemToStore.setItemIndex(-1);
        try {
            itemToStore.setData(serializeToJsonString(data));
            itemToStore.setName(name);
            navigationItemDao.create(itemToStore);
        } catch (NavigationItemDataSerializationException e) {
            // should not occur since converting a NavigationItemDataTO with simple values
            LOGGER.error("Unexpected exception creating a built-in navigation item", e);
            // TODO throw a NavigationManagementException (RuntimeException)?
        }
    }

    /**
     * @param navigationItem
     *            The item to convert.
     * @param type
     *            Type of data.
     * @param <DATA_TYPE>
     *            Type of the data.
     * @return The converted item.
     * @throws NavigationItemDataSerializationException
     *             Thrown, when something went wrong deserializing the data.
     */
    private <DATA_TYPE extends NavigationItemDataTO> NavigationItemTO<DATA_TYPE> toNavigationItemTO(
            NavigationItem navigationItem, Class<DATA_TYPE> type)
            throws NavigationItemDataSerializationException {
        NavigationItemTO<DATA_TYPE> result = new NavigationItemTO<DATA_TYPE>();
        result.setId(navigationItem.getId());
        result.setIndex(navigationItem.getItemIndex());
        if (type != null) {
            try {
                result.setData(JsonHelper.getSharedObjectMapper().readValue(
                        navigationItem.getData(),
                        type));
            } catch (IOException e) {
                throw new NavigationItemDataSerializationException(
                        "Deserialization from JSON string failed", e);
            }
        }
        result.setDataAsJson(navigationItem.getData());
        result.setLastAccessDate(navigationItem.getLastAccessDate());
        result.setName(navigationItem.getName());
        return result;
    }
}
