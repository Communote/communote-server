package com.communote.server.persistence.blog;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.model.blog.ResolvedTopicToTopic;
import com.communote.server.test.CommunoteIntegrationTest;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResolvedTopicToTopicDaoTest extends CommunoteIntegrationTest {

    @Autowired
    private ResolvedTopicToTopicDao resolvedTopicToTopicDao;

    /**
     * This tests the standard CRUD functionality of the DAO.
     */
    @Test(priority = 0)
    public void testSimpleCRUD() {
        resolvedTopicToTopicDao.remove(resolvedTopicToTopicDao.loadAll());

        // Check create and read/find
        ResolvedTopicToTopic entity = ResolvedTopicToTopic.Factory.newInstance();
        entity.setChildTopicId(1L);
        entity.setParentTopicId(1L);
        entity.setTopicPath("/1/1/");

        resolvedTopicToTopicDao.create(entity);
        entity = resolvedTopicToTopicDao.load(entity.getId());
        Assert.assertTrue(entity.getChildTopicId().equals(1L));
        Assert.assertTrue(entity.getParentTopicId().equals(1L));
        Assert.assertEquals(entity.getTopicPath(), "/1/1/");

        // Check update
        entity.setChildTopicId(2L);
        entity.setParentTopicId(2L);
        entity.setTopicPath("/2/2/");
        resolvedTopicToTopicDao.update(entity);

        entity = resolvedTopicToTopicDao.load(entity.getId());
        Assert.assertTrue(entity.getChildTopicId().equals(2L));
        Assert.assertTrue(entity.getParentTopicId().equals(2L));
        Assert.assertEquals(entity.getTopicPath(), "/2/2/");

        // Provide unique constraint exception.
        ResolvedTopicToTopic duplicate = ResolvedTopicToTopic.Factory.newInstance();
        duplicate.setChildTopicId(2L);
        duplicate.setParentTopicId(2L);
        duplicate.setTopicPath("/2/2/");
        try {
            resolvedTopicToTopicDao.create(duplicate);
            Assert.fail("It should not be possible to insert the same value twice.");
        } catch (Exception e) {
            if (!(e instanceof ConstraintViolationException || e.getCause() instanceof ConstraintViolationException)) {
                Assert.fail("ConstraintViolationException expected.");
            }
        }

        // Delete entity
        resolvedTopicToTopicDao.remove(entity);
        Assert.assertNull(resolvedTopicToTopicDao.load(entity.getId()));
    }

    /**
     * Test for #isChild()
     */
    @Test(priority = 1)
    public void testIsChild() {
        resolvedTopicToTopicDao.remove(resolvedTopicToTopicDao.loadAll());
        resolvedTopicToTopicDao.create(ResolvedTopicToTopic.Factory.newInstance(1L, 2L, "/1/2/"));
        resolvedTopicToTopicDao.create(ResolvedTopicToTopic.Factory.newInstance(2L, 3L, "/2/3/"));
        resolvedTopicToTopicDao.create(ResolvedTopicToTopic.Factory.newInstance(1L, 3L, "/1/2/3/"));

        Assert.assertTrue(resolvedTopicToTopicDao.isChild(1L, 2L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(2L, 3L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(1L, 3L));

        Assert.assertFalse(resolvedTopicToTopicDao.isChild(2L, 1L));
        Assert.assertFalse(resolvedTopicToTopicDao.isChild(3L, 2L));
        Assert.assertFalse(resolvedTopicToTopicDao.isChild(3L, 1L));

        Assert.assertFalse(resolvedTopicToTopicDao.isChild(1L, 4L));
        Assert.assertFalse(resolvedTopicToTopicDao.isChild(4L, 5L));
    }

    /**
     * Test for #connect()
     * 
     * The graph is as the following: 1-> 2 -> 3 -> 5 and 2 -> 4 -> 5
     */
    @Test(priority = 2)
    public void testConnect() {
        resolvedTopicToTopicDao.remove(resolvedTopicToTopicDao.loadAll());
        Assert.assertEquals(resolvedTopicToTopicDao.connect(1L, 2L), 1);
        Assert.assertEquals(resolvedTopicToTopicDao.connect(1L, 2L), 0); // Already connected
        Assert.assertEquals(resolvedTopicToTopicDao.connect(2L, 3L), 2);
        Assert.assertEquals(resolvedTopicToTopicDao.connect(2L, 3L), 0); // Already connected
        Assert.assertEquals(resolvedTopicToTopicDao.connect(2L, 4L), 2);
        Assert.assertEquals(resolvedTopicToTopicDao.connect(3L, 5L), 3);
        Assert.assertEquals(resolvedTopicToTopicDao.connect(4L, 5L), 3);
        Assert.assertEquals(resolvedTopicToTopicDao.loadAll().size(), 11);

        try {
            resolvedTopicToTopicDao.connect(5L, 1L);
            Assert.fail("A reverse connection should not be possible.");
        } catch (ParentIsAlreadyChildDataIntegrityViolationException e) {
            // Okay.
        }
    }

    /**
     * Test for #remove().
     * 
     * We expect the graph from #testConnect here.
     */
    @Test(dependsOnMethods = "testConnect")
    public void testRemove() {
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(4L, 5L));

        // Removes connection from 1 -> 5 (over 4) , 2 -> 5 (over 4) and 4-> 5
        Assert.assertEquals(resolvedTopicToTopicDao.disconnect(4L, 5L), 3);

        Assert.assertFalse(resolvedTopicToTopicDao.isChild(4L, 5L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(1L, 3L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(1L, 4L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(1L, 5L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(2L, 5L));

        // Removes connection from 1 -> 5, 1 -> 3, 2 -> 3, 2 -> 5
        Assert.assertEquals(resolvedTopicToTopicDao.disconnect(2L, 3L), 4);

        // Only 1 -> 2 , 1 -> 4 and 2 -> 4, 3 -> 5 are left
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(1L, 2L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(1L, 4L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(2L, 4L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(3L, 5L));
        Assert.assertEquals(resolvedTopicToTopicDao.loadAll().size(), 4);
    }

    /**
     * Test to reconnect 2 -> 3
     * 
     * We expect the graph from #testConnect here.
     */
    @Test(dependsOnMethods = "testRemove")
    public void testReconnect() {
        // 1 -> 3, 1 -> 5, 2 -> 3, 2 -> 5, 3 -> 5 (is reconnected)
        Assert.assertEquals(resolvedTopicToTopicDao.connect(2L, 3L), 5);

        Assert.assertTrue(resolvedTopicToTopicDao.isChild(1L, 5L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(1L, 3L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(2L, 3L));
        Assert.assertTrue(resolvedTopicToTopicDao.isChild(2L, 5L));
    }

    /**
     * This method tests the disconnection of a whole topic.
     */
    @Test(dependsOnMethods = "testReconnect")
    public void testDisconnect() {
        Assert.assertEquals(resolvedTopicToTopicDao.disconnect(2L), 7);

        Assert.assertFalse(resolvedTopicToTopicDao.isChild(1L, 2L));
        Assert.assertFalse(resolvedTopicToTopicDao.isChild(1L, 3L));
        Assert.assertFalse(resolvedTopicToTopicDao.isChild(1L, 4L));
        Assert.assertFalse(resolvedTopicToTopicDao.isChild(1L, 5L));
        Assert.assertFalse(resolvedTopicToTopicDao.isChild(2L, 3L));
        Assert.assertFalse(resolvedTopicToTopicDao.isChild(2L, 4L));
        Assert.assertFalse(resolvedTopicToTopicDao.isChild(2L, 5L));
    }
}
