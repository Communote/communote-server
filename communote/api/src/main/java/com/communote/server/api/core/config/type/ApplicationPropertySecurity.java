package com.communote.server.api.core.config.type;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;

/**
 * Property constants for security settings of the application.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum ApplicationPropertySecurity implements ApplicationConfigurationPropertyConstant {
    /**
     * Property describing where the keystore file containing the trusted certificate used for
     * SSL/TLS connections is located.
     */
    TRUSTED_CA_KEYSTORE_FILE("kenmei.trusted.ca.keystore.file"),

    /**
     * Password of the trust store. For legacy the parameter named is called keystore.
     */
    TRUSTED_CA_TRUSTSTORE_PASSWORD("kenmei.trusted.ca.keystore.password"),

    /**
     * Password of the keystore.
     */
    KEYSTORE_PASSWORD("com.communote.core.keystore.password");

    private final String key;

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     */
    private ApplicationPropertySecurity(String keyString) {
        this.key = keyString;
    }

    /**
     * String representation of the constant to be used as key in Properties objects.
     *
     * @return the constant as string
     */
    @Override
    public String getKeyString() {
        return key;
    }

    /**
     * @return The actual value for this property of the current client.
     */
    public String getValue() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties().getProperty(this);
    }
}
