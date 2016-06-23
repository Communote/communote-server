package com.communote.server.web.fe.widgets.blog;

import java.util.List;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.widgets.AbstractMultipleResultWidget;

/**
 * Widget to show a list of the top n blogs, which are most used by the current user.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopBlogsWidget extends AbstractMultipleResultWidget<BlogData> {

    /**
     * {@inheritDoc}
     */
    public String getTile(String outputType) {
        return "core.widget.filter.blog.topused";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BlogData> handleQueryList() {
        int numberOfMaxResults = getIntParameter("numberOfMaxResults", 5);
        List<BlogData> mostUsedBlogsByUser = ServiceLocator
                .instance().getService(BlogManagement.class)
                .getMostUsedBlogs(numberOfMaxResults, true);
        return mostUsedBlogsByUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        // Does nothing.
    }
}
