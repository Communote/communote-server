package com.communote.server.api.core.config.type;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.CoreConfigurationPropertyConstant;

/**
 * Property constants that are required for start of the application.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum CoreProperty implements CoreConfigurationPropertyConstant {

    /** Property denoting whether the installation of the application is done. */
    INSTALLATION_DONE("communote.installation.done"),

    /** Version of the actual installed version. */
    APPLICATION_VERSION("communote.application.version");

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(CoreProperty.class);

    private final String key;

    private boolean encrypted = false;

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     */
    private CoreProperty(String keyString) {
        this.key = keyString;
    }

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     *
     * @param encrypted
     *            If true, this value will be encrypted.
     */
    private CoreProperty(String keyString, boolean encrypted) {
        this.key = keyString;
        this.encrypted = encrypted;
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
        String value = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties().getCoreProperty(this);
        if (StringUtils.isNotEmpty(value) && isEncrypted()) {
            try {
                return EncryptionUtils.decrypt(value,
                        ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
            } catch (EncryptionException e) {
                LOG.error("Error decrypting a property: " + this.getKeyString());
            }
        }
        return value;
    }

    /**
     * @return the encrypted
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Saves the value for this property.
     *
     * @param value
     *            The value to set.
     * @throws ConfigurationUpdateException
     *             Exception.
     */
    public void setValue(String value) throws ConfigurationUpdateException {
        if (this.isEncrypted()) {
            try {
                value = EncryptionUtils.encrypt(value,
                        ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
            } catch (EncryptionException e) {
                LOG.error("Error encrypting a property: " + this.getKeyString());
            }
        }
        CommunoteRuntime.getInstance().getConfigurationManager().updateStartupProperty(this, value);
    }
}