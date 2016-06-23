package com.communote.server.plugins.api.externals;

import java.util.Date;

import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.plugins.exceptions.PluginException;


/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface ExternalUserGroupAccessor {
    /**
     * This method can be used to visit all groups of a given user.
     * 
     * @param user
     *            The user.
     * @param visitor
     *            A visitor for visiting the found entities.
     * @throws ExternalUserGroupAccessorException
     *             Exception.
     */
    public void acceptGroupsOfUser(User user, ExternalEntityVisitor<ExternalGroupVO> visitor)
            throws ExternalUserGroupAccessorException;

    /**
     * This method can be used to visit all members of the given group. The visitor visits only
     * users which are already a member within Communote, though it only works on their id.
     * 
     * @param group
     *            The child group.
     * @param visitor
     *            A visitor for visiting the found entities.
     * @throws ExternalUserGroupAccessorException
     *             Exception.
     */
    public void acceptMembersOfGroup(ExternalUserGroup group,
            ExternalEntityVisitor<Long> visitor) throws ExternalUserGroupAccessorException;

    /**
     * This method can be used to visit all parent groups of the given groups.
     * 
     * @param group
     *            The child group.
     * @param visitor
     *            A visitor for visiting the found entities.
     * @throws ExternalUserGroupAccessorException
     *             Exception.
     */
    public void acceptParentGroups(ExternalUserGroup group,
            ExternalEntityVisitor<ExternalGroupVO> visitor)
            throws ExternalUserGroupAccessorException;

    /**
     * Checks if the system is available.
     * 
     * @return True if the system is available.
     */
    public boolean canConnect();

    /**
     * Retrieves the current data of a group in the external system.
     * 
     * @param group
     *            The group to retrieve.
     * @return The external group if the group exists in the external system, null otherwise.
     * @throws PluginException
     *             Exception.
     */
    public ExternalGroupVO getGroup(ExternalUserGroup group) throws PluginException;

    /**
     * Retrieves the current data of a group in the external system by the external group
     * identifier.
     * 
     * @param externalGroupId
     *            The identifier of the external group.
     * @return The external group if the group exists in the external system, null otherwise.
     * @throws PluginException
     *             Exception.
     */
    public ExternalGroupVO getGroup(String externalGroupId) throws PluginException;

    /**
     * 
     * @param group
     *            The group to check against.
     * @return True, if the given group exists in the foreign system.
     * @throws PluginException
     *             Exception.
     */
    public boolean hasGroup(ExternalUserGroup group) throws PluginException;

    /**
     * @param date
     *            Date.
     * @return True, if a synchronization is needed.
     */
    public boolean needsSynchronization(Date date);

    /**
     * This method will be called when the accessor starts.
     */
    public void start();

    /**
     * This method will be called when the accessor should be stopped.
     */
    public void stop();

    /**
     * @param externalConfiguration
     *            The external configuration.
     * @return True if the concrete configuration is supported, else false.
     */
    public boolean supports(ExternalSystemConfiguration externalConfiguration);

}
