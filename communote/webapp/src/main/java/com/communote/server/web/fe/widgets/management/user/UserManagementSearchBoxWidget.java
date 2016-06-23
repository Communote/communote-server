package com.communote.server.web.fe.widgets.management.user;

import java.util.List;

import com.communote.server.model.user.UserStatus;
import com.communote.server.widgets.EmptyWidget;

/**
 * Widget for displaying a user search box with management filters
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserManagementSearchBoxWidget extends EmptyWidget {

    /**
     * {@inheritDoc}
     */
    public String getTile(String outputType) {
        return "widget.client.management.searchbox." + outputType;
    }

    /**
     * 
     * @return list of user status
     */
    public List<String> getUserStatusLiterals() {
        return UserStatus.literals();
    }

}
