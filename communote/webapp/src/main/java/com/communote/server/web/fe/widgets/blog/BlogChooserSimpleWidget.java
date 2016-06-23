package com.communote.server.web.fe.widgets.blog;

import com.communote.server.widgets.EmptyWidget;

/**
 * Widget for selcting blogs
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogChooserSimpleWidget extends EmptyWidget {

    /**
     * {@inheritDoc}
     */
    public String getTile(String outputType) {
        return "core.widget.blog.chooser.simple";
    }

}
