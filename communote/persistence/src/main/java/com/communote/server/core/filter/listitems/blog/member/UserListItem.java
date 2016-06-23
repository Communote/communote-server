package com.communote.server.core.filter.listitems.blog.member;

import com.communote.server.core.user.helper.UserNameHelper;

/**
 * List item for users (as part of the blog role assignment)
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserListItem extends CommunoteEntityData {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String alias;
    private String firstName;
    private String lastName;

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
        return UserNameHelper.getDetailedUserSignature(firstName, lastName, alias);
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShortDisplayName() {
        return UserNameHelper.getSimpleDefaultUserSignature(firstName, lastName, alias);
    }

    /**
     * @return the type of this list item as string value.
     */
    @Override
    public String getType() {
        return "USER";
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    /**
     * @param alias
     *            the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
