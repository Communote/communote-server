package com.communote.server.core.user;

import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.UserAnonymizationDisabledException;
import com.communote.server.core.user.UserDisablingDisabledException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.installer.InstallerTest;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.UserToBlogRoleMapping;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.blog.CreateBlogPostHelper;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.persistence.blog.UserToBlogRoleMappingDao;
import com.communote.server.persistence.common.security.SecurityCodeNotFoundException;
import com.communote.server.persistence.crc.ContentRepositoryException;
import com.communote.server.persistence.user.security.EmailSecurityCodeDao;
import com.communote.server.persistence.user.security.UserSecurityCodeDao;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Testing the user management functionality
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserManagementTest extends CommunoteIntegrationTest {

    /** The test user email */
    private final static String TEST_USER_EMAIL = "new_normal-user@localhost";
    /** test user alias */
    private final static String TEST_USER_ALIAS = "new_nuser";

    /** second test user email */
    private final static String TEST_USER_EMAIL_2 = "second-norma-user@localhost";
    /** second test user alias */
    private final static String TEST_USER_ALIAS_2 = "seconduser";

    private final static String TEST_MASS_USER_EMAIL_PATTERN = "user%d@localhost";
    private final static String TEST_MASS_USER_ALIAS_PATTERN = "user%d";

    private final static int TEST_MASS_USER_COUNT = 50;

    /** The password of the test user */
    public final static String TEST_USER_CLEAR_PASSWORD = "123456";
    /** The email address of the test user for email address changing test */
    private final static String TEST_USER_EMAILCHANGE_EMAIL = "emailchanging@localhost";
    private final static String TEST_USER_EMAILCHANGE_ALIAS = "emailchanging";
    private final static String TEST_USER_PASSWORDCHANGE_EMAIL = "passwortchanging@localhost";
    private final static String TEST_USER_PASSWORDCHANGE_ALIAS = "passwortchanging";

    private final static String TEST_DEL_USER_EMAIL = "del-user@localhost";
    private final static String TEST_DEL_USER_ALIAS = "deluser";
    private final static String TEST_DEL_USER_2_EMAIL = "del-user2@localhost";
    private final static String TEST_DEL_USER_2_ALIAS = "deluser2";

    private final static String TEST_DEL_USER_2_BLOG_ALIAS = "deluser2blog";
    private final static String TEST_DEL_USER_2_BLOG_TITLE = "blog of deluser2";
    private final static String TEST_DEL_USER_2_PRIV_BLOG_ALIAS = "deluser2privblog";
    private final static String TEST_DEL_USER_2_PRIV_BLOG_TITLE = "private blog of deluser2";
    private final static String TEST_DEL_USER_2_USED_BLOG_ALIAS = "deluser2pubblog";
    private final static String TEST_DEL_USER_2_USED_BLOG_TITLE = "blog of deluser2 with member";

    private boolean oldAllCanReadWriteAllowed;
    private boolean oldAutomaticUserActivation;

    @Autowired
    private UserManagement userManagement;
    @Autowired
    private NoteService noteService;
    @Autowired
    private BlogManagement topicManagement;
    @Autowired
    private UserToBlogRoleMappingDao userToBlogRoleMappingDao;
    @Autowired
    private BlogRightsManagement topicRightsManagement;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private QueryManagement queryManagement;
    @Autowired
    private EmailSecurityCodeDao emailSecurityCodeDao;
    @Autowired
    private UserSecurityCodeDao userSecurityCodeDao;
    private boolean oldCreatePresonalTopic;

    /**
     * Sets the appropriate client properties to enable/disable user anonymization/permanently
     * disabling.
     *
     * @param disableEnabled
     *            whether permanently disabling should be enabled
     * @param anonymizeEnabled
     *            whether anonymization should be enabled
     */
    private void changeEnabledStateOfUserDeletion(boolean disableEnabled, boolean anonymizeEnabled) {
        HashMap<ClientConfigurationPropertyConstant, String> map = new HashMap<ClientConfigurationPropertyConstant, String>();
        map.put(ClientProperty.DELETE_USER_BY_ANONYMIZE_ENABLED, Boolean.toString(anonymizeEnabled));
        map.put(ClientProperty.DELETE_USER_BY_DISABLE_ENABLED, Boolean.toString(disableEnabled));
        CommunoteRuntime.getInstance().getConfigurationManager()
        .updateClientConfigurationProperties(map);
    }

    /**
     * Cleanup method for the test group DeleteTestUserByManager
     */
    @AfterGroups(groups = { "DeleteTestUserByManager" })
    public void cleanupDeleteUserByManagerTests() {
        try {
            if (userManagement.findUserByAlias(TEST_DEL_USER_ALIAS) != null) {
                internalTestAnonymizeUser(TEST_DEL_USER_EMAIL, TEST_DEL_USER_ALIAS);
            }
        } catch (Exception e) {
            Assert.fail("Cleanup operation failed.", e);
        } finally {
            AuthenticationTestUtils.setAuthentication(null);
        }
    }

    /**
     * Cleanup method for the test group DeleteTestUserByUser
     */
    @AfterGroups(groups = { "DeleteTestUserByUser" })
    public void cleanupDeleteUserByUserTests() {
        AuthenticationTestUtils.setAuthentication(null);
    }

    /**
     * Cleanup after tests.
     */
    @AfterClass
    public void cleanupTests() {
        // put a manager in security context
        AuthenticationTestUtils.setSecurityContext(userManagement
                .findUserByAlias(InstallerTest.TEST_MANAGER_USER_ALIAS));
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(
                ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS,
                Boolean.toString(oldAllCanReadWriteAllowed));
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(ClientProperty.AUTOMATIC_USER_ACTIVATION,
                Boolean.toString(oldAutomaticUserActivation));
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(ClientProperty.CREATE_PERSONAL_BLOG,
                Boolean.toString(oldCreatePresonalTopic));
        AuthenticationTestUtils.setAuthentication(null);
    }

    /**
     * Creates a UserVO.
     *
     * @param createAsManager
     *            whether the user should have the client manager role
     * @return the user VO
     */
    private UserVO createCommonUserVoBase(boolean createAsManager) {
        UserVO userVo = new UserVO();
        if (createAsManager) {
            userVo.setRoles(new UserRole[] { UserRole.ROLE_KENMEI_USER,
                    UserRole.ROLE_KENMEI_CLIENT_MANAGER });
        } else {
            userVo.setRoles(new UserRole[] { UserRole.ROLE_KENMEI_USER });
        }
        userVo.setPassword(TEST_USER_CLEAR_PASSWORD);
        userVo.setPlainPassword(true);
        userVo.setLanguage(Locale.ENGLISH);
        return userVo;
    }

    /**
     * create some blogs and posts for testing deletion of blog manager.
     *
     * @param creator
     *            the user that will be the manager of the blogs
     * @throws Exception
     *             in case of errors
     */
    private void createTestBlogsAndPost(User creator) throws Exception {
        Long userToRestoreInSecurityContext = SecurityHelper.getCurrentUserId();
        AuthenticationTestUtils.setSecurityContext(creator);
        CreationBlogTO blogTO = new CreationBlogTO();
        blogTO.setCreatorUserId(creator.getId());
        blogTO.setNameIdentifier(TEST_DEL_USER_2_BLOG_ALIAS);
        blogTO.setTitle(TEST_DEL_USER_2_BLOG_TITLE);
        Blog topic = topicManagement.createBlog(blogTO);
        topicRightsManagement.setAllCanReadAllCanWrite(topic.getId(), true, false);
        NoteStoringTO storingTO = new NoteStoringTO();
        storingTO.setBlogId(topic.getId());
        storingTO.setCreationSource(NoteCreationSource.WEB);
        storingTO.setCreatorId(creator.getId());
        storingTO.setContent("A post to my blog");
        storingTO.setContentType(NoteContentType.PLAIN_TEXT);
        storingTO.setPublish(true);
        storingTO.setVersion(0L);
        CreateBlogPostHelper.setDefaultFailLevel(storingTO);
        NoteModificationResult res = noteService.createNote(storingTO, null);
        Assert.assertEquals(res.getStatus(), NoteModificationStatus.SUCCESS);
        // create another private blog
        blogTO.setNameIdentifier(TEST_DEL_USER_2_PRIV_BLOG_ALIAS);
        blogTO.setTitle(TEST_DEL_USER_2_PRIV_BLOG_TITLE);
        topicManagement.createBlog(blogTO);
        // create another blog with a member
        blogTO.setNameIdentifier(TEST_DEL_USER_2_USED_BLOG_ALIAS);
        blogTO.setTitle(TEST_DEL_USER_2_USED_BLOG_TITLE);
        topic = topicManagement.createBlog(blogTO);
        Long memberUserId = userManagement.findUserByAlias(InstallerTest.TEST_MANAGER_USER_ALIAS)
                .getId();
        topicRightsManagement.addEntity(topic.getId(), memberUserId, BlogRole.MEMBER);

        AuthenticationTestUtils.setAuthentication(null);
        if (userToRestoreInSecurityContext != null) {
            User u = userManagement.findUserByUserId(userToRestoreInSecurityContext);
            AuthenticationTestUtils.setSecurityContext(u);
        }
    }

    /**
     * Creates a user for the anonymize and permanently disable tests if not yet existing.
     *
     * @return the user created just for deletion
     * @throws Exception
     *             if creation fails
     */
    private User createUserForDeletion() throws Exception {
        return createUserForDeletion(TEST_DEL_USER_EMAIL, TEST_DEL_USER_ALIAS);
    }

    /**
     * Create user for deletion
     *
     * @param email
     *            the email
     * @param alias
     *            the alias
     * @return the created user
     * @throws Exception
     *             in case of an error
     */
    private User createUserForDeletion(String email, String alias) throws Exception {
        UserManagement um = userManagement;
        User user = um.findUserByAlias(alias);
        if (user == null) {
            UserVO userVo = setEmailAlias(createCommonUserVoBase(false), email, alias);
            um.createUser(userVo, false, false);
        }
        user = um.findUserByAlias(alias);
        Assert.assertEquals(user.getEmail(), email);
        Assert.assertEquals(user.getAlias(), alias);
        Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
        return user;
    }

    /**
     * Find a user to the given email. Asserts that the user cannot be null.
     *
     * @param email
     *            The email of a user
     * @return The user.
     */
    private User findUser(String email) {
        UserManagement um = userManagement;
        User user = um.findUserByEmail(email);

        Assert.assertNotNull(user, "User with email " + email + " is null!");
        return user;
    }

    /**
     * anonymizes a user, identified by email and alias
     *
     * @param email
     *            the email of the user
     * @param alias
     *            the alias of the user
     * @throws Exception
     *             in case something goes wrong
     */
    private void internalTestAnonymizeUser(String email, String alias) throws Exception {
        UserManagement um = userManagement;
        um.anonymizeUser(um.findUserByAlias(alias).getId(), null, false);
        validateUserAnonymizationSuccess(email, alias);
    }

    /**
     * deletes a user and checks for an expected exception
     *
     * @param userId
     *            the user id
     * @param anonymize
     *            if true the user will be anonymized, if false the user will be permanently
     *            disabled
     * @param expectedException
     *            parameter giving an exception that must occur
     * @return true if the expectedException occurred
     * @throws Exception
     *             in case an exception occurs that is not the expected exception
     */
    private boolean internalTestDeleteUserWithExpectedException(Long userId, boolean anonymize,
            Class<?> expectedException) throws Exception {
        try {
            if (anonymize) {
                userManagement.anonymizeUser(userId, null, false);
            } else {
                userManagement.permanentlyDisableUser(userId, null, false);
            }
        } catch (Exception e) {
            if (e.getClass().equals(expectedException)) {
                return true;
            }
            throw e;
        }
        return false;
    }

    /**
     * permanently disables a user, identified by email and alias
     *
     * @param email
     *            the email of the user
     * @param alias
     *            the alias of the user
     * @throws Exception
     *             in case something goes wrong
     */
    private void internalTestPermanentlyDisableUser(String email, String alias) throws Exception {
        userManagement.permanentlyDisableUser(userManagement.findUserByAlias(alias).getId(), null,
                false);
        validateUserPermanentlyDisableSuccess(email, alias);
    }

    /**
     * Prepare deletion
     */
    @BeforeGroups(groups = { "DeleteTestUserByManager" })
    public void prepareDeleteTestUserByManagerTests() {
        try {
            createUserForDeletion();
            AuthenticationTestUtils.setSecurityContext(userManagement
                    .findUserByAlias(InstallerTest.TEST_MANAGER_USER_ALIAS));
        } catch (Exception e) {
            Assert.fail("Preparing the user deletion by manager failed.", e);
        }
    }

    /**
     * Prepare deletion
     */
    @BeforeGroups(groups = { "DeleteTestUserByUser" })
    public void prepareDeleteTestUserByUserTests() {
        try {
            User userToDel = createUserForDeletion();
            AuthenticationTestUtils.setSecurityContext(userToDel);
        } catch (Exception e) {
            Assert.fail("Preparing the user deletion by user failed.", e);
        }
    }

    /**
     * Prepare deletion
     */
    @BeforeGroups(groups = { "DeleteTestUserWithBlogs" })
    public void prepareDeleteTestUserWithBlogTests() {
        try {
            User userToDel = createUserForDeletion(TEST_DEL_USER_2_EMAIL, TEST_DEL_USER_2_ALIAS);
            createTestBlogsAndPost(userToDel);
            AuthenticationTestUtils.setSecurityContext(userManagement
                    .findUserByAlias(InstallerTest.TEST_MANAGER_USER_ALIAS));
        } catch (Exception e) {
            Assert.fail("Preparing the deletion of a user with blogs failed.", e);
        }
    }

    /**
     * Does required preparations to run the tests.
     */
    @BeforeClass(dependsOnGroups = GROUP_INTEGRATION_TEST_SETUP)
    public void prepareTests() {
        oldAllCanReadWriteAllowed = ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS
                .getValue(false);
        oldAutomaticUserActivation = ClientProperty.AUTOMATIC_USER_ACTIVATION.getValue(false);
        oldCreatePresonalTopic = ClientProperty.CREATE_PERSONAL_BLOG.getValue(false);
        ConfigurationManager confManager = CommunoteRuntime.getInstance().getConfigurationManager();
        confManager.updateClientConfigurationProperty(
                ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS, "true");

        confManager.updateClientConfigurationProperty(ClientProperty.AUTOMATIC_USER_ACTIVATION,
                "false");
        confManager.updateClientConfigurationProperty(ClientProperty.CREATE_PERSONAL_BLOG, "false");
    }

    /**
     * Sets email address, alias first and last name of a user VO
     *
     * @param userVo
     *            the VO to change
     * @param email
     *            the email address
     * @param alias
     *            the alias of the user
     * @return the modified VO
     */
    private UserVO setEmailAlias(UserVO userVo, String email, String alias) {
        userVo.setEmail(email);
        userVo.setAlias(alias);
        userVo.setFirstName("firstName-" + alias);
        userVo.setLastName("lastName-" + alias);
        return userVo;
    }

    /**
     * Tests anonymization of a permanently disabled user by manager.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "DeleteTestUserByManager" }, dependsOnMethods = { "testPermanentlyDisableUser" })
    public void testAnonymizePermanentlyDisabledUser() throws Exception {
        internalTestAnonymizeUser(TEST_DEL_USER_EMAIL, TEST_DEL_USER_ALIAS);
    }

    /**
     * Tests anonymization of a user by manager.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "DeleteTestUserByManager" }, dependsOnGroups = { "CreateTestUser" })
    public void testAnonymizeUser() throws Exception {
        // must work for manager even if disabled
        changeEnabledStateOfUserDeletion(false, false);
        internalTestAnonymizeUser(TEST_DEL_USER_EMAIL, TEST_DEL_USER_ALIAS);
    }

    /**
     * tests the anonymization of a user by that user
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(groups = { "DeleteTestUserByUser" }, dependsOnMethods = { "testDeleteUserByUserAccessRestrictions" })
    public void testAnonymizeUserByUser() throws Exception {
        internalTestAnonymizeUser(TEST_DEL_USER_EMAIL, TEST_DEL_USER_ALIAS);
    }

    @Test
    public void testAnonymizeUserWithAttachements() throws Exception {
        User manager = TestUtils.createRandomUser(true);
        User userForDeletion = TestUtils.createRandomUser(false);

        final String uniqueTag = UUID.randomUUID().toString();
        Blog blog = TestUtils.createRandomBlog(true, true, manager);

        final Set<Long> noteIds = new HashSet<>();
        final Set<String> attachmentContentIds = new HashSet<>();

        // creation phase
        AuthenticationTestUtils.setSecurityContext(userForDeletion);
        for (int i = 0; i < 10; i++) {

            final String fileName = "attachement-for-deletion-" + i + ".txt";
            final String content = "delete me content " + i + ".";

            NoteStoringTO storingTO = TestUtils.generateCommonNoteStoringTO(userForDeletion, blog);
            // add unique tag for retrieval after creation
            storingTO.setUnparsedTags(uniqueTag);

            Attachment attachment = TestUtils.addAttachment(storingTO, fileName, content);

            NoteModificationResult result = noteService.createNote(storingTO, null);

            AttachmentTO attachmentTO = TestUtils
                    .getAttachmentFromDefaultFilesystemConnectorByContentId(attachment
                            .getContentIdentifier());
            Assert.assertNotNull(attachmentTO);

            noteIds.add(result.getNoteId());
            attachmentContentIds.add(attachment.getContentIdentifier());

        }

        // action
        AuthenticationTestUtils.setSecurityContext(manager);
        userManagement.anonymizeUser(userForDeletion.getId(), null, false);

        // assert that security context is not changed
        Assert.assertEquals(SecurityHelper.getCurrentUserId(), manager.getId());

        // check for anonymized user
        validateUserAnonymizationSuccess(userForDeletion.getEmail(), userForDeletion.getAlias());

        // check for deletion success
        for (Long noteId : noteIds) {
            Note note = this.noteService.getNote(noteId, new IdentityConverter<Note>());
            Assert.assertNull(note);
        }
        for (String contentId : attachmentContentIds) {
            try {
                TestUtils.getAttachmentFromDefaultFilesystemConnectorByContentId(contentId);
                Assert.fail("Expected ContentRepositoryException exception.");
            } catch (ContentRepositoryException e) {
                // expected case
            }
        }
    }

    /**
     * Tests the anonymization of a user that is the last manager of some blogs and author of a
     * post. The anonymization is run by the client manager. The client manager makes himself to the
     * manager of the topics where the user is the last manager.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(groups = { "DeleteTestUserWithBlogs" }, dependsOnMethods = { "testAnonymizeUserWithBlogsRemoveBlogs" })
    public void testAnonymizeUserWithBlogsBecomeManager() throws Exception {
        User user = createUserForDeletion(TEST_DEL_USER_2_EMAIL, TEST_DEL_USER_2_ALIAS);
        // re-create the blogs
        createTestBlogsAndPost(user);
        Long[] blogsToHandle = new Long[2];
        try {
            userManagement.anonymizeUser(user.getId(), null, false);
        } catch (NoBlogManagerLeftException e) {
            validateNoBlogManagerLeftException(e, blogsToHandle, topicManagement);
        }
        AuthenticationTestUtils.setSecurityContext(userManagement
                .findUserByAlias(InstallerTest.TEST_MANAGER_USER_ALIAS));
        // handle the manager-less blogs by becoming manager
        userManagement.anonymizeUser(user.getId(), blogsToHandle, true);
        validateUserAnonymizationSuccess(TEST_DEL_USER_2_EMAIL, TEST_DEL_USER_2_ALIAS);
        // make sure the private blog is removed
        Assert.assertNull(topicManagement.findBlogByIdentifier(TEST_DEL_USER_2_PRIV_BLOG_ALIAS),
                "Blog " + TEST_DEL_USER_2_PRIV_BLOG_ALIAS + " still exists.");
        // assert there is only one member
        validateCorrectBlogMember(TEST_DEL_USER_2_USED_BLOG_ALIAS,
                InstallerTest.TEST_MANAGER_USER_ALIAS);
        validateCorrectBlogMember(TEST_DEL_USER_2_BLOG_ALIAS, InstallerTest.TEST_MANAGER_USER_ALIAS);
        // assert posts are removed
        validateCorrectRegularPostCount(TEST_DEL_USER_2_BLOG_ALIAS, 0, topicManagement);
        // cleanup by removing the remaining blogs
        topicManagement.deleteBlogs(blogsToHandle);
    }

    /**
     * Tests the anonymization of a user that is the last manager of some blogs and author of a
     * post. The anonymization is run by the client manager. The client manager removes the blogs
     * where the user is the last manager.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(groups = { "DeleteTestUserWithBlogs" }, dependsOnGroups = { "DeleteTestUserByUser" })
    public void testAnonymizeUserWithBlogsRemoveBlogs() throws Exception {
        User user = userManagement.findUserByAlias(TEST_DEL_USER_2_ALIAS);
        Long[] blogsToHandle = new Long[2];
        try {
            userManagement.anonymizeUser(user.getId(), null, false);
        } catch (NoBlogManagerLeftException e) {
            validateNoBlogManagerLeftException(e, blogsToHandle, topicManagement);
        }
        // handle the manager-less blogs by removing them
        userManagement.anonymizeUser(user.getId(), blogsToHandle, false);
        validateUserAnonymizationSuccess(TEST_DEL_USER_2_EMAIL, TEST_DEL_USER_2_ALIAS);
        // make sure the blogs are removed
        Assert.assertNull(topicManagement.findBlogByIdentifier(TEST_DEL_USER_2_PRIV_BLOG_ALIAS),
                "Blog " + TEST_DEL_USER_2_PRIV_BLOG_ALIAS + " still exists.");
        Assert.assertNull(topicManagement.findBlogByIdentifier(TEST_DEL_USER_2_BLOG_ALIAS), "Blog "
                + TEST_DEL_USER_2_BLOG_ALIAS + " still exists.");
        Assert.assertNull(topicManagement.findBlogByIdentifier(TEST_DEL_USER_2_USED_BLOG_ALIAS),
                "Blog " + TEST_DEL_USER_2_USED_BLOG_ALIAS + " still exists.");
    }

    /**
     * Test for changing the email address.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "CreateTestUser" }, dependsOnMethods = { "testCreateUser" })
    public void testChangeEmailAddress() throws Exception {
        testCreateUser(TEST_USER_EMAILCHANGE_EMAIL, TEST_USER_EMAILCHANGE_ALIAS);
        User user = findUser(TEST_USER_EMAILCHANGE_EMAIL);
        AuthenticationTestUtils.setManagerContext();
        Assert.assertTrue(userManagement.changeEmailAddress(user.getId(),
                "newemailaddress@localhost", true));
    }

    /**
     * Test for changing the user password.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "CreateTestUser" }, dependsOnMethods = { "testCreateUser" })
    public void testChangePassword() throws Exception {
        testCreateUser(TEST_USER_PASSWORDCHANGE_EMAIL, TEST_USER_PASSWORDCHANGE_ALIAS);
        User user = findUser(TEST_USER_PASSWORDCHANGE_EMAIL);
        String oldPwd = user.getPassword();
        userManagement.changePassword(user.getId(), "newPassword");
        String newPwd = findUser(TEST_USER_PASSWORDCHANGE_EMAIL).getPassword();
        Assert.assertFalse(oldPwd.equals(newPwd), "The password was not changed.");
    }

    /**
     * Test for confirming the email address
     *
     * @throws SecurityCodeNotFoundException
     *             exception if the code was not found
     * @throws EmailAlreadyExistsException
     *             the email adress already exists
     */
    @Test(groups = { "CreateTestUser" }, dependsOnMethods = { "testChangeEmailAddress" })
    public void testConfirmEmailAddress() throws SecurityCodeNotFoundException,
    EmailAlreadyExistsException {
        SecurityCode code = emailSecurityCodeDao.findByEmailAddress("newemailaddress@localhost");
        userManagement.confirmNewEmailAddress(code.getCode());
        Assert.assertNotNull(userManagement.findUserByEmail("newemailaddress@localhost"));
    }

    /**
     * Test for confirming a user
     *
     * @param alias
     *            the alias of the user
     * @throws Exception
     *             in case of an error
     */
    @Parameters({ "alias" })
    @Test(groups = { "CreateTestUser" }, dependsOnMethods = { "testCreateUser" })
    public void testConfirmUser(@Optional("new_nuser") String alias) throws Exception {

        User manager = TestUtils.createRandomUser(true);
        // put a manager in security context
        AuthenticationTestUtils.setSecurityContext(manager);

        User user = userManagement.findUserByAlias(alias);
        assert user != null : "User cannot be null";

        SecurityCode code = userSecurityCodeDao.findByUser(user.getId(),
                SecurityCodeAction.CONFIRM_USER);
        Assert.assertNotNull(code, "code not found");

        UserVO userVo = new UserVO();
        userVo.setAlias(user.getAlias());
        userVo.setEmail(user.getEmail());
        userVo.setFirstName(user.getProfile().getFirstName());
        userVo.setLastName(user.getProfile().getLastName());
        userVo.setPassword(user.getPassword());
        userVo.setPlainPassword(false);
        userVo.setRoles(userManagement.getRolesOfUser(user.getId()));
        userVo.setLanguage(user.getLanguageLocale());

        try {
            user = userManagement.confirmUser(code.getCode(), userVo);
        } catch (Exception e) {
            Assert.fail("Unable to confirm " + user.getEmail() + ": " + e.getMessage());
        }
        Assert.assertEquals(user.getStatus(), UserStatus.CONFIRMED);

        // Activate user
        userManagement.changeUserStatusByManager(user.getId(), UserStatus.ACTIVE);
        user = userManagement.getUserById(user.getId(), new IdentityConverter<User>());
        Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
        // remove manager from security context
        AuthenticationTestUtils.setAuthentication(null);
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "CreateTestUser" })
    public void testCreateDummyUser() throws Exception {
        UserManagement um = userManagement;
        UserVO userVo = createCommonUserVoBase(false);
        for (int i = 1; i <= TEST_MASS_USER_COUNT; i++) {
            setEmailAlias(userVo, new Formatter().format(TEST_MASS_USER_EMAIL_PATTERN, i)
                    .toString(), new Formatter().format(TEST_MASS_USER_ALIAS_PATTERN, i).toString());
            um.createUser(userVo, false, true);
        }
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "CreateTestUser" }, dependsOnMethods = "testCreateUser")
    public void testCreateSecondUser() throws Exception {
        testCreateUser(TEST_USER_EMAIL_2, TEST_USER_ALIAS_2);
    }

    /**
     * Test for creating a normal user.
     *
     * @param email
     *            email address
     * @param alias
     *            the alias of the user
     * @throws Exception
     *             in case of an error
     */
    @Parameters({ "eMailAddress", "alias" })
    @Test(groups = { "CreateTestUser" })
    public void testCreateUser(@Optional("new_normal-user@localhost") String email,
            @Optional("new_nuser") String alias) throws Exception {
        UserVO userVo = setEmailAlias(createCommonUserVoBase(false), email, alias);
        userManagement.createUser(userVo, true, false);
    }

    /**
     * Tests whether the access restrictions are checked properly when a user deletes himself.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "DeleteTestUserByUser" }, dependsOnGroups = { "DeleteTestUserByManager" })
    public void testDeleteUserByUserAccessRestrictions() throws Exception {
        UserManagement um = userManagement;

        User userToDel = um.findUserByAlias(TEST_DEL_USER_ALIAS);
        // deactivate anonymization and disabling
        changeEnabledStateOfUserDeletion(false, false);
        boolean expectedExceptionOccured = internalTestDeleteUserWithExpectedException(
                userToDel.getId(), true, UserAnonymizationDisabledException.class);
        Assert.assertTrue(expectedExceptionOccured,
                "Anonymization must not be possible if disabled.");
        // try disabling
        expectedExceptionOccured = internalTestDeleteUserWithExpectedException(userToDel.getId(),
                false, UserDisablingDisabledException.class);
        Assert.assertTrue(expectedExceptionOccured,
                "Permanently disabling must not be possible if disabled.");
        // deactivate only anonymization
        changeEnabledStateOfUserDeletion(true, false);
        expectedExceptionOccured = internalTestDeleteUserWithExpectedException(userToDel.getId(),
                true, UserAnonymizationDisabledException.class);
        Assert.assertTrue(expectedExceptionOccured,
                "Anonymization must not be possible if disabled.");
        // deactivate only disabling
        changeEnabledStateOfUserDeletion(false, true);
        expectedExceptionOccured = internalTestDeleteUserWithExpectedException(userToDel.getId(),
                false, UserDisablingDisabledException.class);
        Assert.assertTrue(expectedExceptionOccured,
                "Permanently disabling must not be possible if disabled.");
        // try to delete another user
        User anotherUser = um.findUserByAlias(TEST_USER_ALIAS);
        internalTestDeleteUserWithExpectedException(anotherUser.getId(), true,
                AuthorizationException.class);
    }

    /**
     * Test for finding a user
     */
    @Test(groups = { "CreateTestUser" }, dependsOnMethods = { "testCreateUser" })
    public void testFindUserByEmail() {
        User user = findUser(TEST_USER_EMAIL);
        Assert.assertEquals(user.getEmail(), TEST_USER_EMAIL, "User email must be equal!");
    }

    /**
     * Test for finding a user with upper cased email
     */
    @Test(groups = { "CreateTestUser" }, dependsOnMethods = { "testCreateUser" })
    public void testFindUserByUpperCaseEmail() {
        UserManagement um = userManagement;

        User user = um.findUserByEmail(TEST_USER_EMAIL.toUpperCase());
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getEmail(), TEST_USER_EMAIL);
    }

    /**
     * Tests permanently disabling a user by manager.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "DeleteTestUserByManager" }, dependsOnMethods = { "testAnonymizeUser" })
    public void testPermanentlyDisableUser() throws Exception {
        // re-create user for deletion
        createUserForDeletion();
        internalTestPermanentlyDisableUser(TEST_DEL_USER_EMAIL, TEST_DEL_USER_ALIAS);
    }

    /**
     * Tests permanently disabling a user by manager.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "DeleteTestUserByUser" }, dependsOnMethods = { "testAnonymizeUserByUser" })
    public void testPermanentlyDisableUserByUser() throws Exception {
        changeEnabledStateOfUserDeletion(true, false);
        // re-create user for deletion
        User userToDel = createUserForDeletion();
        // reset this user as current user (because he has another id)
        AuthenticationTestUtils.setSecurityContext(userToDel);
        internalTestPermanentlyDisableUser(TEST_DEL_USER_EMAIL, TEST_DEL_USER_ALIAS);
    }

    /**
     * Tests the anonymization of a user that is the last manager of some blogs and author of a
     * post. The anonymization is run by the client manager. The client manager removes the blogs
     * where the user is the last manager.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(groups = { "DeleteTestUserWithBlogs" }, dependsOnMethods = { "testAnonymizeUserWithBlogsBecomeManager" })
    public void testPermanentlyDisableUserWithBlogs() throws Exception {
        User user = createUserForDeletion(TEST_DEL_USER_2_EMAIL, TEST_DEL_USER_2_ALIAS);
        // re-create the blogs
        createTestBlogsAndPost(user);
        Long[] blogsToHandle = new Long[2];
        try {
            userManagement.permanentlyDisableUser(user.getId(), null, false);
        } catch (NoBlogManagerLeftException e) {
            validateNoBlogManagerLeftException(e, blogsToHandle, topicManagement);
        }
        // handle the manager-less blogs by removing them
        userManagement.permanentlyDisableUser(user.getId(), blogsToHandle, true);
        validateUserPermanentlyDisableSuccess(TEST_DEL_USER_2_EMAIL, TEST_DEL_USER_2_ALIAS);
        // make sure the private blog is not removed
        Assert.assertNotNull(topicManagement
                .findBlogByIdentifierWithoutAuthorizationCheck(TEST_DEL_USER_2_PRIV_BLOG_ALIAS),
                "Blog " + TEST_DEL_USER_2_PRIV_BLOG_ALIAS + " does not exist.");
        // assert there is only one member
        validateCorrectBlogMember(TEST_DEL_USER_2_USED_BLOG_ALIAS,
                InstallerTest.TEST_MANAGER_USER_ALIAS);
        validateCorrectBlogMember(TEST_DEL_USER_2_BLOG_ALIAS, InstallerTest.TEST_MANAGER_USER_ALIAS);
        // assert the post is still there
        validateCorrectRegularPostCount(TEST_DEL_USER_2_BLOG_ALIAS, 1, topicManagement);
    }

    /**
     * Validate that a blog has only one member.
     *
     * @param blogNameId
     *            the nameId/alias of the blog
     * @param managerAlias
     *            user alias of the only member
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    private void validateCorrectBlogMember(String blogNameId, String managerAlias)
            throws BlogAccessException {
        Blog topic = topicManagement.findBlogByIdentifier(blogNameId);
        Collection<UserToBlogRoleMapping> mappings = userToBlogRoleMappingDao.findMappings(
                topic.getId(), null, null, false, null);
        int mappingsFound = mappings.size();
        mappings = userToBlogRoleMappingDao.findMappings(topic.getId(), null, null, true, null);
        mappingsFound += mappings.size();
        Assert.assertEquals(mappingsFound, 1, "Blog has more than one member.");
        Long userId = userManagement.findUserByAlias(managerAlias).getId();
        boolean isMember = topicRightsManagement.isEntityDirectMember(topic.getId(), userId);
        Assert.assertTrue(isMember, "user is not member");
    }

    /**
     * Validate that a blog has the expected number of regular (non-SYSTEM-) posts.
     *
     * @param blogNameId
     *            the nameId/alias of the blog
     * @param expectedPostCount
     *            the expected number of regular posts
     * @param blogManagement
     *            the blogmanagement
     */
    private void validateCorrectRegularPostCount(String blogNameId, int expectedPostCount,
            BlogManagement blogManagement) {
        NoteQuery query = QueryDefinitionRepository.instance().getQueryDefinition(NoteQuery.class);
        NoteQueryParameters queryInstance = query.createInstance();
        TaggingCoreItemUTPExtension utpExt = queryInstance.getTypeSpecificExtension();
        utpExt.setTopicAccessLevel(TopicAccessLevel.READ);
        utpExt.setUserId(SecurityHelper.getCurrentUserId());
        utpExt.setBlogFilter(new Long[] { blogManagement
                .findBlogByIdentifierWithoutAuthorizationCheck(blogNameId).getId() });
        Collection<SimpleNoteListItem> items = queryManagement.executeQueryComplete(query,
                queryInstance);
        int regularPosts = 0;
        for (SimpleNoteListItem listItem : items) {
            Note note = noteDao.load(listItem.getId());
            if (!note.getCreationSource().equals(NoteCreationSource.SYSTEM)) {
                regularPosts++;
            }
        }
        Assert.assertEquals(regularPosts, expectedPostCount,
                "Not all posts of the anonymized user were removed.");
    }

    /**
     * Validates that the NoBlogManagerLeftException lists the correct blogs.
     *
     * @param e
     *            the exception
     * @param blogsToHandle
     *            used to store the IDs of the blogs that would become manager-less for further
     *            handling
     * @param bm
     *            the BlogManagement service
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    private void validateNoBlogManagerLeftException(NoBlogManagerLeftException e,
            Long[] blogsToHandle, BlogManagement bm) throws BlogAccessException {
        // test that the correct blogs are returned
        Set<Long> blogIds = e.getBlogIdsToTitleMapping().keySet();
        Assert.assertEquals(blogIds.size(), 2,
                "User is not last manager of expected number of blogs.");
        int i = 0;
        Long idBlog1 = bm.findBlogByIdentifier(TEST_DEL_USER_2_BLOG_ALIAS).getId();
        Long idBlog2 = bm.findBlogByIdentifier(TEST_DEL_USER_2_USED_BLOG_ALIAS).getId();
        for (Long id : blogIds) {
            boolean expectedBlog = id.equals(idBlog1) || id.equals(idBlog2);
            Assert.assertTrue(expectedBlog, "Blog " + id
                    + " was falsly categorized as blog with no other manager left.");
            blogsToHandle[i] = id;
            i++;
        }
    }

    /**
     * Validate that the user anonymization was successful by checking that there is no user with
     * the provided alias and email address.
     *
     * @param email
     *            the email address
     * @param alias
     *            the alias
     */
    private void validateUserAnonymizationSuccess(String email, String alias) {
        User user = userManagement.findUserByAlias(alias);
        Assert.assertNull(user, "User must not be retrievable by alias after anonymization.");
        user = userManagement.findUserByEmail(email);
        Assert.assertNull(user, "User must not be retrievable by email after anonymization.");
    }

    /**
     * Validate that the user permanently disabling was successful by checking that there is no user
     * with the provided email address, but that a user with the alias still exists.
     *
     * @param email
     *            the email address
     * @param alias
     *            the alias
     */
    private void validateUserPermanentlyDisableSuccess(String email, String alias) {
        User user = userManagement.findUserByAlias(alias);
        Assert.assertEquals(user.getAlias(), alias,
                "User must still be retrievable by alias after permanently disabling.");
        user = userManagement.findUserByEmail(email);
        Assert.assertNull(user,
                "User must not be retrievable by email after permanently disabling.");
    }
}
