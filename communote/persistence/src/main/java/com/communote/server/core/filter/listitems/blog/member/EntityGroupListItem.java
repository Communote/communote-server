package com.communote.server.core.filter.listitems.blog.member;

/**
 * List item for groups (as part of the blog role assignment)
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EntityGroupListItem extends CommunoteEntityData {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String alias;
    private String name;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAlias() {
        return alias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return name + " (" + alias + ")";
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShortDisplayName() {
        return getName();
    }

    /**
     * @return the type of this list item as string value.
     */
    @Override
    public String getType() {
        return "GROUP";
    }

    @Override
    public boolean isGroup() {
        return true;
    }

    /**
     * @param alias
     *            the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
