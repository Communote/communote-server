package com.communote.server.core.user.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.events.UserStatusChangedEvent;
import com.communote.server.model.user.UserStatus;
import com.communote.server.service.NavigationItemService;

/**
 * Event handler for the UserStatusChangedEvent which creates the built-in navigation items if the
 * user provided by the event was activated.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CreateBuiltInNavigationItemsOnUserActivation implements
        EventListener<UserStatusChangedEvent> {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(CreateBuiltInNavigationItemsOnUserActivation.class);

    @Override
    public Class<UserStatusChangedEvent> getObservedEvent() {
        return UserStatusChangedEvent.class;
    }

    @Override
    public void handle(UserStatusChangedEvent event) {
        // if the user was activated create the built-in items. If the user was temp. disabled we
        // also try to create the built-in items since the db-update does not handle these users. If
        // the items already exist nothing will happen.
        if (UserStatus.ACTIVE.equals(event.getNewStatus())) {
            SecurityContext currentContext = AuthenticationHelper
                    .setInternalSystemToSecurityContext();
            try {
                ServiceLocator.findService(NavigationItemService.class)
                        .createBuiltInNavigationItems(event.getUserId());
            } catch (AuthorizationException e) {
                // should not occur
                LOGGER.error("Unexpected exception creating built-in navigation items");
            } finally {
                AuthenticationHelper.setSecurityContext(currentContext);
            }
        }
    }

}
