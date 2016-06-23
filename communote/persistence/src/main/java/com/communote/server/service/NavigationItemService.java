package com.communote.server.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.common.caching.AbstractCacheElementProvider;
import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.common.caching.IdBasedCacheKey;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.NavigationItemManagement;
import com.communote.server.core.vo.user.NavigationItemDataTO;
import com.communote.server.core.vo.user.NavigationItemTO;
import com.communote.server.service.exceptions.NavigationItemDataSerializationException;
import com.communote.server.service.exceptions.NavigationItemServiceException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class NavigationItemService {

    private final class NavigationItemCacheElementProvider
    extends
    AbstractCacheElementProvider<IdBasedCacheKey, ArrayList<NavigationItemTO<NavigationItemDataTO>>> {

        public NavigationItemCacheElementProvider() {
            super("navigationitems", 3600);
        }

        @Override
        public ArrayList<NavigationItemTO<NavigationItemDataTO>> load(IdBasedCacheKey key) {
            try {
                return new ArrayList<NavigationItemTO<NavigationItemDataTO>>(
                        navigationItemManagement.find(NavigationItemDataTO.class));
            } catch (NavigationItemDataSerializationException e) {
                LOGGER.error(
                        "Deserialization of persisted navigation items failed. Data corrupted in database?",
                        e);
                // TODO better throw CacheElementProviderException?
                throw new NavigationItemServiceException("Loading navigation items failed",
                        e);
            }
        }

    }

    private final static Logger LOGGER = LoggerFactory.getLogger(NavigationItemService.class);

    @Autowired
    private NavigationItemManagement navigationItemManagement;

    @Autowired
    private CacheManager cacheManager;

    private final NavigationItemCacheElementProvider cacheElementProvider = new NavigationItemCacheElementProvider();

    /**
     * Create the built-in navigation items for the provided user if they do not yet exist.
     *
     * @param userId
     *            The ID of the user for whom the items should be created, if omitted the current
     *            user will be taken
     * @throws AuthorizationException
     *             If the current user is not the provided user or the internal system user
     */
    public void createBuiltInNavigationItems(Long userId) throws AuthorizationException {
        // note: not checking against cache whether items already exist before creating them to
        // avoid an additional read (and thus another transaction). The method is expected to not be
        // called that often.
        if (userId == null) {
            userId = SecurityHelper.getCurrentUserId();
        }
        if (userId != null) {
            if (navigationItemManagement.createBuiltInNavigationItems(userId)) {
                invalidateCache(userId);
            }
        }
    }

    /**
     * Deletes the given item.
     *
     * @param navigationItemId
     *            Item to delete.
     * @throws AuthorizationException
     *             in case the current user is not the owner of the item
     */
    public void delete(Long navigationItemId) throws AuthorizationException {
        if (navigationItemManagement.delete(navigationItemId)) {
            invalidateCache(SecurityHelper.getCurrentUserId());
        }
    }

    /**
     * Get the matching navigation items of the current user.
     *
     * @param navigationItemIds
     *            IDs of the items to load. Provide no ID to get all
     * @return List of loaded navigation items.
     */
    public List<NavigationItemTO<NavigationItemDataTO>> find(Long... navigationItemIds) {
        List<NavigationItemTO<NavigationItemDataTO>> filteredResult;
        filteredResult = new ArrayList<NavigationItemTO<NavigationItemDataTO>>();
        Long currentUserId = SecurityHelper.getCurrentUserId();
        if (currentUserId != null && navigationItemIds != null) {
            Cache cache = cacheManager.getCache();
            List<NavigationItemTO<NavigationItemDataTO>> cachedResult;
            IdBasedCacheKey cacheKey = new IdBasedCacheKey(currentUserId);
            if (cache != null) {
                cachedResult = cache.get(cacheKey, cacheElementProvider);
            } else {
                cachedResult = cacheElementProvider.load(cacheKey);
            }

            if (navigationItemIds.length == 0) {
                // always clone cached result as it should not be modifiable by caller
                for (NavigationItemTO<NavigationItemDataTO> item : cachedResult) {
                    filteredResult.add(NavigationItemTO.clone(item));
                }
            } else {
                List<Long> itemIds = Arrays.asList(navigationItemIds);
                for (NavigationItemTO<NavigationItemDataTO> item : cachedResult) {
                    if (itemIds.contains(item.getId())) {
                        filteredResult.add(NavigationItemTO.clone(item));
                    }
                }
            }
        }
        return filteredResult;
    }

    /**
     * Get a specific navigation item.
     *
     * @param navigationItemId
     *            ID of the item to get
     * @return The item or null if it does not exist or is not owned by the current user.
     */
    public NavigationItemTO<NavigationItemDataTO> get(Long navigationItemId) {
        List<NavigationItemTO<NavigationItemDataTO>> items = this.find(navigationItemId);
        if (items.size() == 1) {
            return items.get(0);
        }
        return null;
    }

    /**
     * Invalidate the navigation item cache for the given user.
     *
     * @param userId
     *            The ID of the user for whom the cache should be invalidated
     */
    private void invalidateCache(Long userId) {
        Cache cache = cacheManager.getCache();
        if (cache != null) {
            cache.invalidate(new IdBasedCacheKey(userId), cacheElementProvider);
        }
    }

    /**
     * Update a navigation item.
     *
     * @param navigationItemTO
     *            the item to update.
     * @param name
     *            The name to set. If null the current name will be used.
     * @param index
     *            Index of the item. If null, this will not be updated.
     * @param lastAccessDate
     *            The last access date. If null, this will not be updated.
     * @param data
     *            The data. If null, this will not be updated.
     *
     * @return Id of the stored item.
     * @throws AuthorizationException
     *             in case the item to update exists but the current user is not the owner of it
     * @throws NavigationItemDataSerializationException
     *             in case the data can not be converted into a string
     */
    private Long store(NavigationItemTO<NavigationItemDataTO> navigationItemTO, String name,
            Integer index, Date lastAccessDate,
            NavigationItemDataTO data) throws AuthorizationException,
            NavigationItemDataSerializationException {
        if (index != null) {
            navigationItemTO.setIndex(index);
        }
        if (lastAccessDate != null) {
            navigationItemTO.setLastAccessDate(lastAccessDate);
        }
        if (data != null) {
            navigationItemTO.setData(data);
        }
        if (name != null) {
            navigationItemTO.setName(name);
        }
        Long result = navigationItemManagement.store(navigationItemTO);
        invalidateCache(SecurityHelper.getCurrentUserId());
        return result;
    }

    /**
     * Create a navigation item for the current user.
     *
     * @param name
     *            The name to set.
     * @param index
     *            Index of the item. If null the index will be 0.
     * @param lastAccessDate
     *            The last access date. If null the current date will be used.
     * @param data
     *            The data.
     * @return Id of the stored item.
     * @throws NavigationItemDataSerializationException
     *             in case the data can not be converted into a string
     */
    public Long store(String name, Integer index, Date lastAccessDate, NavigationItemDataTO data)
            throws NavigationItemDataSerializationException {
        if (data == null) {
            throw new IllegalArgumentException("Data might not be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name might not be null");
        }
        SecurityHelper.assertCurrentUser();
        try {
            return store(new NavigationItemTO<NavigationItemDataTO>(), name, index == null ? 0
                    : index, lastAccessDate == null ? new Date() : lastAccessDate, data);

        } catch (AuthorizationException e) {
            LOGGER.error("Unexpected exception creating a new navigation item", e);
            throw new NavigationItemServiceException(
                    "Unexpected exception creating a new navigation item", e);
        }
    }

    /**
     * Update a navigation item of the current user.
     *
     * @param navigationItemId
     *            Id of the item to update.
     * @param name
     *            The name to set. If null the current name will be used.
     * @param index
     *            Index of the item. If null, this will not be updated.
     * @param lastAccessDate
     *            The last access date. If null, this will not be updated.
     * @param data
     *            The data. If null, this will not be updated.
     *
     * @return Id of the stored item.
     * @throws NotFoundException
     *             in case the item to update does not exist or does not belong to the current user
     * @throws NavigationItemDataSerializationException
     *             in case the data can not be converted into a string
     */
    public Long update(Long navigationItemId, String name, Integer index, Date lastAccessDate,
            NavigationItemDataTO data) throws NotFoundException,
            NavigationItemDataSerializationException {
        if (navigationItemId == null) {
            throw new IllegalArgumentException("Data might not be null");
        }
        SecurityHelper.assertCurrentUser();
        NavigationItemTO<NavigationItemDataTO> navigationItemTO = get(navigationItemId);
        if (navigationItemTO == null) {
            throw new NotFoundException("The NavigationItem with ID " + navigationItemId
                    + " does not exist for the current user");
        }
        try {
            return store(navigationItemTO, name, index, lastAccessDate, data);
        } catch (AuthorizationException e) {
            LOGGER.error("Unexpected exception updating a navigation item of the current user", e);
            // silently treat this as not found like above
            throw new NotFoundException("The NavigationItem with ID " + navigationItemId
                    + " does not exist for the current user");
        }
    }
}
