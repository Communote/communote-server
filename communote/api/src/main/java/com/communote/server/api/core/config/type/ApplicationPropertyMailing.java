package com.communote.server.api.core.config.type;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;

/**
 * Property constants for the mailing settings of the application.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum ApplicationPropertyMailing implements ApplicationConfigurationPropertyConstant {

    /** Property of the mailing port. */
    PORT("mailing.port"),

    /** Property of the mailing host. */
    HOST("mailing.host"),

    /** Property of the from address personal name. */
    FROM_ADDRESS_NAME("mailing.from.address.name"),

    /** Property of the from address. */
    FROM_ADDRESS("mailing.from.address"),

    /** Property for user login. */
    LOGIN("mailing.login"),

    /** Property for user password. */
    PASSWORD("mailing.password"),

    /** Property describing whether to use starttls. */
    USE_STARTTLS("mailing.starttls");

    private final String key;

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     */
    private ApplicationPropertyMailing(String keyString) {
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

    /**
     * @param fallback
     *            fallback to this default value if the actual value is whitespace, empty ("") or
     *            {@code null}.
     * @return The actual value for this property of the current client.
     */
    public boolean getValue(boolean fallback) {
        String value = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties().getProperty(this);

        Boolean result = BooleanUtils.toBooleanObject(value);
        if (result == null) {
            result = fallback;
        }
        return result;
    }

    /**
     * @param fallback
     *            fallback to this default value if the actual value is whitespace, empty ("") or
     *            {@code null}.
     * @return The actual value for this property of the current client.
     */
    public String getValue(String fallback) {
        String value = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties().getProperty(this);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return fallback;
    }
}