package com.communote.plugin.ldap;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.common.ldap.LdapUserAttribute;
import com.communote.server.core.common.ldap.LdapUserAttributesMapper;

/**
 * Activator for the LDAP plugin.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapActivator implements BundleActivator {

    /** System if for LDAP */
    // public final static String EXTERNAL_SYSTEM_ID_DEFAULT_LDAP = "DefaultLDAP";
    public final static String EXTERNAL_SYSTEM_ID_DEFAULT_LDAP = ConfigurationManagement.DEFAULT_LDAP_SYSTEM_ID;

    @Override
    public void start(BundleContext arg0) throws Exception {
        ServiceLocator
                .instance()
                .getService(PropertyManagement.class)
                .addObjectPropertyFilter(
                        PropertyType.UserProperty,
                        arg0.getBundle().getSymbolicName(),
                        LdapUserAttributesMapper.getUserPropertyKeyName(
                                EXTERNAL_SYSTEM_ID_DEFAULT_LDAP,
                                LdapUserAttribute.UPN));
    }

    @Override
    public void stop(BundleContext arg0) throws Exception {
        ServiceLocator
                .instance()
                .getService(PropertyManagement.class)
                .removeObjectPropertyFilter(
                        PropertyType.UserProperty,
                        arg0.getBundle().getSymbolicName(),
                        LdapUserAttributesMapper.getUserPropertyKeyName(
                                EXTERNAL_SYSTEM_ID_DEFAULT_LDAP,
                                LdapUserAttribute.UPN));
    }

}
