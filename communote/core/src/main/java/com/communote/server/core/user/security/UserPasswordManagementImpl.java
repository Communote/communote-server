package com.communote.server.core.user.security;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.security.SecurityCodeManagement;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.common.exceptions.PasswordLengthException;
import com.communote.server.core.common.exceptions.PasswordValidationException;
import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.ForgottenPWMailMessage;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.ExternalUserPasswordChangeNotAllowedException;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.ForgottenPasswordSecurityCode;
import com.communote.server.persistence.common.security.SecurityCodeNotFoundException;
import com.communote.server.persistence.helper.dao.LazyClassLoaderHelper;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao;

@Service("userPasswordManagement")
public class UserPasswordManagementImpl implements UserPasswordManagement {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserPasswordManagementImpl.class);

    // TODO make configurable
    private static final int PASSWORD_MIN_LENGTH = 6;

    private final UserDao userDao;
    private final SecurityCodeManagement securityCodeManagement;
    private final ForgottenPasswordSecurityCodeDao forgottenPasswordSecurityCodeDao;
    private final MailSender mailSender;
    private Map<String, PasswordHashFunction> hashFunctions = new LinkedHashMap<>();
    private final String defaultHashFunctionId;

    @Autowired
    public UserPasswordManagementImpl(UserDao userDao,
            ForgottenPasswordSecurityCodeDao forgottenPasswordSecurityCodeDao,
            SecurityCodeManagement securityCodeManagement, MailSender mailSender) {
        this.userDao = userDao;
        this.forgottenPasswordSecurityCodeDao = forgottenPasswordSecurityCodeDao;
        this.securityCodeManagement = securityCodeManagement;
        this.mailSender = mailSender;
        PasswordHashFunction hashFunction = new BcryptPasswordHashFunction();
        this.defaultHashFunctionId = hashFunction.getIdentifier();
        hashFunctions.put(hashFunction.getIdentifier(), hashFunction);
        hashFunction = new Md5PasswordHashFunction();
        hashFunctions.put(hashFunction.getIdentifier(), hashFunction);
    }

    private void assertLocalUser(User user) throws ExternalUserPasswordChangeNotAllowedException {
        ClientConfigurationProperties clientConfigurationProperties = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();
        String primaryExternalAuthentication = clientConfigurationProperties
                .getPrimaryExternalAuthentication();
        if (primaryExternalAuthentication != null) {
            if (user.hasExternalAuthentication(primaryExternalAuthentication)) {
                throw new ExternalUserPasswordChangeNotAllowedException(user.getAlias(),
                        primaryExternalAuthentication);
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void changePassword(Long userId, String newPassword)
            throws PasswordValidationException, UserNotFoundException, AuthorizationException,
            ExternalUserPasswordChangeNotAllowedException {
        if (userId == null) {
            throw new IllegalArgumentException("User ID and password must not be null.");
        }
        User user = userDao.load(userId);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + userId + " does not exist");
        }
        Long currentUserId = SecurityHelper.getCurrentUserId();
        if (userId.equals(currentUserId) || SecurityHelper.isClientManager()
                || SecurityHelper.isInternalSystem()) {
            changePassword(user, newPassword);
        } else {
            throw new AuthorizationException("The current user " + currentUserId
                    + " is not allowed to change the password of user " + user.getAlias());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void changePassword(String securityCode, String password)
            throws SecurityCodeNotFoundException, PasswordValidationException,
            ExternalUserPasswordChangeNotAllowedException {
        SecurityCode code = securityCodeManagement.findByCode(securityCode);
        if (code == null) {
            throw new SecurityCodeNotFoundException(
                    "Security code " + securityCode + " does not exist");
        }
        try {
            LazyClassLoaderHelper.deproxy(code, ForgottenPasswordSecurityCode.class);
        } catch (ClassCastException e) {
            throw new SecurityCodeNotFoundException(
                    "Forgotten password security code " + securityCode + " does not exist");
        }
        changePassword(code.getUser(), password);
        securityCodeManagement.deleteAllCodesByUser(code.getUser().getId(),
                ForgottenPasswordSecurityCode.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void changePassword(User user, String newPassword)
            throws PasswordValidationException, ExternalUserPasswordChangeNotAllowedException {
        validatePassword(newPassword);
        assertLocalUser(user);
        user.setPassword(getHash(newPassword));
        // TODO send event (who changed password of whom)?
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean checkAndUpdatePassword(User user, String password) {
        if (checkPassword(user, password)) {
            String updatedHash = getUpdatedHash(user.getPassword(), password);
            if (updatedHash != null) {
                user.setPassword(updatedHash);
            }
            return true;
        }
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public boolean checkPassword(Long userId, String password) {
        User user = userDao.load(userId);
        if (user != null) {
            return checkPassword(user, password);
        }
        return false;
    }

    private boolean checkPassword(String passwordHash, String password) {
        if (passwordHash == null) {
            return password == null;
        }
        if (password != null) {
            PasswordHashFunction hashFunction = getHashFunction(passwordHash);
            if (hashFunction != null) {
                return hashFunction.check(passwordHash, password);
            }
            LOGGER.warn("Found no suitable hash function for checking a password");
        }
        return false;
    }

    @Override
    public boolean checkPassword(User user, String password) {
        return checkPassword(user.getPassword(), password);
    }

    private PasswordHashFunction getConfiguredHashFunction() {
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties();
        String functionId = props
                .getProperty(ApplicationPropertyUserPassword.LOCAL_USER_PASSWORD_HASH_FUNCTION);
        if (StringUtils.isBlank(functionId)) {
            functionId = defaultHashFunctionId;
        }
        PasswordHashFunction hashFunction = hashFunctions.get(functionId);
        if (hashFunction == null) {
            LOGGER.warn(
                    "Password hash function with ID '{}' does not exist, falling back to default '{}'",
                    functionId, defaultHashFunctionId);
            hashFunction = hashFunctions.get(defaultHashFunctionId);
        }
        return hashFunction;
    }

    private String getHash(String password) {
        if (password == null) {
            return null;
        }
        PasswordHashFunction hashFunction = getConfiguredHashFunction();
        return hashFunction.generate(password);
    }

    private PasswordHashFunction getHashFunction(String passwordHash) {
        for (PasswordHashFunction hashFunction : hashFunctions.values()) {
            if (hashFunction.canHandle(passwordHash)) {
                return hashFunction;
            }
        }
        return null;
    }

    private String getUpdatedHash(String passwordHash, String password) {
        if (password == null) {
            return null;
        }
        boolean needsUpdate = true;
        PasswordHashFunction hashFunction;
        if (passwordHash == null) {
            hashFunction = getConfiguredHashFunction();
        } else {
            hashFunction = getHashFunction(passwordHash);
            if (hashFunction == null) {
                hashFunction = getConfiguredHashFunction();
                LOGGER.debug(
                        "Found no hash function for an existing password. Using '{}' for update",
                        hashFunction.getIdentifier());
            } else {
                PasswordHashFunction configuredHashFunction = getConfiguredHashFunction();
                if (configuredHashFunction.getIdentifier().equals(hashFunction.getIdentifier())) {
                    needsUpdate = hashFunction.needsUpdate(passwordHash);
                } else {
                    LOGGER.debug("Password was hashed with '{}'. Using '{}' for update",
                            hashFunction.getIdentifier(), configuredHashFunction.getIdentifier());
                    hashFunction = configuredHashFunction;
                }
            }
        }
        if (needsUpdate) {
            return hashFunction.generate(password);
        }
        return passwordHash;
    }

    @Override
    public synchronized void register(PasswordHashFunction hashFunction) {
        String id = hashFunction.getIdentifier();
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException(
                    "Identifier is invalid. The identifier must have at least one character.");
        }
        if (!hashFunctions.containsKey(id)) {
            HashMap<String, PasswordHashFunction> newFunctions = new LinkedHashMap<>();
            newFunctions.put(id, hashFunction);
            newFunctions.putAll(hashFunctions);
            hashFunctions = newFunctions;
        } else {
            LOGGER.warn("There is already a password hash function with ID '{}'", id);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void requestPasswordChange(String email)
            throws UserNotFoundException, ExternalUserPasswordChangeNotAllowedException {
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User with email '" + email + "' does not exist");
        }
        assertLocalUser(user);
        ForgottenPasswordSecurityCode code = forgottenPasswordSecurityCodeDao.createCode(user);
        ForgottenPWMailMessage message = new ForgottenPWMailMessage(user, code);
        mailSender.send(message);
    }

    @Override
    public synchronized void unregister(PasswordHashFunction hashFunction) {
        String id = hashFunction.getIdentifier();
        if (hashFunctions.containsKey(id)) {
            if (defaultHashFunctionId.equals(id)) {
                throw new IllegalArgumentException("The default hash function "
                        + defaultHashFunctionId + " cannot be removed");
            }
            HashMap<String, PasswordHashFunction> newFunctions = new LinkedHashMap<>(hashFunctions);
            newFunctions.remove(id);
            hashFunctions = newFunctions;
        } else {
            LOGGER.warn("There is no password hash function with ID {}", id);
        }
    }

    @Override
    public void validatePassword(String newPassword) throws PasswordValidationException {
        if (StringUtils.isEmpty(newPassword)) {
            throw new PasswordLengthException(PASSWORD_MIN_LENGTH, 0);
        }
        if (newPassword.length() < PASSWORD_MIN_LENGTH) {
            throw new PasswordLengthException(PASSWORD_MIN_LENGTH, newPassword.length());
        }
    }
}
