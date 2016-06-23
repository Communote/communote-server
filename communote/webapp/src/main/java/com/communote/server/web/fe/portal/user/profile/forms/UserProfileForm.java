package com.communote.server.web.fe.portal.user.profile.forms;

import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.user.MasterDataManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.model.user.Country;
import com.communote.server.model.user.PhoneNumber;
import com.communote.server.model.user.UserProfileFields;
import com.communote.server.persistence.user.UserProfileVO;

/**
 * The Class UserProfileForm.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileForm {
    /**
     * Wrapper class to avoid unallowed changes on the phone number.
     */
    private class FieldCheckPhoneNumber extends PhoneNumber {

        private static final long serialVersionUID = 4723508057264981507L;

        private final PhoneNumber phone;

        private final String fieldToCheck;

        /**
         * @param phone
         *            The phone number to check.
         * @param fieldToCheck
         *            The field to check.
         */
        public FieldCheckPhoneNumber(PhoneNumber phone, String fieldToCheck) {
            this.phone = phone;
            this.fieldToCheck = fieldToCheck;

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getAreaCode() {
            return phone.getAreaCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getCountryCode() {
            return phone.getCountryCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getPhoneNumber() {
            return phone.getPhoneNumber();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setAreaCode(String areaCode) {
            if (!fixedProfileFields.contains(fieldToCheck)) {
                phone.setAreaCode(areaCode);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setCountryCode(String countryCode) {
            if (!fixedProfileFields.contains(fieldToCheck)) {
                phone.setCountryCode(countryCode);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setPhoneNumber(String phoneNumber) {
            if (!fixedProfileFields.contains(fieldToCheck)) {
                phone.setPhoneNumber(phoneNumber);
            }
        }
    }

    /** The user profile. */
    private final UserProfileVO userProfile;

    private String tags;

    /** the language code */
    private String languageCode;

    private final Set<String> fixedProfileFields = new HashSet<String>();

    /**
     * Instantiates a new user profile form.
     */
    public UserProfileForm() {
        this(new UserProfileVO(), null, null, new HashSet<String>());
    }

    /**
     * Instantiates a new user profile form.
     *
     * @param profile
     *            the profile
     * @param languageCode
     *            The language code.
     * @param tags
     *            Tags of the user.
     * @param fixedProfileFields
     *            List of profile fields not to change.
     */
    public UserProfileForm(UserProfileVO profile, String languageCode, String tags,
            Set<String> fixedProfileFields) {
        if (profile == null) {
            throw new IllegalArgumentException("Profile can not be null!");
        }
        this.userProfile = profile;
        this.languageCode = languageCode;
        this.setTags(tags);
    }

    /**
     * @return The city.
     */
    public String getCity() {
        return getUserProfile().getCity();
    }

    /**
     * @return The company.
     */
    public String getCompany() {
        return getUserProfile().getCompany();
    }

    /**
     * @return The position.
     */
    public String getCompanyPosition() {
        return getUserProfile().getPosition();
    }

    /**
     * Gets the country code.
     *
     * @return the country code
     */
    public String getCountryCode() {

        String countryCode = null;

        if (getUserProfile().getCountryCode() != null) {
            countryCode = getUserProfile().getCountryCode();
        }

        return countryCode;

    }

    /**
     * Get the fax object of this User and if it's associated fax is null create a new one.
     *
     * @return faxnumber details
     */
    public PhoneNumber getFax() {
        PhoneNumber fax = getUserProfile().getFax();
        if (fax == null) {
            fax = PhoneNumber.newInstance(null, null, null);
            getUserProfile().setFax(fax);
        }
        return new FieldCheckPhoneNumber(fax, UserProfileFields.FAX.name());

    }

    /**
     * Gets the first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return getUserProfile().getFirstName();
    }

    /**
     * @return the fixedProfileFields
     */
    public Set<String> getFixedProfileFields() {
        return fixedProfileFields;
    }

    /**
     * @return the language code
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * Gets the last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return getUserProfile().getLastName();
    }

    /**
     * Get the phone object of this User and if it's associated phone is null create a new one.
     *
     * @return Phonenumber details
     */
    public PhoneNumber getPhone() {
        PhoneNumber phone = getUserProfile().getPhone();
        if (phone == null) {
            phone = PhoneNumber.newInstance(null, null, null);
            getUserProfile().setPhone(phone);
        }
        return new FieldCheckPhoneNumber(phone, UserProfileFields.PHONE.name());
    }

    /**
     * @return The salutation.
     */
    public String getSalutation() {
        return getUserProfile().getSalutation();
    }

    /**
     * @return The street.
     */
    public String getStreet() {
        return getUserProfile().getStreet();
    }

    /**
     * @return the tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * Get the timezone id of this User.
     *
     * @return timezone id
     */
    public String getTimeZoneId() {
        return userProfile.getTimeZoneId();
    }

    /**
     * Gets the current time zone offset by user
     *
     * @return the current time zone offset
     */

    public Integer getTimeZoneOffset() {
        TimeZone timeZone = UserManagementHelper.getEffectiveUserTimeZone();
        return timeZone.getRawOffset();
    }

    /**
     * @return the userProfile
     */
    public UserProfileVO getUserProfile() {
        return userProfile;
    }

    /**
     * @return The zip.
     */
    public String getZip() {
        return getUserProfile().getZip();
    }

    /**
     * @param city
     *            The city to set.
     */
    public void setCity(String city) {
        if (!fixedProfileFields.contains(UserProfileFields.CITY.name())) {
            getUserProfile().setCity(city == null ? null : city.trim());
        }
    }

    /**
     * @param company
     *            The company to set.
     */
    public void setCompany(String company) {
        if (!fixedProfileFields.contains(UserProfileFields.COMPANY.name())) {
            getUserProfile().setCompany(company == null ? null : company.trim());
        }
    }

    /**
     * @param position
     *            The position to set.
     */
    public void setCompanyPosition(String position) {
        if (!fixedProfileFields.contains(UserProfileFields.POSITION.name())) {
            getUserProfile().setPosition(StringUtils.trim(position));
        }
    }

    /**
     * Sets the country code.
     *
     * @param countryCode
     *            the new country code
     */
    public void setCountryCode(String countryCode) {
        if (fixedProfileFields.contains(UserProfileFields.COUNTRY.name())) {
            return;
        }
        Country country = null;
        if (StringUtils.isNotBlank(countryCode)) {
            country = ServiceLocator.findService(MasterDataManagement.class).findCountryByCode(
                    countryCode);
        }
        if (country != null || countryCode == null) {
            this.getUserProfile().setCountryCode(StringUtils.trim(countryCode));
        } else {
            this.getUserProfile().setCountryCode(null);
        }
    }

    /**
     * @param firstname
     *            The first name to set.
     */
    public void setFirstName(String firstname) {
        if (!fixedProfileFields.contains(UserProfileFields.FIRSTNAME.name())) {
            getUserProfile().setFirstName(StringUtils.trim(firstname));
        }
    }

    /**
     * @param languageCode
     *            the language code
     */
    public void setLanguageCode(String languageCode) {
        if (!fixedProfileFields.contains(UserProfileFields.LANGUAGE.name())) {
            this.languageCode = languageCode == null ? null : languageCode.trim();
        }
    }

    /**
     * @param lastname
     *            The lastname to set.
     */
    public void setLastName(String lastname) {
        if (!fixedProfileFields.contains(UserProfileFields.LASTNAME.name())) {
            getUserProfile().setLastName(StringUtils.trim(lastname));
        }
    }

    /**
     * @param salutation
     *            The salutation to set.
     */
    public void setSalutation(String salutation) {
        if (!fixedProfileFields.contains(UserProfileFields.SALUTATION.name())) {
            getUserProfile().setSalutation(StringUtils.trim(salutation));
        }
    }

    /**
     * @param street
     *            The street to set.
     */
    public void setStreet(String street) {
        if (!fixedProfileFields.contains(UserProfileFields.STREET.name())) {
            getUserProfile().setStreet(StringUtils.trim(street));
        }
    }

    /**
     * @param tags
     *            the tags to set
     */
    public void setTags(String tags) {
        this.tags = StringUtils.trim(tags);
    }

    /**
     * Set the timezone id of this User.
     *
     * @param timeZoneId
     *            the timeZoneId of the user
     */
    public void setTimeZoneId(String timeZoneId) {
        userProfile.setTimeZoneId(StringUtils.trim(timeZoneId));
    }

    /**
     * @param zip
     *            The zip to set.
     */
    public void setZip(String zip) {
        if (!fixedProfileFields.contains(UserProfileFields.ZIP.name())) {
            getUserProfile().setZip(StringUtils.trim(zip));
        }
    }
}
