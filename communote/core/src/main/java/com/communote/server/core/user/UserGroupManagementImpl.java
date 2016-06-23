package com.communote.server.core.user;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.communote.common.converter.Converter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.group.AliasValidationException;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.user.group.GroupOperationNotPermittedException;
import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.core.vo.user.group.GroupVO;
import com.communote.server.model.blog.BlogMember;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.blog.BlogMemberDao;
import com.communote.server.persistence.helper.dao.LazyClassLoaderHelper;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;
import com.communote.server.persistence.user.group.GroupDao;

/**
 * @see com.communote.server.core.user.UserGroupManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("userGroupManagement")
public class UserGroupManagementImpl extends UserGroupManagementBase {
    private static final String UNSUPPORTED_CHARACTER_REPLACEMENT = "_";

    protected static final Logger LOG = LoggerFactory.getLogger(UserGroupManagementImpl.class);

    @Autowired
    private GroupDao groupDao;
    @Autowired
    private ExternalUserGroupDao externalUserGroupDao;

    /**
     * Throws an exception if the current user is not client manager or the internal system user.
     *
     * @throws AuthorizationException
     *             if the current user is not client manager or the internal system
     */
    private void assertClientManagerOrInternalSystem() throws AuthorizationException {
        if (SecurityHelper.isClientManager() || SecurityHelper.isInternalSystem()) {
            return;
        }
        throw new AccessDeniedException("Current user is not client manager or internal system.");
    }

    /**
     * Throws an exception if a group is an external group.
     *
     * @param group
     *            the group or proxy
     * @throws GroupOperationNotPermittedException
     *             if the group is an external group
     */
    private void assertIsNotExternalGroup(Group group) throws GroupOperationNotPermittedException {
        try {
            LazyClassLoaderHelper.deproxy(group, ExternalUserGroup.class);
        } catch (ClassCastException e) {
            return;
        }
        throw new GroupOperationNotPermittedException("External groups cannot be edited.");
    }

    /**
     * Tries to create a unique legal group alias.
     *
     * @param source
     *            an alias to convert into a unique and legal group alias
     * @return the generated alias
     * @throws AliasAlreadyExistsException
     *             if creation of the alias failed
     */
    private String createUniqueAlias(String source) throws AliasAlreadyExistsException {
        String proposal = source.toLowerCase();
        if (!isGroupAliasLegal(proposal)) {
            proposal = proposal.replaceAll(ValidationPatterns.UNSUPPORTED_CHARACTERS_IN_ALIAS,
                    UNSUPPORTED_CHARACTER_REPLACEMENT);
        }
        int proposalSuffix = 1;
        int maxTries = 100;
        String proposalPrefix = proposal;
        while (groupAliasAlreadyExists(proposal)) {
            if (proposalSuffix > maxTries) {
                throw new AliasAlreadyExistsException("The generation of a unique alias failed.");
            }
            proposal = proposalPrefix + proposalSuffix;
            proposalSuffix++;
        }

        return proposal;
    }

    /**
     * Get an existing external user group by first checking for one with the externalId and if
     * there is no such group it uses the additionalProperty if it is unique.
     *
     * @param groupVO
     *            the group VO
     * @return the found group or null
     */
    private ExternalUserGroup findExistingExternalUserGroup(ExternalGroupVO groupVO) {
        ExternalUserGroup exisitingGroup = externalUserGroupDao.findByExternalId(
                groupVO.getExternalId(), groupVO.getExternalSystemId());
        if (exisitingGroup == null && groupVO.getAdditionalProperty() != null
                && groupVO.isMergeOnAdditionalProperty()) {
            exisitingGroup = externalUserGroupDao.findByAdditionalProperty(
                    groupVO.getAdditionalProperty(), groupVO.getExternalSystemId());
        }
        return exisitingGroup;
    }

    /**
     * Tests whether there is already a group for an alias.
     *
     * @param alias
     *            the alias to check
     * @return true if a group exists; false otherwise
     */
    private boolean groupAliasAlreadyExists(String alias) {
        Group group = groupDao.findByAlias(alias);
        return group != null;
    }

    @Override
    protected Long handleCreateExternalGroup(ExternalGroupVO groupVO)
            throws AliasValidationException, AliasAlreadyExistsException,
            GroupAlreadyExistsException {
        // TODO handle authorization. Only internal system user should be allowed.
        ExternalUserGroup exisitingGroup = externalUserGroupDao.findByExternalId(
                groupVO.getExternalId(), groupVO.getExternalSystemId());
        if (exisitingGroup != null) {
            throw new GroupAlreadyExistsException("External group with external ID "
                    + groupVO.getExternalId() + " from external system "
                    + groupVO.getExternalSystemId() + " already exists");
        }
        return internalCreateExternalGroup(groupVO);
    }

    /**
     * {@inheritDoc}
     *
     * @throws AuthorizationException
     */
    @Override
    protected Group handleCreateGroup(GroupVO groupVO) throws AliasAlreadyExistsException,
            AliasValidationException, AuthorizationException {
        assertClientManagerOrInternalSystem();
        Group group = Group.Factory.newInstance();
        initGroupFromVO(group, groupVO);
        return groupDao.create(group);
    }

    @Override
    protected Long handleCreateOrUpdateExternalGroup(ExternalGroupVO groupVO)
            throws AliasAlreadyExistsException, AliasValidationException {
        // TODO handle Authorization. Complicated since used by rest api
        ExternalUserGroup exisitingGroup = findExistingExternalUserGroup(groupVO);
        if (exisitingGroup == null) {
            return internalCreateExternalGroup(groupVO);
        }
        internalUpdateExternalGroup(exisitingGroup, groupVO);
        return exisitingGroup.getId();
    }

    @Override
    protected void handleDeleteExternalGroup(Long groupId, String externalSystemId)
            throws AuthorizationException {
        if (!SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException(
                    "Only the internal system user is allowed to run this operation");
        }
        ExternalUserGroup group = loadExternalGroup(groupId);
        if (group != null && group.getExternalSystemId().equals(externalSystemId)) {
            internalRemoveGroupMembers(group);
            externalUserGroupDao.remove(group);
        }
    }

    @Override
    protected void handleDeleteGroup(Long groupId) throws AuthorizationException {
        assertClientManagerOrInternalSystem();
        Group group = groupDao.load(groupId);
        if (group == null) {
            return;
        }
        assertIsNotExternalGroup(group);
        internalRemoveGroupMembers(group);
        groupDao.remove(group);
    }

    @Override
    protected <T> T handleFindGroupByAlias(String alias, Converter<Group, T> converter) {
        Group group = groupDao.findByAlias(alias);
        if (group != null) {
            return converter.convert(group);
        }
        return null;
    }

    @Override
    protected <T> T handleFindGroupById(Long id, Converter<Group, T> converter) {
        Group group = groupDao.load(id);
        if (group != null) {
            return converter.convert(group);
        }
        return null;
    }

    @Override
    protected boolean handleIsExternalGroup(Long groupId) {
        try {
            return loadExternalGroup(groupId) != null;
        } catch (ClassCastException e) {
            // strangely hibernate loads the entity even if it is not an external group
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUpdateExternalGroup(Long groupId, ExternalGroupVO groupVO)
            throws GroupNotFoundException {
        ExternalUserGroup exisitingGroup = loadExternalGroup(groupId);
        if (exisitingGroup == null) {
            throw new GroupNotFoundException("External group with external ID "
                    + groupVO.getExternalId() + " from external system "
                    + groupVO.getExternalSystemId() + " does not exist");
        }
        if (!exisitingGroup.getExternalId().equals(groupVO.getExternalId())) {
            ExternalUserGroup exisitingGroupWithSameExternalId = externalUserGroupDao
                    .findByExternalId(groupVO.getExternalId(), groupVO.getExternalSystemId());
            if (exisitingGroupWithSameExternalId != null) {
                // due to a bug external groups might have been added again after changing the
                // mapping for the externalId. There is not easy way to fix this automatically as we
                // would have to merge the groups including access rights for topics...
                LOG.error("Cannot update external group {} ({}) because there is another"
                        + " group with the same new external ID {}", groupId,
                        exisitingGroup.getExternalId(), groupVO.getExternalId());
                return;
            }
            exisitingGroup.setExternalId(groupVO.getExternalId());
        }
        internalUpdateExternalGroup(exisitingGroup, groupVO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUpdateGroup(Long groupId, GroupVO groupVO) throws GroupNotFoundException,
            GroupOperationNotPermittedException {
        SecurityHelper.isClientManager();
        Group group = groupDao.load(groupId);
        if (group == null) {
            throw new GroupNotFoundException("The group with ID " + groupId + " does not exist.");
        }
        assertIsNotExternalGroup(group);
        internalUpdateGroup(group, groupVO);
    }

    /**
     * Initiates a group from a value object.
     *
     * @param group
     *            the group to init
     * @param groupVO
     *            the value object
     * @throws AliasAlreadyExistsException
     *             if the alias already exists
     * @throws AliasValidationException
     *             if the alias is not valid
     */
    private void initGroupFromVO(Group group, GroupVO groupVO) throws AliasAlreadyExistsException,
            AliasValidationException {
        if (groupVO.getAlias() == null) {
            group.setAlias(createUniqueAlias(groupVO.getName()));
        } else {
            // force lower case alias
            groupVO.setAlias(groupVO.getAlias().toLowerCase());
            validateGroupAlias(groupVO.getAlias());
            group.setAlias(groupVO.getAlias());
        }
        group.setName(groupVO.getName());
        group.setDescription(groupVO.getDescription());
    }

    /**
     * Create the external group.
     *
     * @param groupVO
     *            a value object with details about the group. If the alias member is not set it
     *            will be generated from the name.
     * @return the ID of the new group
     * @throws AliasAlreadyExistsException
     *             in case the alias already exists
     * @throws AliasValidationException
     *             in case the alias is not valid
     */
    private Long internalCreateExternalGroup(ExternalGroupVO groupVO)
            throws AliasAlreadyExistsException, AliasValidationException {
        ExternalUserGroup group = ExternalUserGroup.Factory.newInstance();
        initGroupFromVO(group, groupVO);
        group.setExternalId(groupVO.getExternalId());
        group.setExternalSystemId(groupVO.getExternalSystemId());
        group.setAdditionalProperty(groupVO.getAdditionalProperty());
        return ServiceLocator.findService(ExternalUserGroupDao.class).create(group).getId();
    }

    /**
     * Removes the members from a group.
     *
     * @param group
     *            the group to edit
     */
    private void internalRemoveGroupMembers(Group group) {
        BlogMemberDao bmd = ServiceLocator.findService(BlogMemberDao.class);
        Collection<BlogMember> blogMembers = bmd.findByEntity(group.getId());
        bmd.remove(blogMembers);
        for (CommunoteEntity member : group.getGroupMembers()) {
            member.getGroups().remove(group);
        }
        group.getGroupMembers().clear();
        try {
            ServiceLocator.findService(UserGroupMemberManagement.class).removeGroupFromAllGroups(
                    group.getId());
        } catch (GroupNotFoundException e) {
            LOG.error("UnexpectedException while removing the members of a group", e);
        } catch (AuthorizationException e) {
            LOG.error("UnexpectedException while removing the members of a group", e);
        }
        group.getMemberships().clear();
    }

    /**
     * update an existing external group, the alias is not modified.
     *
     * @param exisitingGroup
     *            the existing external group
     * @param groupVO
     *            the value object with the data to update
     */
    private void internalUpdateExternalGroup(ExternalUserGroup exisitingGroup,
            ExternalGroupVO groupVO) {
        internalUpdateGroup(exisitingGroup, groupVO);
        exisitingGroup.setAdditionalProperty(groupVO.getAdditionalProperty());
    }

    /**
     * Update the data of a group, the alias is not modified.
     *
     * @param group
     *            the group to update
     * @param groupVO
     *            the value object with the data to update
     */
    private void internalUpdateGroup(Group group, GroupVO groupVO) {
        // update but skip alias
        if (!StringUtils.equals(group.getName(), groupVO.getName())) {
            group.setName(groupVO.getName());
        }
        if (!StringUtils.equals(group.getDescription(), groupVO.getDescription())) {
            group.setDescription(groupVO.getDescription());
        }
    }

    /**
     * Tests whether a group alias is of legal format.
     *
     * @param alias
     *            the alias
     * @return true if the alias is legal, false otherwise
     */
    private boolean isGroupAliasLegal(String alias) {
        if (!StringUtils.isBlank(alias)) {
            if (alias.matches(ValidationPatterns.PATTERN_ALIAS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Load an external user group.
     *
     * @param groupId
     *            the ID of the group to load
     * @return the group or null if it does not exist
     */
    private ExternalUserGroup loadExternalGroup(Long groupId) {
        try {
            return externalUserGroupDao.load(groupId);
        } catch (ClassCastException e) {
            // strangely hibernate loads the entity even if it is not an external group
            return null;
        }
    }

    /**
     * Validates a group alias.
     *
     * @param alias
     *            the alias to validate
     * @throws AliasValidationException
     *             if the alias does not conform to the required format
     * @throws AliasAlreadyExistsException
     *             if there is already a group for that alias
     */
    private void validateGroupAlias(String alias) throws AliasValidationException,
            AliasAlreadyExistsException {
        if (!isGroupAliasLegal(alias)) {
            throw new AliasValidationException("The alias " + alias
                    + " does not conform to the required format.");
        }
        if (groupAliasAlreadyExists(alias)) {
            throw new AliasAlreadyExistsException("The alias " + alias + " already exists.");
        }
    }

}
