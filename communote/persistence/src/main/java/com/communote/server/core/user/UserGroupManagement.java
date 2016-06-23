package com.communote.server.core.user;

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
 * Manage an user group.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface UserGroupManagement {

    /**
     * <p>
     * Create a group imported from an external system.
     * </p>
     * 
     * @param groupVO
     *            a value object with details about the group. If the alias member is not set it
     *            will be generated from the name.
     * @return the ID of the created group
     * @throws AliasValidationException
     *             in case the alias field is not valid
     * @throws AliasAlreadyExistsException
     *             in case the alias already exists
     * @throws GroupAlreadyExistsException
     *             in case there is already a local group for the external group
     */
    public Long createExternalGroup(ExternalGroupVO groupVO)
            throws com.communote.server.core.user.group.AliasValidationException,
            com.communote.server.core.user.AliasAlreadyExistsException,
            GroupAlreadyExistsException;

    /**
     * Create a new group.
     * 
     * @throws AuthorizationException
     *             in case the current user is not the client manager or the internal system user
     */
    public Group createGroup(GroupVO groupVO)
            throws com.communote.server.core.user.AliasAlreadyExistsException,
            com.communote.server.core.user.group.AliasValidationException, AuthorizationException;

    /**
     * <p>
     * Create a group imported from an external system or if that group already exists it is
     * updated.
     * </p>
     * 
     * @param groupVO
     *            a value object with details about the group. If the alias member is not set it
     *            will be generated from the name.
     * @return the ID of the created group
     * @throws AliasValidationException
     *             in case the alias field is not valid
     * @throws AliasAlreadyExistsException
     *             in case the alias already exists
     */
    public Long createOrUpdateExternalGroup(ExternalGroupVO groupVO)
            throws com.communote.server.core.user.group.AliasValidationException,
            com.communote.server.core.user.AliasAlreadyExistsException;

    /**
     * Delete an external group. If the group does not exist nothing will happen.
     * 
     * @throws AuthorizationException
     *             in case the current user is not the internal system user
     */
    public void deleteExternalGroup(Long groupId, String externalSystemId)
            throws AuthorizationException;

    /**
     * Delete a group. The group members are note deleted.
     * 
     * @param groupId
     *            the Id of the group to delete
     * @throws GroupOperationNotPermittedException
     *             if the group refers to an external group
     * @throws AuthorizationException
     *             if the current user is not client manager
     */
    public void deleteGroup(Long groupId)
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
    public <T> T findGroupByAlias(String alias, Converter<Group, T> converter);

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
    public <T> T findGroupById(Long id, Converter<Group, T> converter);

    /**
     * Test whether a group is an external group or an internal group.
     * 
     * @param groupId
     *            the ID of the group to test
     * @return true if the group exists and is an external group, false otherwise
     */
    boolean isExternalGroup(Long groupId);

    /**
     * Update a group that was imported from an external system. The alias won't be changed.
     * 
     * @param groupId
     *            ID of the group to update
     * @param groupVO
     *            a value object with details about the group
     * @throws GroupNotFoundException
     *             in case the group to update does not exist
     */
    public void updateExternalGroup(Long groupId, ExternalGroupVO groupVO)
            throws GroupNotFoundException;

    /**
     * <p>
     * Update an existing group. The alias won't be changed.
     * </p>
     * 
     * @param groupId
     *            the ID of the group to update
     * @param groupVO
     *            a value object with details about the group
     * @throws GroupNotFoundException
     *             in case the group to update does not exist
     * @throws GroupNotFoundException
     *             in case the group is an external group
     */
    public void updateGroup(Long groupId, GroupVO groupVO)
            throws GroupNotFoundException,
            GroupOperationNotPermittedException;

}
