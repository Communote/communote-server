package com.communote.server.web.fe.portal.user.forms;

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.user.UserVO;

/**
 * The form for registering a user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RegistrationForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The Constant REGISTRATION_FORM_SEND. */
    public final static String REGISTRATION_FORM_SEND = "RegistrationFormSend";

    /** The password. */
    private String password = StringUtils.EMPTY;

    /** The password2. */
    private String password2 = StringUtils.EMPTY;

    /** The time zone of the client */
    private String timeZoneId = StringUtils.EMPTY;

    /**
     * A distinct list with offsets and its time zone ids as JavaScript Object
     */
    private String timeZoneOffsetList = StringUtils.EMPTY;

    /** The user. */
    private UserVO user = new UserVO(StringUtils.EMPTY, null, StringUtils.EMPTY, null,
            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, null);

    private String confirmationCode = null;

    /** The send. */
    private String formSendMarker = StringUtils.EMPTY;

    /** The user agreed to the terms of use. */
    private boolean termsAgreed = false;

    /**
     * Instantiates a new register user form.
     */
    public RegistrationForm() {
    }

    /**
     * Instantiates a new register user form.
     *
     * @param user
     *            the user
     */
    public RegistrationForm(UserVO user) {
        setUser(user);
    }

    /**
     * Formular send.
     *
     * @return true, if successful
     */
    public boolean formularSend() {
        return !StringUtils.isEmpty(this.formSendMarker)
                && this.formSendMarker.equals(REGISTRATION_FORM_SEND);
    }

    /**
     * Gets the alias.
     *
     * @return the alias
     */
    public String getAlias() {
        return user.getAlias();
    }

    /**
     * Gets the confirmation code.
     *
     * @return the confirmation code
     */
    public String getConfirmationCode() {
        return confirmationCode;
    }

    /**
     * Gets the email.
     *
     * @return the email
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Gets the first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return user.getFirstName();
    }

    /**
     * Gets the send.
     *
     * @return the send
     */
    public String getFormSendMarker() {
        return formSendMarker;
    }

    /**
     * Gets the language code.
     *
     * @return the languageCode
     */
    public String getLanguageCode() {
        String code = null;
        if (user.getLanguage() != null) {
            code = user.getLanguage().getLanguage();
        }
        return code;
    }

    /**
     * Gets the last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return user.getLastName();
    }

    /**
     * Gets the locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return this.user.getLanguage();
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the password2.
     *
     * @return the password2
     */
    public String getPassword2() {
        return password2;
    }

    /**
     * Has the user agreed to the Terms of use?.
     *
     * @return true, if checkbox was set, otherwise false
     */
    public boolean getTermsAgreed() {
        return termsAgreed;
    }

    /**
     * @return the timeZoneId
     */
    public String getTimeZoneId() {
        return timeZoneId;
    }

    /**
     * @return the timeZoneOffsetList
     */
    public String getTimeZoneOffsetList() {
        return timeZoneOffsetList;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public UserVO getUser() {
        return user;
    }

    /**
     * Sets the alias.
     *
     * @param alias
     *            the new alias
     */
    public void setAlias(String alias) {
        this.user.setAlias(alias == null ? null : alias.trim());
    }

    /**
     * Sets the confirmation code.
     *
     * @param confirmationCode
     *            the new confirmation code
     */
    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode == null ? null : confirmationCode.trim();
    }

    /**
     * Sets the email.
     *
     * @param email
     *            the new email
     */
    public void setEmail(String email) {
        user.setEmail(email == null ? null : email.trim());
    }

    /**
     * Sets the first name.
     *
     * @param value
     *            the new first name
     */
    public void setFirstName(String value) {
        user.setFirstName(value == null ? null : value.trim());
    }

    /**
     * Sets the send.
     *
     * @param send
     *            the send to set
     */
    public void setFormSendMarker(String send) {
        this.formSendMarker = send == null ? null : send.trim();
    }

    /**
     * Sets the language code.
     *
     * @param code
     *            the new language code
     */
    public void setLanguageCode(String code) {
        this.user.setLanguage(new Locale(code == null ? null : code.trim()));
    }

    /**
     * Sets the last name.
     *
     * @param lastName
     *            the new last name
     */
    public void setLastName(String lastName) {
        user.setLastName(lastName == null ? null : lastName.trim());
    }

    /**
     * Sets the password.
     *
     * @param password
     *            the new password
     */
    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
        setUserPassword();
    }

    /**
     * Sets the password2.
     *
     * @param password2
     *            the new password2
     */
    public void setPassword2(String password2) {
        this.password2 = password2 == null ? null : password2.trim();
        setUserPassword();
    }

    /**
     * Has the user agreed to the Terms of use?.
     *
     * @return true, if checkbox was set, otherwise false
     */
    public boolean setTermsAgreed() {
        return termsAgreed;
    }

    /**
     * The user agreed to the terms of use.
     *
     * @param terms
     *            the current status of the associated checkbox
     */
    public void setTermsAgreed(boolean terms) {
        this.termsAgreed = terms;
    }

    /**
     * @param timeZoneId
     *            the timeZoneId to set
     */
    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId == null ? null : timeZoneId.trim();
    }

    /**
     * @param timeZoneOffsetList
     *            the timeZoneOffsetList to set
     */
    public void setTimeZoneOffsetList(String timeZoneOffsetList) {
        this.timeZoneOffsetList = timeZoneOffsetList == null ? null : timeZoneOffsetList.trim();
    }

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    public void setUser(UserVO user) {
        this.user = user;
    }

    /**
     * Sets the user password.
     */
    private void setUserPassword() {
        user.setPassword(null);
        if (StringUtils.isNotBlank(password) && StringUtils.equals(password, password2)) {
            user.setPassword(password);
        }
    }
}
