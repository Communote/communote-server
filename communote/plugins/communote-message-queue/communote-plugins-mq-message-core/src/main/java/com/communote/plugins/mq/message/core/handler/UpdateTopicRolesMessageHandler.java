package com.communote.plugins.mq.message.core.handler;

import java.util.ArrayList;

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
import com.communote.plugins.mq.message.core.message.topic.UpdateTopicRolesMessage;
import com.communote.plugins.mq.message.core.util.TopicUtils;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.core.external.BlogRightsSynchronizer;
import com.communote.server.core.vo.external.ExternalTopicRoleTO;

/**
 * Update topic roles message handler
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provides(specifications = CommunoteMessageHandler.class)
@Component
@Instantiate
public class UpdateTopicRolesMessageHandler extends
CommunoteMessageHandler<UpdateTopicRolesMessage> {

    /** The LOG. */
    private static Logger LOGGER = LoggerFactory.getLogger(UpdateTopicRolesMessageHandler.class);

    /**
     * Create a synchronizer for external topic roles
     *
     * @param topicId
     *            the ID of the topic
     * @param externalSystemId
     *            the ID of the external system
     * @return the synchronizer
     */
    protected BlogRightsSynchronizer createBlogRightsSynchronizer(Long topicId,
            String externalSystemId) {
        return new BlogRightsSynchronizer(topicId, externalSystemId);
    }

    @Override
    public Class<UpdateTopicRolesMessage> getHandledMessageClass() {
        return UpdateTopicRolesMessage.class;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public CommunoteReplyMessage handleMessage(UpdateTopicRolesMessage message)
            throws BlogNotFoundException, BlogAccessException {

        validateMessage(message);

        ExternalTopicRole[] roles = message.getRoles();
        if (roles != null) {
            ExternalTopicRoleToExternalTopicRoleTOConverter converter;
            converter = new ExternalTopicRoleToExternalTopicRoleTOConverter();
            ArrayList<ExternalTopicRoleTO> rolesToAddAndUpdate = new ArrayList<ExternalTopicRoleTO>();
            ArrayList<ExternalTopicRoleTO> rolesToRemove = new ArrayList<ExternalTopicRoleTO>();
            for (ExternalTopicRole topicRoleObject : roles) {
                ExternalTopicRoleTO convertedRole = converter.convert(topicRoleObject);
                // null value for role derives from NONE and should be interpreted as a remove
                if (convertedRole.getRole() == null) {
                    rolesToRemove.add(convertedRole);
                } else {
                    rolesToAddAndUpdate.add(convertedRole);
                }
            }
            try {
                Long topicId = TopicUtils.getTopicId(message.getTopic());

                BlogRightsSynchronizer synchronizer = createBlogRightsSynchronizer(topicId,
                        message.getExternalSystemId());
                synchronizer.mergeRights(rolesToAddAndUpdate, rolesToRemove);
            } catch (BlogAccessException e) {
                LOGGER.error("Cannot access topic with ID " + message.getTopic().getTopicId());
                throw e;
            } catch (BlogNotFoundException e) {
                LOGGER.error("Topic with ID " + message.getTopic().getTopicId() + " not found");
                throw e;
            }
        } else {
            LOGGER.debug("No roles provided");
        }

        return null;
    }

    /**
     * @param message
     *            to be validated
     * @throws NoTopicSpecifiedException
     *             exception
     */
    public void validateMessage(UpdateTopicRolesMessage message) throws NoTopicSpecifiedException {
        if (message.getTopic() == null) {
            throw new NoTopicSpecifiedException();
        }
    }
}
