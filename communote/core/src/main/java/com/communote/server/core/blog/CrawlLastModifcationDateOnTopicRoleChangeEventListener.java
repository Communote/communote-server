package com.communote.server.core.blog;

import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.core.blog.events.UserToTopicRoleMappingChangedEvent;
import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;

/**
 * Event donating that a manager granted himself management access to a topic.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class CrawlLastModifcationDateOnTopicRoleChangeEventListener implements
EventListener<UserToTopicRoleMappingChangedEvent> {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(CrawlLastModifcationDateOnTopicRoleChangeEventListener.class);
    private final BlogManagement topicManagement;

    private final BlogRightsManagement topicRightsManagement;

    /**
     * Constructor.
     *
     * @param topicManagement
     *            The topic management.
     * @param topicRightsManagement
     *            The topic rights management.
     * @param eventDispatcher
     *            The event dispatcher to register to.
     */
    @Autowired
    public CrawlLastModifcationDateOnTopicRoleChangeEventListener(
            BlogManagement topicManagement,
            BlogRightsManagement topicRightsManagement,
            EventDispatcher eventDispatcher) {
        this.topicManagement = topicManagement;
        this.topicRightsManagement = topicRightsManagement;
        eventDispatcher.register(this);
    }

    /**
     * @return UserToTopicRoleMappingChangedEvent.class
     */
    @Override
    public Class<UserToTopicRoleMappingChangedEvent> getObservedEvent() {
        return UserToTopicRoleMappingChangedEvent.class;
    }

    /**
     * Sends a mail to all other managers.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void handle(UserToTopicRoleMappingChangedEvent event) {

        if (event.getNewRoleOfMapping() == event.getOldRoleOfMapping()
                || event.getNewRoleOfMapping() != null
                && event.getNewRoleOfMapping().equals(event.getOldRoleOfMapping())) {
            // the role did not change, (only the granting group) so nothing to do
            return;
        }

        BlogRole currentRole = this.topicRightsManagement.getRoleOfEntity(event.getTopicId(),
                event.getEntityId(), false);

        int currentRoleNum = currentRole == null ? 0 : BlogRoleHelper
                .convertRoleToNumeric(currentRole);
        int beforeRoleNum = event.getBeforeTopicRole() == null ? 0 : BlogRoleHelper
                .convertRoleToNumeric(event.getBeforeTopicRole());

        if (currentRoleNum > beforeRoleNum || (currentRoleNum == 0 && beforeRoleNum != 0)) {
            Blog topic;
            try {
                topic = topicManagement.getBlogById(event.getTopicId(), false);

                topic.setCrawlLastModificationDate(new Timestamp(new Date().getTime()));

            } catch (BlogNotFoundException e) {
                LOGGER.warn("Topic not found for but event got fired? topicId="
                        + event.getTopicId() + " event=" + event + " " + e.getMessage());
            } catch (BlogAccessException e) {
                LOGGER.error("Error accessing topic but event got fired? topicId="
                        + event.getTopicId() + " event=" + event + " " + e.getMessage());
            }
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "Final role not change, no need to change last modification date of topic. currentRole={} beforeRole={} event={}",
                        currentRole, event.getBeforeTopicRole(), event);
            }
        }
    }
}
