package com.communote.server.api.core.config.type;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;

/**
 * Property constants for configuring the mail fetching.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum ApplicationPropertyMailfetching implements ApplicationConfigurationPropertyConstant {
    /**
     * denotes whether the mailfetching is enabled.
     */
    ENABLED("mailfetching.enabled"),
    /**
     * Property holding the single email address to which all posts by email will be sent.
     */
    SINGLE_ADDRESS("mailfetching.single.address"),

    /** Property representing the the mail server domain */
    DOMAIN("mailfetching.domain"),

    /** Property representing the static mail address suffix */
    STATIC_SUFFIX("mailfetching.static.suffix"),

    /**
     * Property describing whether the email addresses for the global client should contain the
     * client ID.
     */
    NO_CLIENTID_IN_ADDRESS_FOR_GLOBAL("mailfetching.no.clientid.in.address.for.global"),

    /**
     * Property describing whether mails that are not for this instance should be deleted. These are
     * mails with a from-address that does not match the SINGLE_ADDRESS (if defined) or those having
     * another static suffix (if the SINGLE_ADDRESS is not defined).
     */
    DELETE_MAILS_NOT_FOR_THIS_INSTANCE("mailfetching.delete.mails.not" + ".for.this.instance"),

    /** Property representing the protocol to use for connection. */
    PROTOCOL("mailfetching.protocol"),

    /** Property describing whether to use starttls. */
    USE_STARTTLS("mailfetching.starttls"),

    /** Property representing the host of the mail server. */
    HOST("mailfetching.host"),

    /** Property representing the port where the mailserver is listening. */
    PORT("mailfetching.port"),

    /** Property representing the mailbox to use. */
    MAILBOX("mailfetching.mailbox"),

    /** Property representing the login of the user owning the mailbox. */
    USER_LOGIN("mailfetching.user.login"),

    /** Property representing the password of the user owning the mailbox. */
    USER_PASSWORD("mailfetching.user.password"),

    /**
     * Property representing the time in milliseconds to wait between mailbox fetches.
     */
    FETCH_TIMEOUT("mailfetching.fetch.timeout"),

    /**
     * Property representing the time in milliseconds to wait between reconnect attempts.
     */
    RECONNECT_TIMEOUT("mailfetching.reconnect.timeout"),

    /**
     * Property representing the time in milliseconds after which a keep alive packet should be sent
     * when IMAP IDLE is used.
     */
    IMAP_IDLE_KEEP_ALIVE_TIMEOUT("mailfetching.imap.idle.keep.alive.timeout");

    private final String key;

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     */
    private ApplicationPropertyMailfetching(String keyString) {
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
