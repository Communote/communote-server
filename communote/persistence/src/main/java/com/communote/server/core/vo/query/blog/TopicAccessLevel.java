package com.communote.server.core.vo.query.blog;

/**
 * Enumeration, which defines possible levels of access for topics.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum TopicAccessLevel {
    /** If set, the current user must have read access to the topic. */
    READ,
    /** If set, the current user must have write access to the topic. */
    WRITE,
    /** If set, the current user must be a manager of the topic. */
    MANAGER,
    /** If set, the current user must be the internal system user. */
    SYSTEM
}
