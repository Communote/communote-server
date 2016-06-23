package com.communote.server.model.follow;


/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface Followable extends com.communote.server.model.global.GlobalIdentifiable {

    /**
     * 
     */
    public com.communote.server.model.global.GlobalId getFollowId();

}