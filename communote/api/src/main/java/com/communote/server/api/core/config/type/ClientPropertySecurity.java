package com.communote.server.api.core.config.type;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;

/**
 * Property constants for client security settings.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum ClientPropertySecurity implements ClientConfigurationPropertyConstant {

    /**
     * locked timespan of failed auth
     */
    FAILED_AUTH_LOCKED_TIMESPAN("kenmei.failed_auth.locked_timespan"),

    /**
     * temporary action steps
     */
    FAILED_AUTH_STEPS_TEMPLOCK("kenmei.failed_auth.steps.templock"),

    /**
     * permanently action limit
     */
    FAILED_AUTH_LIMIT_PERMLOCK("kenmei.failed_auth.limit.permlock"),

    /**
     * risk level steps
     */
    FAILED_AUTH_STEPS_RISK_LEVEL("kenmei.failed_auth.steps.risk_level"),

    /**
     * If true, users can authenticate against the internal database, when an external
     * authentication is activated.
     */
    ALLOW_DB_AUTH_ON_EXTERNAL("communote.allow.db.auth.on.external");

    /**
     * default value for ALLOW_DB_AUTH_ON_EXTERNAL
     */
    public static final boolean DEFAULT_ALLOW_DB_AUTH_ON_EXTERNAL = true;
    /**
     * default value: temporary action steps
     */
    public static final int DEFAULT_FAILED_AUTH_STEPS_TEMPLOCK = 5;
    /**
     * default value: permanently action limit
     */
    public static final int DEFAULT_FAILED_AUTH_LIMIT_PERMLOCK = 15;

    /**
     * default value: risk level steps
     */
    public static final int DEFAULT_FAILED_AUTH_STEPS_RISK_LEVEL = 3;

    /**
     * default value: locked timespan of failed auth
     */
    public static final int DEFAULT_FAILED_AUTH_LOCKED_TIMESPAN = 30;

    private final String key;

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     */
    private ClientPropertySecurity(String keyString) {
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
                .getClientConfigurationProperties().getProperty(this);
    }

    /**
     * @param defaultValue
     *            The default value to return.
     * @return The actual value for this property of the current client or the default value, if the
     *         actual value is no set.
     */
    public String getValue(String defaultValue) {
        String value = getValue();
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
}