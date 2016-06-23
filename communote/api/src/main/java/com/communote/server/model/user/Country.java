package com.communote.server.model.user;

/**
 * <p>
 * Represents a country.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class Country implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.user.Country}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.user.Country}.
         */
        public static com.communote.server.model.user.Country newInstance() {
            return new com.communote.server.model.user.CountryImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.Country}, taking all
         * possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.Country newInstance(String countryCode,
                String name) {
            final com.communote.server.model.user.Country entity = new com.communote.server.model.user.CountryImpl();
            entity.setCountryCode(countryCode);
            entity.setName(name);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2970311908473374097L;

    private String countryCode;

    private String name;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("countryCode='");
        sb.append(countryCode);
        sb.append("', ");

        sb.append("name='");
        sb.append(name);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Country instance and all identifiers for this
     * entity equal the identifiers of the argument entity. Returns <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Country)) {
            return false;
        }
        final Country that = (Country) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * The code of the country
     * </p>
     */
    public String getCountryCode() {
        return this.countryCode;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * The name of the country
     * </p>
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}