package com.communote.server.persistence.user;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileVO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6746108530512936420L;

    private String firstName;

    private String lastName;

    private String salutation;

    private String position;

    private String company;

    private String street;

    private String zip;

    private String city;

    private com.communote.server.model.user.PhoneNumber phone;

    private com.communote.server.model.user.PhoneNumber fax;

    private String countryCode;

    private String timeZoneId;

    public UserProfileVO() {
    }

    public UserProfileVO(String firstName, String lastName, String salutation, String position,
            String company, String street, String zip, String city,
            com.communote.server.model.user.PhoneNumber phone,
            com.communote.server.model.user.PhoneNumber fax, String countryCode, String timeZoneId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.salutation = salutation;
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
     * Copies constructor from other UserProfileVO
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public UserProfileVO(UserProfileVO otherBean) {
        this(otherBean.getFirstName(), otherBean.getLastName(), otherBean.getSalutation(),
                otherBean.getPosition(), otherBean.getCompany(), otherBean.getStreet(), otherBean
                        .getZip(), otherBean.getCity(), otherBean.getPhone(), otherBean.getFax(),
                otherBean.getCountryCode(), otherBean.getTimeZoneId());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(UserProfileVO otherBean) {
        if (otherBean != null) {
            this.setFirstName(otherBean.getFirstName());
            this.setLastName(otherBean.getLastName());
            this.setSalutation(otherBean.getSalutation());
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
     *
     */
    public String getCity() {
        return this.city;
    }

    /**
     *
     */
    public String getCompany() {
        return this.company;
    }

    /**
     *
     */
    public String getCountryCode() {
        return this.countryCode;
    }

    /**
     *
     */
    public com.communote.server.model.user.PhoneNumber getFax() {
        return this.fax;
    }

    /**
     *
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     *
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     *
     */
    public com.communote.server.model.user.PhoneNumber getPhone() {
        return this.phone;
    }

    /**
     *
     */
    public String getPosition() {
        return this.position;
    }

    /**
     *
     */
    public String getSalutation() {
        return this.salutation;
    }

    /**
     *
     */
    public String getStreet() {
        return this.street;
    }

    /**
     * <p>
     * The ID of the TimeZone.
     * </p>
     */
    public String getTimeZoneId() {
        return this.timeZoneId;
    }

    /**
     *
     */
    public String getZip() {
        return this.zip;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setFax(com.communote.server.model.user.PhoneNumber fax) {
        this.fax = fax;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhone(com.communote.server.model.user.PhoneNumber phone) {
        this.phone = phone;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

}