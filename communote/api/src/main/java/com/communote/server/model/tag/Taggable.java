package com.communote.server.model.tag;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface Taggable {

    /**
     * 
     */
    public java.util.Set<com.communote.server.model.tag.Tag> getTags();

    /**
     * 
     */
    public void setTags(java.util.Set<com.communote.server.model.tag.Tag> tags);

}