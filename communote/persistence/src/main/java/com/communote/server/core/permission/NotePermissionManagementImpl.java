package com.communote.server.core.permission;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.security.permission.BasePermissionManagement;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.api.core.security.permission.PermissionManagement;
import com.communote.server.core.permission.filters.NoteRolePermissionFilter;
import com.communote.server.core.template.NoteTemplatePermissionFilter;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.note.Note;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.service.NoteService;

/**
 * {@link PermissionManagement} for {@link Note}s.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("notePermissionManagement")
@Transactional(readOnly = true)
public class NotePermissionManagementImpl extends
        BasePermissionManagement<Note, NoteStoringTO, NoteManagementAuthorizationException>
        implements NotePermissionManagement {

    @Autowired
    private NoteDao noteDao;
    @Autowired
    private NoteService noteService;
    @Autowired
    private BlogRightsManagement blogRightsManagement;
    @Autowired
    private UserManagement userManagement;

    @Override
    protected NoteManagementAuthorizationException createPermissonViolationException(Note entity,
            Permission<Note> permission) {
        return new NoteManagementAuthorizationException(
                "The current user has not the required permission " + permission.getIdentifier(),
                entity.getBlog().getTitle());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Note getEntity(Long noteId) {
        return noteDao.load(noteId);
    }

    /**
     * Initializer.
     */
    @PostConstruct
    public void init() {
        addPermissionFilter(new NoteRolePermissionFilter(noteService, blogRightsManagement,
                userManagement));
        addPermissionFilter(new NoteTemplatePermissionFilter());
    }
}
