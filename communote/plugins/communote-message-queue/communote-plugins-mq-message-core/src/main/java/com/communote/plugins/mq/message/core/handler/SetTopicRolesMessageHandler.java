package com.communote.plugins.mq.message.core.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.mq.message.base.handler.CommunoteMessageHandler;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.message.core.data.role.ExternalTopicRole;
import com.communote.plugins.mq.message.core.handler.converter.ExternalTopicRoleToExternalTopicRoleTOConverter;
import com.communote.plugins.mq.message.core.handler.exception.NoTopicSpecifiedException;
import com.communote.plugins.mq.message.core.message.topic.SetTopicRolesMessage;
import com.communote.plugins.mq.message.core.util.TopicUtils;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.core.external.BlogRightsSynchronizer;
import com.communote.server.core.vo.external.ExternalTopicRoleTO;

/**
 * set topic roles message handler
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provides(specifications = CommunoteMessageHandler.class)
@Component
@Instantiate
public class SetTopicRolesMessageHandler extends
        CommunoteMessageHandler<SetTopicRolesMessage> {

    /** The LOG. */
    private static Logger LOG = LoggerFactory
            .getLogger(SetTopicRolesMessageHandler.class);

    /**
     * @param topicId
     *            ID of the topic
     * @param externalSystemId
     *            external system id
     * @return created synchronizer
     */
    protected BlogRightsSynchronizer createBlogRightsSynchronizer(Long topicId,
            String externalSystemId) {
        return new BlogRightsSynchronizer(topicId, externalSystemId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<SetTopicRolesMessage> getHandledMessageClass() {
        return SetTopicRolesMessage.class;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public CommunoteReplyMessage handleMessage(SetTopicRolesMessage message)
            throws BlogNotFoundException, BlogAccessException {
        validateMessage(message);
        ExternalTopicRole[] roles = message.getRoles();
        if (roles != null) {
            ExternalTopicRoleToExternalTopicRoleTOConverter converter;
            converter = new ExternalTopicRoleToExternalTopicRoleTOConverter();
            List<ExternalTopicRoleTO> externalTopicRoleTOs = new ArrayList<ExternalTopicRoleTO>();
            for (ExternalTopicRole externalTopicRole : roles) {
                ExternalTopicRoleTO role = converter.convert(externalTopicRole);
                // don't add roles with null value since the synchronizer doesn't support this, and
                // it will work like a remove anyway
                if (role.getRole() != null) {
                    externalTopicRoleTOs.add(role);
                } else {
                    LOG.debug("Ignoring role with null value");
                }
            }
            try {
                createBlogRightsSynchronizer(TopicUtils.getTopicId(message.getTopic()),
                        message.getExternalSystemId()).replaceRights(externalTopicRoleTOs);
            } catch (BlogAccessException e) {
                LOG.error("Cannot access topic with ID " + message.getTopic().getTopicId());
                throw e;
            } catch (BlogNotFoundException e) {
                LOG.error("Topic with ID " + message.getTopic().getTopicId() + " not found");
                throw e;
            }
        }
        return null;
    }

    /**
     * @param message
     *            to be validated
     * @throws NoTopicSpecifiedException
     *             exception
     */
    public void validateMessage(SetTopicRolesMessage message)
            throws NoTopicSpecifiedException {
        if (message.getTopic() == null) {
            throw new NoTopicSpecifiedException();
        }
    }

}
