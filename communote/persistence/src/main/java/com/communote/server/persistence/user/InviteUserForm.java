package com.communote.server.persistence.user;

import org.apache.commons.lang3.StringUtils;

/**
 * Form for an user invitation
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class InviteUserForm {

    /** The alias. */
    private String alias = "";

    /** The firstName. */
    private String firstName = "";

    /** The lastName. */
    private String lastName = "";

    /** The email. */
    private String email = "";

    /** The language code. */
    private String languageCode = "";

    /** The email alias. */
    private String emailAlias = "";

    private String externalUsername = "";

    private String invitationProvider;

    /**
     * Instantiates a new invite user to client form.
     */
    public InviteUserForm() {
    }

    /**
     * Gets the alias.
     * 
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Gets the email.
     * 
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the email alias.
     * 
     * @return the email alias
     */
    public String getEmailAlias() {
        return emailAlias;
    }

    /**
     * Gets the external username.
     * 
     * @return the external username
     */
    public String getExternalUsername() {
        return externalUsername;
    }

    /**
     * Gets the firstName.
     * 
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the invitationProvider
     */
    public String getInvitationProvider() {
        return invitationProvider;
    }

    /**
     * Gets the language code.
     * 
     * @return the language code
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * Gets the lastName.
     * 
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the alias.
     * 
     * @param alias
     *            the new alias
     */
    public void setAlias(String alias) {
        this.alias = StringUtils.trim(alias);
    }

    /**
     * Sets the email.
     * 
     * @param email
     *            the new email
     */
    public void setEmail(String email) {
        this.email = StringUtils.trim(email);
    }

    /**
     * Sets the email alias.
     * 
     * @param emailAlias
     *            the new email alias
     */
    public void setEmailAlias(String emailAlias) {
        this.emailAlias = StringUtils.trim(emailAlias);
    }

    /**
     * Sets the alias.
     * 
     * @param username
     *            the new username
     */
    public void setExternalUsername(String username) {
        this.externalUsername = StringUtils.trim(username);
    }

    /**
     * Sets the firstName.
     * 
     * @param firstname
     *            the firstname
     */
    public void setFirstName(String firstname) {
        this.firstName = StringUtils.trim(firstname);
    }

    /**
     * @param invitationProvider
     *            the invitationProvider to set
     */
    public void setInvitationProvider(String invitationProvider) {
        this.invitationProvider = StringUtils.trim(invitationProvider);
    }

    /**
     * Sets the language code.
     * 
     * @param languageCode
     *            the new language code
     */
    public void setLanguageCode(String languageCode) {
        this.languageCode = StringUtils.trim(languageCode);
    }

    /**
     * Sets the lastName.
     * 
     * @param lastName
     *            the surname
     */
    public void setLastName(String lastName) {
        this.lastName = StringUtils.trim(lastName);
    }

}
