package com.communote.server.web.fe.widgets.management.user.group;

/**
 * Simple data object to represent a user or a group.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class EntityListItem {

    private String name;
    private String alias;
    private Long id;
    private boolean isGroup = false;

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return True if this is a group.
     */
    public boolean getIsGroup() {
        return isGroup;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param alias
     *            the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @param isGroup
     *            the isUser to set
     */
    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
