package com.communote.server.core.blog;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.CollectionConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogMemberNotFoundException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.external.ExternalObjectNotAssignedException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.core.blog.events.AllUsersTopicAccessRightsChangedEvent;
import com.communote.server.core.blog.events.EntityTopicAccessRightsChangedEvent;
import com.communote.server.core.blog.events.ManagerGainedTopicAccessRightsChangedEvent;
import com.communote.server.core.blog.events.UserToTopicRoleMappingChangedEvent;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserSecurityHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogMember;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.ExternalBlogMember;
import com.communote.server.model.blog.UserToBlogRoleMapping;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.blog.BlogDao;
import com.communote.server.persistence.blog.BlogMemberDao;
import com.communote.server.persistence.blog.UserToBlogRoleMappingDao;
import com.communote.server.persistence.helper.dao.LazyClassLoaderHelper;
import com.communote.server.persistence.user.CommunoteEntityDao;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.group.UserOfGroupDao;

/**
 * @see com.communote.server.api.core.blog.BlogRightsManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("blogRightsManagement")
public class BlogRightsManagementImpl extends BlogRightsManagementBase {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(BlogRightsManagementImpl.class);

    private static final Logger ACCESS_RIGHTS_CHANGED_LOGGER = LoggerFactory
            .getLogger("accessRightsChangedLogger");

    private AssignedBlogRoleRetriever roleRetriever = null;

    @Autowired
    private BlogDao blogDao;

    @Autowired
    private CommunoteEntityDao kenmeiEntityDao;

    @Autowired
    private UserDao kenmeiUserDao;

    @Autowired
    private UserToBlogRoleMappingDao userToBlogRoleMappingDao;

    @Autowired
    private BlogMemberDao blogMemberDao;

    @Autowired
    private EventDispatcher eventDispatcher;

    @Autowired
    private ExternalObjectManagement externalObjectManagement;

    @Autowired
    private TopicPermissionManagement topicPermissionManagement;

    /**
     * Load a blog by its ID and throws an exception if the blog does not exist.
     *
     * @param blogId
     *            the ID of the blog to load
     * @return the blog
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     */
    private Blog assertBlogExists(Long blogId) throws BlogNotFoundException {
        Blog b = blogDao.load(blogId);
        if (b == null) {
            throw new BlogNotFoundException("Blog with ID " + blogId + " was not found.", blogId,
                    null);
        }
        return b;
    }

    /**
     * Assert that the current user is allowed to change and remove roles from a blog that were
     * added for an external system
     *
     * @param blogId
     *            The blogs id.
     * @throws BlogAccessException
     *             in case the current user is not client manager or blog manager
     */
    private void assertChangeAndRemoveRoleAccessForExternal(Long blogId) throws BlogAccessException {
        boolean hasAccess = SecurityHelper.isClientManager();
        if (!hasAccess) {
            BlogRole role = getRoleOfCurrentUser(blogId, true);
            // TODO use permissions instead?
            hasAccess = BlogRole.MANAGER.equals(role);
        }
        if (!hasAccess) {
            throw new BlogAccessException(
                    "The current user is not authorized to modify the blog access rights", blogId,
                    BlogRole.MANAGER, null);
        }
    }

    /**
     * Asserts whether a change of the blog role is possible or the user already has the role.
     *
     * @param blog
     *            the blog
     * @param entityId
     *            the ID of the entity whose role is to be changed
     * @param role
     *            the new role to assign
     * @param externalSystemId
     *            the ID of the external system
     * @return the blog member entity whose role should be changed or null if a role change is not
     *         necessary
     * @throws BlogMemberNotFoundException
     *             if the member does not exist
     * @throws NoBlogManagerLeftException
     *             if a role change would lead to a blog without manager
     */
    private BlogMember assertChangeRolePossibleOrNeeded(Blog blog, Long entityId, BlogRole role,
            String externalSystemId) throws BlogMemberNotFoundException, NoBlogManagerLeftException {
        BlogMember member = internalFindBlogMember(blog, entityId, externalSystemId);
        if (member == null) {
            throw new BlogMemberNotFoundException("Communote blog member for ID " + entityId
                    + " was not found.");
        }

        // check if modification is required
        BlogRole oldRole = member.getRole();
        if (oldRole.equals(role)) {
            return null;
        }
        CommunoteEntity entity = LazyClassLoaderHelper.deproxy(member.getMemberEntity(),
                CommunoteEntity.class);
        // avoid removal of last manager if entity is user and manager
        if (externalSystemId == null && BlogRole.MANAGER.equals(oldRole) && entity instanceof User) {
            if (!handleHasAnotherManager(blog.getId(), entityId)) {
                Map<Long, String> details = new HashMap<Long, String>();
                details.put(blog.getId(), blog.getTitle());
                throw new NoBlogManagerLeftException("Changing role of user " + entityId
                        + " would reuslt in blog without manager.", details);
            }
        }
        return member;
    }

    /**
     * Check if the user has the given permission on a blog
     *
     * @param topic
     *            the blog
     * @param permission
     *            The permission to check.
     * @throws BlogAccessException
     *             Thrown, when the use doesn't have the given permission.
     */
    private void assertPermission(Blog topic, Permission<Blog> permission)
            throws BlogAccessException {
        if (!topicPermissionManagement.hasPermission(topic.getId(), permission)) {
            throw new BlogAccessException(
                    "User got no permission to access this blog. Required permission: "
                            + permission.getIdentifier(), topic.getId(), permission);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public <T> Collection<T> getMappedUsers(Long topicId,
            CollectionConverter<UserToBlogRoleMapping, T> converter, BlogRole... roles) {
        Set<UserToBlogRoleMapping> mappings = new HashSet<UserToBlogRoleMapping>();
        for (BlogRole role : roles) {
            mappings.addAll(userToBlogRoleMappingDao.findMappings(topicId, null, null, null, role));
        }
        return converter.convert(mappings);
    }

    /**
     * Returns the role dependent of all read and write.
     *
     * @param canRead
     *            Flag to set if can read.
     * @param canWrite
     *            Falg to set if can write.
     * @return Member if canWrite, Viewer if not canWrite, but canRead, else null.
     */
    private BlogRole getRole(boolean canRead, boolean canWrite) {
        return canWrite ? BlogRole.MEMBER : canRead ? BlogRole.VIEWER : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleAddEntity(Long blogId, Long entityId, BlogRole role)
            throws BlogNotFoundException, CommunoteEntityNotFoundException, BlogAccessException {
        Blog blog = assertBlogExists(blogId);
        // don't check management right during blog creation
        if (blog.getMembers().size() != 0) {
            assertPermission(blog, TopicPermissionManagement.PERMISSION_EDIT_ACCESS_CONTROL_LIST);
        }
        internalAddEntity(blog, entityId, role, null);
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    protected void handleAddEntityForExternal(Long blogId, Long entityId, BlogRole role,
            String externalSystemId, String externalId) throws BlogNotFoundException,
            CommunoteEntityNotFoundException, ExternalObjectNotAssignedException,
            BlogAccessException {
        internalAddEntityForExternal(assertBlogExists(blogId), entityId, role, externalSystemId,
                externalId);
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    protected void handleAddEntityForExternalTrusted(Long blogId, Long entityId, BlogRole role,
            String externalSystemId) throws BlogNotFoundException,
            CommunoteEntityNotFoundException, BlogAccessException {
        internalAddEntityForExternalTrusted(assertBlogExists(blogId), entityId, role,
                externalSystemId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleAssignEntity(Long blogId, Long entityId, BlogRole role)
            throws BlogAccessException, BlogNotFoundException, CommunoteEntityNotFoundException,
            NoBlogManagerLeftException {
        handleAssignEntity(blogId, entityId, role, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleAssignEntity(Long blogId, Long entityId, BlogRole role, boolean createNote)
            throws BlogAccessException, BlogNotFoundException, CommunoteEntityNotFoundException,
            NoBlogManagerLeftException {
        Blog blog = assertBlogExists(blogId);
        assertPermission(blog, TopicPermissionManagement.PERMISSION_EDIT_ACCESS_CONTROL_LIST);
        internalAssignEntity(blog, entityId, role, createNote);
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    protected void handleAssignEntityForExternal(Long blogId, Long entityId, BlogRole role,
            String externalSystemId, String externalId) throws BlogNotFoundException,
            CommunoteEntityNotFoundException, ExternalObjectNotAssignedException,
            BlogAccessException {
        Blog blog = assertBlogExists(blogId);
        if (internalFindBlogMember(blog, entityId, externalSystemId) == null) {
            internalAddEntityForExternal(blog, entityId, role, externalSystemId, externalId);
        } else {
            try {
                handleChangeRoleOfMemberByEntityIdForExternal(blogId, entityId, role,
                        externalSystemId);
            } catch (BlogMemberNotFoundException e) {
                LOGGER.error("Unexpected exception", e);
                throw new BlogRightsManagementException("Unexpected exception", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleAssignEntityForExternalTrusted(Long blogId, Long entityId, BlogRole role,
            String externalSystemId) throws BlogAccessException, BlogNotFoundException,
            CommunoteEntityNotFoundException {
        Blog blog = assertBlogExists(blogId);
        if (internalFindBlogMember(blog, entityId, externalSystemId) == null) {
            // call trusted internal method which takes care of access check
            internalAddEntityForExternalTrusted(blog, entityId, role, externalSystemId);
        } else {
            try {
                // when changing a role there is no need to differentiate between trusted and not
                // trusted, so just call the change role method which also does the required access
                // check
                handleChangeRoleOfMemberByEntityIdForExternal(blogId, entityId, role,
                        externalSystemId);
            } catch (BlogMemberNotFoundException e) {
                LOGGER.error("Unexpected exception", e);
                throw new BlogRightsManagementException("Unexpected exception", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleAssignManagementAccessToCurrentUser(Long blogId)
            throws AuthorizationException, BlogNotFoundException {
        if (!SecurityHelper.isClientManager()) {
            throw new AuthorizationException(
                    "Current user must be client manager to add himself as blog manager.");
        }
        Blog blog = assertBlogExists(blogId);
        try {
            Long currentUserId = SecurityHelper.getCurrentUserId();
            internalAssignEntity(blog, currentUserId, BlogRole.MANAGER, false);
            eventDispatcher.fire(new ManagerGainedTopicAccessRightsChangedEvent(blogId, blog
                    .getTitle(), currentUserId));
        } catch (NoBlogManagerLeftException e) {
            // must not occur when adding a manager
            LOGGER.error("System error: NoBlogManagerLeftException although user was added as manager");
            throw new BlogRightsManagementException("Unexpected exception occurred.", e);
        } catch (CommunoteEntityNotFoundException e) {
            LOGGER.error("User currently logged in is not in DB.");
            throw new BlogRightsManagementException("Current user not found.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws BlogAccessException
     */
    @Override
    protected void handleChangePublicAccess(Long topicId, boolean allowPublicAccess)
            throws BlogNotFoundException, BlogAccessException {
        Blog topic = assertBlogExists(topicId);
        // don't check management right during blog creation
        if (topic.getMembers().size() != 0) {
            assertPermission(topic, TopicPermissionManagement.PERMISSION_EDIT_ACCESS_CONTROL_LIST);
        }
        topic = blogDao.load(topicId);
        if (topic == null) {
            throw new BlogNotFoundException("Blog not found.", topicId, "");
        }

        UserSecurityHelper.assertIsManagerOfBlog(topic.getId());
        boolean oldPublicAccess = topic.isPublicAccess();
        topic.setPublicAccess(allowPublicAccess);
        logTopicAccessChange("Change Public Access", topic.getNameIdentifier(), "public access",
                false, oldPublicAccess ? null : BlogRole.VIEWER,
                        allowPublicAccess ? BlogRole.VIEWER : null, null);
    }

    @Override
    protected void handleChangeRoleOfMemberByEntityId(Long blogId, Long entityId, BlogRole role)
            throws BlogNotFoundException, BlogMemberNotFoundException, NoBlogManagerLeftException,
            BlogAccessException {
        Blog blog = assertBlogExists(blogId);
        assertPermission(blog, TopicPermissionManagement.PERMISSION_EDIT_ACCESS_CONTROL_LIST);
        // TODO forcing a null entity ID is bad if we need to change an external membership for an
        // external system that is down
        internalChangeRoleOfMemberByEntityIdWithNotification(blog, entityId, role, null);
    }

    @Override
    protected void handleChangeRoleOfMemberByEntityIdForExternal(Long blogId, Long entityId,
            BlogRole role, String externalSystemId) throws BlogNotFoundException,
            BlogMemberNotFoundException, BlogAccessException {
        Blog blog = assertBlogExists(blogId);
        assertChangeAndRemoveRoleAccessForExternal(blogId);
        try {
            internalChangeRoleOfMemberByEntityId(blog, entityId, role, externalSystemId);
        } catch (NoBlogManagerLeftException e) {
            // cannot occur since handleHasAnotherManager asserts that there is always another not
            // external user with management access, so modifications for external won't change this
            LOGGER.error("Unexpected exception", e);
            throw new BlogRightsManagementException("Unexpected exception occurred.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleCurrentUserHasManagementAccess(Long blogId) {
        if (SecurityHelper.isPublicUser()) {
            return false;
        }
        if (SecurityHelper.isInternalSystem()) {
            return true;
        }
        return handleUserHasManagementAccess(blogId, SecurityHelper.assertCurrentUserId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleCurrentUserHasReadAccess(Long blogId, boolean ignoreAllCanFlags) {
        Blog topic = blogDao.load(blogId);
        boolean canRead = false;
        if (SecurityHelper.isPublicUser()) {
            if (isPublicAccessEnabled() && topic != null && topic.isPublicAccess()) {
                canRead = true;
            }
        } else if (SecurityHelper.isInternalSystem()) {
            canRead = true;
        } else {
            canRead = internalUserHasReadAccess(topic, SecurityHelper.assertCurrentUserId(),
                    ignoreAllCanFlags);
        }
        return canRead;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleCurrentUserHasWriteAccess(Long blogId, boolean ignoreAllCanFlags) {
        if (SecurityHelper.isPublicUser()) {
            return false;
        }
        if (SecurityHelper.isInternalSystem()) {
            return true;
        }
        return handleUserHasWriteAccess(blogId, SecurityHelper.assertCurrentUserId(),
                ignoreAllCanFlags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Blog handleGetAndCheckBlogAccess(Long blogId, BlogRole blogRole)
            throws BlogAccessException, BlogNotFoundException {
        Blog blog = blogDao.load(blogId);
        if (blog == null) {
            throw new BlogNotFoundException("Blog not found", blogId, null);
        }
        if (BlogRole.MANAGER.equals(blogRole)) {
            if (!handleCurrentUserHasManagementAccess(blogId)) {
                throw new BlogAccessException(
                        "User got no right to access this blog rightRequired=" + blogRole, blogId,
                        blogRole, null);
            }
        } else if (BlogRole.MEMBER.equals(blogRole)) {
            if (!handleCurrentUserHasWriteAccess(blogId, false)) {
                throw new BlogAccessException(
                        "User got no right to access this blog rightRequired=" + blogRole, blogId,
                        blogRole, null);
            }
        } else if (BlogRole.VIEWER.equals(blogRole)) {
            if (!handleCurrentUserHasReadAccess(blogId, false)) {
                throw new BlogAccessException(
                        "User got no right to access this blog rightRequired=" + blogRole, blogId,
                        blogRole, null);
            }
        }

        // TODO dislike!
        Hibernate.initialize(blog.getTags());
        return blog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BlogRole handleGetRoleOfCurrentUser(Long blogId, boolean ignoreAllCanFlags) {
        if (SecurityHelper.isInternalSystem()) {
            // internal system can do everything
            return BlogRole.MANAGER;
        }
        Blog blog = blogDao.load(blogId);
        BlogRole role = null;
        if (SecurityHelper.isPublicUser()) {
            if (isPublicAccessEnabled() && blog != null && blog.isPublicAccess()) {
                role = BlogRole.VIEWER;
            }
        } else {
            role = internalGetRoleOfUser(blog, SecurityHelper.assertCurrentUserId(),
                    ignoreAllCanFlags);
        }
        return role;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BlogRole handleGetRoleOfEntity(Long blogId, Long entityId, boolean ignoreAllCanFlags) {
        CommunoteEntity entity = kenmeiEntityDao.loadWithImplementation(entityId);
        if (entity == null) {
            return null;
        }
        Blog topic = blogDao.load(blogId);
        if (entity instanceof User) {
            return internalGetRoleOfUser(topic, entityId, ignoreAllCanFlags);
        }
        return internalGetRoleOfGroup(topic, (Group) entity, ignoreAllCanFlags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BlogRole handleGetRoleOfUser(Long blogId, Long userId, boolean ignoreAllCanFlags) {
        Blog b = blogDao.load(blogId);
        return internalGetRoleOfUser(b, userId, ignoreAllCanFlags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleHasAnotherManager(Long blogId, Long userId) {
        Blog blog = blogDao.load(blogId);
        if (blog != null) {
            for (BlogMember member : blog.getMembers()) {
                CommunoteEntity entity = LazyClassLoaderHelper.deproxy(member.getMemberEntity(),
                        CommunoteEntity.class);
                if (BlogRole.MANAGER.equals(member.getRole()) && !entity.getId().equals(userId)
                        && entity instanceof User && member.getExternalSystemId() == null) {
                    if (((User) entity).hasStatus(UserStatus.ACTIVE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleHasAnotherReader(Long blogId, Long userId) {
        Blog b = blogDao.load(blogId);
        if (b != null) {
            boolean hasReader = b.isAllCanRead() || b.isAllCanWrite();
            if (!hasReader) {
                Collection<UserToBlogRoleMapping> mappings = userToBlogRoleMappingDao.findMappings(
                        blogId, null, null, false, null);
                hasReader = mappingsContainAnotherActiveOrDeactivatedUser(mappings, userId);
                if (!hasReader) {
                    mappings = userToBlogRoleMappingDao
                            .findMappings(blogId, null, null, true, null);
                    hasReader = mappingsContainAnotherActiveOrDeactivatedUser(mappings, userId);
                }
            }
            return hasReader;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleIsEntityDirectMember(Long blogId, Long entityId) {
        Blog blog = blogDao.load(blogId);
        CommunoteEntity entity = kenmeiEntityDao.load(entityId);
        if (entity != null && blog != null) {
            Set<BlogMember> members = blog.getMembers();
            for (BlogMember member : members) {
                if (member.getMemberEntity().getId().equals(entityId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleRemoveMemberByEntityId(Long topicId, Long entityId)
            throws BlogAccessException, NoBlogManagerLeftException, BlogNotFoundException {
        Blog topic = assertBlogExists(topicId);
        assertPermission(topic, TopicPermissionManagement.PERMISSION_EDIT_ACCESS_CONTROL_LIST);
        CommunoteEntity entity = kenmeiEntityDao.loadWithImplementation(entityId);
        if (entity instanceof User && !hasAnotherManager(topicId, entityId)) {
            Map<Long, String> details = new HashMap<Long, String>();
            details.put(topicId, topic.getTitle());
            throw new NoBlogManagerLeftException("Cannot remove last blog manager from blog.",
                    details);
        }
        BlogMember member = internalFindBlogMember(topic, entityId, null);
        User currentUser = kenmeiUserDao.load(SecurityHelper.getCurrentUserId());
        if (member != null) {
            eventDispatcher.fire(new EntityTopicAccessRightsChangedEvent(topic.getId(), topic
                    .getTitle(), currentUser.getId(), entityId, !(entity instanceof User), member
                    .getRole(), null));
            internalRemoveMember(topic, member);
        } else {
            LOGGER.debug("The entity with ID {} is not a blog member. Will not delete.", entityId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleRemoveMemberByEntityIdForExternal(Long blogId, Long entityId,
            String externalSystemId) throws BlogNotFoundException, BlogAccessException {
        Blog blog = assertBlogExists(blogId);
        assertChangeAndRemoveRoleAccessForExternal(blogId);
        BlogMember member = internalFindBlogMember(blog, entityId, externalSystemId);
        if (member == null) {
            LOGGER.debug("The entity with ID {} is not a blog member. Ignored.", entityId);
        } else {
            internalRemoveMember(blog, member);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleRemoveUserFromAllBlogs(Long userId,
            Collection<Long> blogsToNotCheckForLastManager) throws AuthorizationException,
            NoBlogManagerLeftException {
        if (!SecurityHelper.isInternalSystem() && !userId.equals(SecurityHelper.getCurrentUserId())
                && !SecurityHelper.isClientManager()) {
            throw new AuthorizationException("Current user has not the right to remove the user "
                    + userId + " from all blogs.");
        }
        Collection<UserToBlogRoleMapping> mappings = userToBlogRoleMappingDao.findMappings(null,
                userId, null, false, null);
        for (UserToBlogRoleMapping mapping : mappings) {
            Long blogId = mapping.getBlogId();
            Blog b = blogDao.load(blogId);
            if (!blogsToNotCheckForLastManager.contains(blogId)
                    && !hasAnotherManager(blogId, userId)) {
                Map<Long, String> details = new HashMap<Long, String>();
                details.put(mapping.getBlogId(), b.getTitle());
                throw new NoBlogManagerLeftException("Blog would end up without manager.", details);
            }
            BlogMember member = internalFindBlogMember(b, userId, mapping.getExternalSystemId());
            if (member == null) {
                throw new BlogRightsManagementException(
                        "DB inconsistency: user found in role mapping "
                                + "but not in member table.");
            }
            internalRemoveMember(b, member);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Blog handleSetAllCanReadAllCanWrite(Long blogId, boolean allCanRead,
            boolean allCanWrite) throws BlogNotFoundException, BlogAccessException {

        // only set the flags if the user is allowed to do so and blog exists
        Blog topic = assertBlogExists(blogId);
        assertPermission(topic, TopicPermissionManagement.PERMISSION_EDIT_ACCESS_CONTROL_LIST);

        if (topic == null
                || (topic.isAllCanRead() == allCanRead && topic.isAllCanWrite() == allCanWrite)
                || (!BlogManagementHelper.canSetAllCanReadWrite() && (allCanRead || allCanWrite))) {
            return topic;
        }

        User creator = kenmeiUserDao.load(SecurityHelper.getCurrentUserId());
        BlogRole oldRole = getRole(topic.isAllCanRead(), topic.isAllCanWrite());
        BlogRole newRole = getRole(allCanRead, allCanWrite);
        eventDispatcher.fire(new AllUsersTopicAccessRightsChangedEvent(topic.getId(), topic
                .getTitle(), creator.getId(), oldRole, newRole));
        topic.setAllCanRead(allCanRead);
        topic.setAllCanWrite(allCanWrite);
        topic.setLastModificationDate(new Timestamp(new Date().getTime()));

        logTopicAccessChange("Change All Users", topic.getNameIdentifier(), "All Users", false,
                oldRole, getRole(allCanRead, allCanWrite), null);

        return topic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleUserHasManagementAccess(Long blogId, Long userId) {
        BlogRole role = this.roleRetriever.getAssignedRole(blogId, userId);
        return role != null && role.equals(BlogRole.MANAGER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleUserHasReadAccess(Long blogId, Long userId, boolean ignoreAllCanFlags) {
        Blog b = blogDao.load(blogId);
        return internalUserHasReadAccess(b, userId, ignoreAllCanFlags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleUserHasWriteAccess(Long blogId, Long userId, boolean ignoreAllCanFlags) {
        Blog b = blogDao.load(blogId);
        boolean canWrite = false;
        if (b != null) {
            canWrite = !ignoreAllCanFlags && (b.isAllCanWrite());
            if (!canWrite) {
                BlogRole role = this.roleRetriever.getAssignedRole(blogId, userId);
                canWrite = role != null && !role.equals(BlogRole.VIEWER);
            }
        }
        return canWrite;
    }

    /**
     * Initializes necessary resources.
     */
    @PostConstruct
    public void init() {
        roleRetriever = new AssignedBlogRoleRetriever(userToBlogRoleMappingDao);
        eventDispatcher.register(roleRetriever);
    }

    /**
     * Adds an entity to a blog.
     *
     * @param blog
     *            the blog to add the entity to
     * @param entityId
     *            the ID of the entity to add
     * @param role
     *            the role the entity should get
     * @param externalSystemId
     *            ID of the external system for which the entity is added; if null the system is
     *            communote
     * @return the added blog member or null if the entity is already a member
     * @throws CommunoteEntityNotFoundException
     *             if the entity does not exist
     */
    private CommunoteEntity internalAddEntity(Blog blog, Long entityId, BlogRole role,
            String externalSystemId) throws CommunoteEntityNotFoundException {
        CommunoteEntity entity = kenmeiEntityDao.loadWithImplementation(entityId);
        if (entity == null) {
            throw new CommunoteEntityNotFoundException("The entity with ID " + entityId
                    + " was not found.");
        }
        if (internalFindBlogMember(blog, entityId, externalSystemId) != null) {
            // user does already exist -> do nothing
            return null;
        }
        BlogMember member;
        if (StringUtils.isEmpty(externalSystemId)) {
            member = BlogMember.Factory.newInstance(role, blog, entity);
        } else {
            member = ExternalBlogMember.Factory.newInstance(externalSystemId, role, blog, entity);
        }
        blogMemberDao.create(member);
        blog.getMembers().add(member);
        // TODO it's not validated that the user is not deleted, etc.
        if (entity instanceof User) {
            UserToBlogRoleMapping mapping = UserToBlogRoleMapping.Factory.newInstance(blog.getId(),
                    entityId, BlogRoleHelper.convertRoleToNumeric(role), externalSystemId, false,
                    null);
            userToBlogRoleMappingDao.create(mapping);

            eventDispatcher.fire(new UserToTopicRoleMappingChangedEvent(blog.getId(), entity
                    .getId(), false, null, role, null));
        } else {
            Group group = (Group) entity;
            storeBlogRolesForGroupMembers(blog.getId(), null, role, group, externalSystemId);

        }
        logTopicAccessChange("Add", blog.getNameIdentifier(), entity.getAlias(),
                entity instanceof User, null, role, externalSystemId);
        return entity;
    }

    /**
     * Add an entity to the blog on the behalf of an external system. This will first check if the
     * blog is assigned to the external object and the user has sufficient permissions.
     *
     * @param blog
     *            the blog to add the entity to
     * @param entityId
     *            the Id of the entity
     * @param role
     *            the role the member should get
     * @param externalSystemId
     *            the ID of the external system
     * @param externalId
     *            the ID of the external object within the external system
     * @throws CommunoteEntityNotFoundException
     *             in case there is no entity for the given ID
     * @throws ExternalObjectNotAssignedException
     *             in case the external object is not assigned to the blog
     * @throws BlogAccessException
     *             in case the current user is not allowed add the entity
     * @throws BlogNotFoundException
     *             in case the blog does not exist, should not be thrown.
     */
    private void internalAddEntityForExternal(Blog blog, Long entityId, BlogRole role,
            String externalSystemId, String externalId) throws CommunoteEntityNotFoundException,
            ExternalObjectNotAssignedException, BlogAccessException, BlogNotFoundException {
        if (externalObjectManagement.isExternalObjectAssigned(blog.getId(), externalSystemId,
                externalId)) {
            // for not trusted external operations the user has to have management permissions
            // TODO better check for MANAGER role for external operation?
            assertPermission(blog, TopicPermissionManagement.PERMISSION_EDIT_ACCESS_CONTROL_LIST);
            internalAddEntity(blog, entityId, role, externalSystemId);
        } else {
            throw new ExternalObjectNotAssignedException(
                    "The external object is not assigned to the blog", blog.getId(), null,
                    externalId, externalSystemId);
        }
    }

    /**
     * Add an entity to the blog on the behalf of an external system. This method won't check if the
     * blog is associated with an external object thus it is only intended for trusted invocations
     * were the caller made sure that the association exists.
     *
     * @param blog
     *            the blog to add the entity to
     * @param entityId
     *            the Id of the entity
     * @param role
     *            the role the member should get
     * @param externalSystemId
     *            the ID of the external system
     * @throws CommunoteEntityNotFoundException
     *             in case there is no entity for the given ID
     * @throws BlogAccessException
     *             in case the current user is not allowed add the entity
     */
    private void internalAddEntityForExternalTrusted(Blog blog, Long entityId, BlogRole role,
            String externalSystemId) throws CommunoteEntityNotFoundException, BlogAccessException {
        if (SecurityHelper.isClientManager()) {
            internalAddEntity(blog, entityId, role, externalSystemId);
        } else {
            throw new BlogAccessException("This operation requires the client manager role",
                    blog.getId(), null, null);
        }
    }

    /**
     * Add an entity to a blog or update the role of that entity if it already is a member of the
     * blog. This method is intended for operations that are not executed on the behalf of an
     * external system.
     *
     * @param topic
     *            the blog the entity should be add to
     * @param entityId
     *            the ID of the entity to add or update
     * @param role
     *            the role the entity should get
     * @param createNote
     *            whether to create a note about the change in the access rights
     * @throws CommunoteEntityNotFoundException
     *             in case the entity does not exist
     * @throws NoBlogManagerLeftException
     *             in case the operation would leave the blog without a manager
     */
    private void internalAssignEntity(Blog topic, Long entityId, BlogRole role, boolean createNote)
            throws CommunoteEntityNotFoundException, NoBlogManagerLeftException {
        BlogRole oldRole = getRoleOfEntity(topic.getId(), entityId, false);
        CommunoteEntity entity = null;
        if (internalFindBlogMember(topic, entityId, null) == null) {
            entity = internalAddEntity(topic, entityId, role, null);
        } else {
            try {
                entity = internalChangeRoleOfMemberByEntityId(topic, entityId, role, null);
            } catch (BlogMemberNotFoundException e) {
                LOGGER.error("Blog member was removed while changing it's role.");
                throw new BlogRightsManagementException(
                        "Blog member was removed while changing it's role.");
            }
        }
        if (createNote && entity != null) {
            eventDispatcher.fire(new EntityTopicAccessRightsChangedEvent(topic.getId(), topic
                    .getTitle(), SecurityHelper.getCurrentUserId(), entityId,
                    !(entity instanceof User), oldRole, role));
        }
    }

    /**
     * Changes the role of a blog member. The blog member, identified by its entity ID, must exist.
     *
     * @param topic
     *            the blog to change
     * @param entityId
     *            the ID of the entity whose membership is to be changed
     * @param role
     *            the new role
     * @param externalSystemId
     *            the ID of the external system that added the entity to the blog. Must be null if a
     *            user is sought who was added by using communote FE.
     * @return the entity whose membership was changed or null if the membership was not changed
     *         (e.g. already had the required role)
     * @throws BlogMemberNotFoundException
     *             if the blog member was not found
     * @throws NoBlogManagerLeftException
     *             if changing the role would lead to a blog without manager
     */
    private CommunoteEntity internalChangeRoleOfMemberByEntityId(Blog topic, Long entityId,
            BlogRole role, String externalSystemId) throws BlogMemberNotFoundException,
            NoBlogManagerLeftException {

        final BlogRole beforeTopicRole = this.handleGetRoleOfEntity(topic.getId(), entityId, false);

        BlogMember member = assertChangeRolePossibleOrNeeded(topic, entityId, role,
                externalSystemId);
        if (member == null) {
            return null;
        }
        CommunoteEntity entity = LazyClassLoaderHelper.deproxy(member.getMemberEntity(),
                CommunoteEntity.class);
        BlogRole oldRole = member.getRole();
        member.setRole(role);
        // update the mapping table

        if (entity instanceof User) {
            Collection<UserToBlogRoleMapping> mappings = userToBlogRoleMappingDao
                    .findMappingsForExternal(topic.getId(), entityId, null, false, oldRole,
                            externalSystemId);
            for (UserToBlogRoleMapping mapping : mappings) {
                mapping.setNumericRole(BlogRoleHelper.convertRoleToNumeric(role));
                // because we modifying the mapping directly we must fire the change event, for
                // other operations this is done in the DAO -> pretty ugly
                eventDispatcher.fire(new AssignedBlogRoleChangedEvent(mapping.getBlogId(), mapping
                        .getUserId()));
                eventDispatcher.fire(new UserToTopicRoleMappingChangedEvent(topic.getId(), entity
                        .getId(), false, BlogRoleHelper.convertNumericToRole(mapping
                                .getNumericRole()), null, beforeTopicRole));
            }
        } else {
            Group group = (Group) entity;
            Collection<UserToBlogRoleMapping> mappings = userToBlogRoleMappingDao
                    .findMappingsForExternal(topic.getId(), null, entityId, true, oldRole,
                            externalSystemId);
            // TODO next code sequence might trigger 2 blog role change events for the same
            // user-blog combination (one when removing the mapping and one when creating a new)

            // remove old mappings
            for (UserToBlogRoleMapping mapping : mappings) {
                mapping.getGrantingGroups().remove(group);
                if (mapping.getGrantingGroups().size() == 0) {
                    userToBlogRoleMappingDao.remove(mapping);

                    eventDispatcher.fire(new UserToTopicRoleMappingChangedEvent(topic.getId(),
                            group.getId(), true, BlogRoleHelper.convertNumericToRole(mapping
                                    .getNumericRole()), null, beforeTopicRole));

                }
            }
            // create mappings for group members and new role
            storeBlogRolesForGroupMembers(topic.getId(), oldRole, role, group, externalSystemId);
        }
        logTopicAccessChange("Change", topic.getNameIdentifier(), entity.getAlias(),
                entity instanceof User, oldRole, role, externalSystemId);
        return entity;
    }

    /**
     * Changes the role of a blog member and create a note about this operation in the blog. The
     * blog member, identified by its entity ID, must exist.
     *
     * @param topic
     *            the blog to change
     * @param entityId
     *            the ID of the entity whose membership is to be changed
     * @param role
     *            the new role
     * @param externalSystemId
     *            the ID of the external system that added the entity to the blog. Must be null if a
     *            user is sought who was added by using communote FE.
     * @return the entity whose membership was changed or null if the membership was not changed
     *         (e.g. already had the required role)
     * @throws BlogMemberNotFoundException
     *             if the blog member was not found
     * @throws NoBlogManagerLeftException
     *             if changing the role would lead to a blog without manager
     */
    private CommunoteEntity internalChangeRoleOfMemberByEntityIdWithNotification(Blog topic,
            Long entityId, BlogRole role, String externalSystemId)
                    throws BlogMemberNotFoundException, NoBlogManagerLeftException {
        BlogRole oldRole = getRoleOfEntity(topic.getId(), entityId, true);
        CommunoteEntity entity = internalChangeRoleOfMemberByEntityId(topic, entityId, role,
                externalSystemId);
        if (entity != null) {
            eventDispatcher.fire(new EntityTopicAccessRightsChangedEvent(topic.getId(), topic
                    .getTitle(), SecurityHelper.getCurrentUserId(), entityId,
                    !(entity instanceof User), oldRole, role));
        }
        return entity;
    }

    /**
     * Returns a topic member for the specified entity, external system and blog.
     *
     * @param topic
     *            the blog to check
     * @param entityId
     *            the ID of the entity whose blog membership entity is to be retrieved
     * @param externalSystemId
     *            the ID of the external system that added the entity to the blog. Must be null if a
     *            user is sought who was added by using communote FE.
     * @return the blog member or null if there is none
     */
    private BlogMember internalFindBlogMember(Blog topic, Long entityId, String externalSystemId) {
        Set<BlogMember> members = topic.getMembers();
        if (members == null) {
            members = new HashSet<BlogMember>();
            topic.setMembers(members);
        } else {
            for (BlogMember member : members) {
                // make sure the ID and systemId match
                if (member.getMemberEntity().getId().equals(entityId)
                        && StringUtils.equals(member.getExternalSystemId(), externalSystemId)) {
                    return member;
                }
            }
        }
        return null;
    }

    /**
     * Returns the role of the group in a blog or null if the group has no access.
     *
     * @param blog
     *            the blog to test
     * @param group
     *            the group to test
     * @param ignoreAllCanFlags
     *            if true the allCanRead and allCanWrite flag will be ignored and only the directly
     *            assigned role will be considered.
     * @return the role or null
     */
    private BlogRole internalGetRoleOfGroup(Blog blog, Group group, boolean ignoreAllCanFlags) {
        if (blog == null) {
            return null;
        }
        BlogRole role = null;
        if (!ignoreAllCanFlags) {
            role = blog.isAllCanWrite() ? BlogRole.MEMBER : blog.isAllCanRead() ? BlogRole.VIEWER
                    : null;
        }
        for (BlogMember m : blog.getMembers()) {
            if (m.getMemberEntity().getId().equals(group.getId())) {
                BlogRole assignedRole = m.getRole();
                if (role == null) {
                    role = assignedRole;
                } else if (!assignedRole.equals(BlogRole.VIEWER)) {
                    role = assignedRole;
                }
            }
        }

        return role;
    }

    /**
     * Returns the role of the user in a blog or null if the user has no access.
     *
     * @param blog
     *            the blog to test
     * @param userId
     *            the ID of the user to test
     * @param ignoreAllCanFlags
     *            if true the allCanRead and allCanWrite flag will be ignored and only the directly
     *            assigned role will be considered. The directly assigned role covers the role
     *            gained through group membership.
     * @return the role of the user for the blog
     */
    private BlogRole internalGetRoleOfUser(Blog blog, Long userId, boolean ignoreAllCanFlags) {
        if (blog == null) {
            return null;
        }
        BlogRole role = this.roleRetriever.getAssignedRole(blog.getId(), userId);

        if (!ignoreAllCanFlags && (role == null || role.equals(BlogRole.VIEWER))) {
            if (blog.isAllCanWrite()) {
                role = BlogRole.MEMBER;
            } else if (blog.isAllCanRead()) {
                role = BlogRole.VIEWER;
            }
        }

        if (role == null && isPublicAccessEnabled() && blog.isPublicAccess()) {
            role = BlogRole.VIEWER;
        }
        return role;
    }

    /**
     * Removes a member from a blog.
     *
     * @param blog
     *            the blog
     * @param member
     *            the member to remove
     */
    private void internalRemoveMember(Blog blog, BlogMember member) {
        blog.getMembers().remove(member);
        // update blog rights
        CommunoteEntity entity = LazyClassLoaderHelper.deproxy(member.getMemberEntity(),
                CommunoteEntity.class);
        final BlogRole beforeRole = this.handleGetRoleOfUser(blog.getId(), entity.getId(), false);

        if (entity instanceof User) {
            Collection<UserToBlogRoleMapping> mappings = userToBlogRoleMappingDao
                    .findMappingsForExternal(blog.getId(), entity.getId(), null, false,
                            member.getRole(), member.getExternalSystemId());
            for (UserToBlogRoleMapping mapping : mappings) {

                userToBlogRoleMappingDao.remove(mapping);

                eventDispatcher.fire(new UserToTopicRoleMappingChangedEvent(blog.getId(), mapping
                        .getUserId(), true, beforeRole, null, beforeRole));

            }

        } else {
            Group group = (Group) entity;
            Collection<UserToBlogRoleMapping> mappings = userToBlogRoleMappingDao
                    .findMappingsForExternal(blog.getId(), null, group.getId(), true,
                            member.getRole(), member.getExternalSystemId());
            for (UserToBlogRoleMapping mapping : mappings) {
                mapping.getGrantingGroups().remove(group);

                BlogRole finalRole = BlogRoleHelper.convertNumericToRole(mapping.getNumericRole());
                if (mapping.getGrantingGroups().size() == 0) {

                    userToBlogRoleMappingDao.remove(mapping);
                    finalRole = null;
                }

                eventDispatcher.fire(new UserToTopicRoleMappingChangedEvent(blog.getId(), mapping
                        .getUserId(), false, group.getId(), BlogRoleHelper
                        .convertNumericToRole(mapping.getNumericRole()), finalRole, beforeRole));
            }
        }
        blogMemberDao.remove(member);
        logTopicAccessChange("Remove", blog.getNameIdentifier(), entity.getAlias(),
                entity instanceof User, member.getRole(), null, null);
    }

    /**
     * Tests whether a user has read access to a blog
     *
     * @param blog
     *            the blog ot test
     * @param userId
     *            the ID of the user to test
     * @param ignoreAllCanFlags
     *            if true the allCanRead and allCanWrite flag will be ignored and only the directly
     *            assigned role will be considered. The directly assigned role covers the role
     *            gained through group membership.
     * @return true if the user has read access, false otherwise
     */
    private boolean internalUserHasReadAccess(Blog blog, Long userId, boolean ignoreAllCanFlags) {
        boolean canRead = false;
        if (blog != null) {
            canRead = isPublicAccessEnabled() && blog.isPublicAccess();
            if (!canRead) {
                canRead = !ignoreAllCanFlags && (blog.isAllCanRead() || blog.isAllCanWrite());
            }
            if (!canRead) {
                canRead = this.roleRetriever.getAssignedRole(blog.getId(), userId) != null;
            }
        }
        return canRead;
    }

    /**
     * Checks the client configuration for the public access option. True if the client supports
     * public access and false if not.
     *
     * @return true if client allows public access
     */
    private boolean isPublicAccessEnabled() {
        return CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS,
                        ClientConfigurationHelper.DEFAULT_ALLOW_PUBLIC_ACCESS);
    }

    /**
     * Method to log changes to the topics rights.
     *
     * @param event
     *            The event occured (i.e. "Add")
     * @param topicAlias
     *            The topics alias.
     * @param entityAlias
     *            The entities alias.
     * @param isUser
     *            True, if the entity is a user, else false (the entity is a group).
     * @param oldRole
     *            The old role, if any.
     * @param newRole
     *            The new role, if any
     * @param externalSystemId
     *            The external systems id, if any.
     */
    private void logTopicAccessChange(String event, String topicAlias, String entityAlias,
            boolean isUser, BlogRole oldRole, BlogRole newRole, String externalSystemId) {
        if (!ACCESS_RIGHTS_CHANGED_LOGGER.isDebugEnabled()) {
            return;
        }
        String by = "";
        if (SecurityHelper.isInternalSystem()) {
            by = "Internal System User";
        } else if (SecurityHelper.isPublicUser()) {
            by = "Public User";
        } else {
            by = SecurityHelper.getCurrentUserAlias();
        }
        ACCESS_RIGHTS_CHANGED_LOGGER.debug(
                "Topic access rights changed ({}): Topic: {}, Alias: {}, isUser: {},"
                        + " Old Role: {}, New Role: {}, ExternalSystemId: {}, By: {}", event,
                        topicAlias, entityAlias, isUser, oldRole, newRole, externalSystemId, by);
    }

    /**
     * Returns true if the mappings contain a user with status active or temp. disabled and an ID
     * that is different from the provided ID.
     *
     * @param mappings
     *            the mappings to analyze
     * @param userId
     *            the user ID to check
     * @return true if there is such a mapping
     */
    private boolean mappingsContainAnotherActiveOrDeactivatedUser(
            Collection<UserToBlogRoleMapping> mappings, Long userId) {
        for (UserToBlogRoleMapping mapping : mappings) {
            if (!mapping.getUserId().equals(userId)) {
                User user = kenmeiUserDao.load(mapping.getUserId());
                if (user.getStatus().equals(UserStatus.ACTIVE)
                        || user.getStatus().equals(UserStatus.TEMPORARILY_DISABLED)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Stores the blog role for the group members. This includes the
     *
     * @param blogId
     *            the ID of the blog to store the role for.
     * @param newRole
     *            the role to store
     * @param group
     *            the group that was added to the blog
     * @param externalSystemId
     *            ID of the external system for which the access mappings are to be stored. Should
     *            be null for communote.
     */
    private void storeBlogRolesForGroupMembers(Long blogId, BlogRole oldRole, BlogRole newRole,
            Group group, String externalSystemId) {
        Collection<Long> users = ServiceLocator.findService(UserOfGroupDao.class).getUsersOfGroup(
                group.getId());
        for (Long userId : users) {
            Collection<UserToBlogRoleMapping> mappings = userToBlogRoleMappingDao
                    .findMappingsForExternal(blogId, userId, null, true, newRole, externalSystemId);
            boolean existingMappingFound = false;
            // check for an existing mapping and extend it
            for (UserToBlogRoleMapping existingMapping : mappings) {
                existingMapping.getGrantingGroups().add(group);
                existingMappingFound = true;

                eventDispatcher.fire(new UserToTopicRoleMappingChangedEvent(blogId, userId, false,
                        group.getId(), newRole, newRole, oldRole));

                break;
            }
            if (!existingMappingFound) {

                UserToBlogRoleMapping mapping = UserToBlogRoleMapping.Factory.newInstance(blogId,
                        userId, BlogRoleHelper.convertRoleToNumeric(newRole), true);
                mapping.setGrantingGroups(new HashSet<Group>());
                mapping.getGrantingGroups().add(group);
                mapping.setExternalSystemId(externalSystemId);
                userToBlogRoleMappingDao.create(mapping);

                eventDispatcher.fire(new UserToTopicRoleMappingChangedEvent(blogId, userId, false,
                        group.getId(), null, newRole, oldRole));
            }
        }
    }
}
