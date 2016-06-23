package com.communote.server.web.fe.widgets.management.user;

import java.util.Set;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.user.UserAuthorityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.web.fe.widgets.user.UserProfileWidget;

/**
 * Widget for managing users.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserManagementUserDialogWidget extends UserProfileWidget {

    private static final String PARAM_USER_ID = "userId";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "widget.client.management.user.dialog." + outputType;
    }

    @Override
    public Object handleRequest() {

        Long userId = getLongParameter(PARAM_USER_ID, 0);

        if (userId == 0) {
            return null;
        }

        User user = ServiceLocator.instance().getService(UserManagement.class)
                .findUserByUserId(userId);

        getRequest().setAttribute("userId", user.getId());
        Set<UserRole> roles = UserAuthorityHelper.getUserRoles(userId);
        getRequest().setAttribute(
                "isSystemUser",
                roles.contains(UserRole.ROLE_SYSTEM_USER)
                || roles.contains(UserRole.ROLE_CRAWL_USER));
        setResponseMetadata("userSignature", UserNameHelper.getDetailedUserSignature(user
                .getProfile().getFirstName(), user.getProfile().getLastName(), user.getAlias()));
        return user != null;
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
}
