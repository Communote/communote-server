package com.communote.server.web.fe.widgets.management.user;

import javax.servlet.http.HttpServletRequest;

import com.communote.server.core.user.UserAuthorityHelper;
import com.communote.server.model.user.UserRole;
import com.communote.server.web.fe.widgets.user.profile.UserProfileChangePasswordWidget;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserManagementSystemUserProfileWidget extends UserProfileChangePasswordWidget {

    public UserManagementSystemUserProfileWidget() {
        super(false);
    }

    @Override
    protected Long getUserId(HttpServletRequest request) {
        Long userId = getLongParameter("userId");
        if (UserAuthorityHelper.hasAuthority(userId,
                UserRole.ROLE_SYSTEM_USER) || UserAuthorityHelper.hasAuthority(userId,
                UserRole.ROLE_CRAWL_USER)) {
            return userId;
        }
        return null;
    }
}
