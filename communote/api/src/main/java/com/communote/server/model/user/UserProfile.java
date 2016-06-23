package com.communote.server.model.user;

/**
 * <p>
 * A profile of a user
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfile implements java.io.Serializable, com.communote.server.model.user.UserName {
    /**
     * Constructs new instances of {@link com.communote.server.model.user.UserProfile}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.user.UserProfile}.
         */
        public static com.communote.server.model.user.UserProfile newInstance() {
            return new com.communote.server.model.user.UserProfile();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.UserProfile}, taking
         * all required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.user.UserProfile newInstance(
                java.sql.Timestamp lastModificationDate,
                com.communote.server.model.user.NotificationConfig notificationConfig) {
            final com.communote.server.model.user.UserProfile entity = new com.communote.server.model.user.UserProfile();
            entity.setLastModificationDate(lastModificationDate);
            entity.setNotificationConfig(notificationConfig);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.UserProfile}, taking
         * all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.UserProfile newInstance(String lastName,
                String salutation, String position, String company, String firstName,
                java.sql.Timestamp lastModificationDate,
                java.sql.Timestamp lastPhotoModificationDate, String timeZoneId,
                com.communote.server.model.user.UserImage smallImage,
                com.communote.server.model.user.Contact contact,
                com.communote.server.model.user.UserImage mediumImage,
                com.communote.server.model.user.UserImage largeImage,
                com.communote.server.model.user.NotificationConfig notificationConfig) {
            final com.communote.server.model.user.UserProfile entity = new com.communote.server.model.user.UserProfile();
            entity.setLastName(lastName);
            entity.setSalutation(salutation);
            entity.setPosition(position);
            entity.setCompany(company);
            entity.setFirstName(firstName);
            entity.setLastModificationDate(lastModificationDate);
            entity.setLastPhotoModificationDate(lastPhotoModificationDate);
            entity.setTimeZoneId(timeZoneId);
            entity.setSmallImage(smallImage);
            entity.setContact(contact);
            entity.setMediumImage(mediumImage);
            entity.setLargeImage(largeImage);
            entity.setNotificationConfig(notificationConfig);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6710219306732381741L;

    private String lastName;

    private String salutation;

    private String position;

    private String company;

    private String firstName;

    private java.sql.Timestamp lastModificationDate;

    private java.sql.Timestamp lastPhotoModificationDate;

    private String timeZoneId;

    private Long id;

    private com.communote.server.model.user.UserImage smallImage;

    private com.communote.server.model.user.Contact contact;

    private com.communote.server.model.user.UserImage mediumImage;

    private com.communote.server.model.user.UserImage largeImage;

    private com.communote.server.model.user.NotificationConfig notificationConfig;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("lastName='");
        sb.append(lastName);
        sb.append("', ");

        sb.append("salutation='");
        sb.append(salutation);
        sb.append("', ");

        sb.append("position='");
        sb.append(position);
        sb.append("', ");

        sb.append("company='");
        sb.append(company);
        sb.append("', ");

        sb.append("firstName='");
        sb.append(firstName);
        sb.append("', ");

        sb.append("lastModificationDate='");
        sb.append(lastModificationDate);
        sb.append("', ");

        sb.append("lastPhotoModificationDate='");
        sb.append(lastPhotoModificationDate);
        sb.append("', ");

        sb.append("timeZoneId='");
        sb.append(timeZoneId);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an UserProfile instance and all identifiers for
     * this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof UserProfile)) {
            return false;
        }
        final UserProfile that = (UserProfile) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
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
    public com.communote.server.model.user.Contact getContact() {
        return this.contact;
    }

    /**
     *
     */
    @Override
    public String getFirstName() {
        return this.firstName;
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
    public com.communote.server.model.user.UserImage getLargeImage() {
        return this.largeImage;
    }

    /**
     * <p>
     * Date of the last modification of the user profile
     * </p>
     */
    public java.sql.Timestamp getLastModificationDate() {
        return this.lastModificationDate;
    }

    /**
     *
     */
    @Override
    public String getLastName() {
        return this.lastName;
    }

    /**
     * <p>
     * Date of the last modification of the user photo
     * </p>
     */
    public java.sql.Timestamp getLastPhotoModificationDate() {
        return this.lastPhotoModificationDate;
    }

    /**
     *
     */
    public com.communote.server.model.user.UserImage getMediumImage() {
        return this.mediumImage;
    }

    /**
     *
     */
    public com.communote.server.model.user.NotificationConfig getNotificationConfig() {
        return this.notificationConfig;
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
    @Override
    public String getSalutation() {
        return this.salutation;
    }

    /**
     *
     */
    public com.communote.server.model.user.UserImage getSmallImage() {
        return this.smallImage;
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
     * Returns a hash code based on this entity's identifiers.
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setContact(com.communote.server.model.user.Contact contact) {
        this.contact = contact;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLargeImage(com.communote.server.model.user.UserImage largeImage) {
        this.largeImage = largeImage;
    }

    public void setLastModificationDate(java.sql.Timestamp lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLastPhotoModificationDate(java.sql.Timestamp lastPhotoModificationDate) {
        this.lastPhotoModificationDate = lastPhotoModificationDate;
    }

    public void setMediumImage(com.communote.server.model.user.UserImage mediumImage) {
        this.mediumImage = mediumImage;
    }

    public void setNotificationConfig(
            com.communote.server.model.user.NotificationConfig notificationConfig) {
        this.notificationConfig = notificationConfig;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public void setSmallImage(com.communote.server.model.user.UserImage smallImage) {
        this.smallImage = smallImage;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }
}