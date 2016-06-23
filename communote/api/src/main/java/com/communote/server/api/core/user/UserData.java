package com.communote.server.api.core.user;

import java.util.ArrayList;
import java.util.Collection;

import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.api.core.tag.TagData;

/**
 * Value object holding details about a user.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserData extends IdentifiableEntityData implements com.communote.server.model.user.UserName {

    private static final long serialVersionUID = -5043825598867930914L;

    private String email;

    private String alias;

    private String firstName;

    private String lastName;

    private String salutation;

    private com.communote.server.model.user.UserStatus status;

    private Collection<TagData> tags = new ArrayList<TagData>();

    /**
     * Empty constructor.
     */
    public UserData() {
        // Does nothing.
        this.status = null;
    }

    /**
     * Construtor for the userListItem
     *
     * @param id
     *            identifier
     * @param email
     *            mail of user
     * @param alias
     *            of user
     * @param firstName
     *            of user
     * @param lastName
     *            of user
     * @param salutation
     *            of user
     * @param status
     *            of user
     */
    public UserData(Long id, String email, String alias, String firstName, String lastName,
            String salutation, com.communote.server.model.user.UserStatus status) {
        super();
        setId(id);
        this.email = email;
        this.alias = alias;
        this.firstName = firstName;
        this.lastName = lastName;
        this.salutation = salutation;
        this.status = status;
    }

    /**
     * Copies constructor from other UserData
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public UserData(UserData otherBean) {
        this(otherBean.getId(), otherBean.getEmail(), otherBean.getAlias(), otherBean
                .getFirstName(), otherBean.getLastName(), otherBean.getSalutation(), otherBean
                .getStatus());
    }

    /**
     * Get the alias of user
     *
     * @return string
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * Get the email of user
     *
     * @return string
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Get the firstname of user
     *
     * @return string
     */
    @Override
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Get the lastname of user
     *
     * @return string
     */
    @Override
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Get the salutation of user
     *
     * @return string
     */
    @Override
    public String getSalutation() {
        return this.salutation;
    }

    /**
     * Get the status of user
     *
     * @return {@link com.communote.server.model.user.UserStatus}
     */
    public com.communote.server.model.user.UserStatus getStatus() {
        return this.status;
    }

    /**
     * @return the tags
     */
    public Collection<TagData> getTags() {
        return tags;
    }

    /**
     * Returns the ID of the user.
     *
     * Note: still here for backwards compatibility for old API clients.
     *
     * @return the ID of the user
     *
     * @deprecated Use {@link IdentifiableEntityData#getId(Long)}
     */
    @Deprecated
    public Long getUserId() {
        return getId();
    }

    /**
     * Set the alias of user
     *
     * @param alias
     *            of user
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Set the email of user
     *
     * @param email
     *            of user
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Set the firstname of user
     *
     * @param firstName
     *            of user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Set the lastname of user
     *
     * @param lastName
     *            of user
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Set the salutation of user
     *
     * @param salutation
     *            of user
     */
    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    /**
     * Set the status of user
     *
     * @param status
     *            {@link com.communote.server.model.user.UserStatus}
     */
    public void setStatus(com.communote.server.model.user.UserStatus status) {
        this.status = status;
    }

    /**
     * @param tags
     *            the tags to set
     */
    public void setTags(Collection<TagData> tags) {
        this.tags = tags;
    }

    /**
     * Sets the ID of the user.
     *
     * Note: still here for backwards compatibility for old API clients.
     *
     * @param userId
     *            the ID of the user
     *
     * @deprecated Use {@link IdentifiableEntityData#setId(Long)}
     */
    @Deprecated
    public void setUserId(Long userId) {
        setId(userId);
    }
}