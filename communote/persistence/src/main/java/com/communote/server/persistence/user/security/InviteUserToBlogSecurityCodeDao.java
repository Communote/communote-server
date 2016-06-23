package com.communote.server.persistence.user.security;

import java.util.Collection;
import java.util.List;

import com.communote.server.model.user.security.InviteUserToBlogSecurityCode;

/**
 * @see InviteUserToBlogSecurityCode
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface InviteUserToBlogSecurityCodeDao extends
        InviteSecurityCodeDao<InviteUserToBlogSecurityCode> {

    /**
     * Creates a new instance of InviteUserToBlogSecurityCode and adds from the passed in
     * <code>entities</code> collection
     *
     * @param entities
     *            the collection of InviteUserToBlogSecurityCode instances to create.
     *
     * @return the created instances.
     */
    public Collection<InviteUserToBlogSecurityCode> create(
            Collection<InviteUserToBlogSecurityCode> entities);

    /**
     * Evicts (removes) the entity from the hibernate cache
     *
     * @param entity
     *            the entity to evict
     */
    public void evict(InviteUserToBlogSecurityCode entity);

    /**
     * Loads an instance of InviteUserToBlogSecurityCode from the persistent store.
     */
    public InviteUserToBlogSecurityCode load(Long id);

    /**
     * Loads all entities of type {@link InviteUserToBlogSecurityCode}.
     *
     * @return the loaded entities.
     */
    public List<InviteUserToBlogSecurityCode> loadAll();

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove(Collection<InviteUserToBlogSecurityCode> entities);

    /**
     * Removes the instance of InviteUserToBlogSecurityCode from the persistent store.
     */
    public void remove(InviteUserToBlogSecurityCode inviteUserToBlogSecurityCode);

    /**
     * Removes the instance of InviteUserToBlogSecurityCode having the given <code>identifier</code>
     * from the persistent store.
     */
    public void remove(Long id);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(Collection<InviteUserToBlogSecurityCode> entities);

    /**
     * Updates the <code>inviteUserToBlogSecurityCode</code> instance in the persistent store.
     */
    public void update(InviteUserToBlogSecurityCode inviteUserToBlogSecurityCode);

}
