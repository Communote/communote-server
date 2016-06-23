package com.communote.server.web.fe.widgets.user;

import com.communote.server.widgets.EmptyWidget;

/**
 * Widget for displaying a user search box
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserSearchBoxWidget extends EmptyWidget {
    /**
     * {@inheritDoc}
     */
    public String getTile(String outputType) {
        return "widget.user.usersearchbox." + outputType;
    }
}
