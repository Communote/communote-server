package com.communote.plugins.mq.message.core.handler;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.mq.message.base.handler.CommunoteMessageHandler;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.message.core.data.topic.BaseTopic;
import com.communote.plugins.mq.message.core.handler.exception.NoTopicIdentifierSpecifiedException;
import com.communote.plugins.mq.message.core.handler.exception.NoTopicSpecifiedException;
import com.communote.plugins.mq.message.core.message.topic.DeleteTopicMessage;
import com.communote.plugins.mq.message.core.util.TopicUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;

/**
 * Handler for delete topic message
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provides(specifications = CommunoteMessageHandler.class)
@Component
@Instantiate
public class DeleteTopicMessageHandler extends CommunoteMessageHandler<DeleteTopicMessage> {

    /** The LOG. */
    private static Logger LOG = LoggerFactory.getLogger(DeleteTopicMessageHandler.class);

    private BlogManagement blogManagement;

    /**
     * @return blog management
     */
    private BlogManagement getBlogManagement() {
        if (blogManagement == null) {
            blogManagement = ServiceLocator.instance().getService(BlogManagement.class);
        }
        return blogManagement;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.communote.plugins.mq.service.message.CntMessageHandler# getHandledMessageClass()
     */
    @Override
    public Class<DeleteTopicMessage> getHandledMessageClass() {
        return DeleteTopicMessage.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.communote.plugins.mq.service.message.CntMessageHandler#getVersion()
     */
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommunoteReplyMessage handleMessage(DeleteTopicMessage message)
            throws NoteManagementAuthorizationException, BlogNotFoundException, BlogAccessException {
        validateMessage(message);
        try {
            getBlogManagement().deleteBlog(TopicUtils.getTopicId(message.getTopic()), null);
        } catch (NoteManagementAuthorizationException | BlogAccessException e) {
            LOG.error("The Blog (\"" + message.getTopic().getTopicAlias()
                    + "\") cannot be removed, since the user is not the manager of the blog. ");
            throw e;
        } catch (BlogNotFoundException e) {
            LOG.error("The Blog to be deleted could not be found.", e);
            throw e;
        }
        return null;
    }

    /**
     * checks whether any of topic identification means are provided
     *
     * @param topic
     *            to be chcked
     * @return true if at least one identification mean was provided, false otherwise
     */
    public boolean isTopicIdentifierAvailble(BaseTopic topic) {
        return topic.getTopicId() != null && topic.getTopicId() != 0
                || topic.getTopicAlias() != null && !topic.getTopicAlias().equals("");
    }

    /**
     * only for test purposes
     *
     * @param blogManagement
     *            blog management
     */
    void setBlogManagement(BlogManagement blogManagement) {
        this.blogManagement = blogManagement;
    }

    /**
     * @param message
     *            message to be validated
     * @throws NoTopicSpecifiedException
     *             exception
     * @throws NoTopicIdentifierSpecifiedException
     *             exception
     */
    public void validateMessage(DeleteTopicMessage message) throws NoTopicSpecifiedException,
            NoTopicIdentifierSpecifiedException {
        if (message.getTopic() == null) {
            throw new NoTopicSpecifiedException();
        } else if (!isTopicIdentifierAvailble(message.getTopic())) {
            throw new NoTopicIdentifierSpecifiedException();
        }
    }

}
