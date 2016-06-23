package com.communote.server.model.user;

/**
 * <p>
 * Represents a language
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class Language implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.user.Language}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.user.Language}.
         */
        public static com.communote.server.model.user.Language newInstance() {
            return new com.communote.server.model.user.LanguageImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.Language}, taking all
         * possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.Language newInstance(String languageCode,
                String name, String nameLocalized) {
            final com.communote.server.model.user.Language entity = new com.communote.server.model.user.LanguageImpl();
            entity.setLanguageCode(languageCode);
            entity.setName(name);
            entity.setNameLocalized(nameLocalized);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 4257637226170635212L;

    private String languageCode;

    private String name;

    private String nameLocalized;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("languageCode='");
        sb.append(languageCode);
        sb.append("', ");

        sb.append("name='");
        sb.append(name);
        sb.append("', ");

        sb.append("nameLocalized='");
        sb.append(nameLocalized);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Language instance and all identifiers for
     * this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Language)) {
            return false;
        }
        final Language that = (Language) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * The code of the language
     * </p>
     */
    public String getLanguageCode() {
        return this.languageCode;
    }

    /**
     * <p>
     * The name of language
     * </p>
     */
    public String getName() {
        return this.name;
    }

    /**
     * <p>
     * The localized name of the country
     * </p>
     */
    public String getNameLocalized() {
        return this.nameLocalized;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNameLocalized(String nameLocalized) {
        this.nameLocalized = nameLocalized;
    }
}