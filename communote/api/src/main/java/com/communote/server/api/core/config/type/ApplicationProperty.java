package com.communote.server.api.core.config.type;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;

/**
 * Property constants for general settings of the application.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum ApplicationProperty implements ApplicationConfigurationPropertyConstant {

    /** Property for max upload size of an attachment. */
    ATTACHMENT_MAX_UPLOAD_SIZE("kenmei.attachment.max.upload.size"),

    /**
     * disable captcha
     */
    CAPTCHA_DISABLED("kenmei.captcha.disable"),

    /** Root directory for creating the file system storage directories for different clients */
    FILE_SYSTEM_REPOSITORY_STORAGE_DIR_ROOT("kenmei.crc.file.repository.storage.dir.root"),

    /**
     * Property for the alias/ID to be used for the global client instead of "global".
     */
    GLOBAL_CLIENT_ALIAS("kenmei.application.global.client.alias"),

    /** Property for max upload size of an image. */
    IMAGE_MAX_UPLOAD_SIZE("kenmei.image.max.upload.size"),

    /** Property for the installation date of the application. */
    INSTALLATION_DATE("installation.date"),

    /** Property for the installation id. */
    INSTALLATION_UNIQUE_ID("installation.unique.id"),

    /**
     * Property if the composite widget should include the powered by footer
     */
    INTEGRATION_WIDGET_INCLUDE_FOOTER_POWERED_BY(
            "kenmei.integration.widget.include.footer.powered.by"),

    /**
     * Property for the location of the name of the jad and jar file to download. Either with or
     * without the ending .jad
     */
    MOBILE_MIDP_FILE_NAME("kenmei.mobile.midp.file.name"),

    /**
     * The property for the HTTP port.
     */
    WEB_HTTP_PORT("kenmei.web.http.port"),

    /**
     * The property for the HTTPS port.
     */
    WEB_HTTPS_PORT("kenmei.web.https.port"),

    /**
     * The property to denote whether the server supports HTTPS connection.
     */
    WEB_HTTPS_SUPPORTED("kenmei.web.https.supported"),

    /**
     * The property holding the servlet context name of the installed application.
     */
    WEB_SERVER_CONTEXT_NAME("kenmei.web.server.context.name"),

    /**
     * The property holding the host name (without protocol and port) of the server running the
     * application, e.g. www.communote.com.
     */
    WEB_SERVER_HOST_NAME("kenmei.web.server.host.name"),

    /** Sets if scripts should be compressed. */
    SCRIPTS_COMPRESS("communote.scripts.compress"),

    /** Sets if scripts should be packed into a single file. */
    SCRIPTS_PACK("communote.scripts.pack"),

    /** Sets if styles should be compressed. */
    STYLES_COMPRESS("communote.styles.compress"),

    /** Sets if styles should be packed into a single file. */
    STYLES_PACK("communote.styles.pack");

    /**
     * default value for the attachment upload limit
     */
    public static final long DEFAULT_ATTACHMENT_MAX_UPLOAD_SIZE = 10485760;

    /**
     * default value for the image upload limit
     */
    public static final long DEFAULT_IMAGE_MAX_UPLOAD_SIZE = 1048576;

    /**
     * The default port for HTTP connections.
     */
    public static final int DEFAULT_WEB_HTTP_PORT = 80;
    /**
     * The default port for HTTPS connections.
     */
    public static final int DEFAULT_WEB_HTTPS_PORT = 443;
    /**
     * The default value for {@link #WEB_HTTPS_SUPPORTED}.
     */
    public static final boolean DEFAULT_WEB_HTTPS_SUPPORTED = false;

    private final String key;

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     */
    private ApplicationProperty(String keyString) {
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
     *            The default value, if no value is set.
     * @return The actual value for this property of the current client.
     */
    public String getValue(String defaultValue) {
        String value = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties().getProperty(this);
        return value != null ? value : defaultValue;
    }
}
