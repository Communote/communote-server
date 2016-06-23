package com.communote.server.persistence.global;

/**
 * Helper enum with the types of the global id and their associated path element
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum GlobalIdType {
    /**
     * type for user
     */
    USER("user"),

    /**
     * type for group
     */
    GROUP("group"),
    /**
     * type for blogs
     */
    BLOG("blog"),
    /**
     * type for attachment
     */
    ATTACHMENT("attachment"),
    /**
     * type for tag
     */
    TAG("tag"),
    /**
     * type for note
     */
    NOTE("note");

    /**
     * the path element of the global id
     */
    private String globalIdPath;

    /**
     * @param path
     *            the path element in the global id for this element
     */
    private GlobalIdType(String path) {
        this.globalIdPath = path;
    }

    /**
     * @return the path element in the global id for this element
     */
    public String getGlobalIdPath() {
        return globalIdPath;
    }
}
