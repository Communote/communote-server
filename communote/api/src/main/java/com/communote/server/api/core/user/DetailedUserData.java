package com.communote.server.api.core.user;

import java.util.Date;
import java.util.TimeZone;

/**
 * Value object holding detailed user data.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DetailedUserData extends com.communote.server.api.core.user.UserData implements
        java.io.Serializable {

    private static final long serialVersionUID = -3501778644845596561L;

    private Date lastModificationDate;

    private Date lastPhotoModificationDate;

    private TimeZone effectiveUserTimeZone;

    /**
     * Constructor.
     */
    public DetailedUserData() {
        super();
        this.lastModificationDate = null;
    }

    /**
     * Copies constructor from other UserData
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public DetailedUserData(DetailedUserData otherBean) {
        this(otherBean.getLastModificationDate(), otherBean.getLastPhotoModificationDate(),
                otherBean.getId(), otherBean.getEmail(), otherBean.getAlias(), otherBean
                .getFirstName(), otherBean.getLastName(), otherBean.getSalutation(),
                otherBean.getStatus());
    }

    /**
     * Contructor
     *
     * @param lastModificationDate
     *            of user
     * @param lastPhotoModificationDate
     *            of user
     * @param userId
     *            of user
     * @param email
     *            of user
     * @param alias
     *            of user
     * @param firstName
     *            of user
     * @param lastName
     *            of user
     * @param salutation
     *            of user
     * @param status
     *            of user
     */
    public DetailedUserData(java.util.Date lastModificationDate,
            java.util.Date lastPhotoModificationDate, Long userId, String email, String alias,
            String firstName, String lastName, String salutation,
            com.communote.server.model.user.UserStatus status) {
        super(userId, email, alias, firstName, lastName, salutation, status);
        this.lastModificationDate = lastModificationDate;
        this.lastPhotoModificationDate = lastPhotoModificationDate;
    }

    /**
     * Contructor
     *
     * @param lastModificationDate
     *            of user
     * @param userId
     *            of user
     * @param email
     *            of user
     * @param alias
     *            of user
     * @param firstName
     *            of user
     * @param lastName
     *            of user
     * @param salutation
     *            of user
     * @param status
     *            of user
     */
    public DetailedUserData(java.util.Date lastModificationDate, Long userId, String email,
            String alias, String firstName, String lastName, String salutation,
            com.communote.server.model.user.UserStatus status) {
        super(userId, email, alias, firstName, lastName, salutation, status);
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * @return the effectiveUserTimeZone
     */
    public TimeZone getEffectiveUserTimeZone() {
        return effectiveUserTimeZone;
    }

    /**
     * <p>
     * Date of the last modification happened to the user data
     * </p>
     *
     * @return last modification date
     */
    public java.util.Date getLastModificationDate() {
        return this.lastModificationDate;
    }

    /**
     * <p>
     * Date of the last modification happened to the users photo
     * </p>
     *
     * @return last photo modification date
     */
    public java.util.Date getLastPhotoModificationDate() {
        return this.lastPhotoModificationDate;
    }

    /**
     * @param effectiveUserTimeZone
     *            the effectiveUserTimeZone to set
     */
    public void setEffectiveUserTimeZone(TimeZone effectiveUserTimeZone) {
        this.effectiveUserTimeZone = effectiveUserTimeZone;
    }

    public void setLastModificationDate(java.util.Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public void setLastPhotoModificationDate(java.util.Date lastPhotoModificationDate) {
        this.lastPhotoModificationDate = lastPhotoModificationDate;
    }

}