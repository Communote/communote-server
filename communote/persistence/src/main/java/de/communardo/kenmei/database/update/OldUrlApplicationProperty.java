package de.communardo.kenmei.database.update;

import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;

/**
 * Helper enum which provides the old URL related application properties that are required for the
 * update tasks.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
enum OldUrlApplicationProperty implements ApplicationConfigurationPropertyConstant {
    /** Property for the web application uri prefix. */
    WEB_APPLICATION_URI_PREFIX("kenmei.web.application.uri.prefix"),
    /** Property for the web application url prefix. */
    WEB_APPLICATION_URL_PREFIX("kenmei.web.application.url.prefix"),

    /** Property for the secure web application url prefix. */
    WEB_APPLICATION_URL_PREFIX_SECURE("kenmei.web.application.url.prefix.secure");

    private String key;

    /**
     * Constructor for enum type.
     * 
     * @param keyString
     *            the constant as string
     */
    private OldUrlApplicationProperty(String keyString) {
        this.key = keyString;
    }

    /**
     * String representation of the constant to be used as key in Properties objects.
     * 
     * @return the constant as string
     */
    public String getKeyString() {
        return key;
    }
}
