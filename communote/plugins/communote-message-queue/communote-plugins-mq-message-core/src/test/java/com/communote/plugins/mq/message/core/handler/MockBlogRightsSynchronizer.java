package com.communote.plugins.mq.message.core.handler;

import java.util.ArrayList;
import java.util.List;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.core.external.BlogRightsSynchronizer;
import com.communote.server.core.vo.external.ExternalTopicRoleTO;
import com.communote.server.model.blog.BlogRole;

/**
 * Synchronizer for tests
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MockBlogRightsSynchronizer extends BlogRightsSynchronizer {
    private List<ExternalTopicRoleTO> externalTopicRoleTOs = new ArrayList<ExternalTopicRoleTO>();

    /**
     * @param blogId
     *            the ID of the topic
     * @param externalSystemId
     *            external system id
     */
    public MockBlogRightsSynchronizer(Long blogId, String externalSystemId) {
        super(blogId, externalSystemId);
    }

    /**
     * add a TO that is suitable for the mergeRights test
     * 
     * @param id
     *            the entity ID
     * @param isGroup
     *            whether it is a group
     * @param role
     *            the role
     */
    public void addRole(Long id, boolean isGroup, BlogRole role) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        ExternalTopicRoleTO to = new ExternalTopicRoleTO(role, id, "a" + id, isGroup, null, "e"
                + id);
        externalTopicRoleTOs.add(to);
    }

    /**
     * Find a matching role. Assumes that the members of the existing roles are all set.
     * 
     * @param role
     *            the TO to find
     * @return the index of the role
     */
    private int findMatchingRole(ExternalTopicRoleTO role) {
        for (int i = 0; i < externalTopicRoleTOs.size(); i++) {
            ExternalTopicRoleTO existingRole = externalTopicRoleTOs.get(i);
            boolean idMatch = false;
            if (role.getEntityId() != null) {
                idMatch = role.getEntityId().equals(existingRole.getEntityId());
            } else if (role.getEntityAlias() != null) {
                idMatch = role.getEntityAlias().equals(existingRole.getEntityAlias());
            } else if (role.getExternalEntityId() != null) {
                idMatch = role.getExternalEntityId().equals(existingRole.getExternalEntityId());
            }
            if (idMatch) {
                if (role.getIsGroup() != null
                        && role.getIsGroup().equals(existingRole.getIsGroup())) {
                    return i;
                } else if (role.getIsGroup() == null && !existingRole.getIsGroup()) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * @return the externalTopicRoleTOs
     */
    public List<ExternalTopicRoleTO> getExternalTopicRoleTOs() {
        return externalTopicRoleTOs;
    }

    /**
     * mock merge function which only works if existing roles are completely populated. The easiest
     * way to achieve this is to use {@link #addRole(Long, boolean, BlogRole)}. Moreover roles that
     * should be added need to have the entityId member set.
     * 
     * @param rolesToSetOrUpdate
     *            roles to set and update
     * @param rolesToRemove
     *            roles to remove
     * @throws BlogAccessException
     *             no access
     * @throws BlogNotFoundException
     *             blog not found
     */
    @Override
    public void mergeRights(List<ExternalTopicRoleTO> rolesToSetOrUpdate,
            List<ExternalTopicRoleTO> rolesToRemove) throws BlogAccessException,
            BlogNotFoundException {

        if (rolesToSetOrUpdate != null) {
            for (ExternalTopicRoleTO to : rolesToSetOrUpdate) {
                int index = findMatchingRole(to);
                if (index < 0) {
                    addRole(to.getEntityId(), to.getIsGroup() == null ? false : to.getIsGroup(),
                            to.getRole());
                } else {
                    externalTopicRoleTOs.get(index).setRole(to.getRole());
                }
            }
        }
        if (rolesToRemove != null) {
            for (ExternalTopicRoleTO to : rolesToRemove) {
                int index = findMatchingRole(to);
                if (index >= 0) {
                    externalTopicRoleTOs.remove(index);
                }
            }
        }
    }

    @Override
    public void replaceRights(List<ExternalTopicRoleTO> externalTopicRoleTOs) {
        this.externalTopicRoleTOs = externalTopicRoleTOs;
    }

    /**
     * @param externalTopicRoleTOs
     *            the externalTopicRoleTOs to set
     */
    public void setExternalTopicRoleTOs(List<ExternalTopicRoleTO> externalTopicRoleTOs) {
        this.externalTopicRoleTOs = externalTopicRoleTOs;
    }

}
