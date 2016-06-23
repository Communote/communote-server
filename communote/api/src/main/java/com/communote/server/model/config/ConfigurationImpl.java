package com.communote.server.model.config;

/**
 * @see com.communote.server.model.config.Configuration
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

public class ConfigurationImpl extends Configuration {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7490596990674303164L;

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfluenceConfiguration getConfluenceConfig() {
        ExternalSystemConfiguration conf = getExternalConfig(ConfluenceConfiguration.class);
        return (ConfluenceConfiguration) conf;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalSystemConfiguration getExternalConfig(Class clazz) {
        for (ExternalSystemConfiguration auth : getExternalSystemConfigurations()) {
            if (clazz.isAssignableFrom(auth.getClass())) {
                return auth;
            }

        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LdapConfiguration getLdapConfig() {
        return (LdapConfiguration) getExternalConfig(LdapConfiguration.class);
    }

}
