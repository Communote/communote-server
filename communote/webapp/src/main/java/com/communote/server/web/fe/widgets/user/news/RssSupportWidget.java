package com.communote.server.web.fe.widgets.user.news;

/**
 * Interface to add Newsfeeds in the current Controller
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface RssSupportWidget {

    /**
     * Get the Parameters relevant for the rss or news feed.
     * 
     * @return String with the relevant parameters without starting & or ?
     */
    public String getRssParameters();

}
