package com.communote.server.core.user;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.user.group.AliasValidationException;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.user.group.GroupOperationNotPermittedException;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.core.vo.user.group.GroupVO;
import com.communote.server.model.user.group.Group;

/**
 * <p>
 * Spring Service base class for <code>UserGroupManagement</code>, provides access to all services
 * and entities referenced by this service.
 * </p>
 * 
 * @see UserGroupManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class UserGroupManagementBase implements UserGroupManagement {

    @Override
    public Long createExternalGroup(ExternalGroupVO groupVO)
            throws AliasValidationException, AliasAlreadyExistsException,
            GroupAlreadyExistsException {
        if (groupVO == null) {
            throw new IllegalArgumentException("'groupVO' cannot be null");
        }
        if (groupVO.getExternalId() == null || groupVO.getExternalId().trim().length() == 0) {
            throw new IllegalArgumentException("'groupVO.externalId' cannot be null or empty");
        }
        if (groupVO.getExternalSystemId() == null
                || groupVO.getExternalSystemId().trim().length() == 0) {
            throw new IllegalArgumentException("'groupVO.externalSystemId' cannot be null or empty");
        }
        try {
            return this.handleCreateExternalGroup(groupVO);
        } catch (RuntimeException rt) {
            throw new UserGroupManagementException(
                    "Error performing 'UserGroupManagement.createExternalGroup(ExternalGroupVO groupVO)' --> "
                            + rt, rt);
        }
    }

    @Override
    public Group createGroup(GroupVO groupVO) throws AliasAlreadyExistsException,
            AliasValidationException, AuthorizationException {
        if (groupVO == null) {
            throw new IllegalArgumentException(
                    "UserGroupManagement.createGroup(GroupVO groupVO) - 'groupVO' cannot be null");
        }
        if (groupVO.getName() == null || groupVO.getName().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "UserGroupManagement.createGroup(GroupVO groupVO) - 'groupVO.name' cannot be null or empty");
        }
        try {
            return this.handleCreateGroup(groupVO);
        } catch (RuntimeException rt) {
            throw new UserGroupManagementException(
                    "Error performing 'UserGroupManagement.createGroup(GroupVO groupVO)' --> "
                            + rt,
                    rt);
        }
    }

    @Override
    public Long createOrUpdateExternalGroup(ExternalGroupVO groupVO)
            throws AliasValidationException, AliasAlreadyExistsException {
        if (groupVO == null) {
            throw new IllegalArgumentException("'groupVO' cannot be null");
        }
        if (groupVO.getExternalId() == null || groupVO.getExternalId().trim().length() == 0) {
            throw new IllegalArgumentException("'groupVO.externalId' cannot be null or empty");
        }
        if (groupVO.getExternalSystemId() == null
                || groupVO.getExternalSystemId().trim().length() == 0) {
            throw new IllegalArgumentException("'groupVO.externalSystemId' cannot be null or empty");
        }
        try {
            return this.handleCreateOrUpdateExternalGroup(groupVO);
        } catch (RuntimeException rt) {
            throw new UserGroupManagementException(
                    "Error performing 'UserGroupManagement.createExternalGroup(ExternalGroupVO groupVO)' --> "
                            + rt, rt);
        }
    }

    @Override
    public void deleteExternalGroup(Long groupId, String externalSystemId)
            throws AuthorizationException {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "UserGroupManagement.deleteExternalGroup(Long groupId, String externalSystemId) - 'groupId' cannot be null");
        }
        if (externalSystemId == null || externalSystemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "UserGroupManagement.deleteExternalGroup(Long groupId, String externalSystemId) - 'externalSystemId' cannot be null or empty");
        }
        try {
            this.handleDeleteExternalGroup(groupId, externalSystemId);
        } catch (RuntimeException rt) {
            throw new UserGroupManagementException(
                    "Error performing 'UserGroupManagement.deleteExternalGroup(Long groupId, String externalSystemId)' --> "
                            + rt,
                    rt);
        }
    }

    @Override
    public void deleteGroup(Long groupId) throws AuthorizationException {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "UserGroupManagement.deleteGroup(Long groupId) - 'groupId' cannot be null");
        }
        try {
            this.handleDeleteGroup(groupId);
        } catch (RuntimeException rt) {
            throw new UserGroupManagementException(
                    "Error performing 'UserGroupManagement.deleteGroup(Long groupId)' --> "
                            + rt,
                    rt);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T findGroupByAlias(String alias, Converter<Group, T> converter) {
        if (alias == null) {
            throw new IllegalArgumentException(" 'alias' must not be null");
        }
        if (converter == null) {
            throw new IllegalArgumentException(" 'converter' must not be null");
        }
        return handleFindGroupByAlias(alias, converter);
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T findGroupById(Long id, Converter<Group, T> converter) {
        if (id == null) {
            throw new IllegalArgumentException(" 'alias' must not be null");
        }
        if (converter == null) {
            throw new IllegalArgumentException(" 'converter' must not be null");
        }
        return handleFindGroupById(id, converter);
    }

    /**
     * Performs the core logic for {@link #createExternalGroup(ExternalGroupVO)}
     */
    protected abstract Long handleCreateExternalGroup(ExternalGroupVO groupVO)
            throws AliasValidationException, AliasAlreadyExistsException,
            GroupAlreadyExistsException;

    /**
     * Performs the core logic for {@link #createGroup(GroupVO)}
     * 
     * @throws AuthorizationException
     */
    protected abstract Group handleCreateGroup(GroupVO groupVO)
            throws AliasAlreadyExistsException, AliasValidationException, AuthorizationException;

    /**
     * Performs the core logic for {@link #createOrUpdateExternalGroup(ExternalGroupVO)}
     * 
     * @param groupVO
     *            a value object with details about the group. If the alias member is not set it
     *            will be generated from the name.
     * @throws AliasValidationException
     *             in case the alias field is not valid
     * @throws AliasAlreadyExistsException
     *             in case the alias already exists
     * @return the ID of the group
     */
    protected abstract Long handleCreateOrUpdateExternalGroup(ExternalGroupVO groupVO)
            throws AliasAlreadyExistsException, AliasValidationException;

    /**
     * Performs the core logic for {@link #deleteExternalGroup(Long, String)}
     * 
     * @throws AuthorizationException
     */
    protected abstract void handleDeleteExternalGroup(Long groupId,
            String externalSystemId) throws AuthorizationException;

    /**
     * Performs the core logic for {@link #deleteGroup(Long)}
     * 
     * @throws AuthorizationException
     */
    protected abstract void handleDeleteGroup(Long groupId)
            throws GroupOperationNotPermittedException, AuthorizationException;

    /**
     * Find a user group by its alias and convert it to the object to be returned.
     * 
     * @param <T>
     *            the type of the target object
     * @param alias
     *            the alias of the group to retrieve
     * @param converter
     *            the converter to create the target object
     * @return the found group as an instance of the target type or null if the group wasn't found
     */
    protected abstract <T> T handleFindGroupByAlias(String alias,
            Converter<Group, T> converter);

    /**
     * Find a user group by its ID and convert it to the object to be returned.
     * 
     * @param <T>
     *            the type of the target object
     * @param id
     *            the ID of the group to retrieve
     * @param converter
     *            the converter to create the target object
     * @return the found group as an instance of the target type or null if the group wasn't found
     */
    protected abstract <T> T handleFindGroupById(Long id, Converter<Group, T> converter);

    /**
     * Test whether a group is an external group or an internal group.
     * 
     * @param groupId
     *            the ID of the group to test
     * @return true if the group exists and is an external group, false otherwise
     */
    protected abstract boolean handleIsExternalGroup(Long groupId);

    /**
     * Performs the core logic for {@link #updateExternalGroup(ExternalGroupVO)}
     * 
     * @param groupId
     *            the ID of the group
     * @param groupVO
     *            a value object with details about the group.
     * @throws GroupNotFoundException
     *             in case the group to update does not exist
     */
    protected abstract void handleUpdateExternalGroup(Long groupId, ExternalGroupVO groupVO)
            throws GroupNotFoundException;

    /**
     * Performs the core logic for {@link #updateGroup(Long, GroupVO)}
     */
    protected abstract void handleUpdateGroup(Long groupId, GroupVO groupVO)
            throws GroupNotFoundException,
            GroupOperationNotPermittedException;

    @Override
    @Transactional(readOnly = true)
    public boolean isExternalGroup(Long groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId cannot be null");
        }
        return this.handleIsExternalGroup(groupId);

    }

    @Override
    public void updateExternalGroup(Long groupId, ExternalGroupVO groupVO)
            throws GroupNotFoundException {
        if (groupId == null) {
            throw new IllegalArgumentException("'groupId' cannot be null");
        }
        if (groupVO == null) {
            throw new IllegalArgumentException(
                    "UserGroupManagement.updateExternalGroup(ExternalUserGroup externalGroup, GroupVO groupVO) - 'groupVO' cannot be null");
        }
        if (groupVO.getName() == null || groupVO.getName().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "UserGroupManagement.updateExternalGroup(ExternalUserGroup externalGroup, GroupVO groupVO) - 'groupVO.name' cannot be null or empty");
        }
        try {
            this.handleUpdateExternalGroup(groupId, groupVO);
        } catch (RuntimeException rt) {
            throw new UserGroupManagementException(
                    "Error performing 'UserGroupManagement.updateExternalGroup(ExternalUserGroup externalGroup, GroupVO groupVO)' --> "
                            + rt, rt);
        }
    }

    @Override
    public void updateGroup(Long groupId, GroupVO groupVO)
            throws GroupNotFoundException,
            GroupOperationNotPermittedException {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "UserGroupManagement.updateGroup(Long groupId, GroupVO groupVO) - 'groupId' cannot be null");
        }
        if (groupVO == null) {
            throw new IllegalArgumentException(
                    "UserGroupManagement.updateGroup(Long groupId, GroupVO groupVO) - 'groupVO' cannot be null");
        }
        if (groupVO.getName() == null || groupVO.getName().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "UserGroupManagement.updateGroup(Long groupId, GroupVO groupVO) - 'groupVO.name' cannot be null or empty");
        }
        try {
            this.handleUpdateGroup(groupId, groupVO);
        } catch (RuntimeException rt) {
            throw new UserGroupManagementException(
                    "Error performing 'UserGroupManagement.updateGroup(Long groupId, GroupVO groupVO)' --> "
                            + rt,
                    rt);
        }
    }
}