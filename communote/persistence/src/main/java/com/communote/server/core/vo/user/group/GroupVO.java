package com.communote.server.core.vo.user.group;

/**
 * <p>
 * VO for groups.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GroupVO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7052781283813032237L;

    private String name;

    private String alias;

    private String description;

    public GroupVO() {
        this.name = null;
    }

    /**
     * Copies constructor from other GroupVO
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public GroupVO(GroupVO otherBean) {
        this(otherBean.getName(), otherBean.getAlias(), otherBean.getDescription());
    }

    public GroupVO(String name) {
        this.name = name;
    }

    public GroupVO(String name, String alias, String description) {
        this.name = name;
        this.alias = alias;
        this.description = description;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(GroupVO otherBean) {
        if (otherBean != null) {
            this.setName(otherBean.getName());
            this.setAlias(otherBean.getAlias());
            this.setDescription(otherBean.getDescription());
        }
    }

    /**
     * <p>
     * The alias of the group.
     * </p>
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * <p>
     * A description of the group.
     * </p>
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * <p>
     * The name of the group.
     * </p>
     */
    public String getName() {
        return this.name;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

}