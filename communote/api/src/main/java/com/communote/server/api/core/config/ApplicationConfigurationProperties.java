package com.communote.server.api.core.config;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyVirusScanning;
import com.communote.server.model.config.ApplicationConfigurationSetting;

/**
 * Object holding application wide configuration properties.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ApplicationConfigurationProperties extends
        AbstractConfigurationProperties<ApplicationConfigurationPropertyConstant> implements
        Serializable {
    /**
     * default serial version ID
     */
    private static final long serialVersionUID = 1L;

    private final static Logger LOG = LoggerFactory
            .getLogger(ApplicationConfigurationProperties.class);

    private final Map<String, String> properties;

    private final String urlPrefix;
    private final String urlPrefixSecured;

    /**
     * Constructs a new application properties container and initializes it with the provided
     * settings.
     *
     * @param settings
     *            the application configuration settings
     */
    public ApplicationConfigurationProperties(
            Collection<ApplicationConfigurationSetting> settings) {
        // TODO init with localhost as default?
        String urlPrefixValue = "";
        String urlPrefixSecuredValue = "";
        properties = new HashMap<String, String>();
        if (settings != null) {
            for (ApplicationConfigurationSetting setting : settings) {
                properties.put(setting.getSettingKey(), setting.getSettingValue());
            }
            // initialize the configured URLs required for situations where no request is available
            String hostName = getProperty(ApplicationProperty.WEB_SERVER_HOST_NAME);
            if (StringUtils.isBlank(hostName)) {
                LOG.warn("The application property "
                        + ApplicationProperty.WEB_SERVER_HOST_NAME.getKeyString()
                        + " is not defined. The URL rendering will not work correctly.");
            } else {
                String context = getProperty(ApplicationProperty.WEB_SERVER_CONTEXT_NAME, "")
                        .trim();
                if (StringUtils.isNotBlank(context) && !context.startsWith("/")) {
                    context = "/" + context;
                }
                int port = getProperty(ApplicationProperty.WEB_HTTP_PORT,
                        ApplicationProperty.DEFAULT_WEB_HTTP_PORT);
                urlPrefixValue = buildUrlPrefix(hostName, port, context, false);
                if (getProperty(ApplicationProperty.WEB_HTTPS_SUPPORTED,
                        ApplicationProperty.DEFAULT_WEB_HTTPS_SUPPORTED)) {
                    port = getProperty(ApplicationProperty.WEB_HTTPS_PORT,
                            ApplicationProperty.DEFAULT_WEB_HTTPS_PORT);
                    urlPrefixSecuredValue = buildUrlPrefix(hostName, port, context, true);
                } else {
                    urlPrefixSecuredValue = urlPrefixValue;
                }
            }
        }
        urlPrefix = urlPrefixValue;
        urlPrefixSecured = urlPrefixSecuredValue;
    }

    /**
     * Builds the URL prefix for an absolute URL.
     *
     * @param hostName
     *            the host name to use
     * @param port
     *            the port to use
     * @param context
     *            the servlet context to use
     * @param https
     *            whether the URL should be using the HTTPS protocol
     * @return the URL prefix
     */
    private String buildUrlPrefix(String hostName, int port, String context, boolean https) {
        StringBuilder urlBuilder = new StringBuilder(https ? "https://" : "http://");
        int standardPort = https ? 443 : 80;
        urlBuilder.append(hostName);
        if (port != standardPort) {
            urlBuilder.append(":");
            urlBuilder.append(port);
        }
        urlBuilder.append(context);
        return urlBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProperty(ApplicationConfigurationPropertyConstant key) {
        return properties.get(key.getKeyString());
    }

    /**
     * Get the decrypted value of an encrypted property.
     * 
     * @param key
     *            the key of the property to return
     * @return the decrypted value or null if the property was not set
     * @throws EncryptionException
     *             in case decryption failed
     * @since 3.5
     */
    public String getPropertyDecrypted(ApplicationConfigurationPropertyConstant key)
            throws EncryptionException {
        String value = properties.get(key.getKeyString());
        if (StringUtils.isNotEmpty(value)) {
            return EncryptionUtils.decrypt(value,
                    ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
        }
        return value;
    }

    /**
     * Returns all the properties for constant subset.
     *
     * @param constants
     *            the constants for which the properties are to be returned
     * @return the properties as copy
     */
    private Properties getSpecificProperties(ApplicationConfigurationPropertyConstant[] constants) {
        Properties props = new Properties();
        for (ApplicationConfigurationPropertyConstant constant : constants) {
            String value = getProperty(constant);
            if (value != null) {
                props.setProperty(constant.getKeyString(), value);
            }
        }
        return props;
    }

    /**
     * Returns the configured URL prefix for rendering absolute URLs with HTTP protocol.
     *
     * @return the configured URL prefix, or an empty string if it is not configured
     */
    public String getUrlPrefix() {
        return urlPrefix;
    }

    /**
     * Returns the configured URL prefix for rendering absolute URLs with HTTPS protocol. The return
     * value will be the same as {@link #getUrlPrefix()} if the configuration states that HTTPS is
     * not supported.
     *
     * @return the configured URL prefix for HTTPs connections
     */
    public String getUrlPrefixSecured() {
        return urlPrefixSecured;
    }

    /**
     * Returns the virus scanning properties. The returned value is a copy of the properties, thus
     * modifications will not be persisted.
     *
     * @return the virus scanning properties
     */
    public Properties getVirusScanningProperties() {
        // create a subset with all the VirusChecking properties
        return getSpecificProperties(ApplicationPropertyVirusScanning.values());
    }

}
