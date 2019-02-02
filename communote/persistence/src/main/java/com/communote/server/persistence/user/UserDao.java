package com.communote.server.persistence.user;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.communote.server.api.core.user.UserVO;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;

/**
 * @see User
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface UserDao {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes entities must be transformed into objects of type {@link UserVO}.
     */
    public final static int TRANSFORM_KENMEIUSERVO = 1;

    /**
     * Creates a new instance of User and adds from the passed in <code>entities</code> collection
     *
     * @param entities
     *            the collection of User instances to create.
     *
     * @return the created instances.
     */
    public Collection<User> create(Collection<User> entities);

    /**
     * <p>
     * Does the same thing as {@link #create(User)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entities (into value objects for example). By default,
     * transformation does not occur.
     * </p>
     */
    public Collection<?> create(int transform, Collection<User> entities);

    /**
     * <p>
     * Does the same thing as {@link #create(User)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entity (into a value object for example). By default, transformation
     * does not occur.
     * </p>
     */
    public Object create(int transform, User kenmeiUser);

    /**
     * Creates an instance of User and adds it to the persistent store.
     */
    public User create(User kenmeiUser);

    /**
     * <p>
     * Create the default mail notification config
     * </p>
     */
    public void createMailNotificationConfig(Long notificationConfigId);

    /**
     * Evicts (removes) the entity from the hibernate cache
     *
     * @param entity
     *            the entity to evict
     */
    public void evict(User entity);

    /**
     *
     */
    public User findByAlias(String alias);

    /**
     * <p>
     * Find the user by a given email
     * </p>
     */
    public User findByEmail(String email);

    /**
     * <p>
     * Returns the users which are members of the foreign system.
     * </p>
     */
    public List<User> findByExternalSystemId(String systemId);

    /**
     *
     */
    public User findByExternalUserId(String userId, String systemId);

    /**
     *
     */
    public List<User> findByRole(UserRole userRole, UserStatus status);

    /**
     *
     */
    public List<User> findLatestBySystemId(String externalSystemId, Long userId, int maxCount);

    /**
     * @return All users, which are not confirmed, except system users.
     */
    public List<User> findNotConfirmedUser(Date before, boolean reminderMailSent);

    /**
     * <p>
     * returns a collection of users which do not have one of the deleted statuses
     * </p>
     *
     * @param excludeSystemUsers
     *            whether to exclude the users who have the system user role
     */
    public Collection<User> findNotDeletedUser(boolean excludeSystemUsers);

    /**
     * @param before
     *            The users status should have changed before this date.
     * @param reminderMailSent
     *            A filter for users with or without reminder e-mails already send.
     * @param includeTermsNotAccepted
     *            whether to count user in status TERMS_NOT_ACCEPTED as ACTIVE
     * @return All active users which haven't logged in yet. Anonymous user are not returned.
     */
    public List<User> findNotLoggedInActiveUser(Date before, boolean reminderMailSent,
            boolean includeTermsNotAccepted);

    /**
     * @return the number of users with status ACTIVE. System users are not counted.
     */
    public long getActiveUserCount();

    /**
     * Get the number of users with status ACTIVE.
     *
     * @param systemId
     *            ID of an external system to only count the users which originate from this
     *            external system. If null, all users are counted.
     * @param role
     *            the role of a user. If not null only the users with that role will be counted
     *            otherwise all roles (including system users) are considered.
     * @return the number of active users
     */
    public long getActiveUserCount(String systemId, UserRole role);

    /**
     * <p>
     * Returns the IDs of the blogs which a user follows
     * </p>
     */
    public List<Long> getFollowedBlogs(Long userId, long blogIdRangeStart, long blogIdRangeEnd);

    /**
     * <p>
     * Returns the IDs of the discussions followed by a user.
     * </p>
     */
    public List<Long> getFollowedDiscussions(Long userId, long discussionIdRangeStart,
            long discussionIdRangeEnd);

    /**
     *
     */
    public List<Long> getFollowedTags(Long userId, Long rangeStart, Long rangeEnd);

    /**
     * <p>
     * Returns the IDs of the users a user follows.
     * </p>
     */
    public List<Long> getFollowedUsers(Long userId, long userIdRangeStart, long userIdRangeEnd);

    /**
     * <p>
     * Does the same thing as {@link #load(Long)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined in this class then the result <strong>WILL BE</strong> passed through an operation
     * which can optionally transform the entity (into a value object for example). By default,
     * transformation does not occur.
     * </p>
     *
     * @param id
     *            the identifier of the entity to load.
     * @return either the entity or the object transformed from the entity.
     */
    public Object load(int transform, Long id);

    /**
     * Loads an instance of User from the persistent store.
     */
    public User load(Long id);

    /**
     * Loads all entities of type {@link User}.
     *
     * @return the loaded entities.
     */
    public Collection<User> loadAll();

    /**
     * <p>
     * Does the same thing as {@link #loadAll()} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entity (into a value object for example). By default, transformation
     * does not occur.
     * </p>
     *
     * @param transform
     *            the flag indicating what transformation to use.
     * @return the loaded entities.
     */
    public Collection<?> loadAll(final int transform);

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove(Collection<User> entities);

    /**
     * Removes the instance of User having the given <code>identifier</code> from the persistent
     * store.
     */
    public void remove(Long id);

    /**
     * Removes the instance of User from the persistent store.
     */
    public void remove(User user);

    /**
     * <p>
     * Resets the terms accepted status for all users.
     * </p>
     */
    public void resetTermsAccepted(Long userIdToIgnore);

    /**
     * Converts this DAO's entity to an object of type {@link UserVO}.
     */
    public UserVO toUserVO(User entity);

    /**
     * Copies the fields of the specified entity to the target value object. This method is similar
     * to toUserVO(), but it does not handle any attributes in the target value object that are
     * "read-only" (as those do not have setter methods exposed).
     */
    public void toUserVO(User source, UserVO target);

    /**
     * Converts this DAO's entity to a Collection of instances of type {@link UserVO}.
     */
    public void toUserVOCollection(Collection entities);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(Collection<User> entities);

    /**
     * Updates the <code>user</code> instance in the persistent store.
     */
    public void update(User user);

    /**
     * <p>
     * Returns whether the provided user follows the followable item identified by its global ID.
     * </p>
     */
    public boolean userFollowsItem(Long userId, Long globalId);

    /**
     * Creates a new (not persisted) entity and copies the fields of the VO to the corresponding
     * fields of the entity. Null values and the password are not copied.
     */
    public User userVOToEntity(UserVO userVO);

    /**
     * Copies the fields of the VO to the corresponding fields of the entity. Null values and the
     * password are not copied.
     * @since 3.5
     */
    public void userVOToEntity(UserVO source, User target);

}
