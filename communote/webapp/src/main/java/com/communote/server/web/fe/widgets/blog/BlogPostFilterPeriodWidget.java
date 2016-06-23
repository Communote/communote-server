package com.communote.server.web.fe.widgets.blog;

import com.communote.server.widgets.EmptyWidget;

/**
 * Widget for filter summary
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogPostFilterPeriodWidget extends EmptyWidget {

    /**
     * {@inheritDoc}
     */
    public String getTile(String outputType) {
        return "core.widget.filter.search.date";
    }

}
