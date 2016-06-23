package com.communote.server.service;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.core.user.note.UserNoteEntityService;
import com.communote.server.core.vo.user.note.UserNoteEntityTO;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;


/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserNoteEntityServiceTest extends CommunoteIntegrationTest {

    private UserNoteEntityService userNoteEntityService;
    private User user1;
    private User user2;
    private Long note1;
    private Long note2;
    private Blog blog1;

    /**
     * Do some setup
     */
    @BeforeTest
    public void beforeSetup() {
        userNoteEntityService = ServiceLocator.instance().getService(UserNoteEntityService.class);
        user1 = TestUtils.createRandomUser(false);
        user2 = TestUtils.createRandomUser(false);

        blog1 = TestUtils.createRandomBlog(true, true, user1);

        note1 = TestUtils.createAndStoreCommonNote(blog1, user1.getId(), UUID
                .randomUUID().toString());
        note2 = TestUtils.createAndStoreCommonNote(blog1, user1.getId(), UUID
                .randomUUID().toString());
    }

    /**
     * Test the create and get of a rank.
     * 
     * @throws NotFoundException
     *             in case of an error
     */
    @Test
    public void testCreateGet() throws NotFoundException {
        UserNoteEntityTO to;

        // create a user note entity and get it
        to = new UserNoteEntityTO(user1.getId(), note1);
        to.setUpdateRank(true);
        to.setNormalizedRank(0.12345);

        // create
        userNoteEntityService.updateUserNoteEntity(to);

        // get
        to = userNoteEntityService.getUserNoteEntity(user1.getId(), note1);

        // check
        Assert.assertNotNull(to);
        Assert.assertEquals(to.getNormalizedRank(), 0.12345);

        // update the entity with a new rank
        to.setUpdateRank(true);
        to.setNormalizedRank(0.76543);
        userNoteEntityService.updateUserNoteEntity(to);
        to = userNoteEntityService.getUserNoteEntity(user1.getId(), note1);

        // check the new rank
        Assert.assertNotNull(to);
        Assert.assertEquals(to.getNormalizedRank(), 0.76543);

        // create another entity
        to = new UserNoteEntityTO(user2.getId(), note2);
        to.setUpdateRank(true);
        to.setNormalizedRank(0.2);

        // create
        userNoteEntityService.updateUserNoteEntity(to);

        // get
        to = userNoteEntityService.getUserNoteEntity(user2.getId(), note2);

        // check
        Assert.assertNotNull(to);
        Assert.assertEquals(to.getNormalizedRank(), 0.2);

        // get it for an non existing entity
        to = userNoteEntityService.getUserNoteEntity(user1.getId(), note2);
        Assert.assertNull(to);

    }

    /**
     * Test a too high rank
     * 
     * @throws NotFoundException
     *             in case of an error
     */
    @Test
    public void testTooHighRank() throws NotFoundException {
        UserNoteEntityTO to;

        // create a user note entity and get it
        to = new UserNoteEntityTO(user2.getId(), note1);
        to.setUpdateRank(true);
        to.setNormalizedRank(1.1);

        // create
        userNoteEntityService.updateUserNoteEntity(to);

        to = userNoteEntityService.getUserNoteEntity(user2.getId(), note1);

        // check
        Assert.assertNotNull(to);
        // it should be 1 because the highest possible rank is 1
        Assert.assertEquals(to.getNormalizedRank(), 1.0);

        to.setUpdateRank(true);
        to.setNormalizedRank(-0.123);

        // create
        userNoteEntityService.updateUserNoteEntity(to);
        to = userNoteEntityService.getUserNoteEntity(user2.getId(), note1);

        // check
        Assert.assertNotNull(to);
        // it should be 0 because the lowest possible rank is 0
        Assert.assertEquals(to.getNormalizedRank(), 0.0);

    }
}
