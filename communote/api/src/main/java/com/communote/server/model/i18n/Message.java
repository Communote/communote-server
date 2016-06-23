package com.communote.server.model.i18n;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class Message implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.i18n.Message}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.i18n.Message}.
         */
        public static com.communote.server.model.i18n.Message newInstance() {
            return new com.communote.server.model.i18n.MessageImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.i18n.Message}, taking all
         * required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.i18n.Message newInstance(String messageKey,
                String message, boolean isHtml) {
            final com.communote.server.model.i18n.Message entity = new com.communote.server.model.i18n.MessageImpl();
            entity.setMessageKey(messageKey);
            entity.setMessage(message);
            entity.setIsHtml(isHtml);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.i18n.Message}, taking all
         * possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.i18n.Message newInstance(String messageKey,
                String message, boolean isHtml, com.communote.server.model.user.Language language) {
            final com.communote.server.model.i18n.Message entity = new com.communote.server.model.i18n.MessageImpl();
            entity.setMessageKey(messageKey);
            entity.setMessage(message);
            entity.setIsHtml(isHtml);
            entity.setLanguage(language);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2447725596682851578L;

    private String messageKey;

    private String message;

    private boolean isHtml;

    private Long id;

    private com.communote.server.model.user.Language language;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("messageKey='");
        sb.append(messageKey);
        sb.append("', ");

        sb.append("message='");
        sb.append(message);
        sb.append("', ");

        sb.append("isHtml='");
        sb.append(isHtml);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Message instance and all identifiers for this
     * entity equal the identifiers of the argument entity. Returns <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Message)) {
            return false;
        }
        final Message that = (Message) object;
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
     * 
     */
    public com.communote.server.model.user.Language getLanguage() {
        return this.language;
    }

    /**
     * <p>
     * The message.
     * </p>
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * <p>
     * The key for the message
     * </p>
     */
    public String getMessageKey() {
        return this.messageKey;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    /**
     * <p>
     * Specifies, if the selected value is html or not.
     * </p>
     */
    public boolean isIsHtml() {
        return this.isHtml;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIsHtml(boolean isHtml) {
        this.isHtml = isHtml;
    }

    public void setLanguage(com.communote.server.model.user.Language language) {
        this.language = language;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
}