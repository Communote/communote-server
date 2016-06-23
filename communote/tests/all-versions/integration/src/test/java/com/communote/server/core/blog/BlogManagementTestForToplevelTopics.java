package com.communote.server.core.blog;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.ToplevelTopicCannotBeChildException;
import com.communote.server.core.blog.ToplevelTopicIsAlreadyChildBlogManagementException;
import com.communote.server.core.blog.ToplevelTopicsDisabledBlogManagementException;
import com.communote.server.core.vo.blog.TopicStructureTO;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.BlogDao;
import com.communote.server.service.TopicHierarchyService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogManagementTestForToplevelTopics extends CommunoteIntegrationTest {

    @Autowired
    private BlogManagement blogManagement;
    @Autowired
    private TopicHierarchyService topicHierarchyService;

    @Autowired
    private BlogDao topicDao;

    /**
     * This method tests the functionality for setting and unsetting a top level topic.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void test() throws Exception {
        ConfigurationManager properties = CommunoteRuntime.getInstance().getConfigurationManager();

        User clientManager = TestUtils.createRandomUser(true);
        User user = TestUtils.createRandomUser(false);

        Blog topicA = TestUtils.createRandomBlog(false, false, clientManager);
        Blog topicAA = TestUtils.createRandomBlog(false, false, clientManager);
        Blog topicB = TestUtils.createRandomBlog(false, false, clientManager);
        Blog topicC = TestUtils.createRandomBlog(false, false, user);

        AuthenticationTestUtils.setSecurityContext(clientManager);
        topicHierarchyService.addTopic(topicA.getId(), topicAA.getId());

        // Top level topics enabled
        properties.updateClientConfigurationProperty(ClientProperty.TOP_LEVEL_TOPICS_ENABLED,
                "true");

        TopicStructureTO structureTO = new TopicStructureTO();
        structureTO.setToplevel(true);
        topicHierarchyService.updateTopicStructure(topicA.getId(), structureTO);
        Assert.assertTrue(topicDao.load(topicA.getId()).isToplevelTopic());

        // Should not be possible to set a child topic as top level topic.
        try {
            topicHierarchyService.updateTopicStructure(topicAA.getId(), structureTO);
            Assert.fail("The topic is already a child and therefor can't be a top level topic.");
        } catch (ToplevelTopicIsAlreadyChildBlogManagementException e) {
            // Okay
        }

        // Top level topics may never be a child
        try {
            topicHierarchyService.addTopic(topicB.getId(), topicA.getId());
            Assert.fail("It should not be possible to at top level topic as child.");
        } catch (ToplevelTopicCannotBeChildException e) {
            // Okay.
        }

        // Only client managers should be able to set top level topics.
        AuthenticationTestUtils.setSecurityContext(user);
        try {
            topicHierarchyService.updateTopicStructure(topicC.getId(), structureTO);
            Assert.fail("Only client managers should be able to set top level topics.");
        } catch (AuthorizationException e) {
            // Okay
        }

        // Top level topics disabled
        AuthenticationTestUtils.setSecurityContext(clientManager);
        properties.updateClientConfigurationProperty(ClientProperty.TOP_LEVEL_TOPICS_ENABLED,
                "false");

        // Even if disabled, already defined top level topics should stay top level until explicitly
        // disabled.
        topicHierarchyService.updateTopicStructure(topicA.getId(), structureTO);
        Assert.assertTrue(topicDao.load(topicA.getId()).isToplevelTopic());

        // Removing the topic from top level is okay, if disabled.
        structureTO.setToplevel(false);
        topicHierarchyService.updateTopicStructure(topicA.getId(), structureTO);
        Assert.assertFalse(topicDao.load(topicA.getId()).isToplevelTopic());

        try {
            structureTO.setToplevel(true);
            topicHierarchyService.updateTopicStructure(topicA.getId(), structureTO);
            Assert.fail("Top level topics are disabled, so it should not be possible to set the topic as top level topic");
        } catch (ToplevelTopicsDisabledBlogManagementException e) {
            // Okay
        }

    }
}
