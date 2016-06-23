package com.communote.server.persistence.user;

import org.apache.commons.lang.StringUtils;

import com.communote.server.model.user.Contact;
import com.communote.server.model.user.Country;
import com.communote.server.model.user.UserProfile;

/**
 * @see com.communote.server.model.user.UserProfile
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileDaoImpl extends UserProfileDaoBase {

    /**
     * @param source
     *            the source vo
     * @param target
     *            the entity
     * @param copyIfNull
     *            true if copy always (overwrite)
     */
    private void copyAddress(UserProfileVO source, UserProfile target, boolean copyIfNull) {
        if (target.getContact() == null) {
            return;
        }
        if (copyIfNull || source.getStreet() != null) {
            target.getContact().setStreet(source.getStreet());
        }
        if (copyIfNull || source.getZip() != null) {
            target.getContact().setZip(source.getZip());
        }
        if (copyIfNull || source.getCity() != null) {
            target.getContact().setCity(source.getCity());
        }
    }

    /**
     * @param source
     *            the source vo
     * @param target
     *            the entity
     * @param copyIfNull
     *            true if copy always (overwrite)
     * @param contactSet
     *            true if the contact is set
     * @param countrySet
     *            true if the country ist set
     */
    private void copyData(UserProfileVO source, UserProfile target, boolean copyIfNull,
            boolean contactSet, boolean countrySet) {
        if (target.getContact() == null
                && (!copyIfNull || (copyIfNull && (contactSet || countrySet)))) {
            target.setContact(Contact.Factory.newInstance());
        }
        copyAddress(source, target, copyIfNull);
        copyPhoneFax(source, target, copyIfNull);
    }

    /**
     * @param source
     *            the source vo
     * @param target
     *            the entity
     * @param copyIfNull
     *            true if copy always (overwrite)
     */
    private void copyPhoneFax(UserProfileVO source, UserProfile target, boolean copyIfNull) {
        if (target.getContact() == null) {
            return;
        }
        if (copyIfNull || source.getPhone() != null) {
            target.getContact().setPhone(source.getPhone());
        }
        if (copyIfNull || source.getFax() != null) {
            target.getContact().setFax(source.getFax());
        }
    }

    /**
     * @param source
     *            the vo
     * @return true if the contact data is set
     */
    private boolean isContactSet(UserProfileVO source) {
        return source.getStreet() != null || source.getZip() != null || source.getPhone() != null
                || source.getFax() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toUserProfileVO(com.communote.server.model.user.UserProfile source,
            com.communote.server.persistence.user.UserProfileVO target) {
        super.toUserProfileVO(source, target);
        if (source.getContact() != null) {
            Contact contact = source.getContact();
            target.setStreet(contact.getStreet());
            target.setZip(contact.getZip());
            target.setCity(contact.getCity());
            target.setPhone(contact.getPhone());
            target.setFax(contact.getFax());
            if (contact.getCountry() != null) {
                Country country = contact.getCountry();
                target.setCountryCode(country.getCountryCode());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserProfile userProfileVOToEntity(UserProfileVO userProfileVO) {
        UserProfile profile = UserProfile.Factory.newInstance();
        userProfileVOToEntity(userProfileVO, profile, false);
        return profile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void userProfileVOToEntity(UserProfileVO source, UserProfile target, boolean copyIfNull) {
        // TimeZone cannot be empty
        if (StringUtils.isBlank(source.getTimeZoneId())) {
            source.setTimeZoneId(null);
        }

        super.userProfileVOToEntity(source, target, copyIfNull);

        boolean contactSet = isContactSet(source);
        boolean countrySet = source.getCountryCode() != null;
        if (contactSet || countrySet || copyIfNull) {
            copyData(source, target, copyIfNull, contactSet, countrySet);
        }

    }

}
