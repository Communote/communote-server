package com.communote.server.core.user.listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.core.common.LimitHelper;
import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.messages.user.NotifyUserCountLimitReachedMailMessage;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.user.events.UserStatusChangedEvent;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Notifies client managers if the user limit was reached or is about to be reached after user
 * activations.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserLimitNotificationOnUserActivation implements EventListener<UserStatusChangedEvent> {

    private UserManagement userMangement;
    private MailManagement mailMangement;

    /**
     * An active user was removed, resets the user limit flags
     */
    private void activeUserWasRemoved() {
        long activeUserCountLimit = UserManagementHelper.getCountLimit();
        if (activeUserCountLimit > 0) {
            long activeUserCount = getUserMangement().getActiveUserCount();
            if (activeUserCount < activeUserCountLimit) {
                Map<ClientConfigurationPropertyConstant, String> settings = new HashMap<>();
                if (isAutomaticUserActivationChanged()) {
                    settings.put(ClientProperty.USER_MANAGEMENT_USER_LIMIT_ACTIVATION_CHANGED,
                            Boolean.FALSE.toString());
                    settings.put(ClientProperty.AUTOMATIC_USER_ACTIVATION,
                            String.valueOf(Boolean.TRUE.toString()));
                }
                if (activeUserCount < activeUserCountLimit) {
                    settings.put(ClientProperty.USER_MANAGEMENT_USER_LIMIT_100_MAIL_SENT,
                            Boolean.FALSE.toString());
                }
                if (activeUserCount < activeUserCountLimit * 0.9F) {
                    settings.put(ClientProperty.USER_MANAGEMENT_USER_LIMIT_90_MAIL_SENT,
                            Boolean.FALSE.toString());
                }
                CommunoteRuntime.getInstance().getConfigurationManager()
                .updateClientConfigurationProperties(settings);
            }
        }
    }

    /**
     * Check if the user limit is reached or nearly reached (90%). If so send specific emails.
     */
    private synchronized void checkUserLimitReached() {
        long activeUserCountLimit = UserManagementHelper.getCountLimit();
        if (activeUserCountLimit <= 0) { // 0 or less means no limit
            return;
        }

        ConfigurationManager propertiesManager = CommunoteRuntime.getInstance()
                .getConfigurationManager();
        long activeUserCount = getUserMangement().getActiveUserCount();
        if (activeUserCount >= activeUserCountLimit) {
            boolean mail100Send = ClientProperty.USER_MANAGEMENT_USER_LIMIT_100_MAIL_SENT
                    .getValue(false);
            Map<ClientConfigurationPropertyConstant, String> settings = new HashMap<>();
            if (!mail100Send) {
                if (UserManagementHelper.isAutomaticUserActivation()) {
                    settings.put(ClientProperty.USER_MANAGEMENT_USER_LIMIT_ACTIVATION_CHANGED,
                            Boolean.TRUE.toString());
                    settings.put(ClientProperty.AUTOMATIC_USER_ACTIVATION, Boolean.FALSE.toString());
                }
                settings.put(ClientProperty.USER_MANAGEMENT_USER_LIMIT_100_MAIL_SENT,
                        Boolean.TRUE.toString());
                propertiesManager.updateClientConfigurationProperties(settings);
                sendLimitReachedMail(activeUserCount, activeUserCountLimit);
            }
        } else if (activeUserCount >= activeUserCountLimit * 0.9F) {
            boolean count90mail = ClientProperty.USER_MANAGEMENT_USER_LIMIT_90_MAIL_SENT
                    .getValue(false);
            if (!count90mail) {
                propertiesManager.updateClientConfigurationProperty(
                        ClientProperty.USER_MANAGEMENT_USER_LIMIT_90_MAIL_SENT,
                        Boolean.TRUE.toString());
                sendLimitReachedMail(activeUserCount, activeUserCountLimit);
            }
        }
    }

    private MailManagement getMailManagement() {
        if (mailMangement == null) {
            mailMangement = ServiceLocator.findService(MailManagement.class);
        }
        return mailMangement;
    }

    @Override
    public Class<UserStatusChangedEvent> getObservedEvent() {
        return UserStatusChangedEvent.class;
    }

    private UserManagement getUserMangement() {
        if (userMangement == null) {
            userMangement = ServiceLocator.findService(UserManagement.class);
        }
        return userMangement;
    }

    @Override
    public void handle(UserStatusChangedEvent event) {
        if (UserStatus.ACTIVE.equals(event.getNewStatus())) {
            checkUserLimitReached();
        } else if (UserStatus.ACTIVE.equals(event.getOldStatus())) {
            activeUserWasRemoved();
        }

    }

    /**
     * Checks if is automatic user activation was changed by the user limit controller.
     *
     * @return true, if is automatic user activation was changed
     */
    private boolean isAutomaticUserActivationChanged() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.USER_MANAGEMENT_USER_LIMIT_ACTIVATION_CHANGED, false);
    }

    /**
     * Send "Limit reached" email to all client manager, where they are also informed, if the
     * automatic user activation has been change due to reaching this limit.
     *
     * @param count
     *            the user count
     * @param limit
     *            the user limit
     */
    private void sendLimitReachedMail(long count, long limit) {
        List<User> clientManager = getUserMangement().findUsersByRole(
                UserRole.ROLE_KENMEI_CLIENT_MANAGER, UserStatus.ACTIVE);
        Map<Locale, Collection<User>> localizedUsers = UserManagementHelper
                .getUserByLocale(clientManager);
        for (Locale locale : localizedUsers.keySet()) {
            NotifyUserCountLimitReachedMailMessage message = new NotifyUserCountLimitReachedMailMessage(
                    localizedUsers.get(locale), locale, ClientHelper.getCurrentClient().getName(),
                    isAutomaticUserActivationChanged(), Math.round(LimitHelper.getCountPercent(
                            count, limit)), LimitHelper.getCountLimitAsString(limit));
            getMailManagement().sendMail(message);
        }
    }

}
