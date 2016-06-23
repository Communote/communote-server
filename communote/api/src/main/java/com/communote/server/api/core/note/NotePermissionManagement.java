package com.communote.server.api.core.note;

import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.api.core.security.permission.PermissionManagement;
import com.communote.server.model.note.Note;

/**
 * Permission management for notes.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface NotePermissionManagement extends
PermissionManagement<Note, NoteStoringTO, NoteManagementAuthorizationException> {
    /** If set, the user is allowed to edit the note. */
    public final static Permission<Note> PERMISSION_EDIT = new Permission<Note>("EDIT");

    /** If set, the user is allowed to delete the note. */
    public final static Permission<Note> PERMISSION_DELETE = new Permission<Note>("DELETE");

    /** If set, the user is allowed to comment on the note. */
    public final static Permission<Note> PERMISSION_COMMENT = new Permission<Note>("COMMENT");

    /** If set, the user is allowed to like the note. */
    public final static Permission<Note> PERMISSION_LIKE = new Permission<Note>("LIKE");

    /** If set, the user is allowed to favor the note. */
    public final static Permission<Note> PERMISSION_FAVOR = new Permission<Note>("FAVOR");

    /** If set, the user is allowed to move the note (whole discussion). */
    public final static Permission<Note> PERMISSION_MOVE = new Permission<Note>("MOVE");

    /** If set, the user is allowed to read the note. */
    public final static Permission<Note> PERMISSION_READ = new Permission<Note>("READ");
    /** If set, the user is allowed to repost the note. */
    public final static Permission<Note> PERMISSION_REPOST = new Permission<Note>("REPOST");

}
