package com.communote.server.core.filter.listitems;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserManagementListItem extends
        com.communote.server.api.core.user.UserData implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4801243424559684617L;

    private com.communote.server.model.user.UserRole[] roles;

    public UserManagementListItem() {
        super();
        this.roles = null;
    }

    public UserManagementListItem(com.communote.server.model.user.UserRole[] roles,
            Long userId, String email, String alias,
            String firstName, String lastName, String salutation,
            com.communote.server.model.user.UserStatus status) {
        super(userId, email, alias, firstName, lastName, salutation, status);
        this.roles = roles;
    }

    /**
     * Copies constructor from other UserManagementListItem
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public UserManagementListItem(UserManagementListItem otherBean) {
        this(otherBean.getRoles(), otherBean.getId(), otherBean.getEmail(), otherBean.getAlias(),
                otherBean.getFirstName(), otherBean.getLastName(), otherBean.getSalutation(),
                otherBean.getStatus());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(UserManagementListItem otherBean) {
        if (otherBean != null) {
            this.setStatus(otherBean.getStatus());
            this.setRoles(otherBean.getRoles());
            this.setId(otherBean.getId());
            this.setEmail(otherBean.getEmail());
            this.setAlias(otherBean.getAlias());
            this.setFirstName(otherBean.getFirstName());
            this.setLastName(otherBean.getLastName());
            this.setSalutation(otherBean.getSalutation());
        }
    }

    /**
     * 
     */
    public com.communote.server.model.user.UserRole[] getRoles() {
        return this.roles;
    }

    public void setRoles(com.communote.server.model.user.UserRole[] roles) {
        this.roles = roles;
    }

}