package com.communote.server.core.user;

import java.util.Locale;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.Converter;
import com.communote.common.util.Orderable;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.common.exceptions.InvalidOperationException;
import com.communote.server.core.common.exceptions.PasswordLengthException;
import com.communote.server.core.security.UserIdentification;
import com.communote.server.core.user.validation.UserActivationValidationException;
import com.communote.server.core.user.validation.UserActivationValidator;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.common.security.SecurityCodeNotFoundException;
import com.communote.server.persistence.user.ExternalUserVO;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface UserManagement {
    /**
     * Possible registration types.
     */
    public enum RegistrationType {
        /** Used, when the user registered himself. */
        SELF,
        /** Used, when the user was invited. */
        INVITED;
    }

    /** Prefix for user aliases for anonymized users. */
    public static final String ANONYMIZE_USER_PREFIX = "_";

    /**
     * Accept the terms of use for the given user. If the user has status TERMS_NOT_ACCEPTED the
     * status is changed to ACTIVE. If the user is already ACTIVE a user property which contains the
     * revision of the accepted terms of use is updated. The provided ID must be that of the current
     * user or there should be no current user or the internal system user.
     *
     * @param userId
     *            ID of the user whose ID should be accepted
     * @throws AuthorizationException
     *             in case there is a current user which is not the internal system user and this
     *             user does not have the provided ID
     * @throws UserNotFoundException
     *             in case the user with the given ID does not exist
     * @throws UserActivationValidationException
     *             in case the user has status <code>TERMS_NOT_ACCEPTED</code> but cannot be
     *             activated after accepting the terms
     */
    public void acceptTermsOfUse(Long userId) throws AuthorizationException, UserNotFoundException,
    UserActivationValidationException;

    /**
     * Delete a user by anonymizing his profile and removing the content (notes, topics if possible)
     * he created. The user will have status DELETED afterwards and cannot be restored.
     *
     * @param userId
     *            ID of the user to remove
     * @param blogIds
     *            an array of IDs of topics in which the user to remove is the only manager. If the
     *            current user is not a client manager this parameter is ignored.
     * @param becomeManager
     *            whether the current user, which has to be a client manager, should become manager
     *            of the topics identified by the IDs in blogIds array. If false the topics will be
     *            deleted.
     * @throws AuthorizationException
     *             in case the current user is not a client manager or the user to remove
     * @throws NoClientManagerLeftException
     *             in case the user to delete is the last active client manager
     * @throws UserDeletionDisabledException
     *             in case the anonymization of users is disabled and the current user is not a
     *             client manager
     * @throws NoBlogManagerLeftException
     *             in case there are topics which are managed by the user to remove and have
     *             additional users with read access but no other managers. If the current user is
     *             client manager this exception will only be thrown if the IDs of the topics are
     *             not in the blogIds array. The exception will contain the IDs of the topics that
     *             would become manager-less.
     */
    public void anonymizeUser(Long userId, Long[] blogIds, boolean becomeManager)
            throws AuthorizationException, NoClientManagerLeftException,
            UserDeletionDisabledException, NoBlogManagerLeftException;

    /**
     * Assign the user role to the user
     *
     * @param userId
     *            the id of the user
     * @param role
     *            the role to assign
     * @return the final user
     * @throws AuthorizationException
     *             in case the current user is not a client manager and thus not allowed
     * @throws InvalidOperationException
     *             in case the user is already a system or a crawl user or the role to assign is the
     *             system or crawl user role
     */
    public User assignUserRole(Long userId, UserRole role) throws AuthorizationException,
    InvalidOperationException;

    /**
     * Method to change the users alias.
     *
     * @param userId
     *            The users id.
     * @param newAlias
     *            The new alias.
     * @return True, when the alias was changed, else false.
     * @throws AliasAlreadyExistsException
     *             Thrown, when there is already a user with this alias.
     * @throws AliasInvalidException
     *             Thrown, when the alias is syntactically invalid.
     */
    public boolean changeAlias(Long userId, String newAlias) throws AliasAlreadyExistsException,
            AliasInvalidException;

    /**
     * Changes the email address of an user and validates this new address.
     *
     * @param userId
     *            Id of the user, which email should be changed.
     * @param newEmail
     *            The new email address.
     * @param sendConfirmationLink
     *            If true an confirmation link will be send to the user to confirm the new address.
     *            False is only possible, if the current use an administrator.
     * @return True, when the email was changed, else false.
     */
    public boolean changeEmailAddress(Long userId, String newEmail, boolean sendConfirmationLink)
            throws EmailValidationException, EmailAlreadyExistsException;

    /**
     * Changes the password of an user.
     *
     * @param userId
     *            If of the user, which password should be changed.
     */
    public void changePassword(Long userId, String newPassword) throws PasswordLengthException;

    /**
     * Change the status of a user.
     * <p>
     * Note: Attempts to set the user status to <code>DELETED</code> or
     * <code>PERMANENTLY_DISABLED</code> will result in an InvalidUserStatusTransitionException.
     * Such a status change can only be achieved by invoking the appropriate service methods
     * {@link #anonymizeUser(Long, Long[], boolean)} or
     * {@link #permanentlyDisableUser(Long, Long[], boolean)}.
     * </p>
     *
     * @param userId
     *            the ID of the user whose status should be changed
     * @param newStatus
     *            the new status to set
     * @throws AuthorizationException
     *             in case the current user is not a client manager
     * @throws UserNotFoundException
     *             in case there is no user with the provided ID
     * @throws InvalidUserStatusTransitionException
     *             in case new status cannot be set. This will for example happen if the it is one
     *             of <code>DELETED</code> or <code>PERMANENTLY_DISABLED</code>
     * @throws NoClientManagerLeftException
     *             in case the user to change is the only client manager with status
     *             <code>ACTIVE</code> and the new status is not <code>ACTIVE</code>
     * @throws UserActivationValidationException
     *             in case the user cannot be activated
     */
    public void changeUserStatusByManager(Long userId, UserStatus newStatus)
            throws AuthorizationException, UserNotFoundException,
            InvalidUserStatusTransitionException, NoClientManagerLeftException,
            UserActivationValidationException;

    /**
     * Confirms the new email address.
     *
     * @param securityCode
     *            The security code.
     */
    public void confirmNewEmailAddress(String securityCode) throws SecurityCodeNotFoundException,
            EmailAlreadyExistsException;

    /**
     * <p>
     * Confirms a registered or invited user, sets user data and change status to active or
     * confirmed.
     * </p>
     */
    public User confirmUser(String securitycode, UserVO user) throws SecurityCodeNotFoundException,
    EmailValidationException, EmailAlreadyExistsException, AliasAlreadyExistsException,
    PasswordLengthException, InvalidUserStatusTransitionException;

    /**
     * Creates or updates a user with data retrieved from a external user repository (e.g. via
     * LDAP). If the user does not yet exist it will be created, otherwise updated. In case of an
     * update the null values of the VO are ignored. In case of a create the null values will be set
     * to appropriate defaults.
     *
     * @param userVO
     *            VO with details of the user to create
     * @return the modified user
     * @throws AliasAlreadyExistsException
     *             in case the alias is already assigned to another user
     * @throws EmailAlreadyExistsException
     *             in case the email address is already assigned to another user
     * @throws EmailValidationException
     *             in case the email address is not valid
     * @throws InvalidUserStatusTransitionException
     *             in case the user exists and the old status of that user cannot be updated with
     *             the new status
     * @throws NoClientManagerLeftException
     *             in case the user exists and is the only active client manager and the new status
     *             is not ACTIVE
     * @throws UserActivationValidationException
     *             in case the user cannot be updated
     * @throws PermanentIdMissmatchException
     */
    public User createOrUpdateExternalUser(ExternalUserVO userVO)
            throws AliasAlreadyExistsException, EmailValidationException,
            EmailAlreadyExistsException, UserActivationValidationException,
            InvalidUserStatusTransitionException, NoClientManagerLeftException,
            PermanentIdMissmatchException;

    /**
     * Create a user based on the parameters. If the user already exists, an exception is thrown.
     *
     * @param user
     *            the VO describing the user to create
     * @param emailConfirmationRequired
     *            whether the user should confirm the provided email address. If true a confirmation
     *            email will be sent to the email address.
     * @param managerConfirmationRequired
     *            whether a client manager has to confirm and activate the user. If true the
     *            managers will be performed by email about the new user.
     * @return the new user
     * @throws EmailAlreadyExistsException
     *             in case the provided email address already exists
     * @throws EmailValidationException
     *             in case the provided email address is not valid
     * @throws AliasAlreadyExistsException
     *             in case the provided user alias already exists
     * @throws PasswordLengthException
     *             in case the provided password is not long enough
     */
    public User createUser(UserVO user, boolean emailConfirmationRequired,
            boolean managerConfirmationRequired) throws EmailAlreadyExistsException,
            EmailValidationException, AliasAlreadyExistsException, PasswordLengthException;

    /**
     * Returns a collection with all users that do not have one of the deleted statuses.
     *
     * @param whether
     *            to exclude the users who have the system user role
     */
    public java.util.Collection<User> findNotDeletedUsers(boolean excludeSystemUsers);

    /**
     * Find the User by the given identification, not loading the {@link ExternalUserAuthentication}
     * s
     *
     * @param userIdentification
     *            the identification
     * @return the user found
     */
    public User findUser(UserIdentification userIdentification);

    /**
     * Find the User by the given identification
     *
     * @param userIdentification
     *            the identification
     * @param loadExternalAuthentications
     *            true if the external auths of the user should be loaded as well
     * @return the user found
     */
    public User findUser(UserIdentification userIdentification, boolean loadExternalAuthentications);

    /**
     * Find the User by the given alias s
     *
     * @param alias
     *            the alias
     * @return the user found
     */
    public User findUserByAlias(String alias);

    /**
     * Find a user by the email address
     *
     * @param email
     *            address to search for.
     */
    public User findUserByEmail(String email);

    /**
     * find user by email or alias
     *
     * @param emailOrAlias
     *            May be a user alias or email address
     */
    public User findUserByEmailAlias(String emailOrAlias);

    /**
     * <p>
     * Find the user by the external system specification
     * </p>
     */
    public User findUserByExternalUserId(String externalUserId, String systemId);

    /**
     * Find a user by the its id.
     *
     * <b>Note: </b> This doesn't load the external authentications of the user. Use
     * {@link #findUserByUserId(Long, boolean)} if you need this information or better
     * {@link #getUserById(Long, Converter)}.
     *
     * @param userId
     *            Id of the user to find.
     * @deprecated Use {@link #getUserById(Long, Converter)} instead.
     */
    @Deprecated
    public User findUserByUserId(Long userId);

    /**
     * Find a user by the its id. *
     *
     * @param userId
     *            Id of the user to find.
     * @param true to load the external authentications of the user
     */
    public User findUserByUserId(Long userId, boolean loadExternalAuthentications);

    /**
     * Returns a collection of users having a specific role and optionally a specific status.
     */
    public java.util.List<User> findUsersByRole(UserRole userRole, UserStatus status);

    /**
     * Create a valid, not yet used, user alias.
     *
     * @param externalUserName
     *            the external user name to try first. Can be null.
     * @param emailAddress
     *            the email address to extract the alias from
     * @return the found alias
     */
    public String generateUniqueAlias(String externalUserName, String emailAddress);

    /**
     * @return the number of users with status ACTIVE. System users are not counted.
     */
    public long getActiveUserCount();

    /**
     * Get the number of users with status ACTIVE.
     *
     * @param systemId
     *            ID of an external system to only count the users which originate from this
     *            external system. If null only internal users which can login (have a password)
     *            will be counted.
     * @param role
     *            the role of a user. If not null only the users with that role will be counted
     *            otherwise all roles (including system users) are considered.
     * @return the number of active users
     */
    public long getActiveUserCount(String systemId, UserRole role);

    /**
     * @param userId
     *            The users id.
     * @return A set of the external authentications of the given user. This will always return a
     *         set.
     */
    @Transactional(readOnly = true)
    public Set<ExternalUserAuthentication> getExternalExternalUserAuthentications(long userId);

    /**
     * Returns the roles of a user.
     *
     * @param userId
     *            If of the user to get the roles for.
     * @return Array of the users roles.
     */
    public UserRole[] getRolesOfUser(Long userId);

    /**
     * Return the details of a user. The data of the user will be converted with the provided
     * converter and the resulting object will be returned. In case there is no user for the ID null
     * is returned.
     *
     * @param <T>
     *            the type of the resulting object
     * @param userId
     *            the ID of the user
     * @param converter
     *            the converter to use
     * @return the converted object or null if the user does not exist
     */
    public <T> T getUserById(Long userId, Converter<User, T> converter);

    /**
     * @throws BlogAccessException
     *             in case the current user is not allowed to invite a user
     * @throws AuthorizationException
     *             in case there is no current user
     */
    public User inviteUserToBlog(Long blogId, UserVO userData, BlogRole role)
            throws AliasAlreadyExistsException, EmailValidationException,
            EmailAlreadyExistsException, PermanentIdMissmatchException, BlogAccessException,
            AuthorizationException;

    public User inviteUserToClient(UserVO user) throws EmailValidationException,
    EmailAlreadyExistsException, AliasAlreadyExistsException,
    PermanentIdMissmatchException, AuthorizationException;

    /**
     * <p>
     * Deletes a user by setting his status to PERMANENTLY_DISABLED but keeping all his data.
     * </p>
     */
    public void permanentlyDisableUser(Long userId, Long[] blogIds, boolean becomeManager)
            throws AuthorizationException, NoClientManagerLeftException,
            UserDeletionDisabledException, InvalidUserStatusTransitionException,
            NoBlogManagerLeftException;

    /**
     * Register a validator that will be called before the status of a user is set to
     * <code>ACTIVE</code>. The validators are executed in the order defined by the
     * {@link Orderable} implementation. If the first validator throws an exception the validation
     * will be stopped.
     *
     * @param validator
     *            the validator to add
     */
    public void registerActivationValidator(UserActivationValidator validator);

    /**
     * Method to register an user via email address.
     *
     * @param email
     *            The users email address.
     * @param locale
     *            The locale to use for the confirmation email.
     * @param type
     *            The type of the registration.
     * @return The newly registered user.
     * @throws EmailValidationException
     *             Thrown, when the email address is not syntactically valid.
     * @throws EmailAlreadyExistsException
     *             Thrown, when the email/user address already exists.
     */
    public User registerUser(String email, Locale locale, RegistrationType type)
            throws EmailValidationException, EmailAlreadyExistsException;

    /**
     * Remove a role from a user
     *
     * @throws InvalidOperationException
     *             in case the user is a system or crawl user and the system or crawl user role
     *             should be removed
     * @throws AuthorizationException
     *             in case the current user is not client manager
     * @throws NoClientManagerLeftException
     *             in case the client manager role should be removed from the last user with that
     *             role
     *
     */
    public User removeUserRole(Long userId, UserRole role) throws AuthorizationException,
    InvalidOperationException, NoClientManagerLeftException;

    /**
     * <p>
     * resets the external user ID for users that have an external authentication which has the
     * provided external system ID and a permanentId
     * </p>
     */
    public void resetPermanentId(String systemId) throws AuthorizationException;

    /**
     * Reset the terms of use for all users which have already accepted the terms of use. This will
     * exclude users which are not active or temporarily disabled. The current user is excluded too.
     * The status of the user is not modified only the termsAccepted flag is reset.
     *
     * @throws in
     *             case the current user is not client manager or internal system user
     */
    public void resetTermsOfUse() throws AuthorizationException;

    /**
     *
     */
    public void sendNewPWLink(User user) throws ExternalUsersMayNotChangeTheirPasswordException;

    /**
     *
     */
    public void sendReminderMails();

    /**
     * Unlocks the user for the given security code.
     *
     * @param securityCode
     *            The code, which user should be unlocked.
     * @throws SecurityCodeNotFoundException
     *             Thrown, when the code doesn't exist.
     * @return The unlocked user.
     */
    public User unlockUser(String securityCode) throws SecurityCodeNotFoundException;

    /**
     * Unregister a previously registered validator.
     *
     * @param validator
     *            the validator to remove
     */
    public void unregisterActivationValidator(UserActivationValidator validator);

    /**
     * <p>
     *
     * </p>
     *
     *
     * @return the modified user or null
     *
     *
     * @throws UserActivationValidationException
     *             in case the user should be activated but activation failed
     */

    /**
     * Tries to update a user with data retrieved from an external resource (e.g. via LDAP). If the
     * user does not exist locally the update request is ignored. Otherwise his data will be updated
     * by only copying the non-null values.
     *
     * @param userVO
     *            a VO with data to update the user with
     * @return the modified user or null
     *
     * @throws AliasAlreadyExistsException
     *             in case the new alias is already assigned to another user
     * @throws EmailAlreadyExistsException
     *             in case the new email address is already assigned to another user
     * @throws EmailValidationException
     *             in case the new email address is not valid
     * @throws InvalidUserStatusTransitionException
     *             in case the old status of the user cannot be updated with the new status
     * @throws NoClientManagerLeftException
     *             in case the user is the only active client manager and the new status is not
     *             ACTIVE
     * @throws PermanentIdMissmatchException
     * @throws UserActivationValidationException
     *             in case the user cannot be activated
     */
    public User updateExternalUser(ExternalUserVO userVO) throws AliasAlreadyExistsException,
    EmailAlreadyExistsException, EmailValidationException,
    InvalidUserStatusTransitionException, NoClientManagerLeftException,
    PermanentIdMissmatchException, UserActivationValidationException;

    /**
     * Sets the language of the given user to the given language.
     *
     * @param userId
     *            The user to update.
     * @param languageCode
     *            The language to set.
     */
    public void updateLanguage(Long userId, String languageCode);

    /**
     * Updates the tags of the given user.
     *
     * @param userId
     *            The users id.
     * @param tags
     *            The tags of the user.
     */
    public void updateUserTags(Long userId, Set<TagTO> tags);

}
