package com.communote.server.core.permission.filters;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteRolePermissionFilter implements NotePermissionFilter {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(NoteRolePermissionFilter.class);

    private final BlogRightsManagement blogRightsManagement;
    private final NoteService noteManagement;
    private final UserManagement userManagement;

    /**
     * Constructor.
     * 
     * @param noteManagement
     *            The note management.
     * @param blogRightsManagement
     *            The blog rights management.
     * @param userManagement
     *            the user management
     */
    public NoteRolePermissionFilter(NoteService noteManagement,
            BlogRightsManagement blogRightsManagement, UserManagement userManagement) {
        this.noteManagement = noteManagement;
        this.blogRightsManagement = blogRightsManagement;
        this.userManagement = userManagement;
    }

    /**
     * Add the permission for commenting, if the user is allowed to.
     * 
     * @param note
     *            The note.
     * @param blogRole
     *            The users blog role.
     * @param permissions
     *            The permissions.
     */
    private void handleComment(Note note, BlogRole blogRole, Set<Permission<Note>> permissions) {
        if (NoteCreationSource.SYSTEM.equals(note.getCreationSource())
                || BlogRole.VIEWER.equals(blogRole)) {
            return;
        }
        if (!note.getUser().getAlias().startsWith(UserManagement.ANONYMIZE_USER_PREFIX)
                || note.getUsersToBeNotified().size() > 0 || !note.isDirect()) {
            permissions.add(NotePermissionManagement.PERMISSION_COMMENT);
        }
    }

    /**
     * Add the permission for deletion, if the user is allowed to.
     * 
     * @param note
     *            The note.
     * @param blogRole
     *            The users blog role.
     * @param permissions
     *            The permissions.
     */
    private void handleDelete(Note note, BlogRole blogRole, Set<Permission<Note>> permissions) {
        if (NoteCreationSource.SYSTEM.equals(note.getCreationSource())) {
            return;
        }
        try {
            if (SecurityHelper.isInternalSystem()
                    || BlogRole.MANAGER.equals(blogRole)
                    || (permissions.contains(NotePermissionManagement.PERMISSION_EDIT) && noteManagement
                            .getNumberOfReplies(note.getId()) == 0)) {
                permissions.add(NotePermissionManagement.PERMISSION_DELETE);
            }
        } catch (NoteNotFoundException e) {
            LOGGER.error("A note ({}) which should exist couldn't be found.", note.getId());
        }
    }

    /**
     * Add the permission for editing, if the user is allowed to.
     * 
     * @param note
     *            The note.
     * @param currentUserId
     *            the ID of the current user, can be null
     * @param blogRole
     *            The users blog role.
     * @param permissions
     *            The permissions.
     */
    private void handleEdit(Note note, Long currentUserId, BlogRole blogRole,
            Set<Permission<Note>> permissions) {
        // Direct notes with children might note be edited.
        if (note.isDirect() && note.getChildren() != null && !note.getChildren().isEmpty()) {
            return;
        }
        if (SecurityHelper.isInternalSystem()
                || ((BlogRole.MANAGER.equals(blogRole) || BlogRole.MEMBER.equals(blogRole))
                        && note.getUser().getId().equals(currentUserId)
                        && !NoteCreationSource.SYSTEM.equals(note.getCreationSource()))) {
            permissions.add(NotePermissionManagement.PERMISSION_EDIT);
        }
    }

    /**
     * Method to set the MOVE permission.
     * 
     * @param note
     *            The note.
     * @param currentUserId
     *            the ID of the current user, can be null
     * @param blogRole
     *            The users blog role.
     * @param permissions
     *            The permissions.
     */
    private void handleMove(Note note, Long currentUserId, BlogRole blogRole,
            Set<Permission<Note>> permissions) {
        boolean isTopicManager = BlogRole.MANAGER.equals(blogRole);
        boolean isAuthorAndWriter = note.getUser().getId().equals(currentUserId)
                && BlogRole.MEMBER.equals(blogRole);
        if (note.getParent() == null && (isTopicManager || isAuthorAndWriter)) {
            permissions.add(NotePermissionManagement.PERMISSION_MOVE);
        }
    }

    /**
     * Method to add the read permission if the use is allowed to read the note.
     * 
     * @param note
     *            The note to add the permission for.
     * @param currentUserId
     *            the ID of the current user, can be null
     * @param blogRole
     *            The current blog role, must not be null.
     * @param permissions
     *            The list of permissions to add the permissions to.
     * @return True, if the user is allowed to read the note.
     */
    private boolean handleRead(Note note, Long currentUserId, BlogRole blogRole,
            Set<Permission<Note>> permissions) {
        if (!SecurityHelper.isInternalSystem() && note.isDirect()) {
            if (currentUserId == null) {
                return false;
            }
            if (!note.getUser().getId().equals(currentUserId)) {
                boolean directMessageReceiver = false;
                for (User notifiedUser : note.getUsersToBeNotified()) {
                    if (notifiedUser.getId().equals(currentUserId)) {
                        directMessageReceiver = true;
                        break;
                    }
                }
                if (!directMessageReceiver) {
                    return false;
                }
            }
        }
        permissions.add(NotePermissionManagement.PERMISSION_READ);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(Note note, Set<Permission<Note>> permissions) {
        BlogRole blogRole = blogRightsManagement
                .getRoleOfCurrentUser(note.getBlog().getId(), false);
        // note: doing null check of role here and in canRead for a minor performance optimization:
        // only get currentUserId if necessary
        if (blogRole != null) {
            Long currentUserId = SecurityHelper.getCurrentUserId();
            if (handleRead(note, currentUserId, blogRole, permissions)) {
                handleEdit(note, currentUserId, blogRole, permissions);
                handleDelete(note, blogRole, permissions);
                handleComment(note, blogRole, permissions);
                handleMove(note, currentUserId, blogRole, permissions);
                // if there is a current user he can like, favor or repost a note
                if (currentUserId != null) {
                    permissions.add(NotePermissionManagement.PERMISSION_LIKE);
                    permissions.add(NotePermissionManagement.PERMISSION_FAVOR);
                    permissions.add(NotePermissionManagement.PERMISSION_REPOST);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filterForCreation(NoteStoringTO entity, Set<Permission<Note>> permissions) {
        // nothing to do
    }

    /**
     * @return Integer.MAX_VALUE
     */
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
