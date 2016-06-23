package com.communote.server.api.core.blog;

import java.util.Map;

/**
 * Thrown to indicate that an action would lead to a topic/blog that does not have an active user
 * with management rights.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoBlogManagerLeftException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5797034256652725240L;

    private Map<Long, String> blogIdsToTitleMapping;

    /**
     * Constructs a new instance of NoBlogManagerLeftException
     *
     */
    public NoBlogManagerLeftException(String message, Map<Long, String> blogIdsToTitleMapping) {
        super(message);
        this.blogIdsToTitleMapping = blogIdsToTitleMapping;
    }

    /**
     * <p>
     * a mapping of the blogIds to the blog titles of the affected blogs/topics
     * </p>
     */
    public Map<Long, String> getBlogIdsToTitleMapping() {
        return this.blogIdsToTitleMapping;
    }

    public void setBlogIdsToTitleMapping(Map<Long, String> blogIdsToTitleMapping) {
        this.blogIdsToTitleMapping = blogIdsToTitleMapping;
    }

}
