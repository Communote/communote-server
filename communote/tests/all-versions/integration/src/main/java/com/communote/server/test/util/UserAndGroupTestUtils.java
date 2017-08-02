package com.communote.server.test.util;

import java.util.Locale;

import org.springframework.security.core.context.SecurityContext;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.UserGroupMemberManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.core.vo.user.group.GroupVO;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.group.GroupDao;
import com.communote.server.test.util.TestUtils.TestUtilsException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserAndGroupTestUtils {
    /**
     * Adds a user to a group
     *
     * @param group
     *            the group
     * @param aliasPattern
     *            a pattern for the user alias. A %d will be replaced by the value of the number
     *            parameter.
     * @param number
     *            the number to use for replacing %d in the pattern. If the alias does not contain a
     *            %p this parameter has no effect
     * @param externalSystemId
     *            the externalSystemId or null
     * @throws Exception
     *             if adding fails
     */
    public static void addNthUserToGroup(Group group, String aliasPattern, int number,
            String externalSystemId) throws Exception {
        UserGroupMemberManagement ugm = ServiceLocator.findService(UserGroupMemberManagement.class);
        User u = UserAndGroupTestUtils.findNthUser(aliasPattern, number);
        if (externalSystemId == null) {
            ugm.addUser(group.getId(), u.getId());
        } else {
            ugm.addUserForExternal(group.getId(), u.getId(), externalSystemId);
        }
    }

    /**
     * Adds a bunch of users to a group.
     *
     * @param group
     *            the group
     * @param aliasPattern
     *            a user alias pattern. The users to add will be retrieved by replacing %d with 1 to
     *            count.
     * @param count
     *            the number of users to add; should be at least 1.
     * @param externalSystemId
     *            the externalSystemId or null
     * @throws Exception
     *             if adding fails
     */
    public static void addUsersToGroup(Group group, String aliasPattern, int count,
            String externalSystemId) throws Exception {
        for (int i = 1; i <= count; i++) {
            addNthUserToGroup(group, aliasPattern, i, externalSystemId);
        }

    }

    /**
     * Creates a bunch of active users.
     *
     * @param aliasPattern
     *            a pattern for the user alias (a %d will be replaced by a number)
     * @param emailPattern
     *            a pattern for the user email (a %d will be replaced by a number)
     * @param count
     *            the number of users to create
     * @throws Exception
     *             in case user creation fails
     */
    public static void createDummyUsers(String aliasPattern, String emailPattern, int count)
            throws Exception {
        UserManagement userManagement = ServiceLocator.instance().getService(UserManagement.class);
        UserVO userVo = new UserVO();
        userVo.setRoles(new UserRole[] { UserRole.ROLE_KENMEI_USER });
        userVo.setPassword("123456");
        userVo.setLanguage(Locale.ENGLISH);
        for (int i = 1; i <= count; i++) {
            userVo.setEmail(emailPattern.replace("%d", String.valueOf(i)));
            userVo.setAlias(aliasPattern.replace("%d", String.valueOf(i)));
            // create and activate
            userManagement.createUser(userVo, false, false);
        }
    }

    /**
     * Create a random group
     *
     * @param externalSystemId
     *            optional ID of an external system. If not null an external group is created,
     *            otherwise a normal group
     * @param members
     *            members to add to the group
     * @return the created group
     */
    public static Group createRandomGroup(String externalSystemId, User... members) {
        SecurityContext oldSecurityContext = AuthenticationHelper
                .setInternalSystemToSecurityContext();
        String alias = "group" + TestUtils.createRandomUserAlias();
        UserGroupManagement groupManagement = ServiceLocator.instance()
                .getService(UserGroupManagement.class);
        try {
            Group group;
            if (externalSystemId != null) {
                ExternalGroupVO groupVO = createRandomGroupVoForExternalSystem(externalSystemId);
                Long groupId = groupManagement.createExternalGroup(groupVO);
                group = groupManagement.findGroupById(groupId, new IdentityConverter<Group>());
            } else {
                GroupVO groupVO = new GroupVO();
                groupVO.setAlias(alias);
                groupVO.setDescription("description of " + alias);
                groupVO.setName("name of " + alias);
                group = groupManagement.createGroup(groupVO);
            }
            UserGroupMemberManagement memberManagement = ServiceLocator.instance()
                    .getService(UserGroupMemberManagement.class);
            for (User user : members) {
                if (externalSystemId != null) {
                    memberManagement.addUserForExternal(group.getId(), user.getId(),
                            externalSystemId);
                } else {
                    memberManagement.addUser(group.getId(), user.getId());
                }
            }

            return group;
        } catch (Exception e) {
            throw new TestUtilsException(e);
        } finally {
            AuthenticationHelper.setSecurityContext(oldSecurityContext);
        }
    }

    /**
     * Create a VO for an external group filled with random data
     *
     * @param externalSystemId
     *            the ID of the external system
     * @return the VO
     */
    public static ExternalGroupVO createRandomGroupVoForExternalSystem(String externalSystemId) {
        String alias = "group" + TestUtils.createRandomUserAlias();
        ExternalGroupVO groupVO = new ExternalGroupVO();
        groupVO.setExternalId("external_" + alias);
        groupVO.setExternalSystemId(externalSystemId);
        groupVO.setAlias(alias);
        groupVO.setDescription("description of " + alias);
        groupVO.setName("name of " + alias);
        return groupVO;
    }

    /**
     * Returns the group having the supplied alias or null.
     *
     * @param groupAlias
     *            the alias of the group to retrieve
     * @return the group
     */
    public static Group findGroup(String groupAlias) {
        return ServiceLocator.findService(GroupDao.class).findByAlias(groupAlias);
    }

    /**
     * Returns a test users or null if not found
     *
     * @param aliasPattern
     *            a pattern for the user alias (a %d will be replaced by the number)
     * @param number
     *            the number to use for replacing %d in the pattern. If the alias does not contain a
     *            %p this parameter has no effect
     * @return the user
     */
    public static User findNthUser(String aliasPattern, int number) {
        User user;
        String alias = aliasPattern.replace("%d", String.valueOf(number));
        user = ServiceLocator.instance().getService(UserManagement.class).findUserByAlias(alias);
        return user;
    }

    /**
     * private constructor for utility class
     */
    private UserAndGroupTestUtils() {

    }

}
