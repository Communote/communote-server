package com.communote.server.core.util;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.blog.helper.NoteHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.blog.BlogDao;
import com.communote.server.persistence.blog.NoteDao;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
// TODO Hacky helper to get InitialFilters stuff out of BlogManagement. Should use Converter
// instead.
@Service
public class InitialFilterDataProvider {
    @Autowired
    private BlogDao blogDao;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private NotePermissionManagement notePermissionManagement;
    @Autowired
    private UserManagement userManagement;
    @Autowired
    private BlogRightsManagement blogRightsManagement;

    private void fillInitalFilters(InitalFiltersVO initalFilters, Blog blog, BlogRole blogRole,
            User user) {
        if (blog != null) {
            initalFilters.setBlogAlias(blog.getNameIdentifier());
            initalFilters.setBlogTitle(blog.getTitle());
            initalFilters.setBlogId(blog.getId());
            initalFilters.setBlogRole(blogRole.getValue());
        }
        if (user != null) {
            initalFilters.setUserAlias(user.getAlias());
            initalFilters.setUserId(user.getId());
            initalFilters.setUserShortName(UserNameHelper.getDefaultUserSignature(user));
            initalFilters.setUserLongName(UserNameHelper.getDetailedUserSignature(user));

        }
    }

    /**
     * Fill the inital filter based on the available data (that is use the blog id, blog alias to
     * load the blog and user id, user alias to load the user)
     *
     * @param initalFilters
     *            Initials filters to fill.
     * @param locale
     *            the locale to use create internationalized messages
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public InitalFiltersVO fillInitalFilters(InitalFiltersVO initalFilters, Locale locale)
            throws NotFoundException, AuthorizationException {
        Blog blog = null;
        BlogRole role = null;
        Note note = null;

        if (initalFilters.getNoteId() != null) {
            note = noteDao.load(initalFilters.getNoteId());
            if (note == null) {
                throw new NoteNotFoundException("Note not found! id=" + initalFilters.getNoteId());
            }
            if (!notePermissionManagement.hasPermission(initalFilters.getNoteId(),
                    NotePermissionManagement.PERMISSION_READ)) {
                throw new AuthorizationException("Current user is not allowed to view this note");
            }
            String noteTitle = NoteHelper.getNoteTitle(note, locale);
            initalFilters.setNoteTitle(noteTitle);
            blog = note.getBlog();
        } else if (initalFilters.getBlogAlias() != null) {
            blog = blogDao.findByNameIdentifier(initalFilters.getBlogAlias());
            if (blog == null) {
                throw new BlogNotFoundException("Blog not found! alias="
                        + initalFilters.getBlogAlias(), null, initalFilters.getBlogAlias());
            }
        } else if (initalFilters.getBlogId() != null) {
            blog = blogDao.load(initalFilters.getBlogId());
            if (blog == null) {
                throw new BlogNotFoundException("Blog not found! id=" + initalFilters.getBlogId(),
                        initalFilters.getBlogId(), null);
            }
        }
        if (blog != null) {
            role = blogRightsManagement.getRoleOfUser(blog.getId(),
                    SecurityHelper.getCurrentUserId(), false);
            if (role == null) {
                throw new AuthorizationException("Current user has no read access to the topic.");
            }
        }
        User user = findInitalFilterUser(initalFilters);
        fillInitalFilters(initalFilters, blog, role, user);

        return initalFilters;

    }

    /**
     * Find the user to the inital filters
     *
     * @param initialFilters
     *            the prefilled initial filters
     * @return the user or null if not set
     * @throws UserNotFoundException
     *             in case user not found
     */
    private User findInitalFilterUser(InitalFiltersVO initialFilters) throws UserNotFoundException {
        User user = null;
        if (initialFilters.getUserAlias() != null) {
            user = userManagement.findUserByAlias(initialFilters.getUserAlias());
            if (user == null || UserStatus.DELETED.equals(user.getStatus())) {
                throw new UserNotFoundException("User not found! alias="
                        + initialFilters.getUserAlias());
            }
        } else if (initialFilters.getUserId() != null) {
            user = userManagement.findUserByUserId(initialFilters.getUserId(), false);
            if (user == null) {
                throw new UserNotFoundException("User not found! alias="
                        + initialFilters.getUserAlias());
            }
        }
        return user;
    }

}
