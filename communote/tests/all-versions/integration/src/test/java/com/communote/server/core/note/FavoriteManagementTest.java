package com.communote.server.core.note;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FavoriteManagementTest extends CommunoteIntegrationTest {

    private FavoriteManagement favoriteManagement;

    @Test
    public void afterTest() {
        AuthenticationHelper.removeAuthentication();
    }

    @Override
    @BeforeTest
    public void setupRemoveAuthentication() {
        favoriteManagement = ServiceLocator.findService(FavoriteManagement.class);
    }

    /**
     * Tests {@link FavoriteManagement#isFavorite(Long)}
     *
     * @throws NoteNotFoundException
     *             Exception.
     */
    @Test
    public void testIsFavorite() throws NoteNotFoundException {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(false, false, user);
        for (int i = 0; i <= 600; i++) {
            Long noteId = TestUtils.createAndStoreCommonNote(blog, user.getId(), "Favorite");
            Assert.assertFalse(favoriteManagement.isFavorite(noteId));
            favoriteManagement.markNoteAsFavorite(noteId);
            Assert.assertTrue(favoriteManagement.isFavorite(noteId));
            favoriteManagement.unmarkNoteAsFavorite(noteId);
            Assert.assertFalse(favoriteManagement.isFavorite(noteId));
        }
    }

    @Test
    public void testNumberFavorites() throws NoteNotFoundException, AuthorizationException {

        List<User> users = new ArrayList<User>();

        for (int i = 0; i < 50; i++) {
            users.add(TestUtils.createRandomUser(false));
        }

        Blog blog = TestUtils.createRandomBlog(true, true, users.get(0));
        AuthenticationHelper.removeAuthentication();

        for (int i = 0; i <= 10; i++) {

            AuthenticationHelper.setAsAuthenticatedUser(users.get(i));
            Long noteId = TestUtils.createAndStoreCommonNote(blog, users.get(i).getId(),
                    "Favorite Test");
            AuthenticationHelper.removeAuthentication();

            int faved = 0;
            for (int u = 0; u < 50; u++) {
                if (u % (i + 1) == 0) {
                    AuthenticationHelper.setAsAuthenticatedUser(users.get(u));
                    Assert.assertFalse(favoriteManagement.isFavorite(noteId));
                    favoriteManagement.markNoteAsFavorite(noteId);
                    Assert.assertTrue(favoriteManagement.isFavorite(noteId));
                    faved++;
                    AuthenticationHelper.removeAuthentication();
                }
            }

            AuthenticationHelper.setInternalSystemToSecurityContext();
            int actualFaved = favoriteManagement.getNumberOfFavorites(noteId);
            Assert.assertEquals("Run: " + i, faved, actualFaved);
            AuthenticationHelper.removeAuthentication();
        }

    }
}
