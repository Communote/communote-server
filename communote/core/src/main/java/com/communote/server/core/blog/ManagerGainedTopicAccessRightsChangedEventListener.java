package com.communote.server.core.blog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.communote.common.converter.CollectionConverter;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.core.blog.events.ManagerGainedTopicAccessRightsChangedEvent;
import com.communote.server.core.blog.export.PermalinkGenerationManagement;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.mail.MailMessageHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.UserToBlogRoleMapping;
import com.communote.server.model.user.User;

/**
 * Event donating that a manager granted themself management access to a topic.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class ManagerGainedTopicAccessRightsChangedEventListener implements
        EventListener<ManagerGainedTopicAccessRightsChangedEvent> {

    private final BlogManagement topicManagement;
    private final BlogRightsManagement topicRightsManagement;
    private final PermalinkGenerationManagement permalinkGenerationManagement;
    private final UserManagement userManagement;

    /**
     * Constructor.
     * 
     * @param topicManagement
     *            The topic management.
     * @param topicRightsManagement
     *            The topic rights management.
     * @param permalinkGenerationManagement
     *            permalink generator.
     * @param userManagement
     *            The user management.
     * @param eventDispatcher
     *            The event dispatcher to register to.
     */
    @Autowired
    public ManagerGainedTopicAccessRightsChangedEventListener(BlogManagement topicManagement,
            BlogRightsManagement topicRightsManagement,
            PermalinkGenerationManagement permalinkGenerationManagement,
            UserManagement userManagement, EventDispatcher eventDispatcher) {
        this.topicManagement = topicManagement;
        this.topicRightsManagement = topicRightsManagement;
        this.permalinkGenerationManagement = permalinkGenerationManagement;
        this.userManagement = userManagement;
        eventDispatcher.register(this);
    }

    /**
     * @return ManagerGainedTopicAccessRightsChangedEvent.class
     */
    @Override
    public Class<ManagerGainedTopicAccessRightsChangedEvent> getObservedEvent() {
        return ManagerGainedTopicAccessRightsChangedEvent.class;
    }

    /**
     * Sends a mail to all other managers.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void handle(ManagerGainedTopicAccessRightsChangedEvent event) {
        Blog topic;
        try {
            topic = topicManagement.getBlogById(event.getTopicId(), false);
        } catch (BlogNotFoundException e) {
            throw BlogManagementHelper.convertException(e);
        } catch (BlogAccessException e) {
            throw BlogManagementHelper.convertException(e);
        }
        User manager = userManagement
                .findUserByUserId(event.getGrantingUserId(), false);
        Collection<User> receivers = topicRightsManagement.getMappedUsers(event.getTopicId(),
                new CollectionConverter<UserToBlogRoleMapping, User>() {
                    @Override
                    public User convert(UserToBlogRoleMapping source) {
                        return userManagement.findUserByUserId(source.getUserId(), false);
                    }
                }, BlogRole.MANAGER);
        receivers.remove(manager);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("topicName", topic.getTitle());
        model.put("topicUrl", permalinkGenerationManagement.getBlogLink(topic.getNameIdentifier()));
        model.put("administratorSignature", UserNameHelper.getSimpleDefaultUserSignature(manager));
        MailMessageHelper.sendMessage(receivers, "mail.message.topic.gain-access", model);
    }
}
