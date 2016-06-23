package com.communote.server.web.fe.widgets.blog;

import com.communote.server.widgets.EmptyWidget;

/**
 * Widget for filter user list
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogPostFilterUserListWidget extends EmptyWidget {

    /**
     * {@inheritDoc}
     */
    public String getTile(String outputType) {
        return "widget.blog.post.filter.user." + outputType;
    }

}
