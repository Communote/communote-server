package com.communote.server.persistence.user;

import java.util.Collection;
import java.util.List;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.property.PropertyConstants;
import com.communote.server.model.user.UserNoteProperty;
import com.communote.server.model.user.UserNotePropertyConstants;


/**
 * @see com.communote.server.model.user.UserNoteProperty
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserNotePropertyDaoImpl extends
        com.communote.server.persistence.user.UserNotePropertyDaoBase {

    private final static String FIND_PROPERTY_QUERY = "FROM "
            + UserNotePropertyConstants.CLASS_NAME + " WHERE " + UserNotePropertyConstants.USER
            + ".id = ? AND " + UserNotePropertyConstants.NOTE + ".id" + " = ? AND "
            + PropertyConstants.KEYGROUP + " = ? AND " + PropertyConstants.PROPERTYKEY + " = ?";

    private final static String FIND_PROPERTIES_QUERY = "FROM "
            + UserNotePropertyConstants.CLASS_NAME + " WHERE " + UserNotePropertyConstants.NOTE
            + ".id" + " = ? AND "
            + PropertyConstants.KEYGROUP + " = ? AND " + PropertyConstants.PROPERTYKEY + " = ?";

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<UserNoteProperty> handleFindProperties(Long noteId, String keyGroup,
            String key) {
        List<?> results = getHibernateTemplate().find(FIND_PROPERTIES_QUERY,
                new Object[] { noteId, keyGroup, key });
        return (Collection<UserNoteProperty>) results;
    }

    @Override
    protected UserNoteProperty handleFindProperty(Long noteId, String keyGroup,
            String key) {
        if (SecurityHelper.isInternalSystem() || SecurityHelper.isPublicUser()) {
            return null;
        }
        List<?> results = getHibernateTemplate().find(FIND_PROPERTY_QUERY,
                new Object[] { SecurityHelper.assertCurrentUserId(), noteId, keyGroup, key });
        if (results.size() > 1) {
            throw new IllegalDatabaseState("Can not have more than one result.");
        }
        return results.size() == 0 ? null : (UserNoteProperty) (results.get(0));
    }

    @Override
    protected int handleRemovePropertiesForNote(Long noteId) {
        // TODO actually we would have to invalidate the UserNoteProperty cache, which is kind of
        // expensive (get all set properties for the note and invalidate them). Since this method is
        // only used when deleting a note no one accesses the properties after deletion of the note
        // because they are only read when getting the note data.
        return getHibernateTemplate().bulkUpdate(
                "DELETE FROM " + UserNotePropertyConstants.CLASS_NAME + " property WHERE property."
                        + UserNotePropertyConstants.NOTE + ".id = ?", noteId);
    }

    @Override
    protected int handleRemovePropertiesForUser(Long userId) {
        // TODO actually we would have to invalidate the UserNoteProperty cache, which is currently
        // not possible (=too expensive: fetch all properties we are going to delete and invalidate
        // the cache entries). Since this method is only used when a user is deleted (actually it's
        // only used in tests), we currently have no problem as deleted users are removed from the
        // result set (see UserNotePropertyAccessor). In case we use this method for other stuff we
        // need a solution for easier invalidation.
        return getHibernateTemplate().bulkUpdate(
                "DELETE FROM " + UserNotePropertyConstants.CLASS_NAME + " property WHERE property."
                        + UserNotePropertyConstants.USER + ".id = ?", userId);
    }
}