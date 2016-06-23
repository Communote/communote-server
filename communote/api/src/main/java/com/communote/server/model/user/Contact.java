package com.communote.server.model.user;

/**
 * <p>
 * Contact details for the associated user.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class Contact implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.user.Contact}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.user.Contact}.
         */
        public static com.communote.server.model.user.Contact newInstance() {
            return new com.communote.server.model.user.ContactImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.Contact}, taking all
         * possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.Contact newInstance(String street,
                String zip, String city, com.communote.server.model.user.PhoneNumber phone,
                com.communote.server.model.user.PhoneNumber fax,
                com.communote.server.model.user.Country country) {
            final com.communote.server.model.user.Contact entity = new com.communote.server.model.user.ContactImpl();
            entity.setStreet(street);
            entity.setZip(zip);
            entity.setCity(city);
            entity.setPhone(phone);
            entity.setFax(fax);
            entity.setCountry(country);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 9028982793802942505L;

    private String street;

    private String zip;

    private String city;

    private com.communote.server.model.user.PhoneNumber phone;

    private com.communote.server.model.user.PhoneNumber fax;

    private Long id;

    private com.communote.server.model.user.Country country;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("street='");
        sb.append(street);
        sb.append("', ");

        sb.append("zip='");
        sb.append(zip);
        sb.append("', ");

        sb.append("city='");
        sb.append(city);
        sb.append("', ");

        sb.append("phone='");
        sb.append(phone);
        sb.append("', ");

        sb.append("fax='");
        sb.append(fax);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Contact instance and all identifiers for this
     * entity equal the identifiers of the argument entity. Returns <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Contact)) {
            return false;
        }
        final Contact that = (Contact) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * Cityname of the user address.
     * </p>
     */
    public String getCity() {
        return this.city;
    }

    /**
     * 
     */
    public com.communote.server.model.user.Country getCountry() {
        return this.country;
    }

    /**
     * <p>
     * Faxnumber of the user address with the scheme - countryCode cityCode phoneNumber, which is
     * given by the EmbeddedValue PhoneNumber.
     * </p>
     */
    public com.communote.server.model.user.PhoneNumber getFax() {
        return this.fax;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * Phonenumber of the user address with the scheme - countryCode cityCode phoneNumber, which is
     * given by the EmbeddedValue PhoneNumber.
     * </p>
     */
    public com.communote.server.model.user.PhoneNumber getPhone() {
        return this.phone;
    }

    /**
     * <p>
     * Street and house number of the user address.
     * </p>
     */
    public String getStreet() {
        return this.street;
    }

    /**
     * <p>
     * Zip code of the user address.
     * </p>
     */
    public String getZip() {
        return this.zip;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(com.communote.server.model.user.Country country) {
        this.country = country;
    }

    public void setFax(com.communote.server.model.user.PhoneNumber fax) {
        this.fax = fax;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPhone(com.communote.server.model.user.PhoneNumber phone) {
        this.phone = phone;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}