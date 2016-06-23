package com.communote.service.permission;

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.model.blog.Blog;

/**
 * Test for equals and hashCode methods of Permission.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PermissionImplTest {

    /**
     * Runs the test.
     */
    @Test
    public void test() {
        Permission<Object> permission1 = new Permission<Object>("permission1");
        Permission<Object> permission1clone = new Permission<Object>("permission1");
        Permission<Object> permission1clone2 = new Permission<Object>("permission1");
        Permission<Object> permission2 = new Permission<Object>("permission2");
        Assert.assertTrue(permission1.equals(permission1clone));
        Assert.assertTrue(permission1.equals(permission1clone2));
        Assert.assertTrue(permission1clone.equals(permission1clone2));
        Assert.assertFalse(permission1.equals(permission2));
        Assert.assertFalse(permission1clone.equals(permission2));
        Assert.assertFalse(permission1clone2.equals(permission2));

        Set<Permission<Object>> permissions = new HashSet<Permission<Object>>();
        permissions.add(permission1);
        permissions.add(permission1clone);
        permissions.add(permission1clone2);
        Assert.assertEquals(permissions.size(), 1);
        permissions.add(permission2);
        Assert.assertEquals(permissions.size(), 2);

        Assert.assertTrue(permissions.contains(permission1));
        Assert.assertTrue(permissions.contains(new Permission<Object>("permission1")));
    }

    /**
     * Simple against a default permission.
     */
    @Test
    public void testWithDefaultPermissions() {
        Assert.assertEquals(new Permission<Blog>("EDIT_ACCESS_CONTROL_LIST"), new Permission<Blog>(
                        "EDIT_ACCESS_CONTROL_LIST"));
        Assert.assertEquals(TopicPermissionManagement.PERMISSION_EDIT_ACCESS_CONTROL_LIST,
                new Permission<Blog>("EDIT_ACCESS_CONTROL_LIST"));
    }
}
