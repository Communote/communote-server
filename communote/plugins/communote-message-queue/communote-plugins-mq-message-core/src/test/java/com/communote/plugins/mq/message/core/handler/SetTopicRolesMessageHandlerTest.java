package com.communote.plugins.mq.message.core.handler;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.plugins.mq.message.core.data.role.ExternalTopicRole;
import com.communote.plugins.mq.message.core.data.topic.BaseTopic;
import com.communote.plugins.mq.message.core.message.topic.SetTopicRolesMessage;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.external.BlogRightsSynchronizer;
import com.communote.server.core.vo.external.ExternalTopicRoleTO;

/**
 * Test set topic roles with messages handler
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SetTopicRolesMessageHandlerTest {

    /**
     * BlogRightsSynchronizerMock for SetBulkTopicRoleMessageHandler
     * 
     */
    private class SetBulkTopicRoleMessageHandlerTestImpl extends
            SetTopicRolesMessageHandler {

        @Override
        protected BlogRightsSynchronizer createBlogRightsSynchronizer(Long topicId,
                String externalSystemId) {
            return blogRightsSynchronizerMock;
        }
    }

    private MockBlogRightsSynchronizer blogRightsSynchronizerMock;

    private BaseTopic topic;

    private SetTopicRolesMessage message;

    private final static String ENTITY_USER_ALIAS = "entity_user_alias";
    private final static String BASE_ENTITY_USER_ALIAS = "base_entity_user_alias";
    private final static String ENTITY_GROUP_ALIAS = "entity_group_alias";

    /**
     * Assert the topic role was set.
     * 
     * @param handler
     *            to set topic roles
     * @param externalTopicRole
     *            topic role
     * @param alias
     *            of entity
     * @throws BlogNotFoundException
     *             topic does not exists
     * @throws AuthorizationException
     *             user is not manager
     */
    private void assertTopicRole(SetTopicRolesMessageHandler handler,
            ExternalTopicRole externalTopicRole, String alias)
            throws BlogNotFoundException, AuthorizationException {
        ExternalTopicRole[] topicRoles = new ExternalTopicRole[] { externalTopicRole };
        message.setRoles(topicRoles);

        handler.handleMessage(message);

        Assert.assertEquals(blogRightsSynchronizerMock.getExternalTopicRoleTOs().size(), 1);

        ExternalTopicRoleTO externalTopicRoleTO = blogRightsSynchronizerMock
                .getExternalTopicRoleTOs().get(0);
        Assert.assertEquals(externalTopicRoleTO.getEntityAlias(), alias);
        Assert.assertEquals(externalTopicRoleTO.getExternalObjectId(),
                externalTopicRole.getExternalObjectId());
        message.setRoles(null);
    }

    /**
     * Initialize some objects and the blogRightsSynchronizerMock
     */
    @BeforeMethod
    public void initMocks() {

        topic = new BaseTopic();
        topic.setTopicId(1L);

        message = new SetTopicRolesMessage();
        message.setTopic(topic);

        blogRightsSynchronizerMock = new MockBlogRightsSynchronizer(topic.getTopicId(),
                "exSystemid");
    }

    /**
     * Test to send an message with entity as user, as group and with base entity.
     * 
     * @throws AuthorizationException
     *             user is no manager
     * @throws BlogNotFoundException
     *             topic was not found
     */
    @Test
    public void testHandleMessage() throws BlogNotFoundException, AuthorizationException {

        SetTopicRolesMessageHandler handler = new SetBulkTopicRoleMessageHandlerTestImpl();

        ExternalTopicRole externalTopicRole1 = new ExternalTopicRole();
        externalTopicRole1.setExternalObjectId("exObjId");
        externalTopicRole1.setTopicRole("manager");
        externalTopicRole1.getEntity().setEntityAlias(ENTITY_USER_ALIAS);

        assertTopicRole(handler, externalTopicRole1, ENTITY_USER_ALIAS);

        ExternalTopicRole externalTopicRole2 = new ExternalTopicRole();
        externalTopicRole2.setExternalObjectId("exObjId");
        externalTopicRole2.setTopicRole("member");
        externalTopicRole2.getEntity().setEntityAlias(ENTITY_GROUP_ALIAS);
        externalTopicRole2.getEntity().setIsGroup(true);

        assertTopicRole(handler, externalTopicRole2, ENTITY_GROUP_ALIAS);

        // test with base entity
        ExternalTopicRole externalTopicRole3 = new ExternalTopicRole();
        externalTopicRole3.setExternalObjectId("exObjId");
        externalTopicRole3.setTopicRole("viewer");
        externalTopicRole3.getEntity().setEntityAlias(BASE_ENTITY_USER_ALIAS);

        assertTopicRole(handler, externalTopicRole3, BASE_ENTITY_USER_ALIAS);

    }

}
