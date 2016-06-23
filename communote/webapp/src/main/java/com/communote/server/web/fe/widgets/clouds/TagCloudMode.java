package com.communote.server.web.fe.widgets.clouds;

/**
 * Enum for the tag cloud mode
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum TagCloudMode {
    /**
     * Popular tags are most used tags, based on some time limitation
     */
    PopularTags,
    /**
     * My tags are the tags a user used
     */
    MyTags,
    /**
     * Some tags are some arbitrary tags
     */
    SomeTags

}
