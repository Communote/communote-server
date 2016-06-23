package com.communote.server.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.user.NavigationItemDataTO;
import com.communote.server.core.vo.user.NavigationItemTO;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NavigationItemServiceTest extends CommunoteIntegrationTest {

    @Autowired
    private NavigationItemService navigationItemService;

    /**
     * Assert that the provided collection contains the built-in NavigationItems at top.
     * 
     * @param navigationItems
     *            the items to check for the built-in items
     * @param expectedSize
     *            the expected size of the collection
     */
    private void assertCorrectBuiltInItems(
            List<NavigationItemTO<NavigationItemDataTO>> navigationItems, int expectedSize) {
        Assert.assertEquals(navigationItems.size(), expectedSize);
        NavigationItemTO<NavigationItemDataTO> immutableItem1 = navigationItems.get(0);
        NavigationItemTO<NavigationItemDataTO> immutableItem2 = navigationItems.get(1);
        Assert.assertTrue(immutableItem1.getIndex() < 0);
        Assert.assertTrue(immutableItem2.getIndex() < 0);
        Assert.assertNotEquals(immutableItem1.getName(), immutableItem2.getName());
        Assert.assertTrue(immutableItem1.getName().equals("mentions")
                || immutableItem1.getName().equals("following"));
        Assert.assertTrue(immutableItem2.getName().equals("mentions")
                || immutableItem2.getName().equals("following"));
    }

    /**
     * Assert that the navigation item has the expected values
     * 
     * @param actual
     *            the item to check
     * @param expectedId
     *            the ID the item must have
     * @param expectedName
     *            the name the item must have
     * @param expectedLastAccessDate
     *            the last access data the item must have. Can be null to just test that the last
     *            access date of the actual item is not null
     * @param expectedDataTO
     *            the data the item must have
     */
    private void assertCorrectNavigationItem(NavigationItemTO<NavigationItemDataTO> actual,
            Long expectedId, String expectedName, Date expectedLastAccessDate,
            NavigationItemDataTO expectedDataTO) {
        Assert.assertEquals(actual.getId(), expectedId);
        Assert.assertEquals(actual.getName(), expectedName);
        if (expectedLastAccessDate == null) {
            Assert.assertNotNull(actual.getLastAccessDate());
        } else {
            Assert.assertEquals(actual.getLastAccessDate(), expectedLastAccessDate);
        }
        Assert.assertEquals(actual.getData().getContextId(), expectedDataTO.getContextId());
        Assert.assertEquals(actual.getData().getContextType(), expectedDataTO.getContextType());
    }

    /**
     * Create data TO for tests
     * 
     * @return the TO
     */
    private NavigationItemDataTO createDataTO() {
        NavigationItemDataTO dataTO = new NavigationItemDataTO();
        dataTO.setContextId(random());
        dataTO.setContextType(random());
        return dataTO;
    }

    /**
     * Test that the built-in immutable NavigationItems are handled correctly
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testBuiltInItems() throws Exception {
        User user = TestUtils.createRandomUser(false);

        AuthenticationTestUtils.setSecurityContext(user);
        // check that the built-in items, which have negative index, were created correctly
        List<NavigationItemTO<NavigationItemDataTO>> navigationItems = navigationItemService.find();
        assertCorrectBuiltInItems(navigationItems, 2);
        NavigationItemTO<NavigationItemDataTO> immutableItem1 = navigationItems.get(0);
        NavigationItemTO<NavigationItemDataTO> immutableItem2 = navigationItems.get(1);

        // try creating again for current user, items shouldn't change
        navigationItemService.createBuiltInNavigationItems(null);
        navigationItems = navigationItemService.find();
        Assert.assertEquals(navigationItems.size(), 2);
        assertCorrectNavigationItem(navigationItems.get(0), immutableItem1.getId(),
                immutableItem1.getName(), immutableItem1.getLastAccessDate(),
                immutableItem1.getData());
        assertCorrectNavigationItem(navigationItems.get(1), immutableItem2.getId(),
                immutableItem2.getName(), immutableItem2.getLastAccessDate(),
                immutableItem2.getData());
        // try updating name, item must not change
        Thread.sleep(1000);
        navigationItemService.update(immutableItem1.getId(), "something new", null, null,
                null);
        navigationItems = navigationItemService.find();
        Assert.assertEquals(navigationItems.size(), 2);
        assertCorrectNavigationItem(navigationItems.get(0), immutableItem1.getId(),
                immutableItem1.getName(), immutableItem1.getLastAccessDate(),
                immutableItem1.getData());
        // try updating index, order and index must not change
        navigationItemService.update(immutableItem1.getId(), null, 3, null,
                null);
        navigationItems = navigationItemService.find();
        Assert.assertEquals(navigationItems.size(), 2);
        assertCorrectNavigationItem(navigationItems.get(0), immutableItem1.getId(),
                immutableItem1.getName(), immutableItem1.getLastAccessDate(),
                immutableItem1.getData());
        assertCorrectNavigationItem(navigationItems.get(1), immutableItem2.getId(),
                immutableItem2.getName(), immutableItem2.getLastAccessDate(),
                immutableItem2.getData());
        // try updating data, item must not change
        navigationItemService.update(immutableItem2.getId(), null, null, null,
                createDataTO());
        navigationItems = navigationItemService.find();
        assertCorrectNavigationItem(navigationItems.get(1), immutableItem2.getId(),
                immutableItem2.getName(), immutableItem2.getLastAccessDate(),
                immutableItem2.getData());
        // update last access date
        Date newLastAccessDate = new Date(immutableItem2.getLastAccessDate().getTime() + 1000);
        navigationItemService.update(immutableItem2.getId(), null, null, newLastAccessDate, null);
        navigationItems = navigationItemService.find();
        assertCorrectNavigationItem(navigationItems.get(1), immutableItem2.getId(),
                immutableItem2.getName(), newLastAccessDate, immutableItem2.getData());
        // try deleting, item must not be deleted
        navigationItemService.delete(immutableItem2.getId());
        navigationItems = navigationItemService.find();
        assertCorrectNavigationItem(navigationItems.get(0), immutableItem1.getId(),
                immutableItem1.getName(), immutableItem1.getLastAccessDate(),
                immutableItem1.getData());
        assertCorrectNavigationItem(navigationItems.get(1), immutableItem2.getId(),
                immutableItem2.getName(), newLastAccessDate, immutableItem2.getData());
    }

    /**
     * Test that the built-in NavigationItems are created on activating a new user
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testBuiltInItemsCreatedOnActivation() throws Exception {
        User user = TestUtils.createRandomUser(true, null, UserRole.ROLE_KENMEI_USER);
        AuthenticationTestUtils.setSecurityContext(user);
        // check that the built-in items were not created
        List<NavigationItemTO<NavigationItemDataTO>> navigationItems = navigationItemService.find();
        Assert.assertEquals(navigationItems.size(), 0);
        User admin = TestUtils.createRandomUser(true);
        AuthenticationTestUtils.setSecurityContext(admin);
        ServiceLocator.findService(UserManagement.class).changeUserStatusByManager(user.getId(),
                UserStatus.ACTIVE);
        AuthenticationTestUtils.setSecurityContext(user);
        // check that the built-in items were not created
        navigationItems = navigationItemService.find();
        assertCorrectBuiltInItems(navigationItems, 2);
    }

    /**
     * Test creating, retrieving and deleting NavigationItems
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testCreateGetAndDelete() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);

        AuthenticationTestUtils.setSecurityContext(user1);
        // create two new items
        String user1navigationItem1Name = random();
        NavigationItemDataTO user1navigationItem1DataTO = createDataTO();
        Long user1navigationItem1Id = navigationItemService.store(user1navigationItem1Name, null,
                null, user1navigationItem1DataTO);
        String user1navigationItem2Name = random();
        NavigationItemDataTO user1navigationItem2DataTO = createDataTO();
        Date user1navigationItem2LastAccessDate = new Date();
        // full second for stupid mysql and mssql
        user1navigationItem2LastAccessDate = DateUtils.truncate(user1navigationItem2LastAccessDate,
                Calendar.SECOND);
        Long user1navigationItem2Id = navigationItemService.store(user1navigationItem2Name, 0,
                user1navigationItem2LastAccessDate, user1navigationItem2DataTO);

        // Just make sure we never get the items of user 2
        AuthenticationTestUtils.setSecurityContext(user2);
        Long user2navigationItem1Id = navigationItemService
                .store(random(), 0, null, createDataTO());
        Long user2navigationItem2Id = navigationItemService
                .store(random(), 1, null, createDataTO());

        // Get All
        AuthenticationTestUtils.setSecurityContext(user1);
        List<NavigationItemTO<NavigationItemDataTO>> navigationItems = navigationItemService.find();
        // size must be 4 because of the built-in items
        Assert.assertEquals(navigationItems.size(), 4);
        // second item was added last with index 0 and thus must be first mutable (index > 0)
        assertCorrectNavigationItem(navigationItems.get(2), user1navigationItem2Id,
                user1navigationItem2Name, user1navigationItem2LastAccessDate,
                user1navigationItem2DataTO);
        assertCorrectNavigationItem(navigationItems.get(3), user1navigationItem1Id,
                user1navigationItem1Name, null, user1navigationItem1DataTO);

        // fetch by providing the IDs, result should still be sorted by index
        navigationItems = navigationItemService
                .find(user1navigationItem1Id, user1navigationItem2Id);
        Assert.assertEquals(navigationItems.size(), 2);
        Assert.assertEquals(navigationItems.get(0).getId(), user1navigationItem2Id);
        Assert.assertEquals(navigationItems.get(1).getId(), user1navigationItem1Id);

        // fetch with non existing ID for that user
        navigationItems = navigationItemService
                .find(user1navigationItem1Id, user1navigationItem2Id, user1navigationItem1Id
                        + user1navigationItem2Id);
        Assert.assertEquals(navigationItems.size(), 2);
        Assert.assertEquals(navigationItems.get(0).getId(), user1navigationItem2Id);
        Assert.assertEquals(navigationItems.get(1).getId(), user1navigationItem1Id);

        AuthenticationTestUtils.setSecurityContext(user2);
        navigationItems = navigationItemService.find();
        Assert.assertEquals(navigationItems.size(), 4);
        Assert.assertEquals(navigationItems.get(2).getId(), user2navigationItem1Id);
        Assert.assertEquals(navigationItems.get(3).getId(), user2navigationItem2Id);

        // test removal
        AuthenticationTestUtils.setSecurityContext(user1);
        navigationItemService.delete(user1navigationItem1Id);
        Assert.assertNull(navigationItemService.get(user1navigationItem1Id));
        navigationItems = navigationItemService.find();
        Assert.assertEquals(navigationItems.size(), 3);
        assertCorrectNavigationItem(navigationItems.get(2), user1navigationItem2Id,
                user1navigationItem2Name, user1navigationItem2LastAccessDate,
                user1navigationItem2DataTO);
        navigationItems = navigationItemService
                .find(user1navigationItem1Id, user1navigationItem2Id);
        Assert.assertEquals(navigationItems.size(), 1);
        navigationItemService.delete(user1navigationItem2Id);
        Assert.assertNull(navigationItemService.get(user1navigationItem2Id));
        navigationItems = navigationItemService.find();
        Assert.assertEquals(navigationItems.size(), 2);

        AuthenticationTestUtils.setSecurityContext(user2);
        navigationItems = navigationItemService.find();
        Assert.assertEquals(navigationItems.size(), 4);
        navigationItemService.delete(user2navigationItem1Id);
        navigationItemService.delete(user2navigationItem2Id);
        Assert.assertNull(navigationItemService.get(user2navigationItem1Id));
        Assert.assertNull(navigationItemService.get(user2navigationItem2Id));
    }

    /**
     * Test that updating non existing items leads to a NotFoundException
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testNotFound() throws Exception {
        User user = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user);
        List<NavigationItemTO<NavigationItemDataTO>> navigationItems = navigationItemService.find();
        try {
            Long itemId = Math.max(navigationItems.get(0).getId(), navigationItems.get(1).getId()) + 1;
            navigationItemService.update(itemId, "the new name to set", null, null, null);
            Assert.fail("Updating non existing item should lead to an exception");
        } catch (NotFoundException e) {
            // expected
        }
    }

    /**
     * Test that actions not allowed for the current user result in the proper exception
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testUnauthorized() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);

        AuthenticationTestUtils.setSecurityContext(user1);
        String itemName = random();
        Long itemId = navigationItemService.store(itemName, null, null, createDataTO());
        List<NavigationItemTO<NavigationItemDataTO>> navigationItems = navigationItemService.find();
        AuthenticationTestUtils.setSecurityContext(user2);
        // test that service replies with not found as the item does not exist for the current user
        try {
            navigationItemService.update(itemId, itemName + "_2", null, null, null);
            Assert.fail("User2 must not be able to modify items of user 1");
        } catch (NotFoundException e) {
            // expected
        }
        // try updating a immutable item of another user
        try {
            navigationItemService.update(navigationItems.get(0).getId(), null, null,
                    new Date(System.currentTimeMillis() + 1000), null);
            Assert.fail("User2 must not be able to modify items of user 1");
        } catch (NotFoundException e) {
            // expected
        }
        // try creating immutable items for user1
        try {
            navigationItemService.createBuiltInNavigationItems(user1.getId());
            Assert.fail("User2 must not be able to create immutable items of user 1");
        } catch (AuthorizationException e) {
            // expected
        }
    }

    /**
     * Test updating NavigationItems
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testUpdate() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user1);
        // create two new items
        String user1navigationItem1Name = random();
        NavigationItemDataTO user1navigationItem1DataTO = createDataTO();
        Long user1navigationItem1Id = navigationItemService.store(user1navigationItem1Name, null,
                null, user1navigationItem1DataTO);
        String user1navigationItem2Name = random();
        NavigationItemDataTO user1navigationItem2DataTO = createDataTO();
        Long user1navigationItem2Id = navigationItemService.store(user1navigationItem2Name, 0,
                null, user1navigationItem2DataTO);
        List<NavigationItemTO<NavigationItemDataTO>> navigationItems = navigationItemService.find();
        // size must be 4 because of the built-in items
        Assert.assertEquals(navigationItems.size(), 4);
        // second item was added last with index 0 and thus must be first mutable (index > 0)
        Assert.assertEquals(navigationItems.get(2).getId(), user1navigationItem2Id);
        Assert.assertEquals(navigationItems.get(3).getId(), user1navigationItem1Id);
        // save last access date for further checks
        Date user1navigationItem2LastAccessDate = navigationItems.get(2).getLastAccessDate();
        Date user1navigationItem1LastAccessDate = navigationItems.get(3).getLastAccessDate();
        // Update index, assert that other values haven't changed. Add sleep to ensure measurable
        // interval between operations, so we can be sure that access date isn't set to current
        // millis if no access data is given.
        AuthenticationTestUtils.setSecurityContext(user1);
        Thread.sleep(1000);
        navigationItemService.update(user1navigationItem1Id, null, 0, null, null);
        navigationItems = navigationItemService.find();
        Assert.assertEquals(navigationItems.size(), 4);
        assertCorrectNavigationItem(navigationItems.get(2), user1navigationItem1Id,
                user1navigationItem1Name, user1navigationItem1LastAccessDate,
                user1navigationItem1DataTO);
        assertCorrectNavigationItem(navigationItems.get(3), user1navigationItem2Id,
                user1navigationItem2Name, user1navigationItem2LastAccessDate,
                user1navigationItem2DataTO);
        // Update name, assert that other values haven't changed
        user1navigationItem1Name = user1navigationItem1Name + "2";
        navigationItemService.update(user1navigationItem1Id, user1navigationItem1Name, null, null,
                null);
        navigationItems = navigationItemService.find();
        assertCorrectNavigationItem(navigationItems.get(2), user1navigationItem1Id,
                user1navigationItem1Name, user1navigationItem1LastAccessDate,
                user1navigationItem1DataTO);
        assertCorrectNavigationItem(navigationItems.get(3), user1navigationItem2Id,
                user1navigationItem2Name, user1navigationItem2LastAccessDate,
                user1navigationItem2DataTO);
        // Update data, assert that other values haven't changed
        user1navigationItem1DataTO = createDataTO();
        navigationItemService.update(user1navigationItem1Id, null, null, null,
                user1navigationItem1DataTO);
        navigationItems = navigationItemService.find();
        assertCorrectNavigationItem(navigationItems.get(2), user1navigationItem1Id,
                user1navigationItem1Name, user1navigationItem1LastAccessDate,
                user1navigationItem1DataTO);
        assertCorrectNavigationItem(navigationItems.get(3), user1navigationItem2Id,
                user1navigationItem2Name, user1navigationItem2LastAccessDate,
                user1navigationItem2DataTO);
        // change 2 values
        user1navigationItem1Name = user1navigationItem1Name + "3";
        navigationItemService.update(user1navigationItem1Id, user1navigationItem1Name, 3, null,
                null);
        navigationItems = navigationItemService.find();
        assertCorrectNavigationItem(navigationItems.get(2), user1navigationItem2Id,
                user1navigationItem2Name, user1navigationItem2LastAccessDate,
                user1navigationItem2DataTO);
        assertCorrectNavigationItem(navigationItems.get(3), user1navigationItem1Id,
                user1navigationItem1Name, user1navigationItem1LastAccessDate,
                user1navigationItem1DataTO);
        // change lastAccessDate
        user1navigationItem1LastAccessDate = new Date(
                user1navigationItem1LastAccessDate.getTime() + 2000);
        navigationItemService.update(user1navigationItem1Id, null, null,
                user1navigationItem1LastAccessDate,
                null);
        navigationItems = navigationItemService.find();
        assertCorrectNavigationItem(navigationItems.get(2), user1navigationItem2Id,
                user1navigationItem2Name, user1navigationItem2LastAccessDate,
                user1navigationItem2DataTO);
        assertCorrectNavigationItem(navigationItems.get(3), user1navigationItem1Id,
                user1navigationItem1Name, user1navigationItem1LastAccessDate,
                user1navigationItem1DataTO);
    }
}
