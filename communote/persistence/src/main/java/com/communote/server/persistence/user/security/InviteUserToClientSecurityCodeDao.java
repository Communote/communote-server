package com.communote.server.persistence.user.security;

import java.util.Collection;
import java.util.List;

import com.communote.server.model.user.security.InviteUserToClientSecurityCode;

/**
 * @see InviteUserToClientSecurityCode
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface InviteUserToClientSecurityCodeDao extends
        InviteSecurityCodeDao<InviteUserToClientSecurityCode> {

    /**
     * Creates a new instance of InviteUserToClientSecurityCode and adds from the passed in
     * <code>entities</code> collection
     *
     * @param entities
     *            the collection of InviteUserToClientSecurityCode instances to create.
     *
     * @return the created instances.
     */
    public Collection<InviteUserToClientSecurityCode> create(
            Collection<InviteUserToClientSecurityCode> entities);

    /**
     * Evicts (removes) the entity from the hibernate cache
     *
     * @param entity
     *            the entity to evict
     */
    public void evict(InviteUserToClientSecurityCode entity);

    /**
     * Loads an instance of InviteUserToClientSecurityCode from the persistent store.
     */
    public InviteUserToClientSecurityCode load(Long id);

    /**
     * Loads all entities of type {@link InviteUserToClientSecurityCode}.
     *
     * @return the loaded entities.
     */
    public List<InviteUserToClientSecurityCode> loadAll();

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove(Collection<InviteUserToClientSecurityCode> entities);

    /**
     * Removes the instance of InviteUserToClientSecurityCode from the persistent store.
     */
    public void remove(InviteUserToClientSecurityCode inviteUserToClientSecurityCode);

    /**
     * Removes the instance of InviteUserToClientSecurityCode having the given
     * <code>identifier</code> from the persistent store.
     */
    public void remove(Long id);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(Collection<InviteUserToClientSecurityCode> entities);

    /**
     * Updates the <code>inviteUserToClientSecurityCode</code> instance in the persistent store.
     */
    public void update(InviteUserToClientSecurityCode inviteUserToClientSecurityCode);

}
