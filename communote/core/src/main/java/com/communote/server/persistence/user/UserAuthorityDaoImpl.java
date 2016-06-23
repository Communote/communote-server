package com.communote.server.persistence.user;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import com.communote.server.model.user.UserAuthority;
import com.communote.server.model.user.UserRole;

/**
 * @see com.communote.server.model.user.UserAuthority
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserAuthorityDaoImpl extends
        com.communote.server.persistence.user.UserAuthorityDaoBase {
    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<UserAuthority> handleCreateAuthorities(UserRole[] roles, boolean save) {
        Assert.notEmpty(roles, "no user roles defined on calling createRoles");
        Set<UserRole> distinctRoles = new HashSet<UserRole>();
        CollectionUtils.addAll(distinctRoles, roles);
        Set<UserAuthority> result = new HashSet<UserAuthority>();
        for (UserRole e : distinctRoles) {
            UserAuthority authority = UserAuthority.Factory.newInstance();
            authority.setRole(e);
            result.add(authority);
        }
        if (save) {
            create(result);
        }
        return result;
    }

}
