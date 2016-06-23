package com.communote.server.web.fe.widgets.blog;

import org.apache.commons.lang.StringUtils;

import com.communote.server.widgets.EmptyWidget;

/**
 * Widget for filter search box
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SearchBoxWidget extends EmptyWidget {

    /**
     * Returns the search modes to be rendered.
     * 
     * @return the search mode names
     */
    public String[] getSearchModes() {
        String modes = getParameter("searchModes");
        return StringUtils.split(modes, ",");
    }

    /**
     * {@inheritDoc}
     */
    public String getTile(String outputType) {
        return "core.widget.filter.search.keywords";
    }

}
