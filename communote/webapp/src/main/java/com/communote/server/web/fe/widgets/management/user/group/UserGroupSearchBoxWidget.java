package com.communote.server.web.fe.widgets.management.user.group;

import com.communote.server.widgets.EmptyWidget;

/**
 * Dummy widget for a user group search box
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

public class UserGroupSearchBoxWidget extends EmptyWidget {

    /**
     * {@inheritDoc}
     */
    public String getTile(String outputType) {
        return "widget.user.group.usergroupsearchbox." + outputType;
    }

}
