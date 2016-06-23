package com.communote.server.api.core.config.type;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;

/**
 * Property constants for the XMPP settings of the application.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum ApplicationPropertyXmpp implements ApplicationConfigurationPropertyConstant {
    /** Properties for xmpp host. */
    HOST("kenmei.xmpp.bot.host"),

    /** Properties for xmpp port. */
    PORT("kenmei.xmpp.bot.port"),

    /** Properties for xmpp login. */
    LOGIN("kenmei.xmpp.bot.login"),

    /** Properties for xmpp password. */
    PASSWORD("kenmei.xmpp.bot.password"),

    /** Properties for xmpp user suffix. */
    USER_SUFFIX("kenmei.xmpp.user.suffix"),

    /** Properties for xmpp blog suffix. */
    BLOG_SUFFIX("kenmei.xmpp.blog.suffix"),

    /** Properties for xmpp time to wait for next posting. */
    TIME_TO_WAIT("kenmei.xmpp.message.wait"),

    /** Properties for xmpp debug mode. */
    DEBUG("kenmei.xmpp.debug"),

    /** Properties for xmpp enabled mode. */
    ENABLED("kenmei.xmpp.enabled"),

    /** Property for client priority. */
    PRIORITY("kenmei.xmpp.bot.priority"),

    /** Property for ignoring incoming messages. */
    IGNORE_INCOMING_MESSAGES("kenmei.xmpp.bot.ignore.incoming.messages"),

    /** Property for handling subscription requests. */
    HANDLE_SUBSCRIPTION_REQUESTS("kenmei.xmpp.bot.handle.subscription");

    private final String key;

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     */
    private ApplicationPropertyXmpp(String keyString) {
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
     * @param defaultValue
     *            fallback to this default value if the actual value is whitespace, empty ("") or
     *            {@code null}.
     * @return The actual value for this property of the current client.
     */
    public String getValue(String defaultValue) {
        String value = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties().getProperty(this);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return defaultValue;
    }
}