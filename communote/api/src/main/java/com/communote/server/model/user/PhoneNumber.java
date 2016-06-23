package com.communote.server.model.user;

/**
 * <p>
 * Scheme for a phone number, given by the address.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class PhoneNumber implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 8526987464352952845L;

    /**
     * Creates a new instance from other PhoneNumber instance.
     */
    public static PhoneNumber newInstance(PhoneNumber otherObject) {
        if (otherObject != null) {
            return newInstance(otherObject.getCountryCode(), otherObject.getAreaCode(),
                    otherObject.getPhoneNumber());
        }
        return null;
    }

    /**
     * Creates a new instance of {@link PhoneNumber} taking all properties.
     */
    public static PhoneNumber newInstance(String CountryCode, String areaCode, String phoneNumber) {
        PhoneNumberImpl object = new PhoneNumberImpl();
        object.setCountryCode(CountryCode);
        object.setAreaCode(areaCode);
        object.setPhoneNumber(phoneNumber);
        object.initialize();
        return object;
    }

    private String CountryCode;

    private String areaCode;

    private String phoneNumber;

    protected PhoneNumber() {
    }

    /**
     * Indicates if the argument is of the same type and all values are equal.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PhoneNumber)) {
            return false;
        }
        final PhoneNumber that = (PhoneNumber) object;
        if (this.CountryCode == null || that.getCountryCode() == null
                || !this.CountryCode.equals(that.getCountryCode())) {
            return false;
        }
        if (this.areaCode == null || that.getAreaCode() == null
                || !this.areaCode.equals(that.getAreaCode())) {
            return false;
        }
        if (this.phoneNumber == null || that.getPhoneNumber() == null
                || !this.phoneNumber.equals(that.getPhoneNumber())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * Areacode for this Phone number.
     * </p>
     */
    public String getAreaCode() {
        return this.areaCode;
    }

    /**
     * <p>
     * CountryCode for this phone number
     * </p>
     */
    public String getCountryCode() {
        return this.CountryCode;
    }

    /**
     * <p>
     * The Phone number itself without any other code.
     * </p>
     */
    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (CountryCode == null ? 0 : CountryCode.hashCode());
        hashCode = 29 * hashCode + (areaCode == null ? 0 : areaCode.hashCode());
        hashCode = 29 * hashCode + (phoneNumber == null ? 0 : phoneNumber.hashCode());

        return hashCode;
    }

    /**
     * Hook for initializing the object in the subclass
     */
    protected void initialize() {
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public void setCountryCode(String CountryCode) {
        this.CountryCode = CountryCode;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}