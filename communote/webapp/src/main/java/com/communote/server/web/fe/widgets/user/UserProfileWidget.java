package com.communote.server.web.fe.widgets.user;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget for displaying a user profile
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

public class UserProfileWidget extends AbstractWidget {

    private static final String PARAM_USER_ID = "userId";
    private static final String PARAM_USER_ALIAS = "alias";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "widget.lists.user.profile." + outputType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        User user = null;
        Long userId = getLongParameter(PARAM_USER_ID, 0);
        if (userId != 0) {
            user = ServiceLocator.findService(UserManagement.class).findUserByUserId(userId);
        } else {
            String alias = getParameter(PARAM_USER_ALIAS);
            if (alias != null && alias.length() > 0) {
                user = ServiceLocator.findService(UserManagement.class)
                        .findUserByAlias(alias);
            }
        }
        // no result if the user is deleted
        if (user != null && user.getStatus().equals(UserStatus.DELETED)) {
            return null;
        }
        return user;
    }

    /**
     * Init the widget parameters to these values: filter = ''
     */
    @Override
    protected void initParameters() {
        setParameter(FilterWidgetParameterNameProvider.INSTANCE.getNameForTags(), StringUtils.EMPTY);
    }
}
