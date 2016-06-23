package com.communote.plugins.mq.message.core.handler;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.plugins.mq.message.core.data.role.ExternalTopicRole;
import com.communote.plugins.mq.message.core.data.topic.BaseTopic;
import com.communote.plugins.mq.message.core.data.user.BaseEntity;
import com.communote.plugins.mq.message.core.message.topic.UpdateTopicRolesMessage;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.core.external.BlogRightsSynchronizer;
import com.communote.server.core.vo.external.ExternalTopicRoleTO;
import com.communote.server.model.blog.BlogRole;

/**
 * 
 * TestUpdateTopicRolesMessageHandler
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UpdateTopicRolesMessageHandlerTest {

    /**
     * handler using mock message handler
     * 
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     */
    private class MockUpdateTopicRolesMessageHandler extends UpdateTopicRolesMessageHandler {
        @Override
        protected BlogRightsSynchronizer createBlogRightsSynchronizer(Long topicId,
                String externalSystemId) {

            return mockSynchronizer;
        }
    }

    private MockBlogRightsSynchronizer mockSynchronizer;
    private BaseTopic topic;
    private UpdateTopicRolesMessage message;

    /**
     * initMocks
     */
    @BeforeMethod
    public void initMocks() {
        topic = new BaseTopic();
        topic.setTopicId(2L);
        mockSynchronizer = new MockBlogRightsSynchronizer(topic.getTopicId(), "exSystem");

        mockSynchronizer.addRole(1L, false, BlogRole.MEMBER);
        mockSynchronizer.addRole(2L, false, BlogRole.VIEWER);
        mockSynchronizer.addRole(3L, true, BlogRole.MEMBER);
        mockSynchronizer.addRole(4L, false, BlogRole.VIEWER);

        message = new UpdateTopicRolesMessage();
        message.setRoles(null);
        message.setTopic(topic);
    }

    /**
     * 
     * testHandleMessageForManager
     * 
     * @throws BlogAccessException
     *             exception
     * @throws BlogNotFoundException
     *             exception
     */
    @Test
    public void testHandleMessage() throws BlogAccessException, BlogNotFoundException {
        ExternalTopicRole[] topicRoles = new ExternalTopicRole[4];

        // update entity 2 and 3
        topicRoles[0] = new ExternalTopicRole();
        topicRoles[0].setTopicRole("member");
        BaseEntity entity = new BaseEntity();
        entity.setEntityAlias("a2");
        topicRoles[0].setEntity(entity);
        topicRoles[1] = new ExternalTopicRole();
        topicRoles[1].setTopicRole("viewer");
        entity = new BaseEntity();
        entity.setExternalId("e3");
        entity.setIsGroup(true);
        topicRoles[1].setEntity(entity);
        // add another role for user 5
        topicRoles[2] = new ExternalTopicRole();
        topicRoles[2].setTopicRole("manager");
        entity = new BaseEntity();
        entity.setEntityId(5L);
        topicRoles[2].setEntity(entity);
        // remove user 4
        topicRoles[3] = new ExternalTopicRole();
        topicRoles[3].setTopicRole("none");
        entity = new BaseEntity();
        entity.setEntityId(4L);
        topicRoles[3].setEntity(entity);
        message.setRoles(topicRoles);

        UpdateTopicRolesMessageHandler handler = new MockUpdateTopicRolesMessageHandler();
        handler.handleMessage(message);

        List<ExternalTopicRoleTO> finalRoles = mockSynchronizer.getExternalTopicRoleTOs();
        Assert.assertEquals(finalRoles.size(), 4);
        Assert.assertEquals(finalRoles.get(0).getEntityId(), (Long) 1L);
        Assert.assertEquals(finalRoles.get(0).getRole(), BlogRole.MEMBER);
        Assert.assertEquals(finalRoles.get(1).getEntityId(), (Long) 2L);
        Assert.assertEquals(finalRoles.get(1).getRole(), BlogRole.MEMBER);
        Assert.assertEquals(finalRoles.get(2).getEntityId(), (Long) 3L);
        Assert.assertEquals(finalRoles.get(2).getRole(), BlogRole.VIEWER);
        Assert.assertEquals(finalRoles.get(3).getEntityId(), (Long) 5L);
        Assert.assertEquals(finalRoles.get(3).getRole(), BlogRole.MANAGER);
    }

}
