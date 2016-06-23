package com.communote.server.web.fe.widgets.navigation;

import com.communote.server.widgets.EmptyWidget;

/**
 * Widget that shows navigation items to activate different contexts.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MainPageVerticalNavigationWidget extends EmptyWidget {
    @Override
    public String getTile(String outputType) {
        return "core.widget.navigation.mainPageVertical";
    }
}
