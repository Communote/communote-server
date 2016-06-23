package com.communote.server.model.config;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class Configuration implements Serializable {
    /**
     * Constructs new instances of {@link Configuration}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link Configuration}.
         */
        public static Configuration newInstance() {
            return new ConfigurationImpl();
        }

        /**
         * Constructs a new instance of {@link Configuration}, taking all possible properties
         * (except the identifier(s))as arguments.
         */
        public static Configuration newInstance(Set<Setting> settings,
                ClientConfiguration clientConfig,
                Set<ExternalSystemConfiguration> externalSystemConfigurations) {
            final Configuration entity = new ConfigurationImpl();
            entity.setSettings(settings);
            entity.setClientConfig(clientConfig);
            entity.setExternalSystemConfigurations(externalSystemConfigurations);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2554668692016290232L;

    private Long id;

    private Set<Setting> settings = new HashSet<>();

    private ClientConfiguration clientConfig;

    private Set<ExternalSystemConfiguration> externalSystemConfigurations = new HashSet<>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Configuration instance and all identifiers
     * for this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Configuration)) {
            return false;
        }
        final Configuration that = (Configuration) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     *
     */
    public ClientConfiguration getClientConfig() {
        return this.clientConfig;
    }

    /**
     * <p>
     *
     * @return the confluence authentication configuration if defined in the externalAuthConfigs
     *         </p>
     */
    public abstract ConfluenceConfiguration getConfluenceConfig();

    /**
     * <p>
     *
     * @return the authentication configuration matching the given class
     *         </p>
     */
    public abstract ExternalSystemConfiguration getExternalConfig(Class clazz);

    /**
     *
     */
    public Set<ExternalSystemConfiguration> getExternalSystemConfigurations() {
        return this.externalSystemConfigurations;
    }

    /**
     *
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     *
     * @return the ldap configuration if defined in the externalAuthConfigs
     *         </p>
     */
    public abstract LdapConfiguration getLdapConfig();

    /**
     *
     */
    public Set<Setting> getSettings() {
        return this.settings;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setClientConfig(ClientConfiguration clientConfig) {
        this.clientConfig = clientConfig;
    }

    public void setExternalSystemConfigurations(
            Set<ExternalSystemConfiguration> externalSystemConfigurations) {
        this.externalSystemConfigurations = externalSystemConfigurations;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSettings(Set<Setting> settings) {
        this.settings = settings;
    }

}