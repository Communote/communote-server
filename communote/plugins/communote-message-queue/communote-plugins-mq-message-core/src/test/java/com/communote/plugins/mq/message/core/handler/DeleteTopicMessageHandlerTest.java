package com.communote.plugins.mq.message.core.handler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.testng.annotations.Test;

import com.communote.plugins.mq.message.core.data.topic.BaseTopic;
import com.communote.plugins.mq.message.core.message.topic.DeleteTopicMessage;
import com.communote.server.api.core.blog.BlogManagement;

/**
 * test class for DeleteTopicMessageHandler
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DeleteTopicMessageHandlerTest {

    /**
     * @throws Exception
     *             excepttion
     */
    @Test
    public void testHandleMessage() throws Exception {
        long testBlogId = 2;

        BlogManagement blogManagement = createMock(BlogManagement.class);
        blogManagement.deleteBlog(testBlogId, null);

        replay(blogManagement);

        BaseTopic topic = new BaseTopic();
        topic.setTopicId(testBlogId);
        DeleteTopicMessage message = new DeleteTopicMessage();
        message.setTopic(topic);

        DeleteTopicMessageHandler handler = new DeleteTopicMessageHandler();
        handler.setBlogManagement(blogManagement);
        handler.handleMessage(message);

        verify(blogManagement);
    }
}
