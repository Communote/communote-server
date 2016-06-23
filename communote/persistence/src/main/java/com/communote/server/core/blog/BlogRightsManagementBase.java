package com.communote.server.core.blog;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogMemberNotFoundException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.external.ExternalObjectNotAssignedException;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.model.blog.BlogRole;

/**
 * <p>
 * Spring Service base class for <code>BlogRightsManagement</code>, provides access to all services
 * and entities referenced by this service.
 * </p>
 *
 * @see BlogRightsManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class BlogRightsManagementBase implements BlogRightsManagement {

    @Override
    public void addEntity(Long blogId, Long entityId, BlogRole role)
            throws CommunoteEntityNotFoundException, BlogNotFoundException, BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntity(Long blogId, Long entityId, BlogRole role) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntity(Long blogId, Long entityId, BlogRole role) - 'entityId' can not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntity(Long blogId, Long entityId, BlogRole role) - 'role' can not be null");
        }
        try {
            this.handleAddEntity(blogId, entityId, role);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.addEntity(Long blogId, Long entityId, BlogRole role)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEntityForExternal(Long blogId, Long entityId, BlogRole role,
            String externalSystemId, String externalId) throws BlogNotFoundException,
            CommunoteEntityNotFoundException, ExternalObjectNotAssignedException,
            BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntityForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId, String externalId) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntityForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId, String externalId) - 'entityId' can not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntityForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId, String externalId) - 'role' can not be null");
        }
        if (externalSystemId == null || externalSystemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntityForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId, String externalId) - 'externalSystemId' can not be null or empty");
        }
        if (externalId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntityForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId, String externalId) - 'externalId' can not be null");
        }
        try {
            this.handleAddEntityForExternal(blogId, entityId, role, externalSystemId, externalId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.addEntityForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId, String externalId)' --> "
                            + rt, rt);
        }
    }

    @Override
    public void addEntityForExternalTrusted(Long blogId, Long entityId, BlogRole role,
            String externalSystemId) throws BlogNotFoundException,
            CommunoteEntityNotFoundException, BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntityForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntityForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId) - 'entityId' can not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntityForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId) - 'role' can not be null");
        }
        if (externalSystemId == null || externalSystemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.addEntityForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId) - 'externalSystemId' can not be null or empty");
        }
        try {
            this.handleAddEntityForExternalTrusted(blogId, entityId, role, externalSystemId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.addEntityForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assignEntity(Long blogId, Long entityId, BlogRole role) throws BlogAccessException,
            BlogNotFoundException, CommunoteEntityNotFoundException, NoBlogManagerLeftException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntity(Long blogId, Long entityId, BlogRole role) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntity(Long blogId, Long entityId, BlogRole role) - 'entityId' can not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntity(Long blogId, Long entityId, BlogRole role) - 'role' can not be null");
        }
        try {
            this.handleAssignEntity(blogId, entityId, role);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.assignEntity(Long blogId, Long entityId, BlogRole role)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assignEntity(Long blogId, Long entityId, BlogRole role, boolean createNote)
            throws BlogAccessException, BlogNotFoundException, CommunoteEntityNotFoundException,
            NoBlogManagerLeftException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntity(Long blogId, Long entityId, BlogRole role) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntity(Long blogId, Long entityId, BlogRole role) - 'entityId' can not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntity(Long blogId, Long entityId, BlogRole role) - 'role' can not be null");
        }
        try {
            this.handleAssignEntity(blogId, entityId, role, createNote);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.assignEntity(Long blogId, Long entityId, BlogRole role)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assignEntityForExternal(Long blogId, Long entityId, BlogRole role,
            String externalSystemId, String externalId) throws BlogNotFoundException,
            CommunoteEntityNotFoundException, BlogAccessException,
            ExternalObjectNotAssignedException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntityForExternal 'blogId' can not be null.");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntityForExternal 'entityId' can not be null.");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntityForExternal 'role' can not be null.");
        }
        if (externalSystemId == null || externalSystemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntityForExternal 'externalSystemId' can not be null.");
        }
        if (externalId == null || externalId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntityForExternal 'externalId' can not be null.");
        }
        try {
            this.handleAssignEntityForExternal(blogId, entityId, role, externalSystemId, externalId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing BlogRightsManagement.assignEntityForExternal" + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assignEntityForExternalTrusted(Long topicId, Long entityId, BlogRole role,
            String externalSystemId) throws BlogAccessException, BlogNotFoundException,
            CommunoteEntityNotFoundException {
        if (topicId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntityForExternalTrusted 'topicId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntityForExternalTrusted 'entityId' can not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntityForExternalTrusted 'role' can not be null");
        }
        if (externalSystemId == null || externalSystemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignEntityForExternalTrusted 'externalSystemId' can not be null or empty");
        }
        try {
            this.handleAssignEntityForExternalTrusted(topicId, entityId, role, externalSystemId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing BlogRightsManagement.assignEntityForExternalTrusted  --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assignManagementAccessToCurrentUser(Long blogId) throws BlogNotFoundException,
    com.communote.server.api.core.security.AuthorizationException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.assignManagementAccessToCurrentUser(Long blogId) - 'blogId' can not be null");
        }
        try {
            this.handleAssignManagementAccessToCurrentUser(blogId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.assignManagementAccessToCurrentUser(Long blogId)' --> "
                            + rt, rt);
        }
    }

    @Override
    public void changePublicAccess(Long blogId, boolean allowPublicAccess)
            throws BlogNotFoundException, BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.changePublicAccess(Long blogId, boolean allowPublicAccess) - 'blogId' can not be null");
        }
        try {
            this.handleChangePublicAccess(blogId, allowPublicAccess);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.changePublicAccess(Long blogId, boolean allowPublicAccess)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeRoleOfMemberByEntityId(Long blogId, Long entityId, BlogRole role)
            throws BlogMemberNotFoundException, BlogNotFoundException, NoBlogManagerLeftException,
            BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.changeRoleOfMemberByEntityId(Long blogId, Long entityId, BlogRole role) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.changeRoleOfMemberByEntityId(Long blogId, Long entityId, BlogRole role) - 'entityId' can not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.changeRoleOfMemberByEntityId(Long blogId, Long entityId, BlogRole role) - 'role' can not be null");
        }
        try {
            this.handleChangeRoleOfMemberByEntityId(blogId, entityId, role);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.changeRoleOfMemberByEntityId(Long blogId, Long entityId, BlogRole role)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeRoleOfMemberByEntityIdForExternal(Long blogId, Long entityId, BlogRole role,
            String externalSystemId) throws BlogNotFoundException, BlogMemberNotFoundException,
            BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.changeRoleOfMemberByEntityIdForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.changeRoleOfMemberByEntityIdForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId) - 'entityId' can not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.changeRoleOfMemberByEntityIdForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId) - 'role' can not be null");
        }
        if (externalSystemId == null || externalSystemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.changeRoleOfMemberByEntityIdForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId) - 'externalSystemId' can not be null or empty");
        }
        try {
            this.handleChangeRoleOfMemberByEntityIdForExternal(blogId, entityId, role,
                    externalSystemId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.changeRoleOfMemberByEntityIdForExternal(Long blogId, Long entityId, BlogRole role, String externalSystemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean currentUserHasManagementAccess(Long blogId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.currentUserHasManagementAccess(Long blogId) - 'blogId' can not be null");
        }
        try {
            return this.handleCurrentUserHasManagementAccess(blogId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.currentUserHasManagementAccess(Long blogId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#currentUserHasReadAccess(Long, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean currentUserHasReadAccess(Long blogId, boolean ignoreAllCanFlags) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.currentUserHasReadAccess(Long blogId, boolean ignoreAllCanFlags) - 'blogId' can not be null");
        }
        try {
            return this.handleCurrentUserHasReadAccess(blogId, ignoreAllCanFlags);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.currentUserHasReadAccess(Long blogId, boolean ignoreAllCanFlags)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#currentUserHasWriteAccess(Long, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean currentUserHasWriteAccess(Long blogId, boolean ignoreAllCanFlags) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.currentUserHasWriteAccess(Long blogId, boolean ignoreAllCanFlags) - 'blogId' can not be null");
        }
        try {
            return this.handleCurrentUserHasWriteAccess(blogId, ignoreAllCanFlags);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.currentUserHasWriteAccess(Long blogId, boolean ignoreAllCanFlags)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#getAndCheckBlogAccess(Long, BlogRole)
     */
    @Override
    @Transactional(readOnly = true)
    public com.communote.server.model.blog.Blog getAndCheckBlogAccess(Long blogId, BlogRole blogRole)
            throws BlogNotFoundException, BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.getAndCheckBlogAccess(Long blogId, BlogRole blogRole) - 'blogId' can not be null");
        }
        if (blogRole == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.getAndCheckBlogAccess(Long blogId, BlogRole blogRole) - 'blogRole' can not be null");
        }
        try {
            return this.handleGetAndCheckBlogAccess(blogId, blogRole);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.getAndCheckBlogAccess(Long blogId, BlogRole blogRole)' --> "
                            + rt, rt);
        }
    }

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     *
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }

    /**
     * @see BlogRightsManagement#getRoleOfCurrentUser(Long, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public BlogRole getRoleOfCurrentUser(Long blogId, boolean ignoreAllCanFlags) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.getRoleOfCurrentUser(Long blogId, boolean ignoreAllCanFlags) - 'blogId' can not be null");
        }
        try {
            return this.handleGetRoleOfCurrentUser(blogId, ignoreAllCanFlags);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.getRoleOfCurrentUser(Long blogId, boolean ignoreAllCanFlags)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#getRoleOfEntity(Long, Long, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public BlogRole getRoleOfEntity(Long blogId, Long entityId, boolean ignoreAllCanFlags) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.getRoleOfEntity(Long blogId, Long entityId, boolean ignoreAllCanFlags) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.getRoleOfEntity(Long blogId, Long entityId, boolean ignoreAllCanFlags) - 'entityId' can not be null");
        }
        try {
            return this.handleGetRoleOfEntity(blogId, entityId, ignoreAllCanFlags);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.getRoleOfEntity(Long blogId, Long entityId, boolean ignoreAllCanFlags)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#getRoleOfUser(Long, Long, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public BlogRole getRoleOfUser(Long blogId, Long userId, boolean ignoreAllCanFlags) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.getRoleOfUser(Long blogId, Long userId, boolean ignoreAllCanFlags) - 'blogId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.getRoleOfUser(Long blogId, Long userId, boolean ignoreAllCanFlags) - 'userId' can not be null");
        }
        try {
            return this.handleGetRoleOfUser(blogId, userId, ignoreAllCanFlags);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.getRoleOfUser(Long blogId, Long userId, boolean ignoreAllCanFlags)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #addEntity(Long, Long, BlogRole)}
     */
    protected abstract void handleAddEntity(Long blogId, Long entityId, BlogRole role)
            throws CommunoteEntityNotFoundException, BlogNotFoundException, BlogAccessException;

    /**
     * Performs the core logic for {@link #addEntityForExternal(Long, Long, BlogRole, String
     * externalSystemId, String externalId )}
     *
     * @param blogId
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
    protected abstract void handleAddEntityForExternal(Long blogId, Long entityId, BlogRole role,
            String externalSystemId, String externalObjectId) throws BlogNotFoundException,
            CommunoteEntityNotFoundException, ExternalObjectNotAssignedException,
            BlogAccessException;

    /**
     * Performs the core logic for
     * {@link #addEntityForExternalTrusted(Long, Long, BlogRole, String)}
     *
     * @param blogId
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
     *             in case the current user is not allowed to add the entity
     */
    protected abstract void handleAddEntityForExternalTrusted(Long blogId, Long entityId,
            BlogRole role, String externalSystemId) throws BlogNotFoundException,
            CommunoteEntityNotFoundException, BlogAccessException;

    /**
     * Performs the core logic for {@link #assignEntity(Long, Long, BlogRole)}
     */
    protected abstract void handleAssignEntity(Long blogId, Long entityId, BlogRole role)
            throws BlogAccessException, BlogNotFoundException, CommunoteEntityNotFoundException,
            NoBlogManagerLeftException;

    /**
     * Performs the core logic for {@link #assignEntity(Long, Long, BlogRole, boolean)}
     */
    protected abstract void handleAssignEntity(Long blogId, Long entityId, BlogRole role,
            boolean createNote) throws BlogAccessException, BlogNotFoundException,
            CommunoteEntityNotFoundException, NoBlogManagerLeftException;

    /**
     * Handle the assign of an external entity role to an internal topic
     *
     * @param topicId
     *            identifier of topic
     * @param entityId
     *            user or group identifier
     * @param role
     *            of external
     * @param externalSystemId
     *            identifier of external system
     * @param externalId
     *            identifier of external object in external system
     * @throws CommunoteEntityNotFoundException
     *             can not found user BlogAccessException
     * @throws BlogNotFoundException
     *             can not found topic
     * @throws BlogAccessException
     *             can not access topic
     * @throws ExternalObjectNotAssignedException
     *             external object is not assigned to topic
     */
    protected abstract void handleAssignEntityForExternal(Long topicId, Long entityId,
            BlogRole role, String externalSystemId, String externalId) throws BlogAccessException,
            BlogNotFoundException, CommunoteEntityNotFoundException,
            ExternalObjectNotAssignedException;

    protected abstract void handleAssignEntityForExternalTrusted(Long topicId, Long entityId,
            BlogRole role, String externalSystemId) throws BlogAccessException,
            BlogNotFoundException, CommunoteEntityNotFoundException;

    /**
     * Performs the core logic for {@link #assignManagementAccessToCurrentUser(Long)}
     */
    protected abstract void handleAssignManagementAccessToCurrentUser(Long blogId)
            throws BlogNotFoundException,
            com.communote.server.api.core.security.AuthorizationException;

    /**
     * Performs the core logic for {@link #changePublicAccess(Long, boolean)}
     */
    protected abstract void handleChangePublicAccess(Long blogId, boolean allowPublicAccess)
            throws BlogNotFoundException, BlogAccessException;

    /**
     * Performs the core logic for {@link #changeRoleOfMemberByEntityId(Long, Long, BlogRole)}
     */
    protected abstract void handleChangeRoleOfMemberByEntityId(Long blogId, Long entityId,
            BlogRole role) throws BlogMemberNotFoundException, BlogNotFoundException,
            NoBlogManagerLeftException, BlogAccessException;

    /**
     * Performs the core logic for
     * {@link #changeRoleOfMemberByEntityIdForExternal(Long, Long, BlogRole, String)}
     *
     */
    protected abstract void handleChangeRoleOfMemberByEntityIdForExternal(Long blogId,
            Long entityId, BlogRole role, String externalSystemId) throws BlogNotFoundException,
            BlogMemberNotFoundException, BlogAccessException;

    /**
     * Performs the core logic for {@link #currentUserHasManagementAccess(Long)}
     */
    protected abstract boolean handleCurrentUserHasManagementAccess(Long blogId);

    /**
     * Performs the core logic for {@link #currentUserHasReadAccess(Long, boolean)}
     */
    protected abstract boolean handleCurrentUserHasReadAccess(Long blogId, boolean ignoreAllCanFlags);

    /**
     * Performs the core logic for {@link #currentUserHasWriteAccess(Long, boolean)}
     */
    protected abstract boolean handleCurrentUserHasWriteAccess(Long blogId,
            boolean ignoreAllCanFlags);

    /**
     * Performs the core logic for {@link #getAndCheckBlogAccess(Long, BlogRole)}
     */
    protected abstract com.communote.server.model.blog.Blog handleGetAndCheckBlogAccess(
            Long blogId, BlogRole blogRole) throws BlogNotFoundException, BlogAccessException;

    /**
     * Performs the core logic for {@link #getRoleOfCurrentUser(Long, boolean)}
     */
    protected abstract BlogRole handleGetRoleOfCurrentUser(Long blogId, boolean ignoreAllCanFlags);

    /**
     * Performs the core logic for {@link #getRoleOfEntity(Long, Long, boolean)}
     */
    protected abstract BlogRole handleGetRoleOfEntity(Long blogId, Long entityId,
            boolean ignoreAllCanFlags);

    /**
     * Performs the core logic for {@link #getRoleOfUser(Long, Long, boolean)}
     */
    protected abstract BlogRole handleGetRoleOfUser(Long blogId, Long userId,
            boolean ignoreAllCanFlags);

    /**
     * Performs the core logic for {@link #hasAnotherManager(Long, Long)}
     */
    protected abstract boolean handleHasAnotherManager(Long blogId, Long userId);

    /**
     * Performs the core logic for {@link #hasAnotherReader(Long, Long)}
     */
    protected abstract boolean handleHasAnotherReader(Long blogId, Long userId);

    /**
     * Performs the core logic for {@link #isEntityDirectMember(Long, Long)}
     */
    protected abstract boolean handleIsEntityDirectMember(Long blogId, Long entityId);

    /**
     * Performs the core logic for {@link #removeMemberByEntityId(Long, Long)}
     */
    protected abstract void handleRemoveMemberByEntityId(Long blogId, Long entityId)
            throws NoBlogManagerLeftException, BlogNotFoundException, BlogAccessException;

    /**
     * Performs the core logic for {@link #removeMemberByEntityIdForExternal(Long, Long, String)}
     *
     * @throws BlogAccessException
     */
    protected abstract void handleRemoveMemberByEntityIdForExternal(Long blogId, Long entityId,
            String externalSystemId) throws BlogNotFoundException, BlogAccessException;

    /**
     * Performs the core logic for {@link #removeUserFromAllBlogs(Long, java.util.Collection<Long>)}
     */
    protected abstract void handleRemoveUserFromAllBlogs(Long userId,
            java.util.Collection<Long> blogsToNotCheckForLastManager)
            throws com.communote.server.api.core.security.AuthorizationException,
            NoBlogManagerLeftException;

    /**
     * Performs the core logic for {@link #setAllCanReadAllCanWrite(Long, boolean, boolean)}
     */
    protected abstract com.communote.server.model.blog.Blog handleSetAllCanReadAllCanWrite(
            Long blogId, boolean allCanRead, boolean allCanWrite) throws BlogAccessException,
            BlogNotFoundException;

    /**
     * Performs the core logic for {@link #userHasManagementAccess(Long, Long)}
     */
    protected abstract boolean handleUserHasManagementAccess(Long blogId, Long userId);

    /**
     * Performs the core logic for {@link #userHasReadAccess(Long, Long, boolean)}
     */
    protected abstract boolean handleUserHasReadAccess(Long blogId, Long userId,
            boolean ignoreAllCanFlags);

    /**
     * Performs the core logic for {@link #userHasWriteAccess(Long, Long, boolean)}
     */
    protected abstract boolean handleUserHasWriteAccess(Long blogId, Long userId,
            boolean ignoreAllCanFlags);

    /**
     * @see BlogRightsManagement#hasAnotherManager(Long, Long)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasAnotherManager(Long blogId, Long userId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.hasAnotherManager(Long blogId, Long userId) - 'blogId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.hasAnotherManager(Long blogId, Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleHasAnotherManager(blogId, userId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.hasAnotherManager(Long blogId, Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#hasAnotherReader(Long, Long)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasAnotherReader(Long blogId, Long userId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.hasAnotherReader(Long blogId, Long userId) - 'blogId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.hasAnotherReader(Long blogId, Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleHasAnotherReader(blogId, userId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.hasAnotherReader(Long blogId, Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#isEntityDirectMember(Long, Long)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isEntityDirectMember(Long blogId, Long entityId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.isEntityDirectMember(Long blogId, Long entityId) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.isEntityDirectMember(Long blogId, Long entityId) - 'entityId' can not be null");
        }
        try {
            return this.handleIsEntityDirectMember(blogId, entityId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.isEntityDirectMember(Long blogId, Long entityId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#removeMemberByEntityId(Long, Long)
     */
    @Override
    public void removeMemberByEntityId(Long blogId, Long entityId)
            throws NoBlogManagerLeftException, BlogNotFoundException, BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.removeMemberByEntityId(Long blogId, Long entityId) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.removeMemberByEntityId(Long blogId, Long entityId) - 'entityId' can not be null");
        }
        try {
            this.handleRemoveMemberByEntityId(blogId, entityId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.removeMemberByEntityId(Long blogId, Long entityId)' --> "
                            + rt, rt);
        }
    }

    /**
     *
     * @see BlogRightsManagement#removeMemberByEntityIdForExternal(Long, Long, String)
     */
    @Override
    public void removeMemberByEntityIdForExternal(Long blogId, Long entityId,
            String externalSystemId) throws BlogNotFoundException, BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.removeMemberByEntityIdForExternal(Long blogId, Long entityId, String externalSystemId) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.removeMemberByEntityIdForExternal(Long blogId, Long entityId, String externalSystemId) - 'entityId' can not be null");
        }
        if (externalSystemId == null || externalSystemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.removeMemberByEntityIdForExternal(Long blogId, Long entityId, String externalSystemId) - 'externalSystemId' can not be null or empty");
        }
        try {
            this.handleRemoveMemberByEntityIdForExternal(blogId, entityId, externalSystemId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.removeMemberByEntityIdForExternal(Long blogId, Long entityId, String externalSystemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#removeUserFromAllBlogs(java .lang.Long, java.util.Collection<Long>)
     */
    @Override
    public void removeUserFromAllBlogs(Long userId,
            java.util.Collection<Long> blogsToNotCheckForLastManager)
            throws com.communote.server.api.core.security.AuthorizationException,
            NoBlogManagerLeftException {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.removeUserFromAllBlogs(Long userId, java.util.Collection<Long> blogsToNotCheckForLastManager) - 'userId' can not be null");
        }
        if (blogsToNotCheckForLastManager == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.removeUserFromAllBlogs(Long userId, java.util.Collection<Long> blogsToNotCheckForLastManager) - 'blogsToNotCheckForLastManager' can not be null");
        }
        try {
            this.handleRemoveUserFromAllBlogs(userId, blogsToNotCheckForLastManager);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.removeUserFromAllBlogs(Long userId, java.util.Collection<Long> blogsToNotCheckForLastManager)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#setAllCanReadAllCanWrite(Long, boolean, boolean)
     */
    @Override
    public com.communote.server.model.blog.Blog setAllCanReadAllCanWrite(Long blogId,
            boolean allCanRead, boolean allCanWrite) throws BlogAccessException,
            BlogNotFoundException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.setAllCanReadAllCanWrite(Long blogId, boolean allCanRead, boolean allCanWrite) - 'blogId' can not be null");
        }
        try {
            return this.handleSetAllCanReadAllCanWrite(blogId, allCanRead, allCanWrite);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.setAllCanReadAllCanWrite(Long blogId, boolean allCanRead, boolean allCanWrite)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#userHasManagementAccess(Long, Long)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean userHasManagementAccess(Long blogId, Long userId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.userHasManagementAccess(Long blogId, Long userId) - 'blogId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.userHasManagementAccess(Long blogId, Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleUserHasManagementAccess(blogId, userId);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.userHasManagementAccess(Long blogId, Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogRightsManagement#userHasReadAccess(Long, Long, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean userHasReadAccess(Long blogId, Long userId, boolean ignoreAllCanFlags) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.userHasReadAccess(Long blogId, Long userId, boolean ignoreAllCanFlags) - 'blogId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.userHasReadAccess(Long blogId, Long userId, boolean ignoreAllCanFlags) - 'userId' can not be null");
        }
        try {
            return this.handleUserHasReadAccess(blogId, userId, ignoreAllCanFlags);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.userHasReadAccess(Long blogId, Long userId, boolean ignoreAllCanFlags)' --> "
                            + rt, rt);
        }
    }

    /**
     * @param blogId
     *            identifier of topic
     * @param userId
     *            identifier of user
     * @param ignoreAllCanFlags
     *            ignore all can read or write
     * @see BlogRightsManagement#userHasWriteAccess(Long, Long, boolean)
     * @return user has write access true or false
     */

    @Override
    @Transactional(readOnly = true)
    public boolean userHasWriteAccess(Long blogId, Long userId, boolean ignoreAllCanFlags) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.userHasWriteAccess - 'blogId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "BlogRightsManagement.userHasWriteAccess - 'userId' can not be null");
        }
        try {
            return this.handleUserHasWriteAccess(blogId, userId, ignoreAllCanFlags);
        } catch (RuntimeException rt) {
            throw new BlogRightsManagementException(
                    "Error performing 'BlogRightsManagement.userHasWriteAccess' --> " + rt, rt);
        }
    }
}