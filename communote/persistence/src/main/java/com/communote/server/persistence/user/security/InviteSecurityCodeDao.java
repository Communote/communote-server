package com.communote.server.persistence.user.security;

import com.communote.server.model.security.SecurityCode;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface InviteSecurityCodeDao<T extends SecurityCode> {
    /**
     * Creates an instance of the invite SecurityCode and adds it to the persistent store.
     */
    public T create(T inviteUserToBlogSecurityCode);

    /**
     * Find an existing security code by the ID of the invited user
     *
     * @param userId
     *            the ID of the invited user
     * @return the security code or null if there is none for the given user
     */
    public T findByUser(Long userId);
}
