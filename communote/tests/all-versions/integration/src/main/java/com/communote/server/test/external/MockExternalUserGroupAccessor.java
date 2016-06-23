package com.communote.server.test.external;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.plugins.api.externals.ExternalEntityVisitor;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessor;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessorException;
import com.communote.server.plugins.exceptions.PluginException;


/**
 * This class is only for tests.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MockExternalUserGroupAccessor implements ExternalUserGroupAccessor {

    private final Map<String, ExternalGroupVO> groups = new HashMap<String, ExternalGroupVO>();
    private final Map<String, Set<String>> userToGroups = new HashMap<String, Set<String>>();
    private final Map<String, Set<String>> childToParentGroups = new HashMap<String, Set<String>>();
    private final String externalSystemId;

    /**
     * @param externalSystemId
     *            The external system id.
     */
    public MockExternalUserGroupAccessor(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void acceptGroupsOfUser(User user,
            ExternalEntityVisitor<ExternalGroupVO> visitor)
            throws ExternalUserGroupAccessorException {
        Collection<String> groups = userToGroups.get(user.getAlias());
        if (groups == null) {
            return;
        }
        for (String group : groups) {
            if (this.groups.get(group) != null) {
                try {
                    visitor.visit(this.groups.get(group));
                } catch (Exception e) {
                    throw new ExternalUserGroupAccessorException(e);
                }
            }
        }
    }

    @Override
    public void acceptMembersOfGroup(ExternalUserGroup group, ExternalEntityVisitor<Long> visitor) {
        String externalId = group.getExternalId();
        loop: for (Entry<String, Set<String>> userToGroup : userToGroups.entrySet()) {
            if (userToGroup.getValue() == null || !userToGroup.getValue().contains(externalId)) {
                continue loop;
            }
            User user = ServiceLocator.instance().getService(UserManagement.class)
                    .findUserByAlias(userToGroup.getKey());
            if (user != null) {
                try {
                    visitor.visit(user.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void acceptParentGroups(ExternalUserGroup group,
            ExternalEntityVisitor<ExternalGroupVO> visitor)
            throws ExternalUserGroupAccessorException {
        Collection<String> groups = childToParentGroups.get(group.getExternalId());
        if (groups != null) {
            for (String groupName : groups) {
                if (this.groups.get(groupName) != null) {
                    try {
                        visitor.visit(this.groups.get(groupName));
                    } catch (Exception e) {
                        throw new ExternalUserGroupAccessorException(e);
                    }
                }
            }
        }
    }

    /**
     * Adds or updates the group. Uses the external id as key.
     * 
     * @param group
     *            The group.
     */
    public void addGroup(ExternalGroupVO group) {
        // Alias must be explicitly empty to force generation of an unique internal alias.
        group.setAlias(null);
        groups.put(group.getExternalId(), group);
    }

    /**
     * @param externalId
     *            The externalId, alias, name and description of the new group.
     * 
     * @return The group.
     */
    public ExternalGroupVO addGroup(String externalId) {
        ExternalGroupVO group = new ExternalGroupVO();
        group.setExternalId(externalId);
        // Alias must be explicitly empty to force generation of an unique internal alias.
        group.setAlias(null);
        group.setName(externalId);
        group.setDescription(externalId);
        group.setExternalSystemId(externalSystemId);
        groups.put(group.getExternalId(), group);
        return group;
    }

    /**
     * Groups will be created, if they do not exist.
     * 
     * @param childExternalId
     *            The child group.
     * @param parentExternalId
     *            The parent group.
     */
    public void addParentGroup(String childExternalId, String parentExternalId) {
        ExternalGroupVO childGroup = groups.get(childExternalId);
        if (childGroup == null) {
            childGroup = addGroup(childExternalId);
        }
        ExternalGroupVO parentGroup = groups.get(parentExternalId);
        if (parentGroup == null) {
            parentGroup = addGroup(parentExternalId);
        }
        Set<String> parentGroups = childToParentGroups.get(childExternalId);
        if (parentGroups == null) {
            parentGroups = new HashSet<String>();
            childToParentGroups.put(childExternalId, parentGroups);
        }
        parentGroups.add(parentGroup.getExternalId());
    }

    /**
     * Adds the given user to the group. If the group doesn't exists it will be created.
     * 
     * @param alias
     *            The users alias.
     * @param externalId
     *            The external id of the group.
     */
    public void addUserToGroup(String alias, String externalId) {
        ExternalGroupVO group = groups.get(externalId);
        if (group == null) {
            group = addGroup(externalId);
        }
        Set<String> userGroups = userToGroups.get(alias);
        if (userGroups == null) {
            userGroups = new HashSet<String>();
            userToGroups.put(alias, userGroups);
        }
        userGroups.add(externalId);
    }

    /**
     * @return <code>true</code>
     */
    public boolean canConnect() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public ExternalGroupVO getGroup(ExternalUserGroup group) throws PluginException {
        return groups.get(group.getExternalId());
    }

    /**
     * @param externalId
     *            External id of the group.
     * @return The group.
     */
    public ExternalGroupVO getGroup(String externalId) {
        return groups.get(externalId);
    }

    /**
     * @return the groups
     */
    public Map<String, ExternalGroupVO> getGroups() {
        return groups;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasGroup(ExternalUserGroup group) {
        return groups.containsKey(group.getExternalId());
    }

    /**
     * {@inheritDoc}
     * 
     * @return <code>true</code>
     */
    public boolean needsSynchronization(Date date) {
        return true;
    }

    /**
     * @param externalId
     *            The external Id of the group.
     * 
     * @return The removed group.
     */
    public ExternalGroupVO removeGroup(String externalId) {
        return groups.remove(externalId);
    }

    /**
     * Groups will be created, if they do not exist.
     * 
     * @param childExternalId
     *            The child group.
     * @param parentExternalId
     *            The parent group.
     */
    public void removeParentGroup(String childExternalId, String parentExternalId) {
        Set<String> parentGroups = childToParentGroups.get(childExternalId);
        if (parentGroups != null) {
            parentGroups.remove(parentExternalId);
        }
    }

    /**
     * Adds the given user to the group. If the group doesn't exists it will be created.
     * 
     * @param alias
     *            The users alias.
     * @param externalId
     *            The external id of the group.
     */
    public void removeUserFromGroup(String alias, String externalId) {
        Set<String> groups = userToGroups.get(alias);
        if (groups != null) {
            groups.remove(externalId);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void start() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     * 
     * @return <code>true</code>
     */
    public boolean supports(ExternalSystemConfiguration externalConfiguration) {
        return true;
    }

}
