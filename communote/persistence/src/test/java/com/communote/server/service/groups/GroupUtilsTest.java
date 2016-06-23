package com.communote.server.service.groups;

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.core.groups.GroupUtils;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.group.Group;


/**
 * Tests for {@link GroupUtils}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */

public class GroupUtilsTest {

    private long i = 1;

    /**
     * 
     * @param group
     *            Group the members should be added to.
     * @param groups
     *            Groups to be added.
     */
    private void addMembers(Group group, Group... groups) {
        Set<CommunoteEntity> members = group.getGroupMembers();
        if (members == null) {
            members = new HashSet<CommunoteEntity>();
            group.setGroupMembers(members);
        }
        for (Group groupToAdd : groups) {
            members.add(groupToAdd);
            Set<Group> groupGroups = groupToAdd.getGroups();
            if (groupGroups == null) {
                groupGroups = new HashSet<Group>();
                groupToAdd.setGroups(groupGroups);
            }
            groupGroups.add(group);
        }
    }

    /**
     * Creates an entity group.
     * 
     * @return {@link Group}.
     */
    private Group createGroup() {
        Group group = Group.Factory.newInstance("Group " + i, "group_" + i);
        group.setId(i);
        i++;
        return group;
    }

    /**
     * Test for {@link GroupUtils#isChild(Group, Group)}
     */
    @Test
    public void testIsChild() {
        Group group1 = createGroup();
        Group group2 = createGroup();
        Group group3 = createGroup();
        Group group4 = createGroup();
        Group group5 = createGroup();
        addMembers(group1, group2, group3);
        addMembers(group2, group3, group4);
        addMembers(group3, group4, group5);
        Assert.assertTrue(GroupUtils.isChild(group1, group2));
        Assert.assertTrue(GroupUtils.isChild(group1, group3));
        Assert.assertTrue(GroupUtils.isChild(group1, group4));
        Assert.assertTrue(GroupUtils.isChild(group1, group5));
        Assert.assertTrue(GroupUtils.isChild(group2, group3));
        Assert.assertTrue(GroupUtils.isChild(group2, group4));
        Assert.assertTrue(GroupUtils.isChild(group2, group5));
        Assert.assertTrue(GroupUtils.isChild(group3, group4));
        Assert.assertTrue(GroupUtils.isChild(group3, group5));
        Assert.assertFalse(GroupUtils.isChild(group5, group1));
        Assert.assertFalse(GroupUtils.isChild(group5, group2));
        Assert.assertFalse(GroupUtils.isChild(group5, group3));
        Assert.assertFalse(GroupUtils.isChild(group5, group4));
        Assert.assertFalse(GroupUtils.isChild(group3, group2));
        Assert.assertFalse(GroupUtils.isChild(group3, group1));
    }
}
