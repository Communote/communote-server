package com.communote.server.core.user.client;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.client.InvalidClientIdException;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.user.UserVO;

/**
 * <p>
 * Spring Service base class for <code>ClientManagement</code>, provides access to all services and
 * entities referenced by this service.
 * </p>
 *
 * @see ClientManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class ClientManagementBase implements ClientManagement {

    /**
     * @see ClientManagement#createGlobalClient(String, String)
     */
    @Override
    public void createGlobalClient(String clientName, String timeZoneId)
            throws InvalidClientIdException {
        if (clientName == null || clientName.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "ClientManagement.createGlobalClient(String clientName, String timeZoneId) - 'clientName' can not be null or empty");
        }
        if (timeZoneId == null || timeZoneId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "ClientManagement.createGlobalClient(String clientName, String timeZoneId) - 'timeZoneId' can not be null or empty");
        }
        try {
            this.handleCreateGlobalClient(clientName, timeZoneId);
        } catch (RuntimeException rt) {
            throw new ClientManagementException(
                    "Error performing 'ClientManagement.createGlobalClient(String clientName, String timeZoneId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #createGlobalClient(String, String)}
     */
    protected abstract void handleCreateGlobalClient(String clientName, String timeZoneId)
            throws InvalidClientIdException;

    /**
     * Performs the core logic for
     * {@link #initializeGlobalClient(com.communote.server.api.core.user.UserVO)}
     */
    protected abstract void handleInitializeGlobalClient(UserVO userVO)
            throws EmailValidationException;

    /**
     * @see ClientManagement#initializeGlobalClient(com.communote.server.api.core.user.UserVO)
     */
    @Override
    public void initializeGlobalClient(UserVO userVO)
            throws com.communote.server.api.core.common.EmailValidationException {
        if (userVO == null) {
            throw new IllegalArgumentException(
                    "ClientManagement.initializeGlobalClient(com.communote.server.persistence.user.UserVO userVO) - 'userVO' can not be null");
        }
        if (userVO.getLanguage() == null) {
            throw new IllegalArgumentException(
                    "ClientManagement.initializeGlobalClient(com.communote.server.persistence.user.UserVO userVO) - 'userVO.language' can not be null");
        }
        if (userVO.getEmail() == null || userVO.getEmail().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "ClientManagement.initializeGlobalClient(com.communote.server.persistence.user.UserVO userVO) - 'userVO.email' can not be null or empty");
        }
        if (userVO.getRoles() == null) {
            throw new IllegalArgumentException(
                    "ClientManagement.initializeGlobalClient(com.communote.server.persistence.user.UserVO userVO) - 'userVO.roles' can not be null");
        }
        try {
            this.handleInitializeGlobalClient(userVO);
        } catch (RuntimeException rt) {
            throw new ClientManagementException(
                    "Error performing 'ClientManagement.initializeGlobalClient(com.communote.server.persistence.user.UserVO userVO)' --> "
                            + rt, rt);
        }
    }

}