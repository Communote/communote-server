package com.communote.plugin.ldap.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

import com.communote.server.core.common.ldap.LdapAttributeMappingException;
import com.communote.server.core.common.ldap.LdapUserAttributesMapper;
import com.communote.server.core.common.ldap.RequiredAttributeNotContainedException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.ExternalUserVO;

/**
 * A simple communote context mapper.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserContextMapper implements ContextMapper {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserContextMapper.class);

    private final LdapConfiguration ldapConfiguration;
    private final UserManagement userManagement;

    /**
     * Constructor.
     * 
     * @param ldapConfiguration
     *            The ldap configuration to use.
     * @param userManagement
     *            UserManagement.
     */
    public UserContextMapper(LdapConfiguration ldapConfiguration,
            UserManagement userManagement) {
        this.ldapConfiguration = ldapConfiguration;
        this.userManagement = userManagement;
    }

    /**
     * Mapped the attribute.
     * 
     * @param contex
     *            The context representing the entity.
     * @return The user mapped from the given context.
     */
    public User mapFromContext(Object contex) {
        DirContextAdapter context = (DirContextAdapter) contex;
        User result = null;
        try {
            LdapUserAttributesMapper attributesMapper = new LdapUserAttributesMapper(
                    ldapConfiguration);
            ExternalUserVO externalKenmeiUserVO =
                    attributesMapper.mapAttributes(context.getDn().toString(),
                            context.getAttributes());
            result = userManagement
                    .findUserByExternalUserId(
                            externalKenmeiUserVO.getExternalUserName(),
                            externalKenmeiUserVO.getSystemId());
        } catch (RequiredAttributeNotContainedException e) {
            LOGGER.warn("Problem during context mapping: {}: {} for {}",
                    new Object[] { e.getMessage(),
                            e.getLdapAttributeName(), context.getDn() });
        } catch (LdapAttributeMappingException e) {
            LOGGER.error("Error during context mapping: {} for {}", e.getMessage(), context.getDn());
        }
        if (result == null) {
            result = User.Factory.newInstance();
        }
        return result;
    }
}