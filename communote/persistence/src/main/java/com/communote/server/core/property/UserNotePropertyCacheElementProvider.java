package com.communote.server.core.property;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.model.user.UserStatus;
import com.communote.server.model.user.UserNoteProperty;
import com.communote.server.persistence.user.UserNotePropertyDao;

/**
 * A CacheElementProvider that allows caching of UserNoteProperty instances as a mapping from
 * property value to the IDs of the users that have the property described by the
 * {@link UserNotePropertyCacheKey}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserNotePropertyCacheElementProvider implements
        CacheElementProvider<UserNotePropertyCacheKey, HashMap<String, Set<Long>>> {

    private static final String CONTENT_TYPE = "UserNoteProperty";
    private UserNotePropertyDao propertyDao;

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public int getTimeToLive() {
        return 7200;
    }

    /**
     * @return the lazily initialized DAO
     */
    private UserNotePropertyDao getUserNotePropertyDao() {
        if (this.propertyDao == null) {
            this.propertyDao = ServiceLocator.findService(UserNotePropertyDao.class);
        }
        return this.propertyDao;
    }

    @Override
    public HashMap<String, Set<Long>> load(UserNotePropertyCacheKey key) {
        Collection<UserNoteProperty> properties = getUserNotePropertyDao().findProperties(
                key.getNoteId(),
                key.getPropertyKeyGroup(), key.getPropertyKey());
        if (properties.isEmpty()) {
            return null;
        }
        // group users by value
        HashMap<String, Set<Long>> valueToUsers = new HashMap<String, Set<Long>>();
        for (UserNoteProperty property : properties) {
            Set<Long> users = valueToUsers.get(property.getPropertyValue());
            if (users == null) {
                users = new HashSet<Long>();
                valueToUsers.put(property.getPropertyValue(), users);
            }
            // filter deleted users -> TODO does this use the 2ndlevel cache?
            if (!property.getUser().getStatus().equals(UserStatus.DELETED)) {
                users.add(property.getUser().getId());
            }
        }
        return valueToUsers;
    }

}
