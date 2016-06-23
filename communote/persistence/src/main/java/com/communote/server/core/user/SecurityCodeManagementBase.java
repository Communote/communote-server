package com.communote.server.core.user;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.security.SecurityCodeManagement;
import com.communote.server.model.security.SecurityCode;

/**
 * <p>
 * Spring Service base class for <code>SecurityCodeManagement</code>, provides access to all
 * services and entities referenced by this service.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class SecurityCodeManagementBase
        implements SecurityCodeManagement {

    @Override
    public void deleteAllCodesByUser(
            Long userId,
            Class<? extends SecurityCode> clazz) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "SecurityCodeManagement.deleteAllCodesByUSer(Long userId, Class<? extends SecurityCode> clazz) - 'userId' can not be null");
        }
        if (clazz == null) {
            throw new IllegalArgumentException(
                    "SecurityCodeManagement.deleteAllCodesByUSer(Long userId, Class<? extends SecurityCode> clazz) - 'clazz' can not be null");
        }
        try {
            this.handleDeleteAllCodesByUSer(userId, clazz);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.SecurityCodeManagementException(
                    "Error performing 'SecurityCodeManagement.deleteAllCodesByUSer(Long userId, Class<? extends SecurityCode> clazz)' --> "
                            + rt, rt);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityCode findByCode(String code) {
        if (code == null || code.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "SecurityCodeManagement.findByCode(String code) - 'code' can not be null or empty");
        }
        try {
            return this.handleFindByCode(code);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.SecurityCodeManagementException(
                    "Error performing 'SecurityCodeManagement.findByCode(String code)' --> "
                            + rt, rt);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T findByCode(String code, Converter<SecurityCode, T> converter) {
        if (code == null || code.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "SecurityCodeManagement.findByCode(String, Converter) - 'code' can not be null or empty");
        }
        if (converter == null) {
            throw new IllegalArgumentException("Converter cannot be null");
        }
        try {
            return this.handleFindByCode(code, converter);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.SecurityCodeManagementException(
                    "Error performing 'SecurityCodeManagement.findByCode(String, Converter)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #deleteAllCodesByUSer(Long, Class<? extends
     * SecurityCode>)}
     */
    protected abstract void handleDeleteAllCodesByUSer(Long userId,
            Class<? extends SecurityCode> clazz);

    /**
     * Performs the core logic for {@link #findByCode(String)}
     */
    protected abstract SecurityCode handleFindByCode(String code);

    protected abstract <T> T handleFindByCode(String code, Converter<SecurityCode, T> converter);

    /**
     * Performs the core logic for {@link #removeCode(Long)}
     */
    protected abstract void handleRemoveCode(Long id);

    /**
     * @see com.communote.server.api.core.security.SecurityCodeManagement#removeCode(Long)
     */
    @Override
    public void removeCode(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "SecurityCodeManagement.removeCode(Long id) - 'id' can not be null");
        }
        try {
            this.handleRemoveCode(id);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.SecurityCodeManagementException(
                    "Error performing 'SecurityCodeManagement.removeCode(Long id)' --> "
                            + rt, rt);
        }
    }

}