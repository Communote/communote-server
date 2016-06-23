package com.communote.server.core.filter.listitems;

import java.io.Serializable;

import com.communote.server.api.core.user.UserData;
import com.communote.server.model.user.UserStatus;
import com.communote.server.model.user.PhoneNumber;


/**
 * IdentifiableEntityData for a users profile.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileDetailListItem extends UserData implements Serializable {

    /** The serial version UID of this class. Needed for serialization. */
    private static final long serialVersionUID = -193323271092565198L;

    private String position;

    private String company;

    private String street;

    private String zip;

    private String city;

    private com.communote.server.model.user.PhoneNumber phone;

    private com.communote.server.model.user.PhoneNumber fax;

    private String countryCode;

    private String timeZoneId;

    /**
     * Constructor.
     */
    public UserProfileDetailListItem() {
        super();
    }

    public UserProfileDetailListItem(String alias, String firstName, String lastName,
            String salutation, UserStatus status, String position, String company,
            String street, String zip, String city, PhoneNumber phone, PhoneNumber fax,
            String countryCode, String timeZoneId) {
        super(null, "", alias, firstName, lastName, salutation, status);
        this.position = position;
        this.company = company;
        this.street = street;
        this.zip = zip;
        this.city = city;
        this.phone = phone;
        this.fax = fax;
        this.countryCode = countryCode;
        this.timeZoneId = timeZoneId;
    }

    /**
     * Copies constructor from other UserProfileDetailListItem
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public UserProfileDetailListItem(UserProfileDetailListItem otherBean) {
        this(otherBean.getAlias(), otherBean.getFirstName(), otherBean.getLastName(), otherBean
                .getSalutation(), otherBean.getStatus(), otherBean.getPosition(), otherBean
                .getCompany(), otherBean.getStreet(), otherBean.getZip(), otherBean.getCity(),
                otherBean.getPhone(), otherBean.getFax(), otherBean.getCountryCode(), otherBean
                        .getTimeZoneId());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(UserProfileDetailListItem otherBean) {
        if (otherBean != null) {
            this.setAlias(otherBean.getAlias());
            this.setFirstName(otherBean.getFirstName());
            this.setLastName(otherBean.getLastName());
            this.setSalutation(otherBean.getSalutation());
            this.setStatus(otherBean.getStatus());
            this.setPosition(otherBean.getPosition());
            this.setCompany(otherBean.getCompany());
            this.setStreet(otherBean.getStreet());
            this.setZip(otherBean.getZip());
            this.setCity(otherBean.getCity());
            this.setPhone(otherBean.getPhone());
            this.setFax(otherBean.getFax());
            this.setCountryCode(otherBean.getCountryCode());
            this.setTimeZoneId(otherBean.getTimeZoneId());
        }
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @return the company
     */
    public String getCompany() {
        return company;
    }

    /**
     * @return the countryCode
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * @return An empty string.
     */
    // The original item didn't provide access to the users e-mail address.
    // (For some unknown security reasons?)
    @Override
    public String getEmail() {
        return "";
    }

    /**
     * @return the fax
     */
    public com.communote.server.model.user.PhoneNumber getFax() {
        return fax;
    }

    /**
     * @return the phone
     */
    public com.communote.server.model.user.PhoneNumber getPhone() {
        return phone;
    }

    /**
     * @return the position
     */
    public String getPosition() {
        return position;
    }

    /**
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * @return the timeZoneId
     */
    public String getTimeZoneId() {
        return timeZoneId;
    }

    /**
     * @return the zip
     */
    public String getZip() {
        return zip;
    }

    /**
     * @param city
     *            the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @param company
     *            the company to set
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * @param countryCode
     *            the countryCode to set
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * @param fax
     *            the fax to set
     */
    public void setFax(com.communote.server.model.user.PhoneNumber fax) {
        this.fax = fax;
    }

    /**
     * @param phone
     *            the phone to set
     */
    public void setPhone(com.communote.server.model.user.PhoneNumber phone) {
        this.phone = phone;
    }

    /**
     * @param position
     *            the position to set
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * @param street
     *            the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * @param timeZoneId
     *            the timeZoneId to set
     */
    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    /**
     * @param zip
     *            the zip to set
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

}