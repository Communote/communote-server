package com.communote.server.api.core.blog;

import java.util.Collection;

import com.communote.common.converter.CollectionConverter;
import com.communote.server.api.core.external.ExternalObjectNotAssignedException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.UserToBlogRoleMapping;

/**
 * Service class for retrieving or updating the blog/topic access roles.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO Rename to TopicRightsManagement or TopicRoleManagement
public interface BlogRightsManagement {

    /**
     * <p>
     * Adds member with the given role to the blog. If the entity already is a member of the blog,
     * nothing will happen.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param entityId
     *            ID of a user or a group
     * @param role
     *            the role describing the access right the entity should get
     * @throws CommunoteEntityNotFoundException
     *             in case the user or group does not exist
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user is not allowed to add an entity to the blog. This
     *             exception won't be thrown during blog creation.
     *
     */
    public void addEntity(Long topicId, Long entityId, BlogRole role)
            throws CommunoteEntityNotFoundException, BlogNotFoundException, BlogAccessException;

    /**
     * <p>
     * Add a member with the given role to the blog on the behalf of an external system. If the
     * entity already is a member of the blog and was added on the behalf of the same external
     * system, nothing will happen.
     * </p>
     * <p>
     * When adding a member on the behalf of an external system, the role will not interfere with
     * roles added for other external systems or those that were not added for an external system
     * (for instance by the blog manager via the web FE). Because of this a user can have different
     * roles for the same blog. For such a case the actual access right will always be the highest
     * of all the assigned rights.
     * </p>
     * <p>
     * It is assumed that the blog is associated with an external object from the external system.
     * In case the blog is not associated with the external object identified by the
     * externalObjectId an exception is thrown.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param entityId
     *            ID of a user or a group
     * @param role
     *            the role describing the access right the entity should get
     * @param externalSystemId
     *            identifier of the external system
     * @param externalObjectId
     *            identifier of the external object within the external system
     * @throws CommunoteEntityNotFoundException
     *             in case the user or group does not exist
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws ExternalObjectNotAssignedException
     *             in case the external object is not assigned to the blog
     * @throws BlogAccessException
     *             in case the current user is not manager of the blog
     */
    public void addEntityForExternal(Long topicId, Long entityId, BlogRole role,
            String externalSystemId, String externalObjectId) throws BlogNotFoundException,
            CommunoteEntityNotFoundException, ExternalObjectNotAssignedException,
            BlogAccessException;

    /**
     * <p>
     * Add a member with the given role to the blog on the behalf of an external system. If the
     * entity already is a member of the blog and was added on the behalf of the same external
     * system, nothing will happen.
     * </p>
     * <p>
     * When adding a member on the behalf of an external system, the role will not interfere with
     * roles added for other external systems or those that were not added for an external system
     * (for instance by the blog manager via the web FE). Because of this a user can have different
     * roles for the same blog. For such a case the actual access right will always be the highest
     * of all the assigned rights.
     * </p>
     * <p>
     * It is assumed that the blog is associated with an external object from the external system.
     * The 'Trusted' suffix in the method name denotes that the caller has to take care that there
     * is such an external object. As of for now only the client manager is allowed to call this
     * method.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param entityId
     *            ID of a user or a group
     * @param role
     *            the role describing the access right the entity should get
     * @param externalSystemId
     *            identifier of the external system
     * @throws CommunoteEntityNotFoundException
     *             in case the user or group does not exist
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user is not client manager
     */
    public void addEntityForExternalTrusted(Long topicId, Long entityId, BlogRole role,
            String externalSystemId) throws BlogNotFoundException,
            CommunoteEntityNotFoundException, BlogAccessException;

    /**
     * <p>
     * Add a member with the given role to the blog. If the entity is already a member the role will
     * be changed to the provided role. This method won't change a role that was added on the behalf
     * of an external system.
     * </p>
     *
     * @param topicId
     *            The topics id.
     * @param entityId
     *            The entity to assign.
     *
     * @param role
     *            The role for the entity.
     */
    public void assignEntity(Long topicId, Long entityId, BlogRole role)
            throws BlogAccessException, BlogNotFoundException, CommunoteEntityNotFoundException,
            NoBlogManagerLeftException;

    /**
     * <p>
     * Adds member with the given role to the blog. If the entity already is a member of the blog,
     * nothing will happen.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param entityId
     *            ID of a user or a group
     * @param role
     *            the role describing the access right the entity should get
     * @param createNote
     *            true to create the activity note
     * @throws CommunoteEntityNotFoundException
     *             in case the user or group does not exist
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user is not allowed to add an entity to the blog. This
     *             exception won't be thrown during blog creation.
     *
     */
    public void assignEntity(Long blogId, Long entityId, BlogRole role, boolean createNote)
            throws BlogAccessException, BlogNotFoundException, CommunoteEntityNotFoundException,
            NoBlogManagerLeftException;

    /**
     * <p>
     * Add or update a member with the given role on the behalf of an external system.
     * </p>
     * <p>
     * This method first checks whether there is already a member that was added for the given
     * external system and if this is the case it behaves exactly like
     * {@link #changeRoleOfMemberByEntityIdForExternal(Long, Long, BlogRole, String)}, if it is not
     * the case it does the same as
     * {@link #addEntityForExternal(Long, Long, BlogRole, String, String)}
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param entityId
     *            ID of a user or a group
     * @param role
     *            the role describing the access right the entity should get
     * @param externalSystemId
     *            identifier of the external system
     * @param externalObjectId
     *            identifier of the external object within the external system
     * @throws CommunoteEntityNotFoundException
     *             in case the user or group does not exist
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the blog access rights
     * @throws ExternalObjectNotAssignedException
     *             in case the entity is not yet a member and the external object is not associated
     *             with the blog
     */
    public void assignEntityForExternal(Long topicId, Long entityId, BlogRole role,
            String externalSystemId, String externalObjectId) throws BlogAccessException,
            BlogNotFoundException, CommunoteEntityNotFoundException,
            ExternalObjectNotAssignedException;

    /**
     * <p>
     * Add or update a member with the given role on the behalf of an external system.
     * </p>
     * <p>
     * This method first checks whether there is already a member that was added for the given
     * external system and if this is the case it behaves exactly like
     * {@link #changeRoleOfMemberByEntityIdForExternal(Long, Long, BlogRole, String)}, if it is not
     * the case it does the same as
     * {@link #addEntityForExternalTrusted(Long, Long, BlogRole, String)}
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param entityId
     *            ID of a user or a group
     * @param role
     *            the role describing the access right the entity should get
     * @param externalSystemId
     *            identifier of the external system
     * @throws CommunoteEntityNotFoundException
     *             in case the user or group does not exist
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the blog access rights
     */
    public void assignEntityForExternalTrusted(Long topicId, Long entityId, BlogRole role,
            String externalSystemId) throws BlogAccessException, BlogNotFoundException,
            CommunoteEntityNotFoundException;

    /**
     * Assigns the management role to the current user.
     *
     * @param topicId
     *            The topics id.
     * @throws AuthorizationException
     *             in case the current user is not client manager
     * @throws BlogNotFoundException
     *             in case the topic does not exist
     */
    public void assignManagementAccessToCurrentUser(Long topicId) throws BlogNotFoundException,
    com.communote.server.api.core.security.AuthorizationException;

    /**
     * <p>
     * Change the public access to a blog.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param allowPublicAccess
     *            if true the blog will also be accessible by the unauthorized public user
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the blog access rights
     */
    public void changePublicAccess(Long topicId, boolean allowPublicAccess)
            throws BlogNotFoundException, BlogAccessException;

    /**
     * <p>
     * Changes the role of a blog member. This method won't change the role of a member that was
     * added on the behalf of an external system.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param entityId
     *            ID of a user or a group that has previously added to the blog
     * @param role
     *            the role describing the new access right the entity should get
     *
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws NoBlogManagerLeftException
     *             in case the operation would remove the last active blog manager
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the blog access rights
     * @throws BlogMemberNotFoundException
     *             in case the entity is not a member of the blog
     */
    public void changeRoleOfMemberByEntityId(Long topicId, Long entityId, BlogRole role)
            throws BlogMemberNotFoundException, BlogNotFoundException, NoBlogManagerLeftException,
            BlogAccessException;

    /**
     * <p>
     * Change the role of a blog member that was added on the behalf of an external system. This
     * method won't change the role of a member that was added on the behalf of another external
     * system or that was added directly.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param entityId
     *            ID of a user or a group that has previously added to the blog on the behalf of the
     *            external system
     * @param role
     *            the role describing the new access right the entity should get
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogMemberNotFoundException
     *             in case the entity is not a member of the blog
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the blog access rights
     */
    public void changeRoleOfMemberByEntityIdForExternal(Long topicId, Long entityId, BlogRole role,
            String externalSystemId) throws BlogNotFoundException, BlogMemberNotFoundException,
            BlogAccessException;

    /**
     * <p>
     * Tests whether the current user is manager of a blog.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @return true if the current user has management access, false otherwise
     */
    public boolean currentUserHasManagementAccess(Long topicId);

    /**
     * Tests whether the current user has read access to a blog.
     *
     * @param topicId
     *            ID of the blog
     * @param ignoreAllCanFlags
     *            ignore the all can read and all can write flags and only consider the individual
     *            blog members
     * @return true if the current user has read access, false otherwise
     */
    public boolean currentUserHasReadAccess(Long topicId, boolean ignoreAllCanFlags);

    /**
     * <p>
     * Tests whether the current user has write access to a blog.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param ignoreAllCanFlags
     *            ignore the all can write flag and only consider the individual blog members
     * @return true if the current user has write access, false otherwise
     */
    public boolean currentUserHasWriteAccess(Long topicId, boolean ignoreAllCanFlags);

    /**
     * Check if a topic exists and the current user has access to that topic. If this method returns
     * a topic the following holds: The current user has access to the blog as specified by blogRole
     * and the blog does exist.
     *
     * @param topicId
     *            the ID of the topic to retrieve
     * @param blogRole
     *            the access rights the current user needs to have
     * @return the topic
     * @throws BlogNotFoundException
     *             in case there is no topic with the given ID
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    public Blog getAndCheckBlogAccess(Long topicId, BlogRole blogRole)
            throws BlogNotFoundException, BlogAccessException;

    /**
     * Method to get all users for a given topic with the specified roles. Groups will be resolved
     * to the users.
     *
     * @param topicId
     *            Id of the topic the users are requested.
     * @param converter
     *            An Converter for converting the found users to something else.
     * @param roles
     *            The user must have at least one of these roles.
     * @return Collection of converted items.
     */
    public <T> Collection<T> getMappedUsers(Long topicId,
            CollectionConverter<UserToBlogRoleMapping, T> converter, BlogRole... roles);

    /**
     * Returns the blog role of the current user.
     */
    public BlogRole getRoleOfCurrentUser(Long topicId, boolean ignoreAllCanFlags);

    /**
     * Returns the blog role of an entity or null if the entity does not have access to the blog.
     */
    public BlogRole getRoleOfEntity(Long topicId, Long entityId, boolean ignoreAllCanFlags);

    /**
     * Returns the blog role of a user.
     */
    public BlogRole getRoleOfUser(Long topicId, Long userId, boolean ignoreAllCanFlags);

    /**
     * <p>
     * Returns true if the blog has another active user as member with management access right.
     * </p>
     */
    public boolean hasAnotherManager(Long topicId, Long userId);

    /**
     * <p>
     * Returns true if the blog has another reader than the user passed in.
     * </p>
     */
    public boolean hasAnotherReader(Long topicId, Long userId);

    /**
     * <p>
     * Returns true if the entity was added to the blog. This will return false if the entity has
     * blog access through other means like being member of a group that was added to the blog or
     * the blog has allCanRead, allCanWrite access. If the entity has no blog access false will be
     * returned as well.
     * </p>
     */
    public boolean isEntityDirectMember(Long topicId, Long entityId);

    /**
     *
     */
    public void removeMemberByEntityId(Long topicId, Long entityId)
            throws NoBlogManagerLeftException, BlogNotFoundException, BlogAccessException;

    /**
     * @param topicId
     *            Id of the topic.
     * @param entityId
     *            Id of the entity to remove.
     * @param externalSystemId
     *            Id of the entities external system.
     * @throws BlogNotFoundException
     * @throws BlogAccessException
     */
    public void removeMemberByEntityIdForExternal(Long topicId, Long entityId,
            String externalSystemId) throws BlogNotFoundException, BlogAccessException;

    /**
     * <p>
     * Removes all the direct blog memberships of a user. If the user to remove is not the current
     * user or the current user is not a client manager an AuthorizationException will be thrown. If
     * the blog would end without mananger a NoBlogManagerLEftException is thrown.
     * </p>
     */
    public void removeUserFromAllBlogs(Long userId,
            java.util.Collection<Long> blogsToNotCheckForLastManager)
            throws com.communote.server.api.core.security.AuthorizationException,
            NoBlogManagerLeftException;

    /**
     * <p>
     * Update the blog all you can read /write only
     * </p>
     */
    public Blog setAllCanReadAllCanWrite(Long topicId, boolean allCanRead, boolean allCanWrite)
            throws BlogAccessException, BlogNotFoundException;

    /**
     * <p>
     * Test whether a user is manager of a blog.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param userId
     *            identifier of user
     * @return true if the user has management access, false otherwise
     */
    public boolean userHasManagementAccess(Long topicId, Long userId);

    /**
     * <p>
     * Test whether a user has read access to a blog.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param userId
     *            identifier of user
     * @param ignoreAllCanFlags
     *            ignore the all can read and all can write flags and only consider the individual
     *            blog members
     * @return true if the user has read access, false otherwise
     */
    public boolean userHasReadAccess(Long topicId, Long userId, boolean ignoreAllCanFlags);

    /**
     * <p>
     * Tests whether a user has write access to a blog.
     * </p>
     *
     * @param topicId
     *            ID of the blog
     * @param userId
     *            identifier of user
     * @param ignoreAllCanFlags
     *            ignore the flag all can write
     * @return true or false user has write access
     */
    public boolean userHasWriteAccess(Long topicId, Long userId, boolean ignoreAllCanFlags);

}
