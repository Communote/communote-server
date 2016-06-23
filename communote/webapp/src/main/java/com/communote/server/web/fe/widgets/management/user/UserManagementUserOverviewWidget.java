package com.communote.server.web.fe.widgets.management.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.UnexpectedRollbackException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.common.exceptions.InvalidOperationException;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.user.AliasAlreadyExistsException;
import com.communote.server.core.user.AliasInvalidException;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.InvalidUserStatusTransitionException;
import com.communote.server.core.user.NoClientManagerLeftException;
import com.communote.server.core.user.UserAuthorityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementException;
import com.communote.server.core.user.validation.UserActivationValidationException;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.group.GroupDao;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.widgets.management.user.GroupItem.GroupItemComparator;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget for managing users.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserManagementUserOverviewWidget extends AbstractWidget {

    /**
     * Saves the user settings within a transaction.
     */
    private class SaveInTransaction implements RunInTransaction {

        /**
         * {@inheritDoc}
         */

        @Override
        public void execute() throws TransactionException {
            String userIdAsString = getParameter(PARAM_USER_ID);

            if (StringUtils.isBlank(userIdAsString) || !StringUtils.isNumeric(userIdAsString)) {
                MessageHelper.saveErrorMessageFromKey(getRequest(), MessageHelper.getText(
                        getRequest(), "client.user.management.error.user.not.found",
                        new Object[] { userIdAsString }));
                LOG.warn("Tried access to unknown user: " + userIdAsString);
                return;
            }

            long userId = Long.parseLong(userIdAsString);
            UserManagement userManagement = ServiceLocator.findService(UserManagement.class);

            try {
                if (!saveUserStatus(userId, UserStatus.fromString(getParameter(PARAMETER_STATUS)))) {
                    return;
                }
            } catch (IllegalArgumentException e) {
                LOG.warn("Tried to set unknown status:" + e.getMessage());
            }

            String isAdministrator = getParameter(PARAMETER_IS_ADMINISTRATOR);
            try {
                if (StringUtils.isNotBlank(isAdministrator)) {
                    userManagement.assignUserRole(userId, UserRole.ROLE_KENMEI_CLIENT_MANAGER);
                } else {
                    try {
                        userManagement.removeUserRole(userId, UserRole.ROLE_KENMEI_CLIENT_MANAGER);
                    } catch (NoClientManagerLeftException e) {
                        MessageHelper.saveErrorMessageFromKey(getRequest(),
                                "widget.userManagementUserOverview.changerole.error.manager");
                        LOG.warn("Error removing manager role: " + e.getMessage());
                        return;
                    }
                }
            } catch (AuthorizationException e) {
                MessageHelper.saveErrorMessageFromKey(getRequest(),
                        "widget.userManagementUserOverview.changerole.error.auth");
            } catch (InvalidOperationException e) {
                MessageHelper.saveErrorMessageFromKey(getRequest(),
                        "widget.userManagementUserOverview.changerole.error.invalid");
            }
            updateEmailAdress(userManagement, userId, getParameter(PARAMETER_USER_EMAIL));
            updateAlias(userManagement, userId, getParameter(PARAMETER_USER_ALIAS));
            MessageHelper.saveMessageFromKey(getRequest(), "client.user.management.save.success");
        }

        /**
         * Saves the user status.
         *
         * @param userId
         *            the user Id.
         * @param status
         *            the status.
         * @return True, when all settings where successfully saved.
         */
        private boolean saveUserStatus(long userId, UserStatus status) {
            User user = ServiceLocator.findService(UserManagement.class).findUserByUserId(userId);
            if (user == null) {
                MessageHelper.saveErrorMessageFromKey(getRequest(),
                        "client.user.management.error.user.not.found");
                return false;
            }
            try {
                return saveUserStatus(user, status);
            } catch (AuthorizationException e) {
                LOG.error("user with id " + userId + " not found on calling activateUser");
                MessageHelper.saveErrorMessageFromKey(getRequest(),
                        "client.user.management.activate.user.error.not.admin", user.getAlias());
            }
            return false;
        }

        /**
         * @param user
         *            The user.
         * @param status
         *            The status.
         * @return True, if saved else false.
         * @throws AuthorizationException
         *             Exception.
         */
        private boolean saveUserStatus(User user, UserStatus status) throws AuthorizationException {
            try {
                if (!user.getStatus().equals(status)
                        && (status == UserStatus.ACTIVE || status == UserStatus.TEMPORARILY_DISABLED)) {
                    ServiceLocator.findService(UserManagement.class).changeUserStatusByManager(
                            user.getId(), status);
                }
                return true;
            } catch (InvalidUserStatusTransitionException e) {
                LOG.error("activate user failed, " + e.getMessage());
                MessageHelper.saveErrorMessageFromKey(getRequest(),
                        "client.user.management.change.user.status.notAllowed");
            } catch (NoClientManagerLeftException e) {
                LOG.error("NoClientManagerLeftException while trying to activate user with id "
                        + user.getId());
                MessageHelper.saveErrorMessageFromKey(getRequest(),
                        "widget.user.management.profile.action.disable.no.admin.left",
                        user.getAlias());
            } catch (UserNotFoundException e) {
                LOG.error("user with id " + user.getId() + " not found on calling activateUser");
                MessageHelper.saveErrorMessageFromKey(getRequest(),
                        "client.user.management.error.user.not.found", user.getAlias());
            } catch (UserManagementException e) {
                LOG.error("activating user with id " + user.getId() + " failed", e);
                MessageHelper
                .saveErrorMessageFromKey(getRequest(),
                        "client.user.management.activate.user.error.not.activated",
                        user.getAlias());
            } catch (UserActivationValidationException e) {
                LOG.error("User limit reached.", e);
                MessageHelper
                .saveErrorMessage(
                        getRequest(),
                        e.getReason("client.user.management.activate.user.error.",
                                user.getAlias()));
            }
            return false;
        }

        /**
         * This method changes the users alias
         *
         * @param userManagement
         *            User management to use.
         * @param userId
         *            Id of the user, who should be changed.
         * @param newAlias
         *            The possible new address.
         */
        private void updateAlias(UserManagement userManagement, Long userId, String newAlias) {
            if (newAlias == null || newAlias.isEmpty()) {
                return;
            }
            try {
                if (userManagement.changeAlias(userId, newAlias)) {
                    MessageHelper.saveMessageFromKey(getRequest(),
                            "client.user.profile.alias.success");
                }
            } catch (AliasAlreadyExistsException e) {
                MessageHelper.saveErrorMessageFromKey(getRequest(), "error.alias.already.exists");
            } catch (AliasInvalidException e) {
                MessageHelper.saveErrorMessageFromKey(getRequest(), "error.alias.not.valid");
            }
        }

        /**
         * This method changes the users email address if it differs from the old address.
         *
         * @param userManagement
         *            User management to use.
         * @param userId
         *            Id of the user, who should be changed.
         * @param newEmailAddress
         *            The possible new address.
         */
        private void updateEmailAdress(UserManagement userManagement, Long userId,
                String newEmailAddress) {
            if (newEmailAddress == null || newEmailAddress.isEmpty()) {
                return;
            }
            try {
                if (userManagement.changeEmailAddress(userId, newEmailAddress, false)) {
                    MessageHelper.saveMessageFromKey(getRequest(),
                            "client.user.profile.email.success");
                }
            } catch (EmailAlreadyExistsException e) {
                MessageHelper.saveErrorMessageFromKey(getRequest(), "error.email.already.exists");
            } catch (EmailValidationException e) {
                MessageHelper.saveErrorMessageFromKey(getRequest(), "error.email.not.valid");
            }
        }
    }

    private static final String PARAMETER_USER_EMAIL = "userEmail";

    private static final String PARAMETER_USER_ALIAS = "userAlias";

    /** Logger. */
    private final static Logger LOG = LoggerFactory
            .getLogger(UserManagementUserOverviewWidget.class);

    /** User Id */
    public static final String PARAM_USER_ID = "userId";

    /** Defines the update action. */
    public static final String ACTION_UPDATE = "updateFilter";

    /** Defines the delete action. */
    public static final String ACTION_DELETE = "deleteFilter";

    /** Parameter name for submit. */
    public static final String PARAMETER_SUBMIT = "submit";

    /** Parameter name for status. */
    public static final String PARAMETER_STATUS = "status";

    /** Parameter name for action. */
    public static final String PARAMETER_ACTION = "action";

    /** Parameter name for is admin. */
    public static final String PARAMETER_IS_ADMINISTRATOR = "isAdministrator";

    /**
     * {@inheritDoc}
     */

    @Override
    public String getTile(String outputType) {
        return "widget.client.management.user.overview." + outputType;
    }

    /**
     * Returns the user.
     *
     * @return The user or null if no one was the selected.
     */
    private User getUser() {
        Long userId = getLongParameter(PARAM_USER_ID, 0);
        if (userId == 0) {
            return null;
        }
        User user = ServiceLocator.instance().getService(UserManagement.class)
                .findUserByUserId(userId);

        List<GroupItem> usersGroups = new ArrayList<GroupItem>();
        for (Group group : ServiceLocator.findService(GroupDao.class).getGroupsOfUser(userId)) {
            GroupItem groupItem = new GroupItem();
            groupItem.setGroup(group);
            groupItem.setIsExternal(group instanceof ExternalUserGroup);
            usersGroups.add(groupItem);
        }
        Collections.sort(usersGroups, new GroupItemComparator());

        getRequest().setAttribute("userId", user.getId());
        getRequest().setAttribute(PARAMETER_USER_ALIAS, user.getAlias());
        getRequest().setAttribute("userFirstName", user.getProfile().getFirstName());
        getRequest().setAttribute("userLastName", user.getProfile().getLastName());

        getRequest().setAttribute("userStatus", user.getStatus().getValue());
        boolean userStatusEditable = !UserStatus.PERMANENTLY_DISABLED.equals(user.getStatus())
                && !UserStatus.DELETED.equals(user.getStatus());
        if (userStatusEditable
                && UserStatus.TERMS_NOT_ACCEPTED.equals(user.getStatus())
                && ClientProperty.TERMS_OF_USE_USERS_MUST_ACCEPT
                .getValue(ClientProperty.DEFAULT_TERMS_OF_USE_USERS_MUST_ACCEPT)) {
            // only user can change the status by accepting the terms of use
            userStatusEditable = false;
        }
        getRequest().setAttribute("userStatusEditable", userStatusEditable);
        getRequest().setAttribute(PARAMETER_USER_EMAIL, user.getEmail());

        getRequest().setAttribute("userGroupList", usersGroups);
        getRequest().setAttribute("isClientManager",
                UserAuthorityHelper.hasAuthority(user, UserRole.ROLE_KENMEI_CLIENT_MANAGER));
        getRequest().setAttribute("isSystemUser",
                UserAuthorityHelper.hasAuthority(user, UserRole.ROLE_SYSTEM_USER));
        getRequest().setAttribute("isCrawlUser",
                UserAuthorityHelper.hasAuthority(user, UserRole.ROLE_CRAWL_USER));
        getRequest().setAttribute(
                "externalUserAuthorisations",
                ServiceLocator.instance().getService(UserManagement.class)
                .getExternalExternalUserAuthentications(user.getId()));
        return user;
    }

    /**
     *
     * @return list of user status
     */
    public List<String> getUserStatusLiterals() {
        return UserStatus.literals();
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public Object handleRequest() {
        String action = getParameter(PARAMETER_ACTION);
        if (ACTION_UPDATE.equals(action)) {
            saveUserSettings();
        }
        User user = getUser();
        return user != null;
    }

    @Override
    protected void initParameters() {
        // Do nothing.
    }

    /**
     * Returns whether to preselect the disable deletion mode.
     *
     * @return true when delete-by-disabling is enabled or deletion is disabled
     */
    public boolean isCheckDeleteModeDisable() {
        ClientConfigurationProperties cp = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties();
        boolean check = true;
        if (!cp.getProperty(ClientProperty.DELETE_USER_BY_DISABLE_ENABLED,
                ClientProperty.DEFAULT_DELETE_USER_BY_DISABLE_ENABLED)) {
            if (cp.getProperty(ClientProperty.DELETE_USER_BY_ANONYMIZE_ENABLED,
                    ClientProperty.DEFAULT_DELETE_USER_BY_ANONYMIZE_ENABLED)) {
                check = false;
            }
        }
        return check;
    }

    /**
     * This method saves the user settings.
     */
    private void saveUserSettings() {
        try {
            ServiceLocator.findService(TransactionManagement.class)
                    .execute(new SaveInTransaction());
        } catch (UnexpectedRollbackException e) {
            LOG.warn("This is not a problem here: " + e.getMessage());
        }
    }
}